import sbt.Def
import sbt.Keys.version

val settings: Seq[Def.Setting[String]] = Seq(
  name := "telegramDragon",
  version := "0.0.1",
  organization := "org.encryfoundation",
  scalaVersion := "2.12.8"
)

unmanagedResourceDirectories in Compile ++= Seq(
  //baseDirectory.value / "lib",
  baseDirectory.value / "bin",
  baseDirectory.value / "tdlib"
)

resolvers ++= Seq(
  "Sonatype Public" at "https://oss.sonatype.org/content/groups/public/",
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Typesafe maven releases" at "https://repo.typesafe.com/typesafe/maven-releases/"
)

//javaOptions in run ++= opts
//javaOptions in compile ++= opts

libraryDependencies ++= Seq(
//  "org.typelevel" %% "cats-core" % "2.0.0",
//  "org.typelevel" %% "cats-effect" % "2.1.3",
//  "co.fs2" %% "fs2-core" % "2.1.0",
  "co.fs2" %% "fs2-io" % "2.1.0",
  "eu.timepit" %% "refined"  % "0.9.14",
  "org.typelevel" %% "simulacrum" % "1.0.0",
  "org.encry" %% "cryptoaccumulator" % "0.0.6",
  "org.iq80.leveldb" % "leveldb" % "0.9",
  //"io.chrisdavenport" %% "log4cats-slf4j" % "1.0.1",
  //"org.slf4j" % "slf4j-simple" % "1.7.26",
)

fork in run := true

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

val tg = (project in file(".")).settings(settings: _*)