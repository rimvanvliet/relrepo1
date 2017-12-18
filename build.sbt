name := "CRMService"

version := "1.18.0-SNAPSHOT"

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
