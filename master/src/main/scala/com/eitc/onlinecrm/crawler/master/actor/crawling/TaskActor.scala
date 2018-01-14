package com.eitc.onlinecrm.crawler.master.actor.crawling

import java.time.{ LocalDateTime, ZoneOffset }
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{ Actor, ActorLogging, ActorRef, Props, Stash }
import akka.pattern._
import akka.util.Timeout
import com.eitc.onlinecrm.crawler.master.db.Rows.{ InstanceStateRow, TaskRow }
import com.eitc.onlinecrm.crawler.master.db.table.master.ContainerLogTable.ContainerLogRow
import com.eitc.onlinecrm.crawler.master.db.table.master.InstanceLogTable.InstanceLogRow
import com.eitc.onlinecrm.crawler.master.db.table.master.TaskInstanceScaleTable.TaskInstanceScaleRow
import com.eitc.onlinecrm.crawler.master.db.table.master.TaskLogTable.TaskLogRow
import com.eitc.onlinecrm.crawler.master.util.CrawlingSshOperation
import com.eitc.onlinecrm.crawler.master._
import com.eitc.onlinecrm.crawler.master.db.table.master.CrawlerLogTable.CrawlerLogRow
import com.google.cloud.compute.InstanceInfo.Status
import org.graysurf.util.logging.ProcessSerialConverter
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }
import scala.language.postfixOps
import scala.util.{ Failure, Success, Try }

class TaskActor(taskId: Int, dbConfig: DatabaseConfig[JdbcProfile], config: GcpCrawlerMasterConfig) extends Actor with Stash with ActorLogging {

  import context.dispatcher

  implicit val timeout: Timeout = 10 minutes

  val dbActor: ActorRef = context.system.actorOf(Props(classOf[DbActor], taskId.formatted("%04d"), dbConfig), s"DbActor-$taskId")
  val gcpOperationActor: ActorRef = context.system.actorOf(Props(classOf[GcpOperationActor]), s"GcpOperationActor-$taskId")

  private val sshExecutor = context.system.dispatchers.lookup("dispatcher.ssh-operator")

  // instance_name -> InstanceStateRow(name, taskId, status, ip, activeTime, stopTime, containerNum, containerIds)
  val instances: mutable.Map[String, InstanceStateRow] = new ConcurrentHashMap[String, InstanceStateRow].asScala
  // docker_container_id -> CrawlerLocation(crawler_id,instance_name,ip)
  val containers: mutable.Map[String, CrawlerLocation] = new ConcurrentHashMap[String, CrawlerLocation].asScala

  val programSerial: String = ProcessSerialConverter.serial
  val instanceSerial: AtomicInteger = new AtomicInteger()

  override def preStart(): Unit = {
    super.preStart()
    context.become(working)
    dbActor ! WriteTaskLogMessage(TaskLogRow(taskId, TaskAction.START_TASK.toString))
    val _init = autoScalingInstance()
    _init.onComplete {
      case Success(_) ⇒
        log.info(s"init ${self.path.name} finished")
      case Failure(t) ⇒
        log.error(t, s"init ${self.path.name} error")
        context.system.terminate()
    }
    Await.result(_init, Duration.Inf)

    context.become(receive)
  }

  override def receive: Receive = {
    case ResetMessage ⇒
      context.become(receive)
      log.info("context become receive")
    case RunTaskMessage ⇒
      context.become(working)
      val sdr = sender()
      val _job = (for {
        t ← (dbActor ? GetTaskMessage).mapTo[Seq[TaskRow]]
        s ← (dbActor ? GetCrawlerStatusMessage).mapTo[Seq[CrawlerStatus]]
      } yield {
        val task = t.filter(_.id == taskId)
        val crawlerStatus = s.filter(a ⇒ task.map(_.id).contains(a.task)).sortBy(_.crawler)
        val preStartCrawlers: Seq[Int] = crawlerStatus.filter(_.next.isBefore(LocalDateTime.now())).map(s ⇒ s.crawler)
        if (crawlerStatus.nonEmpty && preStartCrawlers.isEmpty) log.info(s"next execute crawler: ${crawlerStatus.minBy(_.next.toEpochSecond(ZoneOffset.UTC))}")
        if (preStartCrawlers.nonEmpty) log.info(s"starting task [${task.map(_.id).mkString(",")}], ${preStartCrawlers.size} crawlers [${preStartCrawlers.mkString(",")}]...")
        for {
          _ ← checkInstanceHealth()
          startCrawlers ← startCrawlers(preStartCrawlers)
          _ ← writeTaskStatus()
          // _ ← if (startCrawlers.isEmpty) autoScalingInstance() else Future.successful(())
          _ ← autoScalingInstance()
        } yield {
        }
      }).flatMap(f ⇒ f)

      _job.onComplete {
        case Success(_) ⇒
          context.system.scheduler.scheduleOnce(10 seconds) {
            context.become(receive)
            log.info("context become receive")
          }
          sdr ! RunTaskDoneMessage
        case Failure(t) ⇒
          log.error(t, "running error")
          sdr ! RunTaskDoneMessage
          context.become(receive)
          log.info("context become receive")
      }
    case StopTaskMessage ⇒
      context.become(working)
      stopTask().pipeTo(sender())
    case msg ⇒
      log.warning(s"${this.self.path.name} received unknown message $msg")
      sender() ! "done"
  }

  def working: Receive = {
    case ResetMessage ⇒
      context.become(receive)
      log.info("reset, context become receive")
    case StopTaskMessage ⇒
      val sdr = sender()
      log.warning(s"${this.self.path.name} is working")
      context.system.scheduler.scheduleOnce(5 seconds) {
        self.tell(StopTaskMessage, sdr)
      }
    case msg ⇒
      log.warning(s"working now, skip message $msg")
      sender() ! "done"
  }

  def startCrawlers(crawlers: Seq[Int]): Future[Seq[ContainerLogRow]] = {

    val containerSlots = instances.values.toSeq.sortBy(vm ⇒ (-vm.containerIds.size, vm.name)).map(vm ⇒ vm.name → vm.ip).flatMap {
      case (vmName, ip) ⇒
        //val len = Try(config.containerAmountInVm - CrawlingSshOperation(config.sshUser, config.sshKeyPath, ip).runningCrawlerNum).recover { case _ ⇒ 0 }
        val len = config.containerAmountInVm - CrawlingSshOperation(config.sshUser, config.sshKeyPath, ip).runningCrawlerNum
        Seq.empty[(String, String)].padTo(len, vmName → ip)
    }
    val _startContainers = Future.sequence {
      containerSlots.zip(crawlers).map {
        case ((vmName, ip), crawlerId) ⇒
          Future {

            Try {
              val container = CrawlingSshOperation(config.sshUser, config.sshKeyPath, ip).runCrawler(taskId, crawlerId, config.containerImage, config.containerImageVersion, config.containerMemory, config.environment).take(12)
              val crawlerLogRow = CrawlerLogRow(crawlerId, container, taskId, vmName, ip)
              val containerRow = ContainerLogRow(container, taskId, crawlerId, vmName, ip, Status.STAGING.toString)

              // write crawler_log table
              dbActor ! WriteCrawlerLogMessage(crawlerLogRow)
              // write container_log table
              dbActor ! WriteContainerLogMessage(containerRow)
              containers += container → CrawlerLocation(crawlerId, vmName, ip)
              log.info(s"start crawler $crawlerId: $vmName->$ip, container: $container")
              containerRow
            } match {
              case Success(r) ⇒
                r
              case Failure(t) ⇒
                log.error(t, "")
                ContainerLogRow("ERROR", taskId, crawlerId, vmName, ip, Status.STAGING.toString)
            }

          }(sshExecutor)
      }
    }.map {
      rows ⇒
        if (rows.nonEmpty) log.info(s"${rows.size} containers started: ${rows.map(_.crawlerId).mkString(", ")}")
        rows
    }
    _startContainers

  }

