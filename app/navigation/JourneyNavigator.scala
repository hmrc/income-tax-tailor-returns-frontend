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

import javax.inject.{Inject, Singleton}
import play.api.mvc.Call
import controllers.routes
import pages._
import models._
import models.aboutyou._
import models.workandbenefits.AboutYourWork.{Employed, No, SelfEmployed}
import pages.aboutyou._
import pages.propertypensionsinvestments._
import pages.pensions.PaymentsIntoPensionsPage
import pages.workandbenefits.{AboutYourWorkPage, AboutYourWorkRadioPage, ConstructionIndustrySchemePage, JobseekersAllowancePage}

@Singleton
class JourneyNavigator @Inject()() extends Navigator {

  override val normalRoutes: Page => UserAnswers => Call = {
    // About you
    case UkResidenceStatusPage                => ukResidenceStatusRoute
    case YourResidenceStatusPage              => ua => controllers.aboutyou.routes.CharitableDonationsController.onPageLoad(NormalMode, ua.taxYear)
    case CharitableDonationsPage              => ua => controllers.aboutyou.routes.MarriageAllowanceController.onPageLoad(NormalMode, ua.taxYear)
    case MarriageAllowancePage                => ua => controllers.aboutyou.routes.ChildBenefitController.onPageLoad(NormalMode, ua.taxYear)
    case ChildBenefitPage                     => childBenefitRoute
    case ChildBenefitIncomePage               => childBenefitIncomeRoute
    case HighIncomeChildBenefitChargePage     => ua => controllers.aboutyou.routes.FosterCarerController.onPageLoad(NormalMode, ua.taxYear)
    case FosterCarerPage                      => ua => controllers.aboutyou.routes.PatentRoyaltyPaymentsController.onPageLoad(NormalMode, ua.taxYear)
    case PatentRoyaltyPaymentsPage            => ua => controllers.aboutyou.routes.TaxAvoidanceSchemesController.onPageLoad(NormalMode, ua.taxYear)
    case TaxAvoidanceSchemesPage              => ua => routes.AddSectionsController.onPageLoad(ua.taxYear)

    //Income from work and taxable state benefits section
    case AboutYourWorkRadioPage               => ua =>
      controllers.workandbenefits.routes.ConstructionIndustrySchemeController.onPageLoad(NormalMode, ua.taxYear)
    case AboutYourWorkPage                    => aboutYourWorkRoute
    case ConstructionIndustrySchemePage       => ua => controllers.workandbenefits.routes.JobseekersAllowanceController.onPageLoad(NormalMode, ua.taxYear)
    case JobseekersAllowancePage              => ua => routes.AddSectionsController.onPageLoad(ua.taxYear)

    // Income from property, pensions and investments
    case RentalIncomePage                    => ua => controllers.propertypensionsinvestments.routes.PensionsController.onPageLoad(NormalMode, ua.taxYear)
    case PensionsPage                        => ua =>
      controllers.propertypensionsinvestments.routes.UkInsuranceGainsController.onPageLoad(NormalMode, ua.taxYear)
    case UkInsuranceGainsPage                => ua => controllers.propertypensionsinvestments.routes.UkInterestController.onPageLoad(NormalMode, ua.taxYear)
    case UkInterestPage                      => ua =>
      controllers.propertypensionsinvestments.routes.UkDividendsSharesLoansController.onPageLoad(NormalMode, ua.taxYear)
    case UkDividendsSharesLoansPage          => ua => routes.AddSectionsController.onPageLoad(ua.taxYear)

    // Payments into pensions
    case PaymentsIntoPensionsPage             => ua => routes.AddSectionsController.onPageLoad(ua.taxYear)
    case _                                    => ua => routes.IndexController.onPageLoad(ua.taxYear)
  }

  override val checkRouteMap: Page => UserAnswers => Call = {
    case _ => ua => routes.IndexController.onPageLoad(ua.taxYear)
  }

  def ukResidenceStatusRoute(userAnswers: UserAnswers): Call = {
    userAnswers.get(UkResidenceStatusPage) match {
      case Some(UkResidenceStatus.Uk) =>
        controllers.aboutyou.routes.CharitableDonationsController.onPageLoad(NormalMode, userAnswers.taxYear)

      case Some(UkResidenceStatus.Domiciled) =>
        controllers.aboutyou.routes.CharitableDonationsController.onPageLoad(NormalMode, userAnswers.taxYear)

      case Some(UkResidenceStatus.NonUK) =>
        controllers.aboutyou.routes.YourResidenceStatusController.onPageLoad(NormalMode, userAnswers.taxYear)

      case _ => routes.IndexController.onPageLoad(userAnswers.taxYear)
    }
  }

  def childBenefitRoute(userAnswers: UserAnswers): Call = {
    userAnswers.get(ChildBenefitPage) match {
      case Some(true) => controllers.aboutyou.routes.ChildBenefitIncomeController.onPageLoad(NormalMode, userAnswers.taxYear)
      case Some(false) => controllers.aboutyou.routes.FosterCarerController.onPageLoad(NormalMode, userAnswers.taxYear)
      case _ => routes.IndexController.onPageLoad(userAnswers.taxYear)
    }
  }

  def childBenefitIncomeRoute(userAnswers: UserAnswers): Call = {
    userAnswers.get(ChildBenefitIncomePage) match {
      case Some(true) => controllers.aboutyou.routes.HighIncomeChildBenefitChargeController.onPageLoad(NormalMode, userAnswers.taxYear)
      case Some(false) => controllers.aboutyou.routes.FosterCarerController.onPageLoad(NormalMode, userAnswers.taxYear)
      case _ => routes.IndexController.onPageLoad(userAnswers.taxYear)
    }
  }
  def aboutYourWorkRoute(userAnswers: UserAnswers): Call = {
    userAnswers.get(AboutYourWorkPage).map(_.toSeq) match {
      case Some(Seq(Employed)) =>
        controllers.workandbenefits.routes.JobseekersAllowanceController.onPageLoad(NormalMode, userAnswers.taxYear)
      case Some(Seq(SelfEmployed)) =>
        controllers.workandbenefits.routes.ConstructionIndustrySchemeController.onPageLoad(NormalMode, userAnswers.taxYear)
      case Some(Seq(No)) =>
        controllers.workandbenefits.routes.JobseekersAllowanceController.onPageLoad(NormalMode, userAnswers.taxYear)
      case Some(Seq(Employed, SelfEmployed)) =>
        controllers.workandbenefits.routes.ConstructionIndustrySchemeController.onPageLoad(NormalMode, userAnswers.taxYear)
      case _ => routes.IndexController.onPageLoad(userAnswers.taxYear)
    }
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }
}
