package com.eitc.onlinecrm.crawler.master.json

import com.fasterxml.jackson.annotation.JsonProperty

//case class ValueNode[T](
//  @JsonProperty("value") value: T,
//  @JsonProperty("default") default: Option[T],
//  @JsonProperty("option") option: Option[List[T]],
//  @JsonProperty("desc") desc: String
//)

case class StringNode(
  @JsonProperty("value") value: String,
  @JsonProperty("default") default: Option[String],
  @JsonProperty("option") option: Option[List[String]],
  @JsonProperty("desc") desc: String
)

case class IntNode(
  @JsonProperty("value") value: Int,
  @JsonProperty("default") default: Option[Int],
  @JsonProperty("option") option: Option[List[Int]],
  @JsonProperty("desc") desc: String
)

case class DoubleNode(
  @JsonProperty("value") value: Double,
  @JsonProperty("default") default: Option[Double],
  @JsonProperty("option") option: Option[List[Double]],
  @JsonProperty("desc") desc: String
)

case class DurationNode(
  @JsonProperty("value") value: java.time.Duration,
  @JsonProperty("default") default: Option[java.time.Duration],
  @JsonProperty("option") option: Option[List[java.time.Duration]],
  @JsonProperty("desc") desc: String
)