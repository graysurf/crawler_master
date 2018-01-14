package com.eitc.onlinecrm.crawler.master.db

import com.eitc.onlinecrm.crawler.master.db.table.master.TaskTable.TaskHRow

object Rows {

  case class TaskRow(id: Int, name: String, status: String, targetSourceId: Int, targetLabels: List[String], crawlerType: String, crawlerTargetDataType: String, crawlerCrawlingTargetSize: Int, crawlerBreakTime: java.time.Duration, crawlerWorkableStates: List[String], crawlerSearchFailCountThreshold: Int = 5, crawlerClass: String, crawlerAssignedCrawlerId: List[Int], crawlerpoolNewAmount: Int = 0, crawlerpoolSleepingAmount: Int = 0, crawlerpoolRestrictedSearchAmount: Int = 0, crawlerpoolAmount: com.fasterxml.jackson.databind.JsonNode, vmRegion: String, vmMinNum: Int = 0, vmMaxNum: Int = 100, vmCores: Int = 1, vmMemory: Int = 1024, containerImage: String = "gcr.io/crawler-1383/fb-crawler", containerCores: Double = 0.25, containerMemory: Int = 1024, env: String = "product", versionBranch: String = "master", versionTag: String = "latest", creator: Option[String] = None, additionalConf: com.fasterxml.jackson.databind.JsonNode, json: com.fasterxml.jackson.databind.JsonNode)

  implicit class ToTaskRow(hRow: TaskHRow) {
    def toTaskRow: TaskRow = {
      TaskRow(
        hRow(0), hRow(1), hRow(2), hRow(3), hRow(4),
        hRow(5), hRow(6), hRow(7), hRow(8), hRow(9),
        hRow(10), hRow(11), hRow(12), hRow(13), hRow(14),
        hRow(15), hRow(16), hRow(17), hRow(18), hRow(19),
        hRow(20), hRow(21), hRow(22), hRow(23), hRow(24),
        hRow(25), hRow(26), hRow(27), hRow(28), hRow(29),
        hRow(30)
      )
    }
  }

  implicit class ToTaskHRow(row: TaskRow) {

    import slick.collection.heterogeneous._

    def toTaskHRow: TaskHRow = {
      row.id :: row.name :: row.status :: row.targetSourceId :: row.targetLabels ::
        row.crawlerType :: row.crawlerTargetDataType :: row.crawlerCrawlingTargetSize :: row.crawlerBreakTime :: row.crawlerWorkableStates ::
        row.crawlerSearchFailCountThreshold :: row.crawlerClass :: row.crawlerAssignedCrawlerId :: row.crawlerpoolNewAmount :: row.crawlerpoolSleepingAmount ::
        row.crawlerpoolRestrictedSearchAmount :: row.crawlerpoolAmount :: row.vmRegion :: row.vmMinNum :: row.vmMaxNum ::
        row.vmCores :: row.vmMemory :: row.containerImage :: row.containerCores :: row.containerMemory ::
        row.env :: row.versionBranch :: row.versionTag :: row.creator :: row.additionalConf ::
        row.json :: HNil
    }
  }

  /**
   * Entity class storing rows of table Test
   *
   * @param id       Database column id SqlType(int4)
   * @param crawlers Database column crawlers SqlType(_int4), Length(10,false)
   * @param json     Database column json SqlType(jsonb)
   */
  case class TestRow(id: Int, crawlers: List[Int], json: com.fasterxml.jackson.databind.JsonNode, time: java.time.LocalDateTime, sleep_time: java.time.Duration)

  /**
   * Entity class storing rows of table CrawlerLog
   *
   * @param crawler   Database column crawler SqlType(int4)
   * @param task      Database column task SqlType(int4)
   * @param time      Database column time SqlType(timestamp)
   * @param num       Database column num SqlType(int4)
   * @param sleepTime Database column sleep_time SqlType(varchar)
   * @param region    Database column region SqlType(varchar)
   * @param `type`    Database column type SqlType(varchar)
   * @param owner     Database column owner SqlType(varchar)
   */
  case class CrawlerLogRow(crawler: Int, task: Int, time: java.sql.Timestamp, region: String, `type`: String, owner: String, num: Int, sleepTime: String)

  /**
   * Entity class storing rows of table InstanceState
   *
   * @param name         Database column instance_name SqlType(text)
   * @param taskId       Database column task_id SqlType(int4)
   * @param status       Database column status SqlType(text)
   * @param ip           Database column ip SqlType(text)
   * @param activeTime   Database column active_time SqlType(timestamp)
   * @param stopTime     Database column stop_time SqlType(timestamp), Default(None)
   * @param containerNum Database column container_num SqlType(int4)
   * @param containerIds Database column container_ids SqlType(_int4), Length(10,false)
   */
  case class InstanceStateRow(name: String, taskId: Int, status: String, ip: String, activeTime: java.time.LocalDateTime, stopTime: Option[java.time.LocalDateTime] = None, containerNum: Int, containerIds: List[Int])

}
