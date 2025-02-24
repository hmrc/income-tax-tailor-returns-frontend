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

import java.util.UUID

@Singleton
class FrontendAppConfig @Inject()(configuration: Configuration) {

  lazy val host: String = configuration.get[String]("host")
  lazy val appName: String = configuration.get[String]("appName")

  private lazy val allowedRedirectUrls: Seq[String] = configuration.get[Seq[String]]("urls.allowedRedirects")

  private lazy val contactHost = RedirectUrl(configuration.get[String]("contact-frontend.host"))
    .get(OnlyRelative | AbsoluteWithHostnameFromAllowlist(allowedRedirectUrls: _*))
    .url

  private lazy val contactFormServiceIdentifier = configuration.get[String]("contact-frontend.serviceId")

  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${host + request.uri}"

  private lazy val loginUrl: String = RedirectUrl(configuration.get[String]("urls.login"))
    .get(OnlyRelative | AbsoluteWithHostnameFromAllowlist(allowedRedirectUrls: _*))
    .url

  private lazy val loginContinueUrl: String = RedirectUrl(configuration.get[String]("urls.loginContinue"))
    .get(OnlyRelative | AbsoluteWithHostnameFromAllowlist(allowedRedirectUrls: _*))
    .url

  def loginUrl(taxYear: Int): String = s"$loginUrl?continue=$loginContinueUrl/$taxYear/start&origin=$appName"

  lazy val signOutUrl: String = RedirectUrl(configuration.get[String]("urls.signOut"))
    .get(OnlyRelative | AbsoluteWithHostnameFromAllowlist(allowedRedirectUrls: _*))
    .url

  lazy val incomeTaxSubmissionIvRedirect: String = RedirectUrl(configuration.get[String]("urls.ivUplift"))
    .get(OnlyRelative | AbsoluteWithHostnameFromAllowlist(allowedRedirectUrls: _*))
    .url

  private lazy val exitSurveyBaseUrl: String = RedirectUrl(configuration.get[String]("feedback-frontend.host"))
    .get(OnlyRelative | AbsoluteWithHostnameFromAllowlist(allowedRedirectUrls: _*))
    .url

  //Submission Frontend
  lazy val submissionFrontendTaskListRedirect: Int => String = taxYear => RedirectUrl(
    configuration.get[String]("microservice.services.income-tax-submission-frontend.url") + s"/update-and-submit-income-tax-return/$taxYear/tasklist")
    .get(OnlyRelative | AbsoluteWithHostnameFromAllowlist(allowedRedirectUrls: _*))
    .url

  def dividendsBaseUrl: String = configuration.get[String]("microservice.services.personal-income-tax-submission-frontend.url")

  def employmentBaseUrl: String = configuration.get[String]("microservice.services.income-tax-employment-frontend.url")

  def personalFrontendBaseUrl: String =
    s"${configuration.get[String]("microservice.services.personal-income-tax-submission-frontend.url")}/update-and-submit-income-tax-return/personal-income"

  def stateBenefitsBaseUrl: String =
    configuration.get[String]("microservice.services.income-tax-state-benefits.url") + "/income-tax-state-benefits"

  def cisFrontendUrl(taxYear: Int): String =
    configuration.get[String]("microservice.services.income-tax-cis-frontend.url") +
      s"/update-and-submit-income-tax-return/construction-industry-scheme-deductions/$taxYear/contractor-details"

  def pensionsGatewayUrl(taxYear: Int): String =
    configuration.get[String]("microservice.services.income-tax-pensions-frontend.url") +
      s"/update-and-submit-income-tax-return/pensions/$taxYear/pensions-summary"

  def paymentsIntoPensionsGatewayUrl(taxYear: Int): String =
    configuration.get[String]("microservice.services.income-tax-pensions-frontend.url") +
      s"/update-and-submit-income-tax-return/pensions/$taxYear/payments-into-pensions/relief-at-source"

