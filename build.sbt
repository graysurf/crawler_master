import com.eitc.onlinecrm.Dependencies._
import com.eitc.onlinecrm.Settings

lazy val root = (project in file("."))
  .settings(Settings.default: _*)
  .settings(Settings.plugin: _*)
  .settings(Settings.scalariformPlugin: _*)
  .settings(
    name := "Crawler_Master",
    version := "root",
    fork in run := true,
    libraryDependencies ++= AkkaProject ++ DbProject ++ Gcp ++ Scopt ++ TestLib ++ ScalaUtil.value
  )


lazy val master = (project in file("master"))
  .settings(Settings.default: _*)
  .settings(Settings.plugin: _*)
  .settings(Settings.scalariformPlugin: _*)
  .settings(
    name := "Crawler_Master",
    version := "0426",
    fork in run := true,
    libraryDependencies ++= AkkaProject ++ DbProject ++ Gcp ++ Scopt ++ TestLib ++ ScalaUtil.value
  ).dependsOn(util)


lazy val util = (project in file("util"))
  .settings(Settings.default: _*)
  .settings(Settings.plugin: _*)
  .settings(Settings.scalariformPlugin: _*)
  .settings(
    name := "scala_util",
    fork in run := true,
    version := "1.0",
    libraryDependencies ++= AkkaProject ++ DbProject ++ Gcp ++ Scopt ++ TestLib ++ ScalaUtil.value
  )




