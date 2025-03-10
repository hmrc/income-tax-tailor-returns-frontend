/*
 * Copyright 2025 HM Revenue & Customs
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

package utils

import base.SpecBase
import models.errors.SimpleErrorWrapper
import models.prePopulation.PrePopulationResponse
import play.api.http.{HeaderNames, Status => StatusCodes}
import play.api.mvc.Result
import play.api.mvc.Results.Status
import play.api.test.Helpers.await
import play.api.test.{DefaultAwaitTimeout, ResultExtractors}

import scala.concurrent.Future

class PrePopulationHelperSpec extends SpecBase
  with DefaultAwaitTimeout
  with HeaderNames
  with StatusCodes
  with ResultExtractors {

  case class DummyPrePop() extends PrePopulationResponse[Unit] {
    override def toPageModel: Unit = ()
    override def toMessageString(isAgent: Boolean): String = "N/A"
    override val hasPrePop: Boolean = true
  }

  trait Test extends PrePopulationHelper[DummyPrePop] with TestLogging {
    val successAction: DummyPrePop => Result = (_: DummyPrePop) => new Status(OK)
    val errorAction: SimpleErrorWrapper => Result = (_: SimpleErrorWrapper) => new Status(INTERNAL_SERVER_ERROR)
  }

  "blockWithPrePop" -> {
    "when pre-population retrieval call returns an error should process error action" in new Test {
      val result: Future[Result] = blockWithPrePop(
        () => Future.successful(Left(SimpleErrorWrapper(INTERNAL_SERVER_ERROR))),
        successAction = successAction,
        errorAction = errorAction,
        extraLogContext = "N/A",
        dataLog = "N/A",
        incomeType = "N/A"
      )

      status(result) mustBe INTERNAL_SERVER_ERROR
    }

    "when pre-population retrieval call returns pre-pop data should process success action" in new Test {
      val result: Future[Result] = blockWithPrePop(
        () => Future.successful(Right(DummyPrePop())),
        successAction,
        errorAction,
        extraLogContext = "N/A",
        dataLog = "N/A",
        incomeType = "N/A"
      )

      status(result) mustBe OK
    }

    "when pre-population retrieval call returns an exception should throw an exception" in new Test {
      val result: Future[Result] = blockWithPrePop(
        () => Future.failed(new RuntimeException()),
        successAction,
        errorAction,
        extraLogContext = "N/A",
        dataLog = "N/A",
        incomeType = "N/A"
      )

      assertThrows[RuntimeException](await(result))
    }
  }

}
