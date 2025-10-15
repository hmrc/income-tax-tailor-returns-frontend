import play.core.PlayVersion.current
import sbt.*

object AppDependencies {

  private val bootstrapVersion = "10.2.0"
  private val hmrcMongoVersion = "2.10.0"
  private val hmrcPlayFrontend = "12.17.0"

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
    "uk.gov.hmrc"                   %% "play-conditional-form-mapping-play-30"  % "3.3.0",
    "uk.gov.hmrc"                   %% "bootstrap-frontend-play-30"             % bootstrapVersion,
    "uk.gov.hmrc.mongo"             %% "hmrc-mongo-play-30"                     % hmrcMongoVersion,
    "uk.gov.hmrc"                   %% "tax-year"                               % "6.0.0",
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"                   % "2.20.0",
    "org.typelevel"                 %% "cats-core"                              % "2.13.0",
    "com.beachape"                  %% "enumeratum"                             % "1.9.0",
    "com.beachape"                  %% "enumeratum-play-json"                   % "1.9.0" excludeAll (jacksonAndPlayExclusions *)
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "org.scalatestplus"       %% "scalacheck-1-15"         % "3.2.11.0",
    "org.mockito"             %% "mockito-scala"           % "2.0.0",
    "org.scalacheck"          %% "scalacheck"              % "1.19.0",
    "org.jsoup"               %  "jsoup"                   % "1.21.2",
    "org.wiremock"            %  "wiremock"                % "3.13.1",
    "org.scalamock"           %% "scalamock"               % "7.5.0",
    "org.playframework"       %% "play-test"               % current
  ).map(_ % "test")

  def apply(): Seq[ModuleID] = compile ++ test
}
