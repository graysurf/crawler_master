package com.eitc.onlinecrm.crawler.master.json

import com.fasterxml.jackson.annotation.JsonProperty

//case class ValueArrayNode[T](
//  @JsonProperty("value") value: List[T],
//  @JsonProperty("default") default: Option[List[T]],
//  @JsonProperty("option") option: Option[List[T]],
//  @JsonProperty("desc") desc: String
//)

case class StringArrayNode(
  @JsonProperty("value") value: List[String],
  @JsonProperty("default") default: Option[List[String]],
  @JsonProperty("option") option: Option[List[String]],
  @JsonProperty("desc") desc: String
)

case class IntArrayNode(
  @JsonProperty("value") value: List[Int],
  @JsonProperty("default") default: Option[List[Int]],
  @JsonProperty("option") option: Option[List[Int]],
  @JsonProperty("desc") desc: String
)

case class DoubleArrayNode(
  @JsonProperty("value") value: List[Double],
  @JsonProperty("default") default: Option[List[Double]],
  @JsonProperty("option") option: Option[List[Double]],
  @JsonProperty("desc") desc: String
)
