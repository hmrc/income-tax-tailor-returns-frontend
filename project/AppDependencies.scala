import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  private val bootstrapVersion = "8.2.0"
  private val hmrcMongoVersion = "1.6.0"
  private val hmrcPlayFrontend = "8.2.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"                   %% "play-frontend-hmrc-play-30"     % hmrcPlayFrontend,
    "uk.gov.hmrc"                   %% "play-conditional-form-mapping-play-30"  % "2.0.0",
    "uk.gov.hmrc"                   %% "bootstrap-frontend-play-30"     % bootstrapVersion,
    "uk.gov.hmrc.mongo"             %% "hmrc-mongo-play-30"             % hmrcMongoVersion,
    "uk.gov.hmrc"                   %% "tax-year"                       % "3.3.0",
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"           % "2.14.2"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "org.scalatestplus"       %% "scalacheck-1-15"         % "3.2.11.0",
    "org.mockito"             %% "mockito-scala"           % "1.17.30",
    "org.scalacheck"          %% "scalacheck"              % "1.17.0",
    "org.jsoup"               %  "jsoup"                   % "1.16.2",
    "org.playframework"       %% "play-test"               % current
  ).map(_ % "test")

  def apply(): Seq[ModuleID] = compile ++ test
}