package com.eitc.onlinecrm.crawler.master.actor.register

import akka.actor.{ Actor, ActorLogging }

class RegistryActor extends Actor with ActorLogging {

  private def execute(cmd: String): String = {
    sys.process.stringSeqToProcess(Seq("/bin/bash", "-c", cmd)).!!
  }

  override def receive: Receive = {
    case CreateCrawlerImage(id, container) ⇒
      execute(s"docker commit $container gcr.io/crawler-1383/crawler:$id")
      log.info(s"create crawler image $id")
    case PushCrawlerImage(id) ⇒
      execute(s"gcloud docker -- push gcr.io/crawler-1383/crawler:$id")
      log.info(s"push crawler image $id")
    case PullCrawlerImage(id) ⇒
      execute(s"gcloud docker -- pull gcr.io/crawler-1383/crawler:$id")
      log.info(s"pull crawler image $id")
    case msg ⇒
      log.warning(s"${this.self.path.name} received unknown message $msg")
  }

}