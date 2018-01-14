package com.eitc.onlinecrm.crawler.master.db.table.master

object InstanceLogTable {

  import com.eitc.onlinecrm.crawler.master.db.table._
  import profile.api._
  import slick.jdbc.{ GetResult ⇒ GR }

  /**
   * Entity class storing rows of table InstanceLog
   *  @param name Database column name SqlType(text)
   *  @param taskId Database column task_id SqlType(int4)
   *  @param action Database column action SqlType(text)
   *  @param containerNum Database column container_num SqlType(int4)
   *  @param containerIds Database column container_ids SqlType(text[]), Length(10,false)
   *  @param ip Database column ip SqlType(text)
   */
  case class InstanceLogRow(name: String, taskId: Int, action: String, containerNum: Int, containerIds: List[Int], ip: String)
  /** GetResult implicit for fetching InstanceLogRow objects using plain SQL queries */
  implicit def GetResultInstanceLogRow(implicit e0: GR[Int], e1: GR[String], e2: GR[List[Int]], e3: GR[java.time.LocalDateTime], e4: GR[Option[java.time.LocalDateTime]]): GR[InstanceLogRow] = GR {
    prs ⇒
      import prs._
      InstanceLogRow.tupled((<<[String], <<[Int], <<[String], <<[Int], <<[List[Int]], <<[String]))
  }

  /** Table description of table instance_log. Objects of this class serve as prototypes for rows in queries. */
  class InstanceLog(_tableTag: Tag) extends profile.api.Table[InstanceLogRow](_tableTag, Some("master"), "instance_log") {
    def * = (instanceName, taskId, action, containerNum, containerIds, ip) <> (InstanceLogRow.tupled, InstanceLogRow.unapply)

    /** Database column seqno SqlType(serial), AutoInc */
    val seqno: Rep[Int] = column[Int]("seqno", O.AutoInc)
    /** Database column instance_name SqlType(text) */
    val instanceName: Rep[String] = column[String]("instance_name")
    /** Database column task_id SqlType(int4) */
    val taskId: Rep[Int] = column[Int]("task_id")
    /** Database column action SqlType(text) */
    val action: Rep[String] = column[String]("action")
    /** Database column container_num SqlType(int4) */
    val containerNum: Rep[Int] = column[Int]("container_num")
    /** Database column container_ids SqlType(_int4), Length(10,false) */
    val containerIds: Rep[List[Int]] = column[List[Int]]("container_ids", O.Length(10, varying = false))
    /** Database column ip SqlType(text) */
    val ip: Rep[String] = column[String]("ip")
    /** Database column time_insert SqlType(timestamptz) */
    val timeInsert: Rep[java.time.LocalDateTime] = column[java.time.LocalDateTime]("time_insert")
    /** Database column time_last_update SqlType(timestamptz), Default(None) */
    val timeLastUpdate: Rep[Option[java.time.LocalDateTime]] = column[Option[java.time.LocalDateTime]]("time_last_update", O.Default(None))
  }
  /** Collection-like TableQuery object for table InstanceLog */
  lazy val table = new TableQuery(tag ⇒ new InstanceLog(tag))

}

