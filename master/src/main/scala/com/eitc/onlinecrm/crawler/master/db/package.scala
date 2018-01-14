package com.eitc.onlinecrm.crawler.master

import com.fasterxml.jackson.databind.{ DeserializationFeature, ObjectMapper, SerializationFeature }
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

package object db {

  val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule).registerModule(new JavaTimeModule())
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.enable(SerializationFeature.INDENT_OUTPUT)

}