  def writeTaskStatus(): Future[_] = {

    val runningContainers: Map[String, Seq[String]] = Await.result((dbActor ? GetContainerStatus).mapTo[Seq[RunningContainers]], Duration.Inf)
      .map(a ⇒ a.instance_name → a.containers).toMap

    log.debug(s"runningContainers from db: $runningContainers")
    log.debug(s"containers: $containers")

    Future.sequence {
      instances.values.map {
        case oldState @ InstanceStateRow(vmName, _, _, ip, _, _, _, _) ⇒
          Future {
            Try {
              val c = containers.filter { case (_, CrawlerLocation(_, n, _)) ⇒ n == vmName }
              log.debug(s"containers in $vmName: $c")

              c.foreach {
                case (containerId, CrawlerLocation(crawlerId, _, _)) ⇒

                  if (!runningContainers.contains(vmName)) {
                    if (instances(vmName).activeTime.plusMinutes(10).isBefore(LocalDateTime.now())) {
                      log.warning(s"find no response instance: ${instances(vmName)}")
                      //close vm
                      containers -= containerId
                      deleteNoResponseVm(oldState)
                    }
                  } else {

                    log.debug(s"runningContainers(vmName): ${runningContainers(vmName)}")
                    log.debug(s"containerId: $containerId")

                    // skip check new vm start < 10 minutes
                    if (instances(vmName).activeTime.plusMinutes(10).isAfter(LocalDateTime.now())) {
                      log.debug(s"skip check new container: $containerId")
                    } else {
                      val containerStatus = if (runningContainers(vmName).contains(containerId)) Status.RUNNING.toString else Status.TERMINATED.toString
                      // write container_log table
                      dbActor ! WriteContainerLogMessage(ContainerLogRow(containerId, taskId, crawlerId, vmName, ip, containerStatus))
                      if (containerStatus == Status.TERMINATED.toString) {
                        containers -= containerId
                        log.debug(s"$containerId TERMINATED")
                      }
                    }

                  }
              }
              val crawlerIds = containers.filter { case (_, CrawlerLocation(_, n, _)) ⇒ n == vmName }.map { case (_, CrawlerLocation(crawlerId, _, _)) ⇒ crawlerId }.toList

              // write instance_log table
              dbActor ! WriteInstanceLogMessage(InstanceLogRow(vmName, taskId, Status.RUNNING.toString, crawlerIds.size, crawlerIds, ip))

              val newState = oldState.copy(status = Status.RUNNING.toString, containerNum = crawlerIds.size, containerIds = crawlerIds)
              // write instance_state table
              dbActor ! WriteInstanceStateMessage(newState)
              instances(vmName) = newState
            } match {
              case Success(_) ⇒
              case Failure(t) ⇒ log.error(t, s"writeTaskStatus $vmName error")
            }

          }.recover { case t ⇒ log.warning(s"future recovery:$t") }
      }
    }.map {
      _ ⇒
        log.info(s"${containers.size} running crawlers: ${instances.values.map(r ⇒ s"[${r.ip}->${r.containerIds.sorted.mkString(", ")}]").mkString(", ")}")
        log.debug(s"${instances.size} instances: ${instances.mkString(", ")}")
        log.debug(s"${containers.size} containers: ${containers.mkString(", ")}")
    }
  }

