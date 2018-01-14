package com.eitc.onlinecrm.crawler.master.db.table.master

import com.eitc.onlinecrm.crawler.master.db.table.profile
import slick.lifted.ProvenShape

object TaskTable {

  import profile.api._
  import slick.collection.heterogeneous._
  import slick.collection.heterogeneous.syntax._
  import slick.jdbc.{ GetResult ⇒ GR }

  /** Row type of table Task */
  type TaskHRow = HCons[Int, HCons[String, HCons[String, HCons[Int, HCons[List[String], HCons[String, HCons[String, HCons[Int, HCons[java.time.Duration, HCons[List[String], HCons[Int, HCons[String, HCons[List[Int], HCons[Int, HCons[Int, HCons[Int, HCons[com.fasterxml.jackson.databind.JsonNode, HCons[String, HCons[Int, HCons[Int, HCons[Int, HCons[Int, HCons[String, HCons[Double, HCons[Int, HCons[String, HCons[String, HCons[String, HCons[Option[String], HCons[com.fasterxml.jackson.databind.JsonNode, HCons[com.fasterxml.jackson.databind.JsonNode, HNil]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]

  /** Constructor for TaskRow providing default values if available in the database schema. */
  def TaskHRow(id: Int, name: String, status: String, targetSourceId: Int, targetLabels: List[String], crawlerType: String, crawlerTargetDataType: String, crawlerCrawlingTargetSize: Int, crawlerBreakTime: java.time.Duration, crawlerWorkableStates: List[String], crawlerSearchFailCountThreshold: Int = 5, crawlerClass: String, crawlerAssignedCrawlerId: List[Int], crawlerpoolNewAmount: Int = 0, crawlerpoolSleepingAmount: Int = 0, crawlerpoolRestrictedSearchAmount: Int = 0, crawlerpoolAmount: com.fasterxml.jackson.databind.JsonNode, vmRegion: String, vmMinNum: Int = 0, vmMaxNum: Int = 100, vmCores: Int = 1, vmMemory: Int = 1024, containerImage: String = "gcr.io/crawler-1383/fb-crawler", containerCores: Double = 0.25, containerMemory: Int = 1024, env: String = "product", versionBranch: String = "master", versionTag: String = "latest", creator: Option[String] = None, additionalConf: com.fasterxml.jackson.databind.JsonNode, json: com.fasterxml.jackson.databind.JsonNode): TaskHRow = {
    id :: name :: status :: targetSourceId :: targetLabels :: crawlerType :: crawlerTargetDataType :: crawlerCrawlingTargetSize :: crawlerBreakTime :: crawlerWorkableStates :: crawlerSearchFailCountThreshold :: crawlerClass :: crawlerAssignedCrawlerId :: crawlerpoolNewAmount :: crawlerpoolSleepingAmount :: crawlerpoolRestrictedSearchAmount :: crawlerpoolAmount :: vmRegion :: vmMinNum :: vmMaxNum :: vmCores :: vmMemory :: containerImage :: containerCores :: containerMemory :: env :: versionBranch :: versionTag :: creator :: additionalConf :: json :: HNil
  }

  /** GetResult implicit for fetching TaskRow objects using plain SQL queries */
  implicit def GetResultTaskHRow(implicit e0: GR[Int], e1: GR[String], e2: GR[List[String]], e3: GR[java.time.Duration], e4: GR[com.fasterxml.jackson.databind.JsonNode], e5: GR[Double], e6: GR[Option[String]], e7: GR[java.time.LocalDateTime], e8: GR[Option[java.time.LocalDateTime]], e9: GR[List[Int]]): GR[TaskHRow] = GR {
    prs ⇒
      import prs._
      <<[Int] :: <<[String] :: <<[String] :: <<[Int] :: <<[List[String]] :: <<[String] :: <<[String] :: <<[Int] :: <<[java.time.Duration] :: <<[List[String]] :: <<[Int] :: <<[String] :: <<[List[Int]] :: <<[Int] :: <<[Int] :: <<[Int] :: <<[com.fasterxml.jackson.databind.JsonNode] :: <<[String] :: <<[Int] :: <<[Int] :: <<[Int] :: <<[Int] :: <<[String] :: <<[Double] :: <<[Int] :: <<[String] :: <<[String] :: <<[String] :: <<?[String] :: <<[com.fasterxml.jackson.databind.JsonNode] :: <<[com.fasterxml.jackson.databind.JsonNode] :: HNil
  }

  /** Table description of table task. Objects of this class serve as prototypes for rows in queries. */
  class Task(_tableTag: Tag) extends profile.api.Table[TaskHRow](_tableTag, Some("crawler"), "task") {
    def * : ProvenShape[TaskHRow] = id :: name :: status :: targetSourceId :: targetLabels :: crawlerType :: crawlerTargetDataType :: crawlerCrawlingTargetSize :: crawlerBreakTime :: crawlerWorkableStates :: crawlerSearchFailCountThreshold :: crawlerClass :: crawlerAssignedCrawlerId :: crawlerpoolNewAmount :: crawlerpoolSleepingAmount :: crawlerpoolRestrictedSearchAmount :: crawlerpoolAmount :: vmRegion :: vmMinNum :: vmMaxNum :: vmCores :: vmMemory :: containerImage :: containerCores :: containerMemory :: env :: versionBranch :: versionTag :: creator :: additionalConf :: json :: HNil

    /** Database column seqno SqlType(serial), AutoInc */
    val seqno: Rep[Int] = column[Int]("seqno", O.AutoInc)
    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(text) */
    val name: Rep[String] = column[String]("name")
    /** Database column status SqlType(text) */
    val status: Rep[String] = column[String]("status")
    /** Database column target_source_id SqlType(int4) */
    val targetSourceId: Rep[Int] = column[Int]("target_source_id")
    /** Database column target_labels SqlType(_text) */
    val targetLabels: Rep[List[String]] = column[List[String]]("target_labels")
    /** Database column crawler_type SqlType(text) */
    val crawlerType: Rep[String] = column[String]("crawler_type")
    /** Database column crawler_target_data_type SqlType(text) */
    val crawlerTargetDataType: Rep[String] = column[String]("crawler_target_data_type")
    /** Database column crawler_crawling_target_size SqlType(int4) */
    val crawlerCrawlingTargetSize: Rep[Int] = column[Int]("crawler_crawling_target_size")
    /** Database column crawler_break_time SqlType(interval), Length(49,false) */
    val crawlerBreakTime: Rep[java.time.Duration] = column[java.time.Duration]("crawler_break_time", O.Length(49, varying = false))
    /** Database column crawler_workable_states SqlType(_text) */
    val crawlerWorkableStates: Rep[List[String]] = column[List[String]]("crawler_workable_states")
    /** Database column crawler_search_fail_count_threshold SqlType(int4), Default(5) */
    val crawlerSearchFailCountThreshold: Rep[Int] = column[Int]("crawler_search_fail_count_threshold", O.Default(5))
    /** Database column crawler_class SqlType(text) */
    val crawlerClass: Rep[String] = column[String]("crawler_class")
    /** Database column crawler_assigned_crawler_id SqlType(_int4) */
    val crawlerAssignedCrawlerId: Rep[List[Int]] = column[List[Int]]("crawler_assigned_crawler_id")
    /** Database column crawlerpool_new_amount SqlType(int4), Default(0) */
    val crawlerpoolNewAmount: Rep[Int] = column[Int]("crawlerpool_new_amount", O.Default(0))
    /** Database column crawlerpool_sleeping_amount SqlType(int4), Default(0) */
    val crawlerpoolSleepingAmount: Rep[Int] = column[Int]("crawlerpool_sleeping_amount", O.Default(0))
    /** Database column crawlerpool_restricted_search_amount SqlType(int4), Default(0) */
    val crawlerpoolRestrictedSearchAmount: Rep[Int] = column[Int]("crawlerpool_restricted_search_amount", O.Default(0))
    /** Database column crawlerpool_amount SqlType(jsonb) */
    val crawlerpoolAmount: Rep[com.fasterxml.jackson.databind.JsonNode] = column[com.fasterxml.jackson.databind.JsonNode]("crawlerpool_amount")
    /** Database column vm_region SqlType(text) */
    val vmRegion: Rep[String] = column[String]("vm_region")
    /** Database column vm_min_num SqlType(int4), Default(0) */
    val vmMinNum: Rep[Int] = column[Int]("vm_min_num", O.Default(0))
    /** Database column vm_max_num SqlType(int4), Default(100) */
    val vmMaxNum: Rep[Int] = column[Int]("vm_max_num", O.Default(100))
    /** Database column vm_cores SqlType(int4), Default(1) */
    val vmCores: Rep[Int] = column[Int]("vm_cores", O.Default(1))
    /** Database column vm_memory SqlType(int4), Default(1024) */
    val vmMemory: Rep[Int] = column[Int]("vm_memory", O.Default(1024))
    /** Database column container_image SqlType(text), Default(gcr.io/crawler-1383/fb-crawler) */
    val containerImage: Rep[String] = column[String]("container_image", O.Default("gcr.io/crawler-1383/fb-crawler"))
    /** Database column container_cores SqlType(float8), Default(0.25) */
    val containerCores: Rep[Double] = column[Double]("container_cores", O.Default(0.25))
    /** Database column container_memory SqlType(int4), Default(1024) */
    val containerMemory: Rep[Int] = column[Int]("container_memory", O.Default(1024))
    /** Database column env SqlType(text), Default(product) */
    val env: Rep[String] = column[String]("env", O.Default("product"))
    /** Database column version_branch SqlType(text), Default(master) */
    val versionBranch: Rep[String] = column[String]("version_branch", O.Default("master"))
    /** Database column version_tag SqlType(text), Default(latest) */
    val versionTag: Rep[String] = column[String]("version_tag", O.Default("latest"))
    /** Database column creator SqlType(text), Default(None) */
    val creator: Rep[Option[String]] = column[Option[String]]("creator", O.Default(None))
    /** Database column additional_conf SqlType(jsonb) */
    val additionalConf: Rep[com.fasterxml.jackson.databind.JsonNode] = column[com.fasterxml.jackson.databind.JsonNode]("additional_conf")
    /** Database column json SqlType(jsonb) */
    val json: Rep[com.fasterxml.jackson.databind.JsonNode] = column[com.fasterxml.jackson.databind.JsonNode]("json")
    /** Database column time_insert SqlType(timestamptz) */
    val timeInsert: Rep[java.time.LocalDateTime] = column[java.time.LocalDateTime]("time_insert")
    /** Database column time_last_update SqlType(timestamptz), Default(None) */
    val timeLastUpdate: Rep[Option[java.time.LocalDateTime]] = column[Option[java.time.LocalDateTime]]("time_last_update", O.Default(None))
  }

  /** Collection-like TableQuery object for table Task */
  lazy val Task = new TableQuery(tag ⇒ new Task(tag))
}
