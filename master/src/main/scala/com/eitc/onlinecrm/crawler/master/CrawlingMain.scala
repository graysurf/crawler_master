package com.eitc.onlinecrm.crawler.master

import akka.actor.{ ActorSystem, CoordinatedShutdown, Props }
import akka.pattern._
import akka.util.Timeout
import com.eitc.onlinecrm.crawler.master.actor.crawling.{ DbActor, GcpOperationActor, TaskActor }
import org.graysurf.util.QuartzScheduler
import org.slf4j.{ Logger, LoggerFactory }
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.{ Duration, _ }
import scala.concurrent.{ Await, Future }
import scala.language.postfixOps
import scala.util.{ Failure, Success }

/*
crawler_master GCP
Usage: crawler_master [options] task-id

  task-id                  tasks for monitor
  --vm-amount <value>      default 2
  --vm-image <value>       default server-base
  --vm-region <value>      default us-west1
  --vm-zone <value>        default a
  --vm-core <value>        default 1
  --vm-memory <value>      default container-memory * container-amount-in-vm + 256
  --container-image <value>
                           default gcr.io/crawler-1383/fb-crawler
  --container-image-version <value>
                           default latest
  --container-amount-in-vm <value>
                           default 4
  --container-core <value>
                           WARN: not implement yet, default 0.25
  --container-memory <value>
                           default 1024
  -c, --cron <value>       default '0 0/3 * * * ?'
  --first-sleep-time <value>
                           default 60 (seconds)
  -u, --user <value>       default empty
  -b, --branch <value>     default master
  -t, --tag <value>        default ''
  -k, --secret-key <value>
                           key for decrypting PII
  -e, --environment <value>
                           must be 'product' or 'develop' default product
  --ssh-user <value>       default onlinecrm
  --ssh-key-path <value>   default /etc/ssh/onlinecrm/onlinecrm.pem
  --help                   prints this usage text
  */

object CrawlingMain {

  val log: Logger = LoggerFactory.getLogger(this.getClass.getName)

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val timeout: Timeout = 10 minutes

  val parser = new scopt.OptionParser[GcpCrawlerMasterConfig]("crawler_master") {

    head("crawler_master", "GCP")

    arg[Seq[Int]]("task-id")
      .unbounded()
      .action((value, config) ⇒ config.copy(tasks = config.tasks ++ value))
      .text("tasks for monitor")

    opt[Int]("vm-amount")
      .action((value, config) ⇒ config.copy(vmAmount = value))
      .validate {
        n ⇒
          if (n > 0) success
          else failure("vm-amount should >0")
      }
      .text("default 2")

    opt[String]("vm-image")
      .action((value, config) ⇒ config.copy(vmImage = value))
      .text("default server-base")

    opt[String]("vm-region")
      .action((value, config) ⇒ config.copy(vmRegion = value))
      .text("default us-west1")

    opt[String]("vm-zone")
      .action((value, config) ⇒ config.copy(vmZone = value))
      .text("default a")

    opt[Int]("vm-core")
      .action((value, config) ⇒ config.copy(vmCore = value))
      .text("default 1")

    opt[Int]("vm-memory")
      .action((value, config) ⇒ config.copy(vmMemory = 256 * Math.ceil(value / 256.0).toInt))
      .text("default container-memory * container-amount-in-vm + 256 ")

    opt[String]("container-image")
      .action((value, config) ⇒ config.copy(containerImage = value))
      .text("default gcr.io/crawler-1383/fb-crawler")

    opt[String]("container-image-version")
      .action((value, config) ⇒ config.copy(containerImageVersion = value))
      .text("default latest")

    opt[Int]("container-amount-in-vm")
      .action((value, config) ⇒ config.copy(containerAmountInVm = value))
      .text("default 4")

    opt[Double]("container-core")
      .action((value, config) ⇒ config.copy(containerCore = value))
      .text("WARN: not implement yet, default 0.25")

    opt[Int]("container-memory")
      .action((value, config) ⇒ config.copy(containerMemory = 256 * Math.ceil(value / 256.0).toInt))
      .text("default 1024")

    opt[String]('c', "cron")
      .action((value, config) ⇒ config.copy(cron = value))
      .text("default '0 0/3 * * * ?'")

    opt[Int]("first-sleep-time")
      .action((value, config) ⇒ config.copy(firstSleepTime = value))
      .text("default 60 (seconds)")

    opt[String]('u', "user")
      .action((value, config) ⇒ config.copy(user = value.toLowerCase.capitalize))
      .text("default empty")

    opt[String]('b', "branch")
      .action((value, config) ⇒ config.copy(branch = value))
      .text("default master")

    opt[String]('t', "tag")
      .action((value, config) ⇒ config.copy(tag = value))
      .text("default ''")

    opt[String]('k', "secret-key")
      .action((value, config) ⇒ config.copy(secret_key = value))
      .text("key for decrypting PII")

    opt[String]('e', "environment")
      .action((value, config) ⇒ config.copy(environment = value))
      .validate {
        v ⇒
          if (v == "product" || v == "develop") success
          else failure("'product' or 'develop'")
      }
      .text("must be 'product' or 'develop' default product")

    opt[String]("ssh-user")
      .action((value, config) ⇒ config.copy(sshUser = value))
      .text("default onlinecrm")

    opt[String]("ssh-key-path")
      .action((value, config) ⇒ config.copy(sshKeyPath = value))
      .text("default /etc/ssh/onlinecrm/onlinecrm.pem")

    opt[Int]("maxGcpOperationNum")
      .action((value, config) ⇒ config.copy(maxGcpOperationNum = value))
      .text("default 10")

    help("help").text("prints this usage text")

  }

