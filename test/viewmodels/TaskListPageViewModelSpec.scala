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
import models.aboutyou.CharitableDonations.NoDonations
import models.aboutyou.UkResidenceStatus.Domiciled
import models.aboutyou.{CharitableDonations, UkResidenceStatus}
import models.pensions.PaymentsIntoPensions
import models.propertypensionsinvestments.Pensions.NoPensions
import models.propertypensionsinvestments.UkDividendsSharesLoans._
import models.propertypensionsinvestments.UkInterest.NoInterest
import models.propertypensionsinvestments._
import models.workandbenefits.{AboutYourWork, JobseekersAllowance}
import pages.aboutyou.{CharitableDonationsPage, FosterCarerPage, UkResidenceStatusPage}
import pages.pensions.PaymentsIntoPensionsPage
import pages.propertypensionsinvestments._
import pages.workandbenefits.{AboutYourWorkPage, JobseekersAllowancePage}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class TaskListPageViewModelSpec extends SpecBase {

  private implicit val messages: Messages = stubMessages()

  private val prefix: String = "taskList"

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
      val result = TaskListPageViewModel(emptyUserAnswers, prefix).getSections

      result mustBe List.empty
    }

    "must return an empty list when all answers are 'no/none'" in {
      val result = TaskListPageViewModel(allSectionsNegative, prefix).getSections

      result mustBe List.empty
    }

    "must return the ukResidenceStatus task when 'Domiciled'" in {
      val expected = List(
        (s"${prefix}.aboutYou", List(
          Task(Link("heading", "#"), NotStarted, "ukResidenceStatus")))
      )
      val result = TaskListPageViewModel(emptyUserAnswers.copy().set(UkResidenceStatusPage, Domiciled).success.value, prefix).getSections

      result mustBe expected
    }

    "must return a list containing all possible sections for the task list" in {
      val expected =
        List(
          (s"${prefix}.aboutYou", List(
            Task(Link("heading", "#"), NotStarted, "ukResidenceStatus"),
            Task(Link("donationsUsingGiftAid", "#"), NotStarted, "charitableDonations"),
            Task(Link("giftsOfLandOrProperty", "#"), NotStarted, "charitableDonations"),
            Task(Link("giftsOfSharesOrSecurities", "#"), NotStarted, "charitableDonations"),
            Task(Link("fosterCarer", "#"), NotStarted, prefix))),
          (s"${prefix}.allEmployment", List(Task(Link("employers", "#"), NotStarted, prefix))),
          ("aboutYourWork.selfEmployed", List(
            Task(Link("selfEmployment", "#"), NotStarted, prefix),
            Task(Link("reviewCis", "#"), NotStarted, prefix))),
          (s"${prefix}.esa", List(Task(Link("reviewEsa", "#"), NotStarted, prefix))),
          ("jobseekersAllowance.jsa", List(Task(Link("reviewJsa", "#"), NotStarted, prefix))),
          ("rentalIncome.uk", List(Task(Link("ukProperty", "#"), NotStarted, prefix))),
          (s"${prefix}.foreignProperty", List(Task(Link("aboutForeignProperty", "#"), NotStarted, prefix))),
          ("pensions.title", List(
            Task(Link("statePension", "#"), NotStarted, "pensions"),
            Task(Link("otherUkPensions", "#"), NotStarted, "pensions"),
            Task(Link("unauthorisedPayments", "#"), NotStarted, "pensions"),
            Task(Link("shortServiceRefunds", "#"), NotStarted, "pensions"),
            Task(Link("nonUkPensions", "#"), NotStarted, "pensions"))),
          ("ukInsuranceGains.title", List(
            Task(Link("lifeInsurance", "#"), NotStarted, "ukInsuranceGains"),
            Task(Link("lifeAnnuity", "#"), NotStarted, "ukInsuranceGains"),
            Task(Link("capitalRedemption", "#"), NotStarted, "ukInsuranceGains"),
            Task(Link("voidedISA", "#"), NotStarted, "ukInsuranceGains"))),
          ("ukInterest.title", List(
            Task(Link("fromUkBanks", "#"), NotStarted, "ukInterest"),
            Task(Link("fromUkTrustFunds", "#"), NotStarted, "ukInterest"),
            Task(Link("fromGiltEdged", "#"), NotStarted, "ukInterest"))),
          (s"${prefix}.ukDividends", List(
            Task(Link("cashDividendsUkStocksAndShares", "#"), NotStarted, "ukDividendsSharesLoans"),
            Task(Link("stockDividendsUkCompanies", "#"), NotStarted, "ukDividendsSharesLoans"),
            Task(Link("dividendsUnitTrustsInvestmentCompanies", "#"), NotStarted, "ukDividendsSharesLoans"),
            Task(Link("freeOrRedeemableShares", "#"), NotStarted, "ukDividendsSharesLoans"),
            Task(Link("closeCompanyLoansWrittenOffReleased", "#"), NotStarted, "ukDividendsSharesLoans"))),
          ("paymentsIntoPensions.title", List(
            Task(Link("ukPensions", "#"), NotStarted, "paymentsIntoPensions"),
            Task(Link("nonUkPensions", "#"), NotStarted, "paymentsIntoPensions"),
            Task(Link("overseas", "#"), NotStarted, "paymentsIntoPensions")))
        )

      val result = TaskListPageViewModel(fullUserAnswers, prefix).getSections

      result mustBe expected
    }
  }

  ".getNumOfTasks" - {

    "must return 32 when all options have been selected" in {
      val expected = 32
      val result = TaskListPageViewModel(fullUserAnswers, prefix).getNumOfTasks

      result mustBe expected
    }
  }
}
