package com.eitc.onlinecrm.crawler.master.actor.register

import akka.actor.{ Actor, ActorLogging }

import scala.util.Try

class VpnActor(provider: String) extends Actor with ActorLogging {

  val configs: Seq[String] = findOvpnFile(provider)

  override def receive: Receive = {
    case msg ⇒
      log.warning(s"${this.self.path.name} received unknown message $msg")
  }

  def findOvpnFile(provider: String): Seq[String] = {
    import scala.sys.process._
    s"find /etc/openvpn/$provider -name '*.ovpn'".lineStream_!
  }

}