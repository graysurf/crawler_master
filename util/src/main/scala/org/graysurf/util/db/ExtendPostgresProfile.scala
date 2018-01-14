package org.graysurf.util.db

import com.github.tminglei.slickpg.{ PgArraySupport, _ }
import slick.basic.Capability
import slick.jdbc._

trait ExtendPostgresProfile extends ExPostgresProfile
    with PgArraySupport
    with PgDate2Support
    with PgJsonSupport {
  def pgjson = "jsonb" // jsonb support is in postgres 9.4.0 onward; for 9.3.x use "json"

  // Add back `capabilities.insertOrUpdate` to enable native `upsert` support; for postgres 9.5+
  override protected def computeCapabilities: Set[Capability] =
    super.computeCapabilities + JdbcCapabilities.insertOrUpdate

  override val api = ExtendAPI
  object ExtendAPI extends API with JsonImplicits with ArrayImplicits with DateTimeImplicits {
    implicit val jsonNodeJdbcType = new JsonNodeJdbcType
  }

  val plainApi = ExtendPlainAPI
  object ExtendPlainAPI extends API with SimpleArrayPlainImplicits with Date2DateTimePlainImplicits

}

object ExtendPostgresProfile extends ExtendPostgresProfile