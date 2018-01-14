package com.eitc.onlinecrm.crawler.master.actor

package object register {

  case class CreateCrawlerImage(id: String, container: String)
  case class PushCrawlerImage(id: String)
  case class PullCrawlerImage(id: String)

}