  def checkInstanceHealth(): Future[_] = {
    // get instance status from gcp api
    val _checkHealth = (gcpOperationActor ? ListInstanceStatusMessage(taskId, config.vmRegion, config.vmZone)).mapTo[Seq[InstanceStatus]].map {
      instancesStatus ⇒
        log.info(s"${instancesStatus.size} instances: ${instancesStatus.mkString(", ")}")
        // update instances status from instancesStatus
        instances.values.foreach(vm ⇒ instances(vm.name) = vm.copy(status = instancesStatus.find(_.name == vm.name).map(_.status).getOrElse(Status.TERMINATED).toString))
        val unusualTerminatedInstances = instances.values.filter(vm ⇒ vm.status == Status.STOPPING.toString || vm.status == Status.TERMINATED.toString).toSeq
        if (unusualTerminatedInstances.nonEmpty) log.warning(s"${unusualTerminatedInstances.size} instances unusual terminated: ${unusualTerminatedInstances.map(vm ⇒ vm.name → vm.ip).mkString(", ")}")
        val _deleteUnusualTerminatedInstances = Future.sequence {
          unusualTerminatedInstances.map {
            unusualInstanceState ⇒
              // delete unusual terminated instance
              val _deleteInstance = (gcpOperationActor ? DeleteInstanceMessage(taskId, unusualInstanceState.name, config.vmRegion, config.vmZone))
                .mapTo[InstanceStateRow] // write back active_time, ip
                .map(_.copy(activeTime = unusualInstanceState.activeTime, ip = Try(unusualInstanceState.ip).getOrElse("RELEASED")))
                .map {
                  terminatedState ⇒
                    instances -= terminatedState.name
                    // write instance_log table
                    dbActor ! WriteInstanceLogMessage(InstanceLogRow(unusualInstanceState.name, taskId, TaskAction.UNUSUAL_TERMINATED.toString, 0, List.empty, ""))
                    // write instance_state table
                    dbActor ! WriteInstanceStateMessage(unusualInstanceState.copy(stopTime = Some(LocalDateTime.now()), status = TaskAction.UNUSUAL_TERMINATED.toString))
                    instances -= unusualInstanceState.name
                    val unusualTerminatedContainers: mutable.Map[String, CrawlerLocation] = containers.filter { case (_, CrawlerLocation(_, instanceName, _)) ⇒ instanceName == unusualInstanceState.name }
                    unusualTerminatedContainers.foreach {
                      case (containerId, CrawlerLocation(crawlerId, instanceName, ip)) ⇒
                        // write container_log table
                        dbActor ! WriteContainerLogMessage(ContainerLogRow(containerId, taskId, crawlerId, instanceName, ip, TaskAction.UNUSUAL_TERMINATED.toString))
                        containers -= containerId
                    }
                    if (unusualTerminatedContainers.nonEmpty) log.warning(s"${unusualInstanceState.name} unusual terminated, crawler ${unusualTerminatedContainers.mkString(", ")} might be force to stop")
                    terminatedState
                }
                .recover { case t ⇒ log.warning(s"future recovery:$t") }

              _deleteInstance
          }
        }
        _deleteUnusualTerminatedInstances
    }.flatten
    _checkHealth
  }

  def autoScalingInstance(): Future[_] = {

    val currentVmNum = instances.size
    val currentContainerNum = containers.size
    val idleVm = instances.filter(_._2.containerNum == 0).values.toSeq
    val currentEmptyContainerSlot: Int = currentVmNum * config.containerAmountInVm - currentContainerNum

    val _scaling = (dbActor ? GetNextStartCrawlersMessage).mapTo[Seq[NextStartCrawlers]].map(_.find(_.taskId == taskId)).flatMap {
      case row @ Some(NextStartCrawlers(_, nextExecuteCrawlerNum, nextExecuteTime, nextExecuteCrawlers)) ⇒
        log.info(s"next execute crawlers: ${row.get.copy(crawlers = nextExecuteCrawlers.sorted)}")
        // increase instance
        if (instances.size < config.vmAmount && currentEmptyContainerSlot < nextExecuteCrawlerNum) {
          addVm(currentEmptyContainerSlot, nextExecuteCrawlerNum, nextExecuteCrawlers, nextExecuteTime)
          // decrease instance
        } else if (idleVm.nonEmpty) {
          deleteIdleVm(idleVm, currentEmptyContainerSlot, nextExecuteCrawlerNum)
        } else {
          Future.successful(())
        }
      case None ⇒
        // no crawler need running after 5 min
        if (idleVm.nonEmpty) {
          deleteIdleVm(idleVm, currentEmptyContainerSlot, 0)
        } else {
          Future.successful(())
        }
    }
    _scaling
  }

