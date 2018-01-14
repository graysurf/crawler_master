package com.eitc.onlinecrm.crawler.master.db.table.master

object TaskLogTable {

  import com.eitc.onlinecrm.crawler.master.db.table._
  import slick.jdbc.{ GetResult ⇒ GR }

  import profile.api._

  /**
   * Entity class storing rows of table TaskLog
   *
   * @param taskId Database column task_id SqlType(int4)
   * @param action Database column action SqlType(text)
   */
  case class TaskLogRow(taskId: Int, action: String)

  /** GetResult implicit for fetching TaskLogRow objects using plain SQL queries */
  implicit def GetResultTaskLogRow(implicit e0: GR[Int], e1: GR[String], e2: GR[java.time.LocalDateTime], e3: GR[Option[java.time.LocalDateTime]]): GR[TaskLogRow] = GR {
    prs ⇒
      import prs._
      TaskLogRow.tupled((<<[Int], <<[String]))
  }

  /** Table description of table task_log. Objects of this class serve as prototypes for rows in queries. */
  class TaskLog(_tableTag: Tag) extends profile.api.Table[TaskLogRow](_tableTag, Some("master"), "task_log") {
    def * = (taskId, action) <> (TaskLogRow.tupled, TaskLogRow.unapply)

    /** Database column seqno SqlType(serial), AutoInc */
    val seqno: Rep[Int] = column[Int]("seqno", O.AutoInc)
    /** Database column task_id SqlType(int4) */
    val taskId: Rep[Int] = column[Int]("task_id")
    /** Database column action SqlType(text) */
    val action: Rep[String] = column[String]("action")
    /** Database column time_insert SqlType(timestamptz) */
    val timeInsert: Rep[java.time.LocalDateTime] = column[java.time.LocalDateTime]("time_insert")
    /** Database column time_last_update SqlType(timestamptz), Default(None) */
    val timeLastUpdate: Rep[Option[java.time.LocalDateTime]] = column[Option[java.time.LocalDateTime]]("time_last_update", O.Default(None))
  }

  /** Collection-like TableQuery object for table TaskLog */
  lazy val table = new TableQuery(tag ⇒ new TaskLog(tag))

}
