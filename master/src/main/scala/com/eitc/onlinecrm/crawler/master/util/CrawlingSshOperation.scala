package com.eitc.onlinecrm.crawler.master.util

import org.slf4j.{ Logger, LoggerFactory }

import scala.util.{ Failure, Success, Try }

object CrawlingSshOperation {
  def apply(user: String, key: String, ip: String): CrawlingSshOperation = new CrawlingSshOperation(user, key, ip)
}

class CrawlingSshOperation(user: String, key: String, ip: String) {

  //val keyPath = "src/main/resources/key.pem"
  //val keyPath = "~/key.pem"

  val log: Logger = LoggerFactory.getLogger(this.getClass.getName)

  def execute(cmd: String): String = sys.process.stringSeqToProcess(Seq("/bin/bash", "-c", s"ssh -i $key -q -oStrictHostKeyChecking=no -oUserKnownHostsFile=/dev/null $user@$ip '$cmd'")).!!

  def runCrawler(taskId: Int, crawlerId: Int, crawlerImage: String, crawlerImageVersion: String, containerMemory: Int, environment: String): String = {
    val command =
      s"""docker ps --filter "status=exited" -q | xargs -r docker rm ; \\
         |gcloud docker -- pull $crawlerImage:$crawlerImageVersion &&  \\
         |docker run -d \\
         |-v /dev/shm:/dev/shm \\
         |-e FBCRAWLER_ENV=$environment \\
         |-e FBCRAWLER_DB_USER=crawler \\
         |-e FBCRAWLER_DB_PASSWORD=9423c3da7982bce555ea06033d36198b \\
         |-e AWS_SECRET_ACCESS_KEY=r1hhxPNFe226uo3B7g9FBTTkylUm8IM0mDHMiiFD \\
         |-v /home/onlinecrm/log:/app/log \\
         |--memory=${containerMemory}M \\
         |$crawlerImage:$crawlerImageVersion \\
         |--task_id=$taskId \\
         |--crawler_id=$crawlerId""".stripMargin
    lazy val container = execute(command).split("\n").last
    Try(container) match {
      case Success(i) ⇒
        i
      case Failure(t) ⇒
        log.warn(s"SshOperation runCrawler error: $this")
        throw t
    }
  }

  def runningCrawlers: List[String] = {
    lazy val crawlers = execute("docker ps -q") match {
      case "" ⇒ List.empty
      case s  ⇒ s.split('\n').toList
    }
    Try(crawlers) match {
      case Success(i) ⇒
        i
      case Failure(t) ⇒
        log.warn(s"SshOperation runningCrawlers error: $this")
        throw t
    }
  }

  def runningCrawlerNum: Int = Try(execute("docker ps -q | wc -l").trim.toInt) match {
    case Success(i) ⇒
      i
    case Failure(t) ⇒
      log.warn(s"SshOperation runningCrawlerNum error: $this")
      throw t
  }
  override def toString: String = {
    s"SshOperation($user, $key, $ip)"
  }
}
