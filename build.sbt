import play.sbt.routes.RoutesKeys
import sbt.Def
import scoverage.ScoverageKeys
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion
import uk.gov.hmrc.DefaultBuildSettings

lazy val appName: String = "income-tax-tailor-returns-frontend"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(inConfig(Test)(testSettings): _*)
  .settings(majorVersion := 0)
  .settings(ThisBuild / useSuperShell := false)
  .settings(
    scalaVersion := "2.13.12",
    name := appName,
    RoutesKeys.routesImport ++= Seq(
      "models._",
      "uk.gov.hmrc.play.bootstrap.binders.RedirectUrl"
    ),
    TwirlKeys.templateImports ++= Seq(
      "config.FrontendAppConfig",
      "play.twirl.api.HtmlFormat",
      "play.twirl.api.HtmlFormat._",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers._",
      "uk.gov.hmrc.hmrcfrontend.views.config._",
      "views.ViewUtils._",
      "models.Mode",
      "controllers.routes._",
      "viewmodels.govuk.all._"
    ),
    PlayKeys.playDefaultPort := 10007,
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*handlers.*;.*components.*;.*controllers.testonly.*" +
      ".*Routes.*;.*viewmodels.govuk.*;",
    ScoverageKeys.coverageMinimumStmtTotal := 78,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    scalacOptions ++= Seq(
      "-feature",
      "-rootdir",
      baseDirectory.value.getCanonicalPath,
      "-Wconf:cat=deprecation:ws,cat=feature:ws,cat=optimizer:ws,src=target/.*:s"
    ),
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    retrieveManaged := true,
    resolvers ++= Seq(Resolver.jcenterRepo),
    // concatenate js
    Concat.groups := Seq(
      "javascripts/application.js" ->
        group(Seq(
          "javascripts/app.js"
        ))
    ),
    pipelineStages := Seq(digest),
    Assets / pipelineStages := Seq(concat)
  )

lazy val testSettings: Seq[Def.Setting[_]] = Seq(
  fork := true,
  javaOptions ++= Seq("-Dconfig.resource=test.application.conf", "-Dapplication.router=testOnlyDoNotUseInAppConf.Routes"),
  unmanagedSourceDirectories.withRank(KeyRanks.Invisible) += baseDirectory.value / "test-utils"
)

lazy val itSettings = DefaultBuildSettings.itSettings() ++ Seq(
  unmanagedSourceDirectories.withRank(KeyRanks.Invisible) := Seq(
    baseDirectory.value / "it"
  ),
  unmanagedResourceDirectories.withRank(KeyRanks.Invisible) := Seq(
    baseDirectory.value / "it" / "resources"
  )
)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(root % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(testSettings ++ itSettings)
  .settings(scalaVersion := "2.13.12")
  .settings(majorVersion := 0)
  .settings(libraryDependencies ++= AppDependencies.test)