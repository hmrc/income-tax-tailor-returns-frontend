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

package base

import config.FrontendAppConfig
import controllers.actions._
import models.UserAnswers
import models.aboutyou.CharitableDonations.{DonationsUsingGiftAid, GiftsOfLandOrProperty, GiftsOfSharesOrSecurities, GiftsToOverseasCharities}
import models.aboutyou.{CharitableDonations, UkResidenceStatus}
import models.pensions.PaymentsIntoPensions
import models.pensions.PaymentsIntoPensions.{AnnualAllowances, Overseas, UkPensions}
import models.propertypensionsinvestments.Pensions.{OtherUkPensions, ShortServiceRefunds, StatePension, UnauthorisedPayments}
import models.propertypensionsinvestments.{Pensions, RentalIncome, UkDividendsSharesLoans, UkInsuranceGains, UkInterest}
import models.propertypensionsinvestments.UkDividendsSharesLoans._
import models.propertypensionsinvestments.UkInsuranceGains.{CapitalRedemption, LifeAnnuity, LifeInsurance, VoidedISA}
import models.propertypensionsinvestments.UkInterest.{FromGiltEdged, FromUkBanks, FromUkTrustFunds}
import models.requests.IdentifierRequest
import models.session.SessionData
import models.workandbenefits.AboutYourWork.{Employed, SelfEmployed}
import models.workandbenefits.{AboutYourWork, JobseekersAllowance}
import models.workandbenefits.JobseekersAllowance.{Esa, Jsa}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.aboutyou.{CharitableDonationsPage, FosterCarerPage, UkResidenceStatusPage}
import pages.pensions.PaymentsIntoPensionsPage
import pages.propertypensionsinvestments.{PensionsPage, RentalIncomePage, UkDividendsSharesLoansPage, UkInsuranceGainsPage, UkInterestPage}
import pages.workandbenefits.{AboutYourWorkPage, AboutYourWorkRadioPage, ConstructionIndustrySchemePage, JobseekersAllowancePage}
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsEmpty, PlayBodyParsers, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers.stubControllerComponents
import uk.gov.hmrc.time.TaxYear

import scala.concurrent.ExecutionContext

trait SpecBase extends AnyFreeSpec
  with Matchers
  with TryValues
  with OptionValues
  with ScalaFutures
  with IntegrationPatience {

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  val endOfTaxYearRange: Int = TaxYear.current.finishYear
  val startOfTaxYearRange: Int = endOfTaxYearRange - 5
  val taxYear: Int = TaxYear.current.currentYear
  val taxYears: Seq[Int] = (startOfTaxYearRange to endOfTaxYearRange).toList
  val validTaxYears: (String, String) = "validTaxYears" -> taxYears.mkString(",")

  val mtdItId: String = "anMtdItId"
  val nino: String = "AA111111A"
  val anAgent: Boolean = true
  val notAnAgent: Boolean = false
  val parsers: PlayBodyParsers = stubControllerComponents().parsers

  val dummySessionData: SessionData = SessionData(
    mtditid = mtdItId,
    nino = nino,
    utr = "",
    sessionId = ""
  )

  def testIdentifierRequest(request: Request[AnyContentAsEmpty.type] = FakeRequest(),
                            isAgent: Boolean): IdentifierRequest[AnyContentAsEmpty.type] =
    IdentifierRequest(
      request = request,
      nino = nino,
      mtditid = mtdItId,
      sessionId = "aSessionId",
      utr = "aUtr",
      isAgent = isAgent
    )

  val submissionFrontendBaseUrl: String = "http://localhost:9302/update-and-submit-income-tax-return"

  def emptyUserAnswers: UserAnswers = UserAnswers(mtdItId, taxYear)

  val charitableAnswersSet: Set[CharitableDonations] = Set[CharitableDonations](
    DonationsUsingGiftAid,
    GiftsOfSharesOrSecurities,
    GiftsOfLandOrProperty,
    GiftsToOverseasCharities
  )

  val pensionsAnswersSet: Set[Pensions] = Set[Pensions](
    StatePension,
    OtherUkPensions,
    UnauthorisedPayments,
    ShortServiceRefunds,
    Pensions.NonUkPensions
  )

  val ukInsuranceGainsAnswersSet: Set[UkInsuranceGains] = Set[UkInsuranceGains](
    LifeInsurance,
    LifeAnnuity,
    CapitalRedemption,
    VoidedISA
  )

  val ukDividendsAnswersSet: Set[UkDividendsSharesLoans] = Set[UkDividendsSharesLoans](
    CashDividendsFromUkStocksAndShares,
    StockDividendsFromUkCompanies,
    DividendsUnitTrustsInvestmentCompanies,
    FreeOrRedeemableShares,
    CloseCompanyLoansWrittenOffReleased
  )

  val paymentIntoPensionsAnswersSet: Set[PaymentsIntoPensions] = Set[PaymentsIntoPensions](
    UkPensions,
    AnnualAllowances,
    PaymentsIntoPensions.NonUkPensions,
    Overseas
  )

  val fullUserAnswers: UserAnswers = emptyUserAnswers.copy()
    .set(UkResidenceStatusPage, UkResidenceStatus.Uk)
    .flatMap(_.set(CharitableDonationsPage, charitableAnswersSet))
    .flatMap(_.set(FosterCarerPage, true))
    .flatMap(_.set(AboutYourWorkRadioPage, true))
    .flatMap(_.set(AboutYourWorkPage, Set[AboutYourWork](Employed, SelfEmployed)))
    .flatMap(_.set(ConstructionIndustrySchemePage, true))
    .flatMap(_.set(JobseekersAllowancePage, Set[JobseekersAllowance](Jsa, Esa)))
    .flatMap(_.set(RentalIncomePage, Set[RentalIncome](RentalIncome.Uk, RentalIncome.NonUk)))
    .flatMap(_.set(PensionsPage, pensionsAnswersSet))
    .flatMap(_.set(UkInsuranceGainsPage, ukInsuranceGainsAnswersSet))
    .flatMap(_.set(UkInterestPage, Set[UkInterest](FromUkBanks, FromUkTrustFunds, FromGiltEdged)))
    .flatMap(_.set(UkDividendsSharesLoansPage, ukDividendsAnswersSet))
    .flatMap(_.set(PaymentsIntoPensionsPage, paymentIntoPensionsAnswersSet))
    .success.value

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  def config(app: Application): FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  protected def applicationBuilder(userAnswers: Option[UserAnswers] = None, isAgent: Boolean = false): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[IdentifierActionProvider].to(new FakeIdentifierAction(isAgent)(parsers)),
        bind[DataRetrievalActionProvider].toInstance(new FakeDataRetrievalActionProvider(userAnswers, isAgent))
      )
}