  def incomeFromOverseasGatewayUrl(taxYear: Int): String =
    configuration.get[String]("microservice.services.income-tax-pensions-frontend.url") +
      s"/update-and-submit-income-tax-return/pensions/$taxYear/overseas-pensions/income-from-overseas-pensions/pension-overseas-income-status"

  def annualAllowancesUrl(taxYear: Int): String =
    configuration.get[String]("microservice.services.income-tax-pensions-frontend.url") +
      s"/update-and-submit-income-tax-return/pensions/$taxYear/annual-allowance/reduced-annual-allowance"

  def overseasTransferChargesGatewayUrl(taxYear: Int): String =
    configuration.get[String]("microservice.services.income-tax-pensions-frontend.url") +
      s"/update-and-submit-income-tax-return/pensions/$taxYear/overseas-pensions/overseas-transfer-charges/transfer-pension-savings"

  def stateBenefitsEsaJourneyGatewayUrl(taxYear: Int): String =
    configuration.get[String]("microservice.services.income-tax-state-benefits-frontend.url") +
      s"/update-and-submit-income-tax-return/state-benefits/$taxYear/employment-support-allowance/claims"

  def stateBenefitsJsaJourneyGatewayUrl(taxYear: Int): String =
    configuration.get[String]("microservice.services.income-tax-state-benefits-frontend.url") +
      s"/update-and-submit-income-tax-return/state-benefits/$taxYear/jobseekers-allowance/claims"

  def additionalInfoUrl(taxYear: Int): String =
    configuration.get[String]("microservice.services.income-tax-additional-information-frontend.url") +
      s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-name/${UUID.randomUUID().toString}"

  def tailoringUkResidenceUrl(taxYear: Int): String = s"$host/update-and-submit-income-tax-return/tailored-return/$taxYear/about-you/uk-residence-status"

  def tailoringFosterCarerUrl(taxYear: Int): String = s"$host/update-and-submit-income-tax-return/tailored-return/$taxYear/about-you/foster-carer "

  lazy val exitSurveyUrl: String = s"$exitSurveyBaseUrl/feedback/$appName"

  lazy val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("feature-switch.welsh-translation")

  lazy val sessionCookieServiceEnabled: Boolean =
    configuration.get[Boolean]("feature-switch.sessionCookieService")

  def emaSupportingAgentsEnabled: Boolean =
    configuration.get[Boolean]("feature-switch.ema-supporting-agents-enabled")

  def isPrePopEnabled: Boolean =
    configuration.get[Boolean]("feature-switch.isPrePopEnabled")

  def languageMap: Map[String, Lang] = Map(
    "en" -> Lang("en"),
    "cy" -> Lang("cy")
  )

  lazy val timeout: Int = configuration.get[Int]("timeout-dialog.timeout")
  lazy val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  lazy val checkResidenceStatusUrl: String = configuration.get[String]("external-urls.checkResidenceStatus")
  lazy val understandingTaxAvoidanceUrl: String = configuration.get[String]("external-urls.understandingTaxAvoidance")
  lazy val understandingRemunerationUrl: String = configuration.get[String]("external-urls.understandingRemuneration")
  lazy val unauthorisedPaymentsUrl: String = configuration.get[String]("external-urls.unauthorisedPayments")
  lazy val overseasTransferChargeUrl: String = configuration.get[String]("external-urls.overseasTransferCharge")
  lazy val taxOnDividendsUrl: String = configuration.get[String]("external-urls.taxOnDividends")
  lazy val authorisedInvestmentFundsUrl: String = configuration.get[String]("external-urls.authorisedInvestmentFunds")
  lazy val setUpAgentServicesAccountUrl: String = configuration.get[String]("external-urls.set-up-agent-services-account")
  //Subscription Service
  lazy val signUpUrlAgent: String = configuration.get[String]("urls.signUpAgent")
  lazy val signUpUrlIndividual: String = configuration.get[String]("urls.signUpIndividual")
  lazy val viewAndChangeEnterUtrUrl: String = configuration.get[String]("urls.viewAndChangeEnterUtrUrl")
}
