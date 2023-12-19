import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  private val bootstrapVersion = "8.2.0"
  private val hmrcMongoVersion = "1.6.0"
  private val hmrcPlayFrontend = "7.29.0-play-28"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"                   %% "play-frontend-hmrc"             % hmrcPlayFrontend,
    "uk.gov.hmrc"                   %% "play-conditional-form-mapping"  % "1.13.0-play-28",
    "uk.gov.hmrc"                   %% "bootstrap-frontend-play-28"     % bootstrapVersion,
    "uk.gov.hmrc.mongo"             %% "hmrc-mongo-play-28"             % hmrcMongoVersion,
    "uk.gov.hmrc"                   %% "tax-year"                       % "3.3.0",
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"           % "2.14.2"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28" % hmrcMongoVersion,
    "org.scalatest"           %% "scalatest"               % "3.2.17",
    "org.scalatestplus"       %% "scalacheck-1-15"         % "3.2.11.0",
    "org.scalatestplus"       %% "mockito-3-4"             % "3.2.10.0",
    "org.mockito"             %% "mockito-scala"           % "1.17.30",
    "org.scalacheck"          %% "scalacheck"              % "1.17.0",
    "org.pegdown"             %  "pegdown"                 % "1.6.0",
    "org.jsoup"               %  "jsoup"                   % "1.16.2",
    "com.typesafe.play"       %% "play-test"               % current,
    "org.scalatestplus.play"  %% "scalatestplus-play"      % "5.1.0",
    "com.github.tomakehurst"  %  "wiremock-jre8"           % "2.35.1",
    "com.vladsch.flexmark"     %  "flexmark-all"             % "0.64.8"
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}