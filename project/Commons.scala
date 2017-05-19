import sbt.Keys._
import sbt._

object Commons {

  val settings: Seq[Def.Setting[_]] = Seq(
    organization := "io.radicalbit",
    scalaVersion in ThisBuild := "2.11.8"
  )
}
