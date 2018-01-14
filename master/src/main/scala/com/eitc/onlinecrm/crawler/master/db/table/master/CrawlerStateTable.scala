package com.eitc.onlinecrm.crawler.master.db.table.master

import com.eitc.onlinecrm.crawler.master.db.table.profile
import slick.lifted.ProvenShape

object CrawlerStateTable {

  import profile.api._
  import slick.collection.heterogeneous._
  import slick.collection.heterogeneous.syntax._
  import slick.jdbc.{ GetResult ⇒ GR }

  type CrawlerStateHRow = HCons[Int, HCons[Int, HCons[String, HCons[String, HCons[String, HCons[Option[java.time.LocalDateTime], HCons[String, HCons[Boolean, HCons[Int, HCons[Boolean, HCons[Boolean, HCons[com.fasterxml.jackson.databind.JsonNode, HCons[Option[String], HCons[Option[Int], HCons[Option[String], HCons[Int, HCons[Int, HCons[Int, HCons[Int, HCons[Int, HCons[Int, HCons[Int, HCons[Int, HCons[Int, HCons[Int, HCons[Int, HCons[Int, HNil]]]]]]]]]]]]]]]]]]]]]]]]]]]

  /** Constructor for CrawlerStateRow providing default values if available in the database schema. */
  def CrawlerStateHRow(crawlerId: Int, taskId: Int, fbAccount: String, fbPassword: String, state: String = "sleeping", timeLastWorking: Option[java.time.LocalDateTime] = None, language: String = "zh_CN", useCookie: Boolean = false, periodAddFriend: Int = 0, likePages: Boolean = false, likePosts: Boolean = false, browserCookies: com.fasterxml.jackson.databind.JsonNode, browserLocalstorage: Option[String] = None, friendSize: Option[Int] = None, verificationState: Option[String] = None, counterError: Int = 0, counterMemberFetched: Int = 0, counterMemberCompleted: Int = 0, counterEmailNotfoundContinuously: Int = 0, counterPhoneNotfoundContinuously: Int = 0, counterEmailSearchFound: Int = 0, counterEmailSearchRestricted: Int = 0, counterPhoneSearchFound: Int = 0, counterPhoneSearchRestricted: Int = 0, counterLikeCrawled: Int = 0, counterAboutCrawled: Int = 0, counterFriendCrawled: Int = 0): CrawlerStateHRow = {
    crawlerId :: taskId :: fbAccount :: fbPassword :: state :: timeLastWorking :: language :: useCookie :: periodAddFriend :: likePages :: likePosts :: browserCookies :: browserLocalstorage :: friendSize :: verificationState :: counterError :: counterMemberFetched :: counterMemberCompleted :: counterEmailNotfoundContinuously :: counterPhoneNotfoundContinuously :: counterEmailSearchFound :: counterEmailSearchRestricted :: counterPhoneSearchFound :: counterPhoneSearchRestricted :: counterLikeCrawled :: counterAboutCrawled :: counterFriendCrawled :: HNil
  }

