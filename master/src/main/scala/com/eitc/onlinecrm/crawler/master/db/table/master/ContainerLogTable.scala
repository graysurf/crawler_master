package com.eitc.onlinecrm.crawler.master.db.table.master

import slick.lifted.ProvenShape

object ContainerLogTable {

  import com.eitc.onlinecrm.crawler.master.db.table._
  import slick.jdbc.{ GetResult ⇒ GR }

  import profile.api._

  /**
   * Entity class storing rows of table ContainerLog
   *
   * @param containerName Database column name SqlType(text)
   * @param taskId        Database column task_id SqlType(int4)
   * @param crawlerId     Database column crawler_id SqlType(text)
   * @param instanceName  Database column instance_id SqlType(text)
   * @param status        Database column status SqlType(text)
   */
  case class ContainerLogRow(containerName: String, taskId: Int, crawlerId: Int, instanceName: String, ip: String, status: String)

  /** GetResult implicit for fetching ContainerLogRow objects using plain SQL queries */
  implicit def GetResultContainerLogRow(implicit e0: GR[Int], e1: GR[String], e2: GR[java.time.LocalDateTime], e3: GR[Option[java.time.LocalDateTime]]): GR[ContainerLogRow] = GR {
    prs ⇒
      import prs._
      ContainerLogRow.tupled((<<[String], <<[Int], <<[Int], <<[String], <<[String], <<[String]))
  }

  /** Table description of table container_log. Objects of this class serve as prototypes for rows in queries. */
  class ContainerLog(_tableTag: Tag) extends profile.api.Table[ContainerLogRow](_tableTag, Some("master"), "container_log") {
    def * : ProvenShape[ContainerLogRow] = (containerName, taskId, crawlerId, instanceName, ip, status) <> (ContainerLogRow.tupled, ContainerLogRow.unapply)

    /** Database column seqno SqlType(serial), AutoInc */
    val seqno: Rep[Int] = column[Int]("seqno", O.AutoInc)
    /** Database column name SqlType(text) */
    val containerName: Rep[String] = column[String]("container_name")
    /** Database column task_id SqlType(int4) */
    val taskId: Rep[Int] = column[Int]("task_id")
    /** Database column crawler_id SqlType(int4) */
    val crawlerId: Rep[Int] = column[Int]("crawler_id")
    /** Database column instance_id SqlType(int4) */
    val instanceName: Rep[String] = column[String]("instance_name")
    /** Database column instance_id SqlType(text) */
    val ip: Rep[String] = column[String]("ip")
    /** Database column status SqlType(text) */
    val status: Rep[String] = column[String]("status")
    /** Database column time_insert SqlType(timestamptz) */
    val timeInsert: Rep[java.time.LocalDateTime] = column[java.time.LocalDateTime]("time_insert")
    /** Database column time_last_update SqlType(timestamptz), Default(None) */
    val timeLastUpdate: Rep[Option[java.time.LocalDateTime]] = column[Option[java.time.LocalDateTime]]("time_last_update", O.Default(None))
  }

  /** Collection-like TableQuery object for table ContainerLog */
  lazy val table = new TableQuery(tag ⇒ new ContainerLog(tag))

}
