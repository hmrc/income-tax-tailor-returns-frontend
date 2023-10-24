/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.binders.{AbsoluteWithHostnameFromAllowlist, OnlyRelative, RedirectUrl}
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl.idFunctor
@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration) {

  val host: String    = configuration.get[String]("host")
  val appName: String = configuration.get[String]("appName")

  val allowedRedirectUrls: Seq[String] = configuration.get[Seq[String]]("urls.allowedRedirects")

  private val contactHost = configuration.get[String]("contact-frontend.host")
  private val contactFormServiceIdentifier = configuration.get[String]("contact-frontend.serviceId")
  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${host + request.uri}"

  private val loginUrl: String = RedirectUrl(configuration.get[String]("urls.login"))
    .get(OnlyRelative | AbsoluteWithHostnameFromAllowlist(allowedRedirectUrls: _*))
    .url

  private val loginContinueUrl: String = RedirectUrl(configuration.get[String]("urls.loginContinue"))
    .get(OnlyRelative | AbsoluteWithHostnameFromAllowlist(allowedRedirectUrls: _*))
    .url
  def loginUrl(taxYear: Int): String = s"$loginUrl?continue=$loginContinueUrl/$taxYear/start&origin=$appName"

  val signOutUrl: String       = configuration.get[String]("urls.signOut")
  val incomeTaxSubmissionIvRedirect: String = configuration.get[String]("urls.ivUplift")

  private val exitSurveyBaseUrl: String = configuration.get[String]("feedback-frontend.host")
  val exitSurveyUrl: String             = s"$exitSurveyBaseUrl/feedback/income-tax-tailor-returns-frontend"

  val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("features.welsh-translation")

  def languageMap: Map[String, Lang] = Map(
    "en" -> Lang("en"),
    "cy" -> Lang("cy")
  )

  val timeout: Int   = configuration.get[Int]("timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  val checkResidenceStatusUrl: String = configuration.get[String]("external-urls.checkResidenceStatus")

}
