import sbt.Def
import sbt.Keys.version
import NativePackagerHelper._
import com.typesafe.sbt.SbtNativePackager.packageArchetype

enablePlugins(JavaAppPackaging, JDKPackagerPlugin)

val settings = Seq(
  name := "EncryTelegramClient",
  version := "0.0.1",
  organization := "org.encryfoundation",
  scalaVersion := "2.12.8",
  maintainer := "info@encry.ru",
  mainClass in Compile := Some("org.encryfoundation.tg.RunApp"),
  discoveredMainClasses in Compile := Seq()
)

val monocleVersion = "2.0.0"

fork in run := true

unmanagedResourceDirectories in Compile ++= Seq(
  //baseDirectory.value / "lib",
  baseDirectory.value / "tdBin",
  baseDirectory.value / "tdlib",
)

resolvers ++= Seq(
  "Sonatype Public" at "https://oss.sonatype.org/content/groups/public/",
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Typesafe maven releases" at "https://repo.typesafe.com/typesafe/maven-releases/"
)

test in assembly := {}

libraryDependencies ++= Seq(
  "co.fs2" %% "fs2-io" % "2.1.0",
  "eu.timepit" %% "refined"  % "0.9.14",
  "com.github.pureconfig" %% "pureconfig" % "0.12.3",
  "com.github.pureconfig" %% "pureconfig-cats-effect" % "0.12.2",
  "org.typelevel" %% "simulacrum" % "1.0.0",
  "org.encry" %% "cryptoaccumulator" % "0.0.6",
  "org.iq80.leveldb" % "leveldb" % "0.9",
  "io.chrisdavenport" %% "log4cats-slf4j" % "1.0.1",
  "org.slf4j" % "slf4j-simple" % "1.7.26",
  "ru.tinkoff" %% "tofu" % "0.7.7",
  "org.apache.commons" % "commons-lang3" % "3.10",
  "org.apache.commons" % "commons-text" % "1.8",
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
  "com.github.julien-truffaut" %%  "monocle-core"  % monocleVersion,
  "com.github.julien-truffaut" %%  "monocle-macro" % monocleVersion,
  "com.github.julien-truffaut" %%  "monocle-law"   % monocleVersion % "test"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
addCompilerPlugin("org.typelevel"  % "kind-projector" % "0.11.0" cross CrossVersion.full)

val tg = (project in file(".")).settings(settings: _*)

assemblyMergeStrategy in assembly := {
  case "logback.xml" => MergeStrategy.first
  case "module-info.class" => MergeStrategy.discard
  case "META-INF/MANIFEST.MF" => MergeStrategy.discard
  case "META-INF/BC1024KE.SF" => MergeStrategy.discard
  case "META-INF/BC2048KE.SF" => MergeStrategy.discard
  case PathList("reference.conf") => MergeStrategy.concat
  case _ => MergeStrategy.first
}

bashScriptExtraDefines += "umask 077"
bashScriptExtraDefines += """addJava "-Djava.library.path=tdBin""""

mappings in Universal ++= directory("tdlib")
mappings in Universal ++= directory("tdBin")
mappings in Universal ++= directory("src/main/resources/")

assemblyJarName in assembly := "encryTg.jar"

jdkPackagerType := "installer"

jdkPackagerAppArgs := Seq("-Djava.library.path=/Applications/telegramDragon.app/Contents/Java/tdBin")

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value / "src/protobuf"
)

(antPackagerTasks in JDKPackager) := (antPackagerTasks in JDKPackager).value orElse {
  for {
    f <- Some(file("/Library/Java/JavaVirtualMachines/jdk1.8.0_251.jdk/Contents/Home/lib/ant-javafx.jar")) if f.exists()
  } yield f
}
