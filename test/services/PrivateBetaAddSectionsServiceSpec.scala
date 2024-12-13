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

package services

import base.SpecBase
import models.TagStatus.{CannotStartYet, Completed, NotStarted}
import models.aboutyou.{UkResidenceStatus, YourResidenceStatus}
import models.pensions.PaymentsIntoPensions
import models.pensions.PaymentsIntoPensions.UkPensions
import models.propertypensionsinvestments.UkDividendsSharesLoans._
import models.propertypensionsinvestments._
import models.workandbenefits.{AboutYourWork, JobseekersAllowance}
import models.workandbenefits.AboutYourWork.Employed
import models.workandbenefits.JobseekersAllowance.Jsa
import models.{SectionState, UserAnswers}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import pages.aboutyou.{FosterCarerPage, UkResidenceStatusPage, YourResidenceStatusPage}
import pages.pensions.PaymentsIntoPensionsPage
import pages.propertypensionsinvestments.UkDividendsSharesLoansPage
import pages.workandbenefits.{AboutYourWorkPage, JobseekersAllowancePage}
import play.api.test.Helpers.running

class PrivateBetaAddSectionsServiceSpec extends AnyFreeSpec
  with SpecBase
  with Matchers
  with MockitoSugar
  with OptionValues
  with ScalaFutures
  with BeforeAndAfterEach {

  private val privateBetaEnabled = Map("feature-switch.privateBeta" -> "true")

  private val aboutYouCompleteInBeta = Some(UserAnswers(mtdItId, taxYear).set(FosterCarerPage, true).success.value)

  private val aboutYouCompleteNonUkResidenceStatus =
    Some(UserAnswers(mtdItId, taxYear)
      .set(FosterCarerPage, true)
      .flatMap(_.set(UkResidenceStatusPage, UkResidenceStatus.NonUK))
      .success.value)

  private val aboutYouCompleteNonUkNonResidentStatus =
    Some(UserAnswers(mtdItId, taxYear)
      .set(FosterCarerPage, true)
      .flatMap(_.set(UkResidenceStatusPage, UkResidenceStatus.NonUK))
      .flatMap(_.set(YourResidenceStatusPage, YourResidenceStatus.NonResident))
      .success.value)

  private val incomeFromWorkAndBenefitsComplete = Some(aboutYouCompleteInBeta.value.copy().set(AboutYourWorkPage, Set[AboutYourWork](Employed))
    .flatMap(_.set(JobseekersAllowancePage, Set[JobseekersAllowance](Jsa)))
    .success.value)

  private val incomeFromPropertyComplete = Some(incomeFromWorkAndBenefitsComplete.value.copy()
    .set(UkDividendsSharesLoansPage, Set[UkDividendsSharesLoans](CashDividendsFromUkStocksAndShares)).success.value)

  private val incomeFromPensionsComplete = Some(incomeFromPropertyComplete.value.copy()
    .set(PaymentsIntoPensionsPage, Set[PaymentsIntoPensions](UkPensions)).success.value)

  ".getState must" - {

    "when privateBeta is enabled" - {

      "return a model with the initial page state when no data is found" in {
        val application = applicationBuilder()
          .configure(privateBetaEnabled)
          .build()

        running(application) {

          val service = application.injector.instanceOf[PrivateBetaAddSectionsService]

          val model = service.getState(None)
          val expectedResult = SectionState(NotStarted, CannotStartYet, CannotStartYet, NotStarted)

          model mustBe expectedResult
        }
      }

      "return aboutYou section as Completed when data is found for last page" in {
        val application = applicationBuilder()
          .configure(privateBetaEnabled)
          .build()

        running(application) {

          val service = application.injector.instanceOf[PrivateBetaAddSectionsService]

          val model = service.getState(aboutYouCompleteInBeta)
          val expectedResult = SectionState(Completed, NotStarted, CannotStartYet, NotStarted)

          model mustBe expectedResult
        }
      }

      "return aboutYou section as NotStarted when no data is found for last page" in {
        val application = applicationBuilder()
          .configure(privateBetaEnabled)
          .build()

        running(application) {

          val service = application.injector.instanceOf[PrivateBetaAddSectionsService]

          val model = service.getState(aboutYouCompleteInBeta)
          val expectedResult = SectionState(Completed, NotStarted, CannotStartYet, NotStarted)

          model mustBe expectedResult
        }
      }

      "return aboutYou section as NotStarted when only UK Residence Status question is answered" in {
        val application = applicationBuilder()
          .configure(privateBetaEnabled)
          .build()

        running(application) {

          val service = application.injector.instanceOf[PrivateBetaAddSectionsService]

          val model = service.getState(aboutYouCompleteNonUkResidenceStatus)
          val expectedResult = SectionState(NotStarted, CannotStartYet, CannotStartYet, NotStarted)

          model mustBe expectedResult
        }
      }

      "return aboutYou section as Completed when only UK Residence and Your Resident Status questions are answered" in {
        val application = applicationBuilder()
          .configure(privateBetaEnabled)
          .build()

        running(application) {

          val service = application.injector.instanceOf[PrivateBetaAddSectionsService]

          val model = service.getState(aboutYouCompleteNonUkNonResidentStatus)
          val expectedResult = SectionState(Completed, NotStarted, CannotStartYet, NotStarted)

          model mustBe expectedResult
        }
      }

      "return incomeFromWork section as CannotStartYet when aboutYou section is incomplete" in {
        val application = applicationBuilder()
          .configure(privateBetaEnabled)
          .build()

        running(application) {

          val service = application.injector.instanceOf[PrivateBetaAddSectionsService]

          val model = service.getState(Some(emptyUserAnswers))
          val expectedResult = SectionState(NotStarted, CannotStartYet, CannotStartYet, NotStarted)

          model mustBe expectedResult
        }
      }

      "return incomeFromWork section as NotStarted when aboutYou section is complete" in {
        val application = applicationBuilder()
          .configure(privateBetaEnabled)
          .build()

        running(application) {

          val service = application.injector.instanceOf[PrivateBetaAddSectionsService]

          val model = service.getState(aboutYouCompleteInBeta)
          val expectedResult = SectionState(Completed, NotStarted, CannotStartYet, NotStarted)

          model mustBe expectedResult
        }
      }

      "return incomeFromWork section as NotStarted when aboutYou section is complete and AboutYourWork value is not defined" in {
        val application = applicationBuilder()
          .configure(privateBetaEnabled)
          .build()

        running(application) {

          val service = application.injector.instanceOf[PrivateBetaAddSectionsService]

          val model = service.getState(incomeFromWorkAndBenefitsComplete.get.remove(AboutYourWorkPage).toOption)
          val expectedResult = SectionState(Completed, NotStarted, CannotStartYet, NotStarted)

          model mustBe expectedResult
        }
      }

      "return incomeFromWork section as Completed when values in incomeFromWork are defined and aboutYou section is complete" in {
        val application = applicationBuilder()
          .configure(privateBetaEnabled)
          .build()

        running(application) {

          val service = application.injector.instanceOf[PrivateBetaAddSectionsService]

          val model = service.getState(incomeFromWorkAndBenefitsComplete)
          val expectedResult = SectionState(Completed, Completed, NotStarted, NotStarted)

          model mustBe expectedResult
        }
      }

      "return incomeFromProperty section as NotStarted when incomeFromWork section is complete" in {
        val application = applicationBuilder()
          .configure(privateBetaEnabled)
          .build()

        running(application) {

          val service = application.injector.instanceOf[PrivateBetaAddSectionsService]

          val model = service.getState(incomeFromWorkAndBenefitsComplete)
          val expectedResult = SectionState(Completed, Completed, NotStarted, NotStarted)

          model mustBe expectedResult
        }
      }

      "return incomeFromProperty section as Completed when UkDividendsSharesLoans is defined" in {
        val application = applicationBuilder()
          .configure(privateBetaEnabled)
          .build()

        running(application) {

          val service = application.injector.instanceOf[PrivateBetaAddSectionsService]

          val model = service.getState(incomeFromPropertyComplete)
          val expectedResult = SectionState(Completed, Completed, Completed, NotStarted)

          model mustBe expectedResult
        }
      }

      "return incomeFromProperty section as CannotStartYet when incomeFromWork is not completed" in {
        val application = applicationBuilder()
          .configure(privateBetaEnabled)
          .build()

        running(application) {

          val service = application.injector.instanceOf[PrivateBetaAddSectionsService]

          val model = service.getState(incomeFromWorkAndBenefitsComplete.copy().value.remove(JobseekersAllowancePage).toOption)
          val expectedResult = SectionState(Completed, NotStarted, CannotStartYet, NotStarted)

          model mustBe expectedResult
        }
      }

      "return pensions section as NotStarted with no user data" in {
        val application = applicationBuilder()
          .configure(privateBetaEnabled)
          .build()

        running(application) {

          val service = application.injector.instanceOf[PrivateBetaAddSectionsService]

          val model = service.getState(Some(emptyUserAnswers))
          val expectedResult = SectionState(NotStarted, CannotStartYet, CannotStartYet, NotStarted)

          model mustBe expectedResult
        }
      }

      "return pensions section as Completed when PaymentsIntoPensions is defined" in {
        val application = applicationBuilder()
          .configure(privateBetaEnabled)
          .build()

        running(application) {

          val service = application.injector.instanceOf[PrivateBetaAddSectionsService]

          val model = service.getState(incomeFromPensionsComplete)
          val expectedResult = SectionState(Completed, Completed, Completed, Completed)

          model mustBe expectedResult
        }
      }
    }
  }
}
