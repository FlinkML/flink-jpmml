import sbt.Keys._
import sbt._

object PublishSettings {

  lazy val settings: Seq[Def.Setting[_]] = Seq(
    publishArtifact := true,
    publishArtifact in (Compile, packageDoc) := false,
    publishMavenStyle := true,
    publishTo := version { (v: String) =>
      if (v.trim.endsWith("SNAPSHOT"))
        Some("Radicalbit Snapshots" at "https://tools.radicalbit.io/maven/repository/snapshots/")
      else
        Some("Radicalbit Releases" at "https://tools.radicalbit.io/maven/repository/internal/")
    }.value,
    credentials += Credentials(Path.userHome / ".artifactory" / ".archiva-snapshots"),
    credentials += Credentials(Path.userHome / ".artifactory" / ".archiva-releases")
  )
}
