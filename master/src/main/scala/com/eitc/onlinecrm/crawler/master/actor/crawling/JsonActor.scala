package com.eitc.onlinecrm.crawler.master.actor.crawling

import akka.actor.{ Actor, ActorLogging }
import com.eitc.onlinecrm.crawler.master.json.TaskNode
import com.fasterxml.jackson.databind.{ DeserializationFeature, ObjectMapper, SerializationFeature }
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

class JsonActor extends Actor with ActorLogging {

  private val mapper = {
    val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule).registerModule(new JavaTimeModule())
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    mapper.configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, true)
    mapper.enable(SerializationFeature.INDENT_OUTPUT)
    mapper
  }

  override def receive: Receive = {
    case _ ⇒
      val json: TaskNode = mapper.readValue[TaskNode](this.getClass.getClassLoader.getResourceAsStream("task.json"))
  }

}