  private[this] def addVm(currentEmptyContainerSlot: Int, nextExecuteCrawlerNum: Int, nextExecuteCrawlers: Seq[Int], nextExecuteTime: LocalDateTime): Future[_] = {

    if (instances.size < config.vmAmount && currentEmptyContainerSlot < nextExecuteCrawlerNum) {
      val availableVmAmount = config.vmAmount - instances.size
      val needVmAmount = Math.ceil((nextExecuteCrawlerNum - currentEmptyContainerSlot) / config.containerAmountInVm.toDouble).toInt
      val preStartVmAmount = Math.min(config.maxGcpOperationNum, if (needVmAmount <= availableVmAmount) needVmAmount else availableVmAmount)

      val _addVM = Future.sequence {
        (1 to preStartVmAmount)
          .map {
            _ ⇒
              // create vm
              (gcpOperationActor ? CreateInstanceMessage(
                taskId,
                s"crawler-task-${taskId.formatted("%04d")}-$programSerial-${instanceSerial.incrementAndGet().formatted("%04d")}",
                config.vmRegion,
                config.vmZone,
                config.vmImage,
                config.vmCore,
                config.vmMemory
              )).mapTo[InstanceStateRow]
          }
      }.map {
        createdInstanceStateRows ⇒
          createdInstanceStateRows.foreach {
            createdRow ⇒
              // write instance_state table
              dbActor ! WriteInstanceStateMessage(createdRow)
              // write instance_log table
              dbActor ! WriteInstanceLogMessage(InstanceLogRow(createdRow.name, taskId, Status.STAGING.toString, createdRow.containerNum, createdRow.containerIds.sorted, createdRow.ip))
          }
          // write task_instance_scale table
          dbActor ! WriteTaskInstanceScaleMessage(TaskInstanceScaleRow(taskId, TaskAction.ADD_INSTANCE.toString, preStartVmAmount, createdInstanceStateRows.map(_.name).toList, nextExecuteCrawlers.toList, Some(nextExecuteTime)))
          val createdInstances = createdInstanceStateRows.map(r ⇒ r.name → r)
          instances ++= createdInstances
          log.info(s"${createdInstances.size} created instances: ${createdInstances.map { case (_, i) ⇒ s"${i.name}->${i.ip}" }.mkString(", ")}")
      }
      _addVM
    } else {
      Future.successful(())
    }
  }

  private[this] def deleteNoResponseVm(vm: InstanceStateRow): Unit = {

    // terminated idle vm
    (gcpOperationActor ? DeleteInstanceMessage(taskId, vm.name, config.vmRegion, config.vmZone))
      .mapTo[InstanceStateRow]
      // write back active_time, ip
      .map(_.copy(activeTime = vm.activeTime, ip = vm.ip))
      .map {
        terminatedState ⇒
          instances -= terminatedState.name
          // write instance_state table
          dbActor ! WriteInstanceStateMessage(terminatedState)
          // write instance_log table
          dbActor ! WriteInstanceLogMessage(InstanceLogRow(terminatedState.name, terminatedState.taskId, Status.TERMINATED.toString, terminatedState.containerNum, terminatedState.containerIds, terminatedState.ip))
          dbActor ! WriteTaskInstanceScaleMessage(TaskInstanceScaleRow(taskId, TaskAction.REMOVE_NO_RESPONSE_INSTANCE.toString, 1, List(vm.name), List.empty, None))
          log.info(s"terminated no response vm: ${vm.name}->${vm.ip}")
          instances -= vm.name
      }
  }