  def main(args: Array[String]): Unit = {
    parser.parse(args, GcpCrawlerMasterConfig())
      .map {
        config ⇒
          // allocate vm memory
          val totalContainerMemory = config.containerMemory * config.containerAmountInVm
          if (totalContainerMemory + 256 < config.vmMemory && config.vmMemory != GcpCrawlerMasterConfig().vmMemory) {
            config
          } else {
            config.copy(vmMemory = totalContainerMemory + 256)
          }
      }.foreach {
        config ⇒
          log.info(s"crawler master config: $config")

          val system = ActorSystem("crawler_master")

          val dbName = if (config.environment == "product") "database.jetcw_prod" else "database.gcp_dev"

          val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig[JdbcProfile](dbName)

          val taskActors = config.tasks.map {
            taskId ⇒
              system.actorOf(Props(classOf[TaskActor], taskId, dbConfig, config), s"TaskActor-${taskId.formatted("%04d")}")
          }

          log.info(s"create ${taskActors.size} task actors: ${taskActors.mkString(",")}")

          CoordinatedShutdown(system).addJvmShutdownHook {
            log.warn("program shutdown.....")
            val _stopTasks = Future.sequence(taskActors.map(_ ? StopTaskMessage)).flatMap(_ ⇒ system.terminate())
            log.info(Await.result(_stopTasks, Duration.Inf).toString)
            log.warn("program shutdown end")
          }

          Thread.sleep(config.firstSleepTime * 1000)
          taskActors.foreach {
            actor ⇒
              QuartzScheduler.newJob(actor.path.name, config.cron) {
                log.info(s"---------- ${actor.path.name} start ----------")
                val _finish = actor ? RunTaskMessage
                _finish.onComplete {
                  case Success(_) ⇒
                  case Failure(t) ⇒
                    log.error(s"running ${actor.path.name} error", t)
                    log.info(s"reset ${actor.path.name}")
                    actor ! ResetMessage
                }
                Await.result(_finish, Duration.Inf)
                Thread.sleep(100)
                log.info(s"---------- ${actor.path.name} end   ----------")
              }
          }
      }
  }
}

case object RunTaskMessage
case object ResetMessage

case object RunTaskDoneMessage

case object StopTaskMessage

case class GcpCrawlerMasterConfig(
  tasks: Seq[Int] = Seq.empty[Int],
  vmAmount: Int = 2,
  vmImage: String = "server-base",
  vmRegion: String = "us-west1",
  vmZone: String = "a",
  vmCore: Int = 1,
  vmMemory: Int = 4096,
  containerAmountInVm: Int = 4,
  containerImage: String = "gcr.io/crawler-1383/fb-crawler",
  containerImageVersion: String = "latest",
  containerCore: Double = 0.25,
  containerMemory: Int = 1024,
  cron: String = "0 0/3 * * * ?",
  firstSleepTime: Int = 60,
  user: String = "",
  branch: String = "master",
  tag: String = "",
  secret_key: String = "",
  environment: String = "product",
  sshUser: String = "onlinecrm",
  sshKeyPath: String = "/etc/ssh/onlinecrm/onlinecrm.pem",
  maxGcpOperationNum: Int = 10
)
