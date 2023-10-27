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

package navigation

import base.SpecBase
import controllers.routes
import models._
import models.aboutyou._
import pages._
import pages.aboutyou._

import scala.concurrent.ExecutionContext

class JourneyNavigatorSpec extends SpecBase {

  val navigator = new JourneyNavigator

  "JourneyNavigator" - {

    "in Normal mode" - {

      "must go from UkResidentStatus page to YourResidenceStatus page when no is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(UkResidenceStatusPage, UkResidenceStatus.NonUK).success.value

        val expectedRoute = controllers.aboutyou.routes.YourResidenceStatusController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(UkResidenceStatusPage, NormalMode, answers, taxYear) mustBe expectedRoute
      }

      "must go from UkResidentStatus page to CharitableDonations page when yes is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(UkResidenceStatusPage, UkResidenceStatus.Uk).success.value

        val expectedRoute = routes.CharitableDonationsController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(UkResidenceStatusPage, NormalMode, answers, taxYear) mustBe expectedRoute
      }

      "must go from UkResidentStatus page to CharitableDonations page when yes but... is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(UkResidenceStatusPage, UkResidenceStatus.Domiciled).success.value

        val expectedRoute = routes.CharitableDonationsController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(UkResidenceStatusPage, NormalMode, answers, taxYear) mustBe expectedRoute
      }

      "must go from UkResidentStatus page to Index page when no value is found" in {
        val answers = UserAnswers(mtdItId, taxYear)

        val expectedRoute = routes.IndexController.onPageLoad(taxYear)

        navigator.nextPage(UkResidenceStatusPage, NormalMode, answers, taxYear) mustBe expectedRoute
      }

      "must go from YourResidenceStatus page to CharitableDonations page when any value is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(YourResidenceStatusPage, YourResidenceStatus.NonResident).success.value

        val expectedRoute = routes.CharitableDonationsController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(YourResidenceStatusPage, NormalMode, answers, taxYear) mustBe expectedRoute
      }

      "must go from CharitableDonations page to MarriageAllowance page when any value is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(CharitableDonationsPage, Set(CharitableDonations.values.head)).success.value

        val expectedRoute = controllers.aboutyou.routes.MarriageAllowanceController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(CharitableDonationsPage, NormalMode, answers, taxYear) mustBe expectedRoute
      }

      "must go from MarriageAllowance page to ChildBenefit page when true is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(MarriageAllowancePage, true).success.value

        val expectedRoute = routes.ChildBenefitController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(MarriageAllowancePage, NormalMode, answers, taxYear) mustBe expectedRoute
      }

      "must go from MarriageAllowance page to ChildBenefit page when false is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(MarriageAllowancePage, false).success.value

        val expectedRoute = routes.ChildBenefitController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(MarriageAllowancePage, NormalMode, answers, taxYear) mustBe expectedRoute
      }

      "must go from ChildBenefit page to ChildBenefitIncome page when true is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(ChildBenefitPage, true).success.value

        val expectedRoute = routes.ChildBenefitIncomeController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(ChildBenefitPage, NormalMode, answers, taxYear) mustBe expectedRoute
      }

      "must go from ChildBenefit page to FosterCarer page when false is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(ChildBenefitPage, false).success.value

        val expectedRoute = routes.FosterCarerController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(ChildBenefitPage, NormalMode, answers, taxYear) mustBe expectedRoute
      }

      "must go from ChildBenefit page to Index page when no value is found" in {
        val answers = UserAnswers(mtdItId, taxYear)

        val expectedRoute = routes.IndexController.onPageLoad(taxYear)

        navigator.nextPage(ChildBenefitPage, NormalMode, answers, taxYear) mustBe expectedRoute
      }

      "must go from ChildBenefitIncome page to HighIncomeChildBenefitCharge page when true is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(ChildBenefitIncomePage, true).success.value

        val expectedRoute = routes.HighIncomeChildBenefitChargeController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(ChildBenefitIncomePage, NormalMode, answers, taxYear) mustBe expectedRoute
      }

      "must go from ChildBenefitIncome page to FosterCarer page when false is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(ChildBenefitIncomePage, false).success.value

        val expectedRoute = routes.FosterCarerController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(ChildBenefitIncomePage, NormalMode, answers, taxYear) mustBe expectedRoute
      }

      "must go from ChildBenefitIncome page to Index page when no value is found" in {
        val answers = UserAnswers(mtdItId, taxYear)

        val expectedRoute = routes.IndexController.onPageLoad(taxYear)

        navigator.nextPage(ChildBenefitIncomePage, NormalMode, answers, taxYear) mustBe expectedRoute
      }

      "must go from HighIncomeChildBenefitCharge page to FosterCarer page when Option1 is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(HighIncomeChildBenefitChargePage, HighIncomeChildBenefitCharge.Option1).success.value

        val expectedRoute = routes.FosterCarerController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(HighIncomeChildBenefitChargePage, NormalMode, answers, taxYear) mustBe expectedRoute
      }

      "must go from HighIncomeChildBenefitCharge page to FosterCarer page when Option2 is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(HighIncomeChildBenefitChargePage, HighIncomeChildBenefitCharge.Option2).success.value

        val expectedRoute = routes.FosterCarerController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(HighIncomeChildBenefitChargePage, NormalMode, answers, taxYear) mustBe expectedRoute
      }

      "must go from FosterCarer page to PatentRoyaltyPayments page when true is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(FosterCarerPage, true).success.value

        val expectedRoute = routes.PatentRoyaltyPaymentsController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(FosterCarerPage, NormalMode, answers, taxYear) mustBe expectedRoute
      }

      "must go from FosterCarer page to PatentRoyaltyPayments page when false is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(FosterCarerPage, false).success.value

        val expectedRoute = routes.PatentRoyaltyPaymentsController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(FosterCarerPage, NormalMode, answers, taxYear) mustBe expectedRoute
      }

      "must go from PatentRoyaltyPayments page to TaxAvoidanceSchemes page when true is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(PatentRoyaltyPaymentsPage, true).success.value

        val expectedRoute = routes.TaxAvoidanceSchemesController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(PatentRoyaltyPaymentsPage, NormalMode, answers, taxYear) mustBe expectedRoute
      }

      "must go from PatentRoyaltyPayments page to TaxAvoidanceSchemes page when false is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(PatentRoyaltyPaymentsPage, false).success.value

        val expectedRoute = routes.TaxAvoidanceSchemesController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(PatentRoyaltyPaymentsPage, NormalMode, answers, taxYear) mustBe expectedRoute
      }

      "must go from TaxAvoidanceSchemes page to AddSections page when any value is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(TaxAvoidanceSchemesPage, Set(TaxAvoidanceSchemes.values.head)).success.value

        val expectedRoute = routes.AddSectionsController.onPageLoad(taxYear)

        navigator.nextPage(TaxAvoidanceSchemesPage, NormalMode, answers, taxYear) mustBe expectedRoute
      }

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id", taxYear), taxYear) mustBe routes.IndexController.onPageLoad(taxYear)
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id", taxYear), taxYear) mustBe routes.IndexController.onPageLoad(taxYear)
      }
    }
  }
  override implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
}