  private[this] def deleteIdleVm(idleVm: Seq[InstanceStateRow], currentEmptyContainerSlot: Int, nextExecuteCrawlerNum: Int): Future[_] = {

    if (idleVm.nonEmpty) {
      val canTerminateInstanceNum = Math.floor((currentEmptyContainerSlot - nextExecuteCrawlerNum) / config.containerAmountInVm).toInt
      val needTerminatedVm = idleVm.take(canTerminateInstanceNum)
      val _deleteVm = Future.sequence {
        needTerminatedVm.map {
          vm ⇒
            // terminated idle vm
            val _terminatedVmStateRow = (gcpOperationActor ? DeleteInstanceMessage(taskId, vm.name, config.vmRegion, config.vmZone))
              .mapTo[InstanceStateRow]
              // write back active_time, ip
              .map(_.copy(activeTime = vm.activeTime, ip = vm.ip))
              .map {
                terminatedState ⇒
                  instances -= terminatedState.name
                  // write instance_state table
                  dbActor ! WriteInstanceStateMessage(terminatedState)
                  // write instance_log table
                  dbActor ! WriteInstanceLogMessage(InstanceLogRow(terminatedState.name, terminatedState.taskId, Status.TERMINATED.toString, terminatedState.containerNum, terminatedState.containerIds, terminatedState.ip))
                  log.info(s"terminated idle vm: ${vm.name}->${vm.ip}")
                  terminatedState
              }

            _terminatedVmStateRow
        }
      }.map {
        (terminatedStateRows: Seq[InstanceStateRow]) ⇒
          if (terminatedStateRows.nonEmpty) {
            // write task_instance_scale table
            dbActor ! WriteTaskInstanceScaleMessage(TaskInstanceScaleRow(taskId, TaskAction.REMOVE_INSTANCE.toString, terminatedStateRows.size, terminatedStateRows.map(_.name).toList, List.empty, None))
            log.info(s"${needTerminatedVm.size} terminated instances: ${terminatedStateRows.map { vm ⇒ s"${vm.name}->${vm.ip}" }.mkString(", ")}")
          }
      }
      _deleteVm
    } else {
      Future.successful(())
    }
  }

  def stopTask(): Future[_] = {

    log.warning(s"terminate ${this.self.path.name}")
    // write task_log table
    dbActor ! WriteTaskLogMessage(TaskLogRow(taskId, TaskAction.STOP_TASK.toString))
    Await.ready(checkInstanceHealth(), Duration.Inf)
    containers.foreach {
      case (container, CrawlerLocation(crawlerId, instanceName, ip)) ⇒
        val containerRow = ContainerLogRow(container, taskId, crawlerId, instanceName, ip, Status.STOPPING.toString)
        // write container_log table
        dbActor ! WriteContainerLogMessage(containerRow)
    }
    log.warning(s"left ${instances.size} instances: ${instances.values.toList}")
    val _stopTask = Future.sequence {
      instances.values.grouped(config.maxGcpOperationNum).map {
        _.map {
          instanceState ⇒
            val _deleteInstanceState = (gcpOperationActor ? DeleteInstanceMessage(taskId, instanceState.name, config.vmRegion, config.vmZone)).mapTo[InstanceStateRow]
            // write instance_state table
            dbActor ! WriteInstanceStateMessage(instanceState.copy(status = TaskAction.USER_TERMINATED.toString, stopTime = Some(LocalDateTime.now())))
            // write instance_log table
            dbActor ! WriteInstanceLogMessage(InstanceLogRow(instanceState.name, taskId, TaskAction.USER_TERMINATED.toString, 0, instanceState.containerIds, instanceState.ip))
            _deleteInstanceState
        }
      }.flatten
    }.map {
      deleteInstancesState ⇒
        // write task_instance_scale table
        dbActor ! WriteTaskInstanceScaleMessage(TaskInstanceScaleRow(taskId, TaskAction.USER_TERMINATED.toString, deleteInstancesState.size, deleteInstancesState.map(_.name).toList, List.empty, None))
        log.warning(s"${deleteInstancesState.size} force to stop instances: ${deleteInstancesState.map { vm ⇒ s"${vm.name}->${vm.ip}" }.mkString(", ")}")
        log.warning(s"terminate ${this.self.path.name} end")
    }
    _stopTask
  }
}

object TaskAction extends Enumeration {
  type TaskAction = Value
  val START_TASK, STOP_TASK, ADD_INSTANCE, REMOVE_INSTANCE, REMOVE_NO_RESPONSE_INSTANCE, UNUSUAL_TERMINATED, USER_TERMINATED = Value
}

case class CrawlerLocation(crawlerId: Int, instanceName: String, ip: String)