package com.eitc.onlinecrm.crawler.master.db.table.master

import com.eitc.onlinecrm.crawler.master.db.Rows.InstanceStateRow
import slick.lifted.ProvenShape

object InstanceStateTable {

  import com.eitc.onlinecrm.crawler.master.db.table._
  import slick.jdbc.{ GetResult ⇒ GR }

  import profile.api._

  /** GetResult implicit for fetching InstanceStateRow objects using plain SQL queries */
  implicit def GetResultInstanceStateRow(implicit e0: GR[Int], e1: GR[String], e2: GR[java.time.LocalDateTime], e3: GR[Option[java.time.LocalDateTime]], e4: GR[List[Int]]): GR[InstanceStateRow] = GR {
    prs ⇒
      import prs._
      InstanceStateRow.tupled((<<[String], <<[Int], <<[String], <<[String], <<[java.time.LocalDateTime], <<?[java.time.LocalDateTime], <<[Int], <<[List[Int]]))
  }

  /** Table description of table instance_state. Objects of this class serve as prototypes for rows in queries. */
  class InstanceState(_tableTag: Tag) extends profile.api.Table[InstanceStateRow](_tableTag, Some("master"), "instance_state") {
    def * : ProvenShape[InstanceStateRow] = (instanceName, taskId, status, ip, activeTime, stopTime, containerNum, containerIds) <> (InstanceStateRow.tupled, InstanceStateRow.unapply)

    /** Database column seqno SqlType(serial), AutoInc */
    val seqno: Rep[Int] = column[Int]("seqno", O.AutoInc)
    /** Database column name SqlType(text) */
    val instanceName: Rep[String] = column[String]("instance_name", O.PrimaryKey)
    /** Database column task_id SqlType(int4) */
    val taskId: Rep[Int] = column[Int]("task_id")
    /** Database column status SqlType(text) */
    val status: Rep[String] = column[String]("status")
    /** Database column ip SqlType(text) */
    val ip: Rep[String] = column[String]("ip")
    /** Database column active_time SqlType(timestamp) */
    val activeTime: Rep[java.time.LocalDateTime] = column[java.time.LocalDateTime]("active_time")
    /** Database column stop_time SqlType(timestamp), Default(None) */
    val stopTime: Rep[Option[java.time.LocalDateTime]] = column[Option[java.time.LocalDateTime]]("stop_time", O.Default(None))
    /** Database column container_num SqlType(int4) */
    val containerNum: Rep[Int] = column[Int]("container_num")
    /** Database column container_ids SqlType(_int4), Length(10,false) */
    val containerIds: Rep[List[Int]] = column[List[Int]]("container_ids", O.Length(10, varying = false))
    /** Database column time_insert SqlType(timestamptz) */
    val timeInsert: Rep[java.time.LocalDateTime] = column[java.time.LocalDateTime]("time_insert")
    /** Database column time_last_update SqlType(timestamptz), Default(None) */
    val timeLastUpdate: Rep[Option[java.time.LocalDateTime]] = column[Option[java.time.LocalDateTime]]("time_last_update", O.Default(None))
  }

  /** Collection-like TableQuery object for table InstanceState */
  lazy val table = new TableQuery(tag ⇒ new InstanceState(tag))

}
