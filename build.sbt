name := "team-poker"

version := "0.1"

scalaVersion := "2.12.6"

libraryDependencies ++= {

  Seq(
    "org.scalatest" %% "scalatest" % "3.0.5" % "test",
    "org.scalamock" %% "scalamock" % "4.1.0" % Test
  )
}