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
import models.aboutyou.{UkResidenceStatus, YourResidenceStatus}
import pages._
import pages.aboutyou._

import scala.concurrent.ExecutionContext

class PrivateBetaNavigatorSpec extends SpecBase {

  val navigator = new PrivateBetaNavigator

  "PrivateBetaNavigator" - {

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

      "must go from CharitableDonations page to FosterCarer page when any value is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(CharitableDonationsPage, Set(CharitableDonations.values.head)).success.value

        val expectedRoute = controllers.aboutyou.routes.FosterCarerController.onPageLoad(NormalMode, taxYear)

        navigator.nextPage(CharitableDonationsPage, NormalMode, answers, taxYear) mustBe expectedRoute
      }

      "must go from FosterCarer page to AddSections page when true is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(FosterCarerPage, true).success.value

        val expectedRoute = routes.AddSectionsController.onPageLoad(taxYear)

        navigator.nextPage(FosterCarerPage, NormalMode, answers, taxYear) mustBe expectedRoute
      }

      "must go from FosterCarer page to AddSections page when false is selected" in {
        val answers = UserAnswers(mtdItId, taxYear).set(FosterCarerPage, false).success.value

        val expectedRoute = routes.AddSectionsController.onPageLoad(taxYear)

        navigator.nextPage(FosterCarerPage, NormalMode, answers, taxYear) mustBe expectedRoute
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
