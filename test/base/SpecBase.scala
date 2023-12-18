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

import controllers.actions._
import models.UserAnswers
import models.aboutyou.CharitableDonations.{DonationsUsingGiftAid, GiftsOfLandOrProperty, GiftsOfSharesOrSecurities}
import models.aboutyou.{CharitableDonations, UkResidenceStatus}
import models.pensions.PaymentsIntoPensions
import models.pensions.PaymentsIntoPensions.{Overseas, UkPensions}
import models.propertypensionsinvestments.Pensions.{OtherUkPensions, ShortServiceRefunds, StatePension, UnauthorisedPayments}
import models.propertypensionsinvestments.{Pensions, RentalIncome, UkDividendsSharesLoans, UkInsuranceGains, UkInterest}
import models.propertypensionsinvestments.UkDividendsSharesLoans.{CashDividendsFromUkStocksAndShares, CloseCompanyLoansWrittenOffReleased, DividendsUnitTrustsInvestmentCompanies, FreeOrRedeemableShares, StockDividendsFromUkCompanies}
import models.propertypensionsinvestments.UkInsuranceGains.{CapitalRedemption, LifeAnnuity, LifeInsurance, VoidedISA}
import models.propertypensionsinvestments.UkInterest.{FromGiltEdged, FromUkBanks, FromUkTrustFunds}
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
import play.api.mvc.PlayBodyParsers
import play.api.test.FakeRequest
import play.api.test.Helpers.stubControllerComponents

import scala.concurrent.ExecutionContext

trait SpecBase
  extends AnyFreeSpec
    with Matchers
    with TryValues
    with OptionValues
    with ScalaFutures
    with IntegrationPatience {

  val mtdItId: String = "anMtdItId"
  val taxYear: Int = 2024
  val anAgent: Boolean = true
  val notAnAgent: Boolean = false
  val parsers: PlayBodyParsers = stubControllerComponents().parsers
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  def emptyUserAnswers : UserAnswers = UserAnswers(mtdItId, taxYear)

  val fullUserAnswers: UserAnswers = emptyUserAnswers.copy().set(UkResidenceStatusPage, UkResidenceStatus.Uk)
    .flatMap(_.set(CharitableDonationsPage, Set[CharitableDonations](DonationsUsingGiftAid, GiftsOfSharesOrSecurities, GiftsOfLandOrProperty)))
    .flatMap(_.set(FosterCarerPage, true))
    .flatMap(_.set(AboutYourWorkRadioPage, true))
    .flatMap(_.set(AboutYourWorkPage, Set[AboutYourWork](Employed, SelfEmployed)))
    .flatMap(_.set(ConstructionIndustrySchemePage, true))
    .flatMap(_.set(JobseekersAllowancePage, Set[JobseekersAllowance](Jsa, Esa)))
    .flatMap(_.set(RentalIncomePage, Set[RentalIncome](RentalIncome.Uk, RentalIncome.NonUk)))
    .flatMap(_.set(PensionsPage, Set[Pensions](StatePension, OtherUkPensions, UnauthorisedPayments, ShortServiceRefunds, Pensions.NonUkPensions)))
    .flatMap(_.set(UkInsuranceGainsPage, Set[UkInsuranceGains](LifeInsurance, LifeAnnuity, CapitalRedemption, VoidedISA)))
    .flatMap(_.set(UkInterestPage, Set[UkInterest](FromUkBanks, FromUkTrustFunds, FromGiltEdged)))
    .flatMap(_.set(UkDividendsSharesLoansPage, Set[UkDividendsSharesLoans](
      CashDividendsFromUkStocksAndShares,
      StockDividendsFromUkCompanies,
      DividendsUnitTrustsInvestmentCompanies,
      FreeOrRedeemableShares,
      CloseCompanyLoansWrittenOffReleased
    )))
    .flatMap(_.set(PaymentsIntoPensionsPage, Set[PaymentsIntoPensions](UkPensions, PaymentsIntoPensions.NonUkPensions, Overseas)))
    .success.value

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  protected def applicationBuilder(userAnswers: Option[UserAnswers] = None, isAgent: Boolean = false): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[IdentifierActionProvider].to(new FakeIdentifierAction(isAgent)(parsers)),
        bind[DataRetrievalActionProvider].toInstance(new FakeDataRetrievalActionProvider(userAnswers, isAgent))
      )
}
