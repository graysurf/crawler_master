package com.eitc.onlinecrm.crawler.master.db.table.master

import slick.lifted.ProvenShape

object CrawlerLogTable {

  import com.eitc.onlinecrm.crawler.master.db.table._
  import slick.jdbc.{ GetResult ⇒ GR }

  import profile.api._

  /**
   * Entity class storing rows of table CrawlerLog
   *
   * @param crawlerId     Database column crawler_id SqlType(text)
   * @param containerName Database column name SqlType(text)
   * @param taskId        Database column task_id SqlType(int4)
   * @param instanceName  Database column instance_id SqlType(text)
   * @param ip            Database column instance_id SqlType(text)
   */
  case class CrawlerLogRow(crawlerId: Int, containerName: String, taskId: Int, instanceName: String, ip: String)

  /** GetResult implicit for fetching CrawlerLogRow objects using plain SQL queries */
  implicit def GetResultCrawlerLogRow(implicit e0: GR[Int], e1: GR[String], e2: GR[java.time.LocalDateTime], e3: GR[Option[java.time.LocalDateTime]]): GR[CrawlerLogRow] = GR {
    prs ⇒
      import prs._
      CrawlerLogRow.tupled((<<[Int], <<[String], <<[Int], <<[String], <<[String]))
  }

  /** Table description of table container_log. Objects of this class serve as prototypes for rows in queries. */
  class CrawlerLog(_tableTag: Tag) extends profile.api.Table[CrawlerLogRow](_tableTag, Some("master"), "crawler_log") {
    def * : ProvenShape[CrawlerLogRow] = (crawlerId, containerName, taskId, instanceName, ip) <> (CrawlerLogRow.tupled, CrawlerLogRow.unapply)

    /** Database column seqno SqlType(serial), AutoInc */
    val seqno: Rep[Int] = column[Int]("seqno", O.AutoInc)
    /** Database column crawler_id SqlType(int4) */
    val crawlerId: Rep[Int] = column[Int]("crawler_id")
    /** Database column name SqlType(text) */
    val containerName: Rep[String] = column[String]("container_name")
    /** Database column task_id SqlType(int4) */
    val taskId: Rep[Int] = column[Int]("task_id")
    /** Database column instance_id SqlType(int4) */
    val instanceName: Rep[String] = column[String]("instance_name")
    /** Database column instance_id SqlType(text) */
    val ip: Rep[String] = column[String]("ip")
    /** Database column time_insert SqlType(timestamptz) */
    val timeInsert: Rep[java.time.LocalDateTime] = column[java.time.LocalDateTime]("time_insert")
    /** Database column time_last_update SqlType(timestamptz), Default(None) */
    val timeLastUpdate: Rep[Option[java.time.LocalDateTime]] = column[Option[java.time.LocalDateTime]]("time_last_update", O.Default(None))
  }

  /** Collection-like TableQuery object for table CrawlerLog */
  lazy val table = new TableQuery(tag ⇒ new CrawlerLog(tag))

}
