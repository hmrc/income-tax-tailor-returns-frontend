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
import models.propertypensionsinvestments._
import models.pensions.PaymentsIntoPensions
import pages._
import pages.aboutyou._
import pages.propertypensionsinvestments._
import pages.pensions.PaymentsIntoPensionsPage
import pages.workandbenefits._
import models.workandbenefits.AboutYourWork.{Employed, No, SelfEmployed}
import models.workandbenefits.{AboutYourWork, JobseekersAllowance}
import scala.concurrent.ExecutionContext

class JourneyNavigatorSpec extends SpecBase {

  val navigator = new JourneyNavigator

  "JourneyNavigator" - {

    "in Normal mode" - {

      // About you

      "must go from UkResidentStatus page to YourResidenceStatus page when no is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(UkResidenceStatusPage, UkResidenceStatus.NonUK).success.value

        val expectedRoute = controllers.aboutyou.routes.YourResidenceStatusController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(UkResidenceStatusPage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from UkResidentStatus page to CharitableDonations page when yes is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(UkResidenceStatusPage, UkResidenceStatus.Uk).success.value

        val expectedRoute = controllers.aboutyou.routes.CharitableDonationsController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(UkResidenceStatusPage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from UkResidentStatus page to CharitableDonations page when yes but... is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(UkResidenceStatusPage, UkResidenceStatus.Domiciled).success.value

        val expectedRoute = controllers.aboutyou.routes.CharitableDonationsController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(UkResidenceStatusPage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from UkResidentStatus page to Index page when no value is found" in {
        val answers = UserAnswers(mtdItId, taxYear)

        val expectedRoute = routes.IndexController.onPageLoad(taxYear)

        navigator.nextPage(UkResidenceStatusPage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from YourResidenceStatus page to CharitableDonations page when any value is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(YourResidenceStatusPage, YourResidenceStatus.NonResident).success.value

        val expectedRoute = controllers.aboutyou.routes.CharitableDonationsController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(YourResidenceStatusPage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from CharitableDonations page to MarriageAllowance page when any value is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(CharitableDonationsPage, Set(CharitableDonations.values.head)).success.value

        val expectedRoute = controllers.aboutyou.routes.MarriageAllowanceController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(CharitableDonationsPage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from MarriageAllowance page to ChildBenefit page when true is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(MarriageAllowancePage, true).success.value

        val expectedRoute = controllers.aboutyou.routes.ChildBenefitController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(MarriageAllowancePage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from MarriageAllowance page to ChildBenefit page when false is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(MarriageAllowancePage, false).success.value

        val expectedRoute = controllers.aboutyou.routes.ChildBenefitController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(MarriageAllowancePage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from ChildBenefit page to ChildBenefitIncome page when true is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(ChildBenefitPage, true).success.value

        val expectedRoute = controllers.aboutyou.routes.ChildBenefitIncomeController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(ChildBenefitPage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from ChildBenefit page to FosterCarer page when false is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(ChildBenefitPage, false).success.value

        val expectedRoute = controllers.aboutyou.routes.FosterCarerController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(ChildBenefitPage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from ChildBenefit page to Index page when no value is found" in {
        val answers = UserAnswers(mtdItId, taxYear)

        val expectedRoute = routes.IndexController.onPageLoad(taxYear)

        navigator.nextPage(ChildBenefitPage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from ChildBenefitIncome page to HighIncomeChildBenefitCharge page when true is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(ChildBenefitIncomePage, true).success.value

        val expectedRoute = controllers.aboutyou.routes.HighIncomeChildBenefitChargeController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(ChildBenefitIncomePage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from ChildBenefitIncome page to FosterCarer page when false is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(ChildBenefitIncomePage, false).success.value

        val expectedRoute = controllers.aboutyou.routes.FosterCarerController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(ChildBenefitIncomePage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from ChildBenefitIncome page to Index page when no value is found" in {
        val answers = UserAnswers(mtdItId, taxYear)

        val expectedRoute = routes.IndexController.onPageLoad(taxYear)

        navigator.nextPage(ChildBenefitIncomePage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from HighIncomeChildBenefitCharge page to FosterCarer page when selfIncome is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(HighIncomeChildBenefitChargePage, HighIncomeChildBenefitCharge.SelfIncome).success.value

        val expectedRoute = controllers.aboutyou.routes.FosterCarerController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(HighIncomeChildBenefitChargePage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from HighIncomeChildBenefitCharge page to FosterCarer page when partnerIncome is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(HighIncomeChildBenefitChargePage, HighIncomeChildBenefitCharge.PartnerIncome).success.value

        val expectedRoute = controllers.aboutyou.routes.FosterCarerController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(HighIncomeChildBenefitChargePage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from HighIncomeChildBenefitCharge page to FosterCarer page when noPartner is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(HighIncomeChildBenefitChargePage, HighIncomeChildBenefitCharge.NoPartner).success.value

        val expectedRoute = controllers.aboutyou.routes.FosterCarerController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(HighIncomeChildBenefitChargePage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from FosterCarer page to PatentRoyaltyPayments page when true is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(FosterCarerPage, true).success.value

        val expectedRoute = controllers.aboutyou.routes.PatentRoyaltyPaymentsController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(FosterCarerPage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from FosterCarer page to PatentRoyaltyPayments page when false is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(FosterCarerPage, false).success.value

        val expectedRoute = controllers.aboutyou.routes.PatentRoyaltyPaymentsController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(FosterCarerPage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from PatentRoyaltyPayments page to TaxAvoidanceSchemes page when true is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(PatentRoyaltyPaymentsPage, true).success.value

        val expectedRoute = controllers.aboutyou.routes.TaxAvoidanceSchemesController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(PatentRoyaltyPaymentsPage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from PatentRoyaltyPayments page to TaxAvoidanceSchemes page when false is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(PatentRoyaltyPaymentsPage, false).success.value

        val expectedRoute = controllers.aboutyou.routes.TaxAvoidanceSchemesController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(PatentRoyaltyPaymentsPage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from TaxAvoidanceSchemes page to AddSections page when any value is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(TaxAvoidanceSchemesPage, Set(TaxAvoidanceSchemes.values.head)).success.value

        val expectedRoute = routes.AddSectionsController.onPageLoad(taxYear)

        navigator.nextPage(TaxAvoidanceSchemesPage, NormalMode, answers) mustBe expectedRoute
      }

      //Income from work and taxable state benefits section

      "must go from About Your Work Radio page to Construction Industry Scheme page when yes is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(AboutYourWorkRadioPage, true).success.value

        val expectedRoute = controllers.workandbenefits.routes.ConstructionIndustrySchemeController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(AboutYourWorkRadioPage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from About Your Work Radio page to Construction Industry Scheme page when no is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(AboutYourWorkRadioPage, false).success.value

        val expectedRoute = controllers.workandbenefits.routes.ConstructionIndustrySchemeController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(AboutYourWorkRadioPage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from About your work page to Job seeker's Allowance Page when Employed is selected" in {

        val answers = UserAnswers(mtdItId, taxYear).set(AboutYourWorkPage, Set[AboutYourWork](Employed)).success.value

        val expectedRoute = controllers.workandbenefits.routes.JobseekersAllowanceController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(AboutYourWorkPage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from About your work page to Construction Industry Scheme Page when Self-employed is selected" in {

        val answers = UserAnswers(mtdItId, taxYear).set(AboutYourWorkPage, Set[AboutYourWork](SelfEmployed)).success.value

        val expectedRoute = controllers.workandbenefits.routes.ConstructionIndustrySchemeController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(AboutYourWorkPage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from About your work page to Job seeker's Allowance Page when None is selected" in {

        val answers = UserAnswers(mtdItId, taxYear).set(AboutYourWorkPage, Set[AboutYourWork](No)).success.value

        val expectedRoute = controllers.workandbenefits.routes.JobseekersAllowanceController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(AboutYourWorkPage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from About your work page to Construction Industry Scheme Page when both Employed and Self Employed are selected" in {

        val answers = UserAnswers(mtdItId, taxYear).set(AboutYourWorkPage, Set[AboutYourWork](Employed, SelfEmployed)).success.value

        val expectedRoute = controllers.workandbenefits.routes.ConstructionIndustrySchemeController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(AboutYourWorkPage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from Job seeker's Allowance Page to AddSections page when any value is selected" in {

        val answers = UserAnswers(mtdItId, taxYear).set(JobseekersAllowancePage, Set(JobseekersAllowance.values.head)).success.value

        val expectedRoute = routes.AddSectionsController.onPageLoad(taxYear)

        navigator.nextPage(JobseekersAllowancePage, NormalMode, answers) mustBe expectedRoute

      }

      "must go from Construction Industry Scheme Page to Job seeker's Allowance Page when yes is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(ConstructionIndustrySchemePage, true).success.value

        val expectedRoute = controllers.workandbenefits.routes.JobseekersAllowanceController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(ConstructionIndustrySchemePage, NormalMode, answers) mustBe expectedRoute

      }

      "must go from Construction Industry Scheme Page to Job seeker's Allowance Page when no is selected" in {

        val answers = UserAnswers(mtdItId, taxYear).set(ConstructionIndustrySchemePage, false).success.value

        val expectedRoute = controllers.workandbenefits.routes.JobseekersAllowanceController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(ConstructionIndustrySchemePage, NormalMode, answers) mustBe expectedRoute
      }

      // Income from property, pensions and investments

      "must go from RentalIncome page to Pensions page when any value is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(RentalIncomePage, Set(RentalIncome.values.head)).success.value

        val expectedRoute = controllers.propertypensionsinvestments.routes.PensionsController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(RentalIncomePage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from Pensions page to UkInsuranceGains page when any value is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(PensionsPage, Set(Pensions.values.head)).success.value

        val expectedRoute = controllers.propertypensionsinvestments.routes.UkInsuranceGainsController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(PensionsPage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from UkInsuranceGains page to UkInterest page when any value is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(UkInsuranceGainsPage, Set(UkInsuranceGains.values.head)).success.value

        val expectedRoute = controllers.propertypensionsinvestments.routes.UkInterestController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(UkInsuranceGainsPage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from UkInterest page to UkDividendsShareLoans page when any value is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(UkInterestPage, Set(UkInterest.values.head)).success.value

        val expectedRoute = controllers.propertypensionsinvestments.routes.UkDividendsSharesLoansController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(UkInterestPage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from UkDividendsShareLoans page to AddSections page when any value is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(UkDividendsSharesLoansPage, Set(UkDividendsSharesLoans.values.head)).success.value

        val expectedRoute = routes.AddSectionsController.onPageLoad(taxYear)

        navigator.nextPage(UkDividendsSharesLoansPage, NormalMode, answers) mustBe expectedRoute
      }

      // Payments into pensions
      "must go from PaymentsIntoPensions page to AddSections page when any value is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(PaymentsIntoPensionsPage, Set(PaymentsIntoPensions.values.head)).success.value

        val expectedRoute = routes.AddSectionsController.onPageLoad(taxYear)

        navigator.nextPage(PaymentsIntoPensionsPage, NormalMode, answers) mustBe expectedRoute
      }

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id", taxYear)) mustBe routes.IndexController.onPageLoad(taxYear)
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id", taxYear)) mustBe routes.IndexController.onPageLoad(taxYear)
      }
    }
  }
  override implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
}
