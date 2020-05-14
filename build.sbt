import sbt.Def
import sbt.Keys.version

val settings: Seq[Def.Setting[String]] = Seq(
  name := "telegramDragon",
  version := "0.0.1",
  organization := "org.encryfoundation",
  scalaVersion := "2.12.6",
)

assemblyJarName in assembly := "telegramDragon.jar"

mainClass in assembly := Some("org.encryfoundation.tg.RunApp")

test in assembly := {}

unmanagedResourceDirectories in Compile ++= Seq(
  baseDirectory.value / "lib",
  baseDirectory.value / "bin",
  baseDirectory.value / "tdlib"
)

assemblyMergeStrategy in assembly := {
  case "logback.xml" => MergeStrategy.first
  case "module-info.class" => MergeStrategy.discard
  case "META-INF/MANIFEST.MF" => MergeStrategy.discard
  case "META-INF/BC1024KE.SF" => MergeStrategy.discard
  case "META-INF/BC2048KE.SF" => MergeStrategy.discard
  case PathList("reference.conf") => MergeStrategy.concat
  case _ => MergeStrategy.first
}

resolvers ++= Seq(
  "Sonatype Public" at "https://oss.sonatype.org/content/groups/public/",
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Typesafe maven releases" at "https://repo.typesafe.com/typesafe/maven-releases/"
)

val opts = Seq(
  "Djava.library.path=/Users/aleksandr/IdeaProjects/tdLib/td/example/java/bin"
)

//javaOptions in run ++= opts
//javaOptions in compile ++= opts

libraryDependencies ++= Seq(
  "co.fs2" %% "fs2-core" % "2.1.0",
  "co.fs2" %% "fs2-io" % "2.1.0",
  "eu.timepit" %% "refined"  % "0.9.14",
  "org.typelevel" %% "simulacrum" % "1.0.0",
  "org.scalafx" %% "scalafx" % "12.0.2-R18",
  "org.encry" %% "encry-common" % "0.9.3",
  "org.encry" %% "cryptoAccumulator" % "0.0.4",
  "org.iq80.leveldb" % "leveldb" % "0.9",
  "io.chrisdavenport" %% "log4cats-slf4j" % "1.0.1",
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

val tg = (project in file(".")).settings(settings: _*)