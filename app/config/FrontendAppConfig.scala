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
class FrontendAppConfig @Inject()(configuration: Configuration) {

  val host: String = configuration.get[String]("host")
  val appName: String = configuration.get[String]("appName")

  private val allowedRedirectUrls: Seq[String] = configuration.get[Seq[String]]("urls.allowedRedirects")

  private val contactHost = RedirectUrl(configuration.get[String]("contact-frontend.host"))
    .get(OnlyRelative | AbsoluteWithHostnameFromAllowlist(allowedRedirectUrls: _*))
    .url

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

  val signOutUrl: String = RedirectUrl(configuration.get[String]("urls.signOut"))
    .get(OnlyRelative | AbsoluteWithHostnameFromAllowlist(allowedRedirectUrls: _*))
    .url

  val incomeTaxSubmissionIvRedirect: String = RedirectUrl(configuration.get[String]("urls.ivUplift"))
    .get(OnlyRelative | AbsoluteWithHostnameFromAllowlist(allowedRedirectUrls: _*))
    .url

  private val exitSurveyBaseUrl: String = RedirectUrl(configuration.get[String]("feedback-frontend.host"))
    .get(OnlyRelative | AbsoluteWithHostnameFromAllowlist(allowedRedirectUrls: _*))
    .url

  //Submission Frontend
  val submissionFrontendTaskListRedirect: Int => String = taxYear => RedirectUrl(
    configuration.get[String]("microservice.services.income-tax-submission-frontend.url") + s"/update-and-submit-income-tax-return/$taxYear/tasklist")
    .get(OnlyRelative | AbsoluteWithHostnameFromAllowlist(allowedRedirectUrls: _*))
    .url

  def dividendsBaseUrl: String = configuration.get[String]("microservice.services.personal-income-tax-submission-frontend.url")

  def ukInterestGatewayUrl(taxYear: Int): String =
    configuration.get[String]("microservice.services.personal-income-tax-submission-frontend.url") +
      s"/update-and-submit-income-tax-return/personal-income/$taxYear/interest/interest-from-UK"

  def giltEdgedGatewayUrl(taxYear: Int): String =
    configuration.get[String]("microservice.services.personal-income-tax-submission-frontend.url") +
      s"/update-and-submit-income-tax-return/personal-income/$taxYear/interest/interest-from-securities"

  def personalFrontendBaseUrl: String =
    s"${configuration.get[String]("microservice.services.personal-income-tax-submission-frontend.url")}/update-and-submit-income-tax-return/personal-income"

  def cisGatewayUrl(taxYear: Int): String =
    configuration.get[String]("microservice.services.income-tax-cis-frontend.url") +
      s"/update-and-submit-income-tax-return/construction-industry-scheme-deductions/$taxYear/deductions-from-payments"

  def employmentGatewayUrl(taxYear: Int): String =
    configuration.get[String]("microservice.services.income-tax-employment-frontend.url") +
      s"/update-and-submit-income-tax-return/employment-income/$taxYear/income-from-employment"

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
      s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/gains-gateway"

  def tailoringUkResidenceUrl(taxYear: Int): String = s"$host/update-and-submit-income-tax-return/tailored-return/$taxYear/about-you/uk-residence-status"

  def tailoringFosterCarerUrl(taxYear: Int): String = s"$host/update-and-submit-income-tax-return/tailored-return/$taxYear/about-you/foster-carer "

  val exitSurveyUrl: String = s"$exitSurveyBaseUrl/feedback/$appName"

  val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("features.welsh-translation")

  val privateBeta: Boolean =
    configuration.get[Boolean]("features.privateBeta")

  def languageMap: Map[String, Lang] = Map(
    "en" -> Lang("en"),
    "cy" -> Lang("cy")
  )

  val timeout: Int = configuration.get[Int]("timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  val checkResidenceStatusUrl: String = configuration.get[String]("external-urls.checkResidenceStatus")
  val understandingTaxAvoidanceUrl: String = configuration.get[String]("external-urls.understandingTaxAvoidance")
  val understandingRemunerationUrl: String = configuration.get[String]("external-urls.understandingRemuneration")
  val unauthorisedPaymentsUrl: String = configuration.get[String]("external-urls.unauthorisedPayments")
  val overseasTransferChargeUrl: String = configuration.get[String]("external-urls.overseasTransferCharge")
  val taxOnDividendsUrl: String = configuration.get[String]("external-urls.taxOnDividends")
  val authorisedInvestmentFundsUrl: String = configuration.get[String]("external-urls.authorisedInvestmentFunds")
  val setUpAgentServicesAccountUrl: String = configuration.get[String]("external-urls.set-up-agent-services-account")
  //Subscription Service
  val signUpUrlAgent: String = configuration.get[String]("urls.signUpAgent")
  val signUpUrlIndividual: String = configuration.get[String]("urls.signUpIndividual")
}
