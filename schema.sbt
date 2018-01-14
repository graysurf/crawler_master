import com.eitc.onlinecrm.{Dependencies, Settings}

lazy val `schema-gen` = (project in file("schema-gen"))
  .settings(Settings.default: _*)
  //.settings(Settings.plugin: _*)
  .settings(
    name := "schema-gen",
    libraryDependencies ++= Dependencies.SchemaGenProject
  )