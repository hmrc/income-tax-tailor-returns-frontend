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

package controllers.actions

import base.SpecBase
import models.requests.IdentifierRequest
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest

import scala.concurrent.Future
import scala.util.Random

class TaxYearActionSpec extends SpecBase with MockitoSugar {

  class Harness(taxYear: Int) {
    val provider: TaxYearAction = new TaxYearAction(taxYear)(ec)

    def callTransform[A](request: IdentifierRequest[A]): Future[Either[Result, IdentifierRequest[A]]] = {
      provider.refine(request)
    }
  }

  "TaxYearAction" - {

    val identifierRequest = IdentifierRequest(FakeRequest().withSession(validTaxYears), "mtdItId", isAgent = false)

    "when provided a taxYear INSIDE of a valid range" - {

      "should return a Right containing IdentifierRequest" in {
        val random = new Random()
        val validTaxYear = taxYears(random.nextInt(taxYears.length))

        val action: Harness = new Harness(validTaxYear)

        val result: Either[Result, IdentifierRequest[AnyContentAsEmpty.type]] =
          action.callTransform(identifierRequest).futureValue

        result.isRight mustBe true
      }

    }

    "when provided a taxYear OUTSIDE of a valid range" - {

      "should redirect to Error Page when invalid tax year before the start of range" in {
        val invalidTaxYear = startOfTaxYearRange - 1

        val action: Harness = new Harness(invalidTaxYear)

        val result: Either[Result, IdentifierRequest[AnyContentAsEmpty.type]] =
          action.callTransform(identifierRequest).futureValue

        result mustBe Left(Redirect(controllers.routes.IncorrectTaxYearErrorPageController.onPageLoad(invalidTaxYear)))
      }

      "should redirect to Error Page when invalid tax year after the end of range" in {
        val invalidTaxYear = endOfTaxYearRange + 1

        val action: Harness = new Harness(invalidTaxYear)

        val result: Either[Result, IdentifierRequest[AnyContentAsEmpty.type]] =
          action.callTransform(identifierRequest).futureValue

        result mustBe Left(Redirect(controllers.routes.IncorrectTaxYearErrorPageController.onPageLoad(invalidTaxYear)))
      }

    }

    "when valid taxYear range is not present" - {

      "should redirect to Error Page when there is no in Session data" in {
        val identifierRequestWithoutSessionData = identifierRequest.copy(request = FakeRequest())

        val action: Harness = new Harness(endOfTaxYearRange)

        val result: Either[Result, IdentifierRequest[AnyContentAsEmpty.type]] =
          action.callTransform(identifierRequestWithoutSessionData).futureValue

        result mustBe Left(Redirect(controllers.routes.IncorrectTaxYearErrorPageController.onPageLoad(endOfTaxYearRange)))
      }

    }

  }
}
