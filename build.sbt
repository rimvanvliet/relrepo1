name := "CRMService"

version := "1.17.0-SNAPSHOT"

scalaVersion := "2.11.12"

lazy val scoverageSettings = Seq(
  scoverage.ScoverageKeys.coverageOutputHTML := true,
  scoverage.ScoverageKeys.coverageMinimum := 90,
  scoverage.ScoverageKeys.coverageFailOnMinimum := true
)

val akkaVersion = "2.5.7"
val scalaKafkaClientVersion = "0.11.0.1"
val akkaStreamKafkaVersion = "0.18" // Supports Kafka 0.11, Akka 2.5.7

val akkaHttpVersion = "10.0.9"
val scalaTestVersion = "3.0.0"
val logbackVersion = "1.2.3"
val swaggerAnnotationVersion = "1.3.10"
val jsr311Version = "1.1.1"
val lensesVersion = "0.6.1"
val logstashLogbackEncoderVersion = "4.6"
val logbackKafkaAppenderVersion = "0.1.0"
val scalazVersion = "7.2.3"
val catsVersion = "0.6.1"
val postgresqlJDBCDriverVersion = "9.4.1209"


val slickVersion = "3.1.1"

lazy val crmService = (project in file(".")).
  settings(
    buildInfoObject := "CRMServiceInfo",
    buildInfoKeys := Seq[BuildInfoKey](
      name,
      version,
      scalaVersion,
      "akkaVersion" -> akkaVersion,
      "akkaHttpVersion" -> akkaHttpVersion
      //       buildInfoBuildNumber,
    ),
    buildInfoOptions += BuildInfoOption.BuildTime,
    buildInfoOptions += BuildInfoOption.ToJson,
    buildInfoPackage := "uninu.status"
  )

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases",
  "spray repo" at "http://repo.spray.io",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  Resolver.bintrayRepo("cakesolutions", "maven")
)

mappings in Universal <+= (packageBin in Compile, sourceDirectory) map { (_, src) =>
  val conf = src / "main" / "resources" / "logback.xml"
  conf -> "conf/logback.xml"
}
bashScriptExtraDefines += """addJava "-Dlogback.configurationFile=${app_home}/../conf/logback.xml""""
bashScriptExtraDefines += """addDebugger 5555"""

libraryDependencies ++= {
  Seq(
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
    "com.typesafe.akka" %% "akka-persistence-cassandra" % "0.54",
    "com.typesafe.akka" %% "akka-stream-kafka" % akkaStreamKafkaVersion,

    "org.apache.kafka" %% "kafka" % scalaKafkaClientVersion,

    "ch.qos.logback" % "logback-classic" % logbackVersion,
    "net.virtual-void" %% "json-lenses" % lensesVersion,
    "com.wordnik" % "swagger-annotations" % swaggerAnnotationVersion,
    "javax.ws.rs" % "jsr311-api" % jsr311Version,
    "org.scalaz" %% "scalaz-core" % scalazVersion,
    "net.logstash.logback" % "logstash-logback-encoder" % logstashLogbackEncoderVersion,
    "com.github.danielwegener" % "logback-kafka-appender" % logbackKafkaAppenderVersion,
    "org.typelevel" %% "cats" % catsVersion,
    "org.scala-lang.modules" %% "scala-async" % "0.9.6",
    "org.apache.httpcomponents" % "httpclient" % "4.5",
    // Slick libs
    "org.postgresql" % "postgresql" % postgresqlJDBCDriverVersion,
    "com.typesafe.slick" %% "slick" % slickVersion,
    "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
    "com.typesafe.slick" %% "slick-codegen" % slickVersion,

    "com.pauldijou" %% "jwt-core" % "0.4.1",
    "com.nimbusds" % "nimbus-jose-jwt" % "4.21",
    "org.bouncycastle" % "bcprov-jdk15on" % "1.51",

    // ---- These are included as dependencies of some libraries only used for testing, we include them here to keep test and production versions the same ----
    "org.scala-lang.modules" %% "scala-xml" % "1.0.5",
    "org.scala-lang" % "scala-reflect" % "2.11.8",
    "org.slf4j" % "log4j-over-slf4j" % "1.7.12",

    // ----                                               ----

    "org.scalatest" % "scalatest_2.11" % scalaTestVersion % "test",
    "net.cakesolutions" %% "scala-kafka-client-testkit" % scalaKafkaClientVersion % "test",
    "com.github.dnvriend" %% "akka-persistence-inmemory" % "1.3.4" % "test",
    "com.h2database" % "h2" % "1.4.192" % "test"

  )
}

