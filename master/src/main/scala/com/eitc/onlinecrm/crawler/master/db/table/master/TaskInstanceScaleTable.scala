package com.eitc.onlinecrm.crawler.master.db.table.master

object TaskInstanceScaleTable {

  import com.eitc.onlinecrm.crawler.master.db.table._
  import slick.jdbc.{ GetResult ⇒ GR }

  import profile.api._

  /**
   * Entity class storing rows of table TaskInstanceScale
   *
   * @param taskId         Database column task_id SqlType(int4)
   * @param action         Database column action SqlType(text)
   * @param instanceNum    Database column instance_num SqlType(int4)
   * @param instanceNames     Database column instance_id SqlType(_int4), Length(10,false)
   * @param nextCrawlerIds Database column next_crawler_ids SqlType(int4), Default(None)
   * @param nextCrawlTime  Database column next_crawl_time SqlType(timestamp), Default(None)
   */
  case class TaskInstanceScaleRow(taskId: Int, action: String, instanceNum: Int, instanceNames: List[String], nextCrawlerIds: List[Int], nextCrawlTime: Option[java.time.LocalDateTime] = None)

  /** GetResult implicit for fetching TaskInstanceScaleRow objects using plain SQL queries */
  implicit def GetResultTaskInstanceScaleRow(implicit e0: GR[Int], e1: GR[String], e2: GR[List[String]], e3: GR[List[Int]], e4: GR[Option[java.time.LocalDateTime]]): GR[TaskInstanceScaleRow] = GR {
    prs ⇒
      import prs._
      TaskInstanceScaleRow.tupled((<<[Int], <<[String], <<[Int], <<[List[String]], <<[List[Int]], <<?[java.time.LocalDateTime]))
  }

  /** Table description of table task_instance_scale. Objects of this class serve as prototypes for rows in queries. */
  class TaskInstanceScale(_tableTag: Tag) extends profile.api.Table[TaskInstanceScaleRow](_tableTag, Some("master"), "task_instance_scale") {
    def * = (taskId, action, instanceNum, instanceNames, nextCrawlerIds, nextCrawlTime) <> (TaskInstanceScaleRow.tupled, TaskInstanceScaleRow.unapply)

    /** Database column seqno SqlType(serial), AutoInc */
    val seqno: Rep[Int] = column[Int]("seqno", O.AutoInc)
    /** Database column task_id SqlType(int4) */
    val taskId: Rep[Int] = column[Int]("task_id")
    /** Database column action SqlType(text) */
    val action: Rep[String] = column[String]("action")
    /** Database column instance_num SqlType(int4) */
    val instanceNum: Rep[Int] = column[Int]("instance_num")
    /** Database column instance_id SqlType(_int4), Length(10,false) */
    val instanceNames: Rep[List[String]] = column[List[String]]("instance_names")
    /** Database column next_crawler_ids SqlType(int4), Default(None) */
    val nextCrawlerIds: Rep[List[Int]] = column[List[Int]]("next_crawler_ids")
    /** Database column next_crawl_time SqlType(timestamp), Default(None) */
    val nextCrawlTime: Rep[Option[java.time.LocalDateTime]] = column[Option[java.time.LocalDateTime]]("next_crawl_time", O.Default(None))
    /** Database column time_insert SqlType(timestamptz) */
    val timeInsert: Rep[java.time.LocalDateTime] = column[java.time.LocalDateTime]("time_insert")
    /** Database column time_last_update SqlType(timestamptz), Default(None) */
    val timeLastUpdate: Rep[Option[java.time.LocalDateTime]] = column[Option[java.time.LocalDateTime]]("time_last_update", O.Default(None))
  }

  /** Collection-like TableQuery object for table TaskInstanceScale */
  lazy val table = new TableQuery(tag ⇒ new TaskInstanceScale(tag))

}