  /** GetResult implicit for fetching CrawlerStateRow objects using plain SQL queries */
  implicit def GetResultCrawlerStateHRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Option[java.time.LocalDateTime]], e3: GR[Boolean], e4: GR[com.fasterxml.jackson.databind.JsonNode], e5: GR[Option[String]], e6: GR[Option[Int]]): GR[CrawlerStateHRow] = GR {
    prs ⇒
      import prs._
      <<[Int] :: <<[Int] :: <<[String] :: <<[String] :: <<[String] :: <<?[java.time.LocalDateTime] :: <<[String] :: <<[Boolean] :: <<[Int] :: <<[Boolean] :: <<[Boolean] :: <<[com.fasterxml.jackson.databind.JsonNode] :: <<?[String] :: <<?[Int] :: <<?[String] :: <<[Int] :: <<[Int] :: <<[Int] :: <<[Int] :: <<[Int] :: <<[Int] :: <<[Int] :: <<[Int] :: <<[Int] :: <<[Int] :: <<[Int] :: <<[Int] :: HNil
  }

  /** Table description of table crawler_state. Objects of this class serve as prototypes for rows in queries. */
  class CrawlerState(_tableTag: Tag) extends profile.api.Table[CrawlerStateHRow](_tableTag, Some("crawler"), "crawler_state") {
    def * : ProvenShape[CrawlerStateHRow] = crawlerId :: taskId :: fbAccount :: fbPassword :: state :: timeLastWorking :: language :: useCookie :: periodAddFriend :: likePages :: likePosts :: browserCookies :: browserLocalstorage :: friendSize :: verificationState :: counterError :: counterMemberFetched :: counterMemberCompleted :: counterEmailNotfoundContinuously :: counterPhoneNotfoundContinuously :: counterEmailSearchFound :: counterEmailSearchRestricted :: counterPhoneSearchFound :: counterPhoneSearchRestricted :: counterLikeCrawled :: counterAboutCrawled :: counterFriendCrawled :: HNil

    /** Database column seqno SqlType(serial), AutoInc */
    val seqno: Rep[Int] = column[Int]("seqno", O.AutoInc)
    /** Database column crawler_id SqlType(int4), PrimaryKey */
    val crawlerId: Rep[Int] = column[Int]("crawler_id", O.PrimaryKey)
    /** Database column task_id SqlType(int4) */
    val taskId: Rep[Int] = column[Int]("task_id")
    /** Database column fb_account SqlType(text) */
    val fbAccount: Rep[String] = column[String]("fb_account")
    /** Database column fb_password SqlType(text) */
    val fbPassword: Rep[String] = column[String]("fb_password")
    /** Database column state SqlType(text), Default(sleeping) */
    val state: Rep[String] = column[String]("state", O.Default("sleeping"))
    /** Database column time_last_working SqlType(timestamptz), Default(None) */
    val timeLastWorking: Rep[Option[java.time.LocalDateTime]] = column[Option[java.time.LocalDateTime]]("time_last_working", O.Default(None))
    /** Database column language SqlType(text), Default(zh_CN) */
    val language: Rep[String] = column[String]("language", O.Default("zh_CN"))
    /** Database column use_cookie SqlType(bool), Default(false) */
    val useCookie: Rep[Boolean] = column[Boolean]("use_cookie", O.Default(false))
    /** Database column period_add_friend SqlType(int4), Default(0) */
    val periodAddFriend: Rep[Int] = column[Int]("period_add_friend", O.Default(0))
    /** Database column like_pages SqlType(bool), Default(false) */
    val likePages: Rep[Boolean] = column[Boolean]("like_pages", O.Default(false))
    /** Database column like_posts SqlType(bool), Default(false) */
    val likePosts: Rep[Boolean] = column[Boolean]("like_posts", O.Default(false))
    /** Database column browser_cookies SqlType(json) */
    val browserCookies: Rep[com.fasterxml.jackson.databind.JsonNode] = column[com.fasterxml.jackson.databind.JsonNode]("browser_cookies")
    /** Database column browser_localstorage SqlType(text), Default(None) */
    val browserLocalstorage: Rep[Option[String]] = column[Option[String]]("browser_localstorage", O.Default(None))
    /** Database column friend_size SqlType(int4), Default(None) */
    val friendSize: Rep[Option[Int]] = column[Option[Int]]("friend_size", O.Default(None))
    /** Database column verification_state SqlType(text), Default(None) */
    val verificationState: Rep[Option[String]] = column[Option[String]]("verification_state", O.Default(None))
    /** Database column counter_error SqlType(int4), Default(0) */
    val counterError: Rep[Int] = column[Int]("counter_error", O.Default(0))
    /** Database column counter_member_fetched SqlType(int4), Default(0) */
    val counterMemberFetched: Rep[Int] = column[Int]("counter_member_fetched", O.Default(0))
    /** Database column counter_member_completed SqlType(int4), Default(0) */
    val counterMemberCompleted: Rep[Int] = column[Int]("counter_member_completed", O.Default(0))
    /** Database column counter_email_notfound_continuously SqlType(int4), Default(0) */
    val counterEmailNotfoundContinuously: Rep[Int] = column[Int]("counter_email_notfound_continuously", O.Default(0))
    /** Database column counter_phone_notfound_continuously SqlType(int4), Default(0) */
    val counterPhoneNotfoundContinuously: Rep[Int] = column[Int]("counter_phone_notfound_continuously", O.Default(0))
    /** Database column counter_email_search_found SqlType(int4), Default(0) */
    val counterEmailSearchFound: Rep[Int] = column[Int]("counter_email_search_found", O.Default(0))
    /** Database column counter_email_search_restricted SqlType(int4), Default(0) */
    val counterEmailSearchRestricted: Rep[Int] = column[Int]("counter_email_search_restricted", O.Default(0))
    /** Database column counter_phone_search_found SqlType(int4), Default(0) */
    val counterPhoneSearchFound: Rep[Int] = column[Int]("counter_phone_search_found", O.Default(0))
    /** Database column counter_phone_search_restricted SqlType(int4), Default(0) */
    val counterPhoneSearchRestricted: Rep[Int] = column[Int]("counter_phone_search_restricted", O.Default(0))
    /** Database column counter_like_crawled SqlType(int4), Default(0) */
    val counterLikeCrawled: Rep[Int] = column[Int]("counter_like_crawled", O.Default(0))
    /** Database column counter_about_crawled SqlType(int4), Default(0) */
    val counterAboutCrawled: Rep[Int] = column[Int]("counter_about_crawled", O.Default(0))
    /** Database column counter_friend_crawled SqlType(int4), Default(0) */
    val counterFriendCrawled: Rep[Int] = column[Int]("counter_friend_crawled", O.Default(0))
    /** Database column time_insert SqlType(timestamptz) */
    val timeInsert: Rep[java.time.LocalDateTime] = column[java.time.LocalDateTime]("time_insert")
    /** Database column time_last_update SqlType(timestamptz), Default(None) */
    val timeLastUpdate: Rep[Option[java.time.LocalDateTime]] = column[Option[java.time.LocalDateTime]]("time_last_update", O.Default(None))
  }

  /** Collection-like TableQuery object for table CrawlerState */
  lazy val CrawlerState = new TableQuery(tag ⇒ new CrawlerState(tag))

}

