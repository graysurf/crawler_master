package com.eitc.onlinecrm.crawler.master.actor.crawling

import java.time.LocalDateTime

import akka.actor.{ Actor, ActorLogging, ActorRef }
import akka.util.Timeout
import com.eitc.onlinecrm.crawler.master.db.Rows.InstanceStateRow
import com.eitc.onlinecrm.crawler.master.db.table.master.ContainerLogTable.ContainerLogRow
import com.eitc.onlinecrm.crawler.master.db.table.master.CrawlerLogTable.CrawlerLogRow
import com.eitc.onlinecrm.crawler.master.db.table.master.InstanceLogTable.InstanceLogRow
import com.eitc.onlinecrm.crawler.master.db.table.master.TaskInstanceScaleTable.TaskInstanceScaleRow
import com.eitc.onlinecrm.crawler.master.db.table.master.TaskLogTable.TaskLogRow
import com.eitc.onlinecrm.crawler.master.db.table.master._
import org.graysurf.util.db.ExtendPostgresProfile
import slick.basic.DatabaseConfig

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{ Failure, Success }

class DbActor(task: String, dbConfig: DatabaseConfig[_]) extends Actor with ActorLogging {

  implicit val timeout: Timeout = 60 minutes

  import ExtendPostgresProfile.api._
  import context.dispatcher
  import dbConfig.db

  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    super.preStart()
    db.run {
      sql"select VERSION()".as[String].head
    }.foreach(v ⇒ log.info(s" PG DB version $v connection started"))
  }

  override def receive: Receive = {
    case WriteTaskLogMessage(taskLogRow) ⇒
      printError(db.run(TaskLogTable.table.forceInsert(taskLogRow)))
    case WriteInstanceLogMessage(instanceLogRow) ⇒
      printError(db.run(InstanceLogTable.table.forceInsert(instanceLogRow)))
    case WriteInstanceStateMessage(instanceStateRow) ⇒
      printError(db.run(InstanceStateTable.table.insertOrUpdate(instanceStateRow)))
    case WriteContainerLogMessage(containerLogRow) ⇒
      printError(db.run(ContainerLogTable.table.forceInsert(containerLogRow)))
    case WriteTaskInstanceScaleMessage(taskInstanceScaleRow) ⇒
      printError(db.run(TaskInstanceScaleTable.table.forceInsert(taskInstanceScaleRow)))
    case WriteCrawlerLogMessage(crawlerLogRow) ⇒
      printError(db.run(CrawlerLogTable.table.forceInsert(crawlerLogRow)))
    case GetTaskMessage ⇒
      val sdr = sender()
      import com.eitc.onlinecrm.crawler.master.db.Rows._
      val _task = db.run(TaskTable.Task.result).map(_.map(_.toTaskRow))
      printError(_task, Some(sdr))
    case GetCrawlerStatusMessage ⇒
      val sdr = sender()
      val _status = db.run {
        import ExtendPostgresProfile.plainApi._
        sql"""
             |select
             |  c.task_id,
             |  c.crawler_id,
             |  c.state,
             |  coalesce(c.time_last_working + t.crawler_break_time, now()) :: timestamp without time zone as next,
             |  t.crawler_break_time,
             |  c.time_last_working :: timestamp without time zone
             |from crawler.crawler_state c
             |  join crawler.task t on c.task_id = t.id
             |where (c.state::text = any (t.crawler_workable_states) or
             |       (c.state = 'working' and current_timestamp > c.time_last_working + '1 hour'))
             |order by c.task_id, c.crawler_id""".stripMargin
          .as[(Int, Int, String, LocalDateTime, String, Option[LocalDateTime])]
          .map(_.map(CrawlerStatus.tupled))
      }
      printError(_status, Some(sdr))
    case GetNextStartCrawlersMessage ⇒
      val sdr = sender()
      val _f = db.run {
        import ExtendPostgresProfile.plainApi._
        sql"""
             |with next_crawlers as (select
             |                         c.task_id,
             |                         c.crawler_id,
             |                         c.state,
             |                         coalesce(c.time_last_working + t.crawler_break_time,now()) :: timestamp without time zone as next,
             |                         t.crawler_break_time,
             |                         c.time_last_working :: timestamp without time zone
             |                       from crawler.crawler_state c
             |                         join crawler.task t on c.task_id = t.id
             |                       where (c.state::text = any (t.crawler_workable_states) or
             |                              (c.state = 'working' and current_timestamp > c.time_last_working + '1 hour'))
             |                       order by c.task_id, c.crawler_id)
             |select
             |  task_id,
             |  count(crawler_id)                                                                           as num,
             |  min(coalesce(time_last_working + crawler_break_time, now())) :: timestamp without time zone as next_execute_time,
             |  array_agg(crawler_id)                                                                       as crawlers
             |from next_crawlers
             |where current_timestamp + '5 minute' > next
             |group by task_id
           """.stripMargin.as[(Int, Int, LocalDateTime, Seq[Int])].map(_.map(NextStartCrawlers.tupled))
      }
      printError(_f, Some(sdr))
    case GetContainerStatus ⇒
      val sdr = sender()
      val _f = db.run {
        import ExtendPostgresProfile.plainApi._
        sql"""select host, containers from master.view_last_instance_health where task=${task}
           """.as[(String, Seq[String])].map(_.map(RunningContainers.tupled))
      }
      printError(_f, Some(sdr))

    case msg ⇒
      log.warning(s"${this.self.path.name} received unknown message $msg")
  }

  def printError(f: Future[_], sender: Option[ActorRef] = None): Unit = {
    f.onComplete {
      case Success(r) ⇒
        sender match {
          case Some(sdr) ⇒ sdr ! r
          case None      ⇒
        }
      case Failure(e) ⇒
        log.error(e, "DbActor error")
    }
  }

}

case class WriteTaskLogMessage(taskLogRow: TaskLogRow)

case class WriteInstanceLogMessage(instanceLogRow: InstanceLogRow)

case class WriteInstanceStateMessage(instanceStateRow: InstanceStateRow)

case class WriteContainerLogMessage(containerLogRow: ContainerLogRow)

case class WriteTaskInstanceScaleMessage(taskInstanceScaleRow: TaskInstanceScaleRow)

case class WriteCrawlerLogMessage(crawlerLogRow: CrawlerLogRow)

case object GetTaskMessage

case object GetCrawlerStatusMessage

case object GetNextStartCrawlersMessage

case object GetContainerStatus

case class TaskStatus(id: Int, name: String, status: Boolean, city: String, region: String, conf: String)

case class CrawlerStatus(task: Int, crawler: Int, status: String, next: LocalDateTime, sleep: String, last: Option[LocalDateTime])

case class NextStartCrawlers(taskId: Int, crawlerAmount: Int, nextExecuteTime: LocalDateTime, crawlers: Seq[Int])

case class RunningContainers(instance_name: String, containers: Seq[String])
