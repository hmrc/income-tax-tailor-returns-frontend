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
import pages.aboutyou._

@Singleton
class JourneyNavigator @Inject()() extends Navigator {

  override val normalRoutes: Page => UserAnswers => Call = {
    case UkResidenceStatusPage                => ukResidenceStatusRoute
    case YourResidenceStatusPage              => ua => routes.CharitableDonationsController.onPageLoad(NormalMode, ua.taxYear)
    case CharitableDonationsPage              => ua => controllers.aboutyou.routes.MarriageAllowanceController.onPageLoad(NormalMode, ua.taxYear)
    case MarriageAllowancePage                => ua => routes.ChildBenefitController.onPageLoad(NormalMode, ua.taxYear)
    case ChildBenefitPage                     => childBenefitRoute
    case ChildBenefitIncomePage               => childBenefitIncomeRoute
    case HighIncomeChildBenefitChargePage     => ua => routes.FosterCarerController.onPageLoad(NormalMode, ua.taxYear)
    case FosterCarerPage                      => ua => routes.PatentRoyaltyPaymentsController.onPageLoad(NormalMode, ua.taxYear)
    case PatentRoyaltyPaymentsPage            => ua => controllers.aboutyou.routes.TaxAvoidanceSchemesController.onPageLoad(NormalMode, ua.taxYear)
    case TaxAvoidanceSchemesPage              => ua => routes.AddSectionsController.onPageLoad(ua.taxYear)
    case _                                    => ua => routes.IndexController.onPageLoad(ua.taxYear)
  }

  override val checkRouteMap: Page => UserAnswers => Call = {
    case _ => _ => routes.IndexController.onPageLoad(2024)
  }

  def ukResidenceStatusRoute(userAnswers: UserAnswers): Call = {
    userAnswers.get(UkResidenceStatusPage) match {
      case Some(UkResidenceStatus.Uk) =>
        routes.CharitableDonationsController.onPageLoad(NormalMode, userAnswers.taxYear)

      case Some(UkResidenceStatus.Domiciled) =>
        routes.CharitableDonationsController.onPageLoad(NormalMode, userAnswers.taxYear)

      case Some(UkResidenceStatus.NonUK) =>
        controllers.aboutyou.routes.YourResidenceStatusController.onPageLoad(NormalMode, userAnswers.taxYear)

      case _ => routes.IndexController.onPageLoad(userAnswers.taxYear)
    }
  }

  def childBenefitRoute(userAnswers: UserAnswers): Call = {
    userAnswers.get(ChildBenefitPage) match {
      case Some(true) => routes.ChildBenefitIncomeController.onPageLoad(NormalMode, userAnswers.taxYear)
      case Some(false) => routes.FosterCarerController.onPageLoad(NormalMode, userAnswers.taxYear)
      case _ => routes.IndexController.onPageLoad(userAnswers.taxYear)
    }
  }

  def childBenefitIncomeRoute(userAnswers: UserAnswers): Call = {
    userAnswers.get(ChildBenefitIncomePage) match {
      case Some(true) => routes.HighIncomeChildBenefitChargeController.onPageLoad(NormalMode, userAnswers.taxYear)
      case Some(false) => routes.FosterCarerController.onPageLoad(NormalMode, userAnswers.taxYear)
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