// Fix for Classpath contains multiple SLF4J bindings
libraryDependencies ~= { _.map(_.exclude("org.slf4j", "slf4j-log4j12")) }
//libraryDependencies ~= { _.map(_.exclude("log4j", "log4j")) }



// sbt swagger settings //

import com.hootsuite.sbt.swagger

com.hootsuite.sbt.swagger.Sbt.swaggerSettings
swagger.Sbt.apiVersion := version.value
swagger.Sbt.basePath := "http://localhost"
swagger.Sbt.apiPath := "/"
swagger.Sbt.packages := swagger.Sbt.All
//swagger.Sbt.packages := swagger.Sbt.WhitelistPrefixes(Seq("nl.uninu"))
swagger.Sbt.excludePropertyClassPrefixes := Set("play.api.libs.json")

// Protobuf //

import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

PB.protobufSettings

// Sonar //
sonarProperties ++= Map(
  "sonar.host.url" -> "http://172.17.0.1:9000",
  "sonar.projectVersion" -> version.value,
  "sonar.projectName" -> "CRMService",
  "sonar.sources" -> "src/main/scala",
  "sonar.tests" -> "src/test/scala",
  "sonar.scoverage.reportPath" -> "target/scala-2.11/scoverage-report/scoverage.xml",
  "sonar.scala.scapegoat.reportPath" -> "target/scala-2.11/scapegoat-report/scapegoat.xml"
)

sonarRunnerOptions := Seq("-e", "-X")

// Scalariform //
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys

import scalariform.formatter.preferences._

SbtScalariform.scalariformSettings

// Scapegoat //
scapegoatVersion := "1.1.0"

// Docker settings //

// The defaults are mostly what we are looking for //
// See http://www.scala-sbt.org/sbt-native-packager/formats/docker.html#build //
dockerBaseImage := "anapsix/alpine-java"
dockerExposedPorts := Seq(8083)
dockerRepository := Option("triventouninuregieplatform")
dockerUpdateLatest := true


// SBT Buildinfo //

scalacOptions += "-deprecation"
scalacOptions += "-feature"


//Slick stuff 'sbt gen-tables' genereert een Tables class, verwijder hieruit de niet gebruikte tabellen
// http://arnaudt.github.io/2015/03/31/slick-codegen.html
//commando om tables class te genereren:  sbt clean gen-tables compile
slick <<= slickCodeGenTask

lazy val slick = TaskKey[Seq[File]]("gen-tables")
lazy val slickCodeGenTask = (sourceManaged, dependencyClasspath in Compile, runner in Compile, streams) map { (dir, cp, r, s) =>
  val outputDir = (dir / "main/slick").getPath
  val username = "crmservice"
  val password = "crm_service"

  val url = "jdbc:postgresql://boot2docker:5432/uninu"
  val jdbcDriver = "org.postgresql.Driver"
  val slickProfile = "slick.driver.PostgresDriver"
  val pkg = "nl.uninu.db"
  toError(r.run("slick.codegen.SourceCodeGenerator", cp.files, Array(slickProfile, jdbcDriver, url, outputDir, pkg, username, password), s.log))
  val fname = outputDir + "/Tables.scala"
  Seq(file(fname))
}

concurrentRestrictions in Global += Tags.limit(Tags.Test, 1)

enablePlugins(SonarRunnerPlugin, SbtNativePackager, JavaAppPackaging, DockerPlugin, BuildInfoPlugin)

Revolver.settings


lazy val scalariformSettings = Seq(ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignParameters, true)
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 90)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(RewriteArrowSymbols, false)
) ++ SbtScalariform.scalariformSettingsWithIt
