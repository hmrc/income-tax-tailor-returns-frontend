import play.core.PlayVersion.current
import sbt.*

object AppDependencies {

  private val bootstrapVersion = "8.6.0"
  private val hmrcMongoVersion = "2.0.0"
  private val hmrcPlayFrontend = "10.1.0"

  private val jacksonAndPlayExclusions: Seq[InclusionRule] = Seq(
    ExclusionRule(organization = "com.fasterxml.jackson.core"),
    ExclusionRule(organization = "com.fasterxml.jackson.datatype"),
    ExclusionRule(organization = "com.fasterxml.jackson.module"),
    ExclusionRule(organization = "com.fasterxml.jackson.core:jackson-annotations"),
    ExclusionRule(organization = "com.typesafe.play")
  )

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"                   %% "play-frontend-hmrc-play-30"             % hmrcPlayFrontend,
    "uk.gov.hmrc"                   %% "play-conditional-form-mapping-play-30"  % "2.0.0",
    "uk.gov.hmrc"                   %% "bootstrap-frontend-play-30"             % bootstrapVersion,
    "uk.gov.hmrc.mongo"             %% "hmrc-mongo-play-30"                     % hmrcMongoVersion,
    "uk.gov.hmrc"                   %% "tax-year"                               % "4.0.0",
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"                   % "2.17.0",
    "com.beachape"                  %% "enumeratum"                             % "1.7.3",
    "com.beachape"                  %% "enumeratum-play-json"                   % "1.7.3" excludeAll (jacksonAndPlayExclusions *)
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "org.scalatestplus"       %% "scalacheck-1-15"         % "3.2.11.0",
    "org.mockito"             %% "mockito-scala"           % "1.17.31",
    "org.scalacheck"          %% "scalacheck"              % "1.18.0",
    "org.jsoup"               %  "jsoup"                   % "1.17.2",
    "org.playframework"       %% "play-test"               % current
  ).map(_ % "test")

  def apply(): Seq[ModuleID] = compile ++ test
}
