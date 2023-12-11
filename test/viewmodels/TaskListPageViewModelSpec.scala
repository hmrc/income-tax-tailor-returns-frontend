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

package viewmodels

import base.SpecBase
import models.TagStatus.NotStarted
import models.aboutyou.CharitableDonations.{DonationsUsingGiftAid, GiftsOfLandOrProperty, GiftsOfSharesOrSecurities, NoDonations}
import models.aboutyou.{CharitableDonations, UkResidenceStatus}
import models.pensions.PaymentsIntoPensions
import models.pensions.PaymentsIntoPensions.{Overseas, UkPensions}
import models.propertypensionsinvestments.Pensions.{NoPensions, OtherUkPensions, ShortServiceRefunds, StatePension, UnauthorisedPayments}
import models.propertypensionsinvestments.UkDividendsSharesLoans._
import models.propertypensionsinvestments.UkInsuranceGains.{CapitalRedemption, LifeAnnuity, LifeInsurance, VoidedISA}
import models.propertypensionsinvestments.UkInterest.{FromGiltEdged, FromUkBanks, FromUkTrustFunds, NoInterest}
import models.propertypensionsinvestments._
import models.workandbenefits.AboutYourWork.{Employed, SelfEmployed}
import models.workandbenefits.JobseekersAllowance.{Esa, Jsa}
import models.workandbenefits.{AboutYourWork, JobseekersAllowance}
import pages.aboutyou.{CharitableDonationsPage, FosterCarerPage, UkResidenceStatusPage}
import pages.pensions.PaymentsIntoPensionsPage
import pages.propertypensionsinvestments._
import pages.workandbenefits.{AboutYourWorkPage, AboutYourWorkRadioPage, ConstructionIndustrySchemePage, JobseekersAllowancePage}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class TaskListPageViewModelSpec extends SpecBase {

  private implicit val messages: Messages = stubMessages()

  private val allSections = emptyUserAnswers.copy().set(UkResidenceStatusPage, UkResidenceStatus.Uk)
    .flatMap(_.set(CharitableDonationsPage, Set[CharitableDonations](DonationsUsingGiftAid, GiftsOfSharesOrSecurities, GiftsOfLandOrProperty )))
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

  private val allSectionsNegative = emptyUserAnswers.copy().set(UkResidenceStatusPage, UkResidenceStatus.NonUK)
    .flatMap(_.set(CharitableDonationsPage, Set[CharitableDonations](NoDonations)))
    .flatMap(_.set(FosterCarerPage, false))
    .flatMap(_.set(AboutYourWorkPage, Set[AboutYourWork](AboutYourWork.No)))
    .flatMap(_.set(JobseekersAllowancePage, Set[JobseekersAllowance](JobseekersAllowance.No)))
    .flatMap(_.set(RentalIncomePage, Set[RentalIncome](RentalIncome.No)))
    .flatMap(_.set(PensionsPage, Set[Pensions](NoPensions)))
    .flatMap(_.set(UkInsuranceGainsPage, Set[UkInsuranceGains](UkInsuranceGains.No)))
    .flatMap(_.set(UkInterestPage, Set[UkInterest](NoInterest)))
    .flatMap(_.set(UkDividendsSharesLoansPage, Set[UkDividendsSharesLoans](NoUkDividendsSharesOrLoans)))
    .flatMap(_.set(PaymentsIntoPensionsPage, Set[PaymentsIntoPensions](PaymentsIntoPensions.No)))
    .success.value

  ".getSections" - {

    "must return an empty list with an empty userAnswers object" in {
      val expected = List.empty
      val result = TaskListPageViewModel(emptyUserAnswers).getSections

      result mustBe expected
    }

    "must return an empty list when all answers are 'no/none'" in {
      val expected = List.empty
      val result = TaskListPageViewModel(allSectionsNegative).getSections

      result mustBe expected
    }

    "must return a list containing all possible sections for the task list" in {
      val expected =
        List(
          ("addSections.aboutYou", List(
            Task(Link("ukResidenceStatus.heading", "#"), NotStarted),
            Task(Link("charitableDonations.donationsUsingGiftAid", "#"), NotStarted),
            Task(Link("charitableDonations.giftsOfLandOrProperty", "#"), NotStarted),
            Task(Link("charitableDonations.giftsOfSharesOrSecurities", "#"), NotStarted),
            Task(Link("Foster Carer", "#"), NotStarted))),
          ("All Employment", List(Task(Link("Employers", "#"), NotStarted))),
          ("aboutYourWork.selfEmployed", List(Task(Link("Check your self-employment details", "#"), NotStarted), Task(Link("Review CIS", "#"), NotStarted))),
          ("Employment and Support Allowance", List(Task(Link("Review Jobseeker’s Allowance claims", "#"), NotStarted))),
          ("jobseekersAllowance.jsa", List(Task(Link("Review Jobseeker’s Allowance claims", "#"), NotStarted))),
          ("rentalIncome.uk", List(Task(Link("About UK property", "#"), NotStarted))),
          ("rentalIncome.nonUk", List(Task(Link("About Foreign property", "#"), NotStarted))),
          ("pensions.title", List(
            Task(Link("pensions.unauthorisedPayments", "#"), NotStarted),
            Task(Link("pensions.nonUkPensions", "#"), NotStarted),
            Task(Link("pensions.shortServiceRefunds", "#"), NotStarted),
            Task(Link("pensions.otherUkPensions", "#"), NotStarted),
            Task(Link("pensions.statePension", "#"), NotStarted))),
          ("ukInsuranceGains.title", List(
            Task(Link("ukInsuranceGains.lifeInsurance", "#"), NotStarted),
            Task(Link("ukInsuranceGains.lifeAnnuity", "#"), NotStarted),
            Task(Link("ukInsuranceGains.capitalRedemption", "#"), NotStarted),
            Task(Link("ukInsuranceGains.voidedISA", "#"), NotStarted))),
          ("ukInterest.title", List(
            Task(Link("ukInterest.fromUkBanks", "#"), NotStarted),
            Task(Link("ukInterest.fromUkTrustFunds", "#"), NotStarted),
            Task(Link("ukInterest.fromGiltEdged", "#"), NotStarted))),
          ("ukDividendsSharesLoans.title", List(
            Task(Link("ukDividendsSharesLoans.cashDividendsUkStocksAndShares", "#"), NotStarted),
            Task(Link("ukDividendsSharesLoans.closeCompanyLoansWrittenOffReleased", "#"), NotStarted),
            Task(Link("ukDividendsSharesLoans.dividendsUnitTrustsInvestmentCompanies", "#"), NotStarted),
            Task(Link("ukDividendsSharesLoans.freeOrRedeemableShares", "#"), NotStarted),
            Task(Link("ukDividendsSharesLoans.stockDividendsUkCompanies", "#"), NotStarted))),
          ("paymentsIntoPensions.title", List(
            Task(Link("paymentsIntoPensions.ukPensions", "#"), NotStarted),
            Task(Link("paymentsIntoPensions.nonUkPensions", "#"), NotStarted),
            Task(Link("paymentsIntoPensions.overseas", "#"), NotStarted)))
        )

      val result = TaskListPageViewModel(allSections).getSections

      result mustBe expected
    }
  }
}
