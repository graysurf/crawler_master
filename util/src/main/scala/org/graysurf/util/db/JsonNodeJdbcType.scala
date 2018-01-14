package org.graysurf.util.db

import java.sql.{ PreparedStatement, ResultSet, Types }

import com.fasterxml.jackson.databind.{ JsonNode, ObjectMapper }
import org.postgresql.util.PGobject
import slick.ast.FieldSymbol
import slick.jdbc.PostgresProfile.DriverJdbcType

class JsonNodeJdbcType extends DriverJdbcType[JsonNode] {

  import JsonNodeJdbcType.mapper

  override def sqlTypeName(sym: Option[FieldSymbol]) = "JSONB"

  override def sqlType: Int = Types.OTHER

  override def getValue(r: ResultSet, idx: Int): JsonNode = {
    fromPgObject(r.getObject(idx).asInstanceOf[PGobject])
  }

  override def setValue(v: JsonNode, p: PreparedStatement, idx: Int): Unit = {
    p.setObject(idx, toPgObject(v), sqlType)
  }

  override def updateValue(v: JsonNode, r: ResultSet, idx: Int): Unit = {
    r.updateObject(idx, toPgObject(v))
  }

  override def valueToSQLLiteral(value: JsonNode): String = s"'$value'"

  override def hasLiteralForm = true

  private[this] def toPgObject(jsonNode: JsonNode): PGobject = {
    val json = new PGobject
    json.setType("jsonb")
    json.setValue(jsonNode.toString)
    json
  }

  private[this] def fromPgObject(pgObject: PGobject): JsonNode = {
    assert(pgObject.getType == "jsonb")
    mapper.readTree(pgObject.getValue)
  }
}

object JsonNodeJdbcType {
  val mapper = new ObjectMapper
}