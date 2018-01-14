package com.eitc.onlinecrm.crawler.master.actor.register

import akka.actor.{ Actor, ActorLogging }

class DbActor extends Actor with ActorLogging {

  override def receive: Receive = {
    case msg ⇒
      log.warning(s"${this.self.path.name} received unknown message $msg")
  }

}