package com.eitc.onlinecrm.crawler.master.json

import com.eitc.onlinecrm.crawler.master.db.mapper
import com.eitc.onlinecrm.crawler.master.db.table.master.TaskTable.TaskHRow
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode

case class TaskNode(
    @JsonProperty("task") task: StringNode,
    @JsonProperty("db") db: DbNode,
    @JsonProperty("crawler") crawler: CrawlerNode,
    @JsonProperty("vm") vm: VmNode,
    @JsonProperty("container") container: ContainerNode,
    @JsonProperty("additional_conf") additionalConf: JsonNode,
    @JsonProperty("desc") desc: String
) {
  def toHRow: TaskHRow = {
    TaskHRow(
      id                                = 0,
      name                              = task.value,
      status                            = "init",
      targetSourceId                    = db.targetSourceId.value,
      targetLabels                      = db.targetLabels.value,
      crawlerType                       = crawler.job.crawlerType.value,
      crawlerTargetDataType             = crawler.job.crawlerTargetDataType.value,
      crawlerCrawlingTargetSize         = crawler.crawlingTargetSize.value,
      crawlerBreakTime                  = crawler.breakTime.value,
      crawlerWorkableStates             = crawler.workableStates.value,
      crawlerSearchFailCountThreshold   = crawler.searchFailCountThreshold.value,
      crawlerClass                      = crawler.crawlerClass.value,
      crawlerAssignedCrawlerId          = crawler.assignedCrawlerId.value,
      crawlerpoolNewAmount              = crawler.crawlerpoolAmount.`new`.value,
      crawlerpoolSleepingAmount         = crawler.crawlerpoolAmount.sleep.value,
      crawlerpoolRestrictedSearchAmount = crawler.crawlerpoolAmount.restrictedSearch.value,
      crawlerpoolAmount                 = mapper.valueToTree(crawler.crawlerpoolAmount),
      vmRegion                          = vm.region.value,
      vmMinNum                          = vm.minVmNum.value,
      vmMaxNum                          = vm.maxVmNum.value,
      vmCores                           = vm.cores.value,
      vmMemory                          = vm.memory.value,
      containerImage                    = container.image.value,
      containerCores                    = vm.cores.value,
      containerMemory                   = container.memory.value,
      env                               = additionalConf.get("env").get("value").asText(),
      versionBranch                     = additionalConf.get("branch").get("value").asText(),
      versionTag                        = additionalConf.get("secret_key").get("value").asText(),
      creator                           = Option(additionalConf.get("create_user")).map(_.get("value").asText()),
      additionalConf                    = additionalConf,
      json                              = mapper.valueToTree(this)
    )
  }

}

case class DbNode(
  @JsonProperty("db_name") targetSourceId: IntNode,
  @JsonProperty("dataset_label") targetLabels: StringArrayNode,
  @JsonProperty("desc") desc: String
)

case class CrawlerNode(
  @JsonProperty("crawler_job") job: CrawlerJobNode,
  @JsonProperty("crawler_class") crawlerClass: StringNode,
  @JsonProperty("crawler_status_for_crawling") workableStates: StringArrayNode,
  @JsonProperty("crawler_amount") crawlerpoolAmount: CrawlerAmountNode,
  @JsonProperty("crawl_times") crawlingTargetSize: IntNode,
  @JsonProperty("break_time") breakTime: DurationNode,
  @JsonProperty("assigned_crawler_id") assignedCrawlerId: IntArrayNode,
  @JsonProperty("consecutive_fail_times_to_check_mapping_function") searchFailCountThreshold: IntNode,
  @JsonProperty("desc") desc: String
)

case class CrawlerJobNode(
  @JsonProperty("job_type") crawlerType: StringNode,
  @JsonProperty("job_mission") crawlerTargetDataType: JobMissionNode,
  @JsonProperty("desc") desc: String
)

case class JobMissionNode(
  @JsonProperty("value") value: String,
  @JsonProperty("option") option: JobMissionOptionNode,
  @JsonProperty("desc") desc: String
)

case class JobMissionOptionNode(
  @JsonProperty("mapping") mapping: List[String],
  @JsonProperty("crawl") crawl: List[String]
)

case class CrawlerAmountNode(
  @JsonProperty("new") `new`: IntNode,
  @JsonProperty("ok") sleep: IntNode,
  @JsonProperty("broken_search") restrictedSearch: IntNode,
  @JsonProperty("desc") desc: String
)

case class VmNode(
  @JsonProperty("region") region: StringNode,
  @JsonProperty("min_vm_num") minVmNum: IntNode,
  @JsonProperty("max_vm_num") maxVmNum: IntNode,
  @JsonProperty("cores") cores: IntNode,
  @JsonProperty("memory") memory: IntNode,
  @JsonProperty("desc") desc: String
)

case class ContainerNode(
  @JsonProperty("image") image: StringNode,
  @JsonProperty("cores") cores: DoubleNode,
  @JsonProperty("memory") memory: ContainerMemoryNode,
  @JsonProperty("desc") desc: String
)

case class ContainerMemoryNode(
  @JsonProperty("value") value: Int,
  @JsonProperty("default") default: ContainerMemoryDefaultNode,
  @JsonProperty("desc") desc: String
)

case class ContainerMemoryDefaultNode(
  @JsonProperty("mapping") mapping: Int,
  @JsonProperty("crawl") crawl: Int
)

