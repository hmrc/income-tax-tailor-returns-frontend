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
import uk.gov.hmrc.time.TaxYear

import scala.concurrent.Future

class TaxYearActionSpec extends SpecBase with MockitoSugar {

  class Harness(taxYear: Int) {

    val provider: TaxYearAction = new TaxYearAction(taxYear)(ec)

    def callTransform[A](request: IdentifierRequest[A]): Future[Either[Result, IdentifierRequest[A]]] = {
      provider.refine(request)
    }
  }

  "TaxYearAction" - {

    "when provided a taxYear for the current Year" - {

      "should return a Right containing IdentifierRequest" in {

        val action: Harness = new Harness(TaxYear.current.finishYear)

        val result: Either[Result, IdentifierRequest[AnyContentAsEmpty.type]] =
          action.callTransform(IdentifierRequest(FakeRequest(), "mtdItId", isAgent = false)).futureValue

        result.isRight mustBe true
      }
    }
    "when provided a taxYear for the previous Year" - {

      "should return a Right containing IdentifierRequest" in {

        val action: Harness = new Harness(TaxYear.current.previous.finishYear)

        val result: Either[Result, IdentifierRequest[AnyContentAsEmpty.type]] =
          action.callTransform(IdentifierRequest(FakeRequest(), "mtdItId", isAgent = false)).futureValue

        result.isRight mustBe true
      }
    }
    "when provided a taxYear outside of current or end of year" - {

      "should redirect to Error Page when invalid tax year is used" in {

        val invalidTaxYear = 2020

        val action: Harness = new Harness(invalidTaxYear)

        val result: Either[Result, IdentifierRequest[AnyContentAsEmpty.type]] =
          action.callTransform(IdentifierRequest(FakeRequest(), "mtdItId", isAgent = false)).futureValue

        result mustBe Left(Redirect(controllers.routes.IncorrectTaxYearErrorPageController.onPageLoad(invalidTaxYear)))

      }
    }
  }
}
