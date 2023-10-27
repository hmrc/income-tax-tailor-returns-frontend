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
import models.aboutyou.UkResidenceStatus
import pages.aboutyou.{UkResidenceStatusPage, YourResidenceStatusPage}

@Singleton
class JourneyNavigator @Inject()() extends Navigator {
  override def normalRoutes(taxYear: Int): Page => UserAnswers => Call = {
    case UkResidenceStatusPage => ukResidenceStatusRoute(_, taxYear)
    case YourResidenceStatusPage => _ => routes.CharitableDonationsController.onPageLoad(NormalMode, taxYear)
    case CharitableDonationsPage => _ => routes.MarriageAllowanceController.onPageLoad(NormalMode, taxYear)
    case MarriageAllowancePage => _ => routes.ChildBenefitController.onPageLoad(NormalMode, taxYear)
    case ChildBenefitPage => childBenefitRoute(_, taxYear)
    case ChildBenefitIncomePage => childBenefitIncomeRoute(_, taxYear)
    case HighIncomeChildBenefitChargePage => _ => routes.FosterCarerController.onPageLoad(NormalMode, taxYear)
    case FosterCarerPage => _ => routes.PatentRoyaltyPaymentsController.onPageLoad(NormalMode, taxYear)
    case PatentRoyaltyPaymentsPage => _ => routes.TaxAvoidanceSchemesController.onPageLoad(NormalMode, taxYear)
    case TaxAvoidanceSchemesPage => _ => routes.AddSectionsController.onPageLoad(taxYear)
    case _ => _ => routes.IndexController.onPageLoad(taxYear)
  }

  override val checkRouteMap: Page => UserAnswers => Call = {
    case _ => _ => routes.IndexController.onPageLoad(2024)
  }

  def ukResidenceStatusRoute(userAnswers: UserAnswers, taxYear: Int): Call = {
    userAnswers.get(UkResidenceStatusPage) match {
      case Some(UkResidenceStatus.Uk) =>
        routes.CharitableDonationsController.onPageLoad(NormalMode, taxYear)

      case Some(UkResidenceStatus.Domiciled) =>
        routes.CharitableDonationsController.onPageLoad(NormalMode, taxYear)

      case Some(UkResidenceStatus.NonUK) =>
        controllers.aboutyou.routes.YourResidenceStatusController.onPageLoad(NormalMode, taxYear)

      case _ => routes.IndexController.onPageLoad(taxYear)
    }
  }

  def childBenefitRoute(userAnswers: UserAnswers, taxYear: Int): Call = {
    userAnswers.get(ChildBenefitPage) match {
      case Some(true) => routes.ChildBenefitIncomeController.onPageLoad(NormalMode, taxYear)
      case Some(false) => routes.FosterCarerController.onPageLoad(NormalMode, taxYear)
      case _ => routes.IndexController.onPageLoad(taxYear)
    }
  }

  def childBenefitIncomeRoute(userAnswers: UserAnswers, taxYear: Int): Call = {
    userAnswers.get(ChildBenefitIncomePage) match {
      case Some(true) => routes.HighIncomeChildBenefitChargeController.onPageLoad(NormalMode, taxYear)
      case Some(false) => routes.FosterCarerController.onPageLoad(NormalMode, taxYear)
      case _ => routes.IndexController.onPageLoad(taxYear)
    }
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, taxYear: Int): Call = mode match {
    case NormalMode =>
      normalRoutes(taxYear)(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }
}
