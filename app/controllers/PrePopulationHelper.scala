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

package controllers

import cats.data.EitherT
import connectors.ConnectorResponse
import models.errors.SimpleErrorWrapper
import models.prePopulation.PrePopulationResponse
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging

import scala.concurrent.{ExecutionContext, Future}

trait PrePopulationHelper[R <: PrePopulationResponse] { _: Logging =>

  def prePopRetrievalAction(nino: String, taxYear: Int)
                           (implicit hc: HeaderCarrier): () => ConnectorResponse[R]

  protected def blockWithPrePop(nino: String,
                                taxYear: Int,
                                successAction: R => Result,
                                errorAction: SimpleErrorWrapper => Result,
                                extraLogContext: String,
                                dataLog: String,
                                incomeType: String)
                               (implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Result] = {
    val methodContext: String = "blockWithPrePop"

    val infoLogger = infoLog(
      methodLoggingContext = methodContext,
      dataLog = dataLog,
      extraContext = Some(extraLogContext)
    )

    def aa = prePopRetrievalAction(nino, taxYear)

    infoLogger(s"Attempting to retrieve user's pre-pop data for $incomeType")

    val result = for {
      res <- EitherT(aa())
    } yield {
      infoLogger(s"Successfully retrieved user's pre-pop data for $incomeType. Processing success action")
      successAction(res)
    }

    result.leftMap { err =>
      logger.error(
        methodContext = methodContext,
        message = s"Failed to retrieve user's pre-pop data for $incomeType." +
          s"Received error status: ${err.status} from pre-pop service. Processing error action",
        dataLog = dataLog,
        extraContext = Some(extraLogContext)
      )

      errorAction(err)
    }.merge
  }

  protected def doHandleWithPrePop(nino: String,
                                   taxYear: Int,
                                   isAgent: Boolean,
                                   isErrorScenario: Boolean,
                                   agentSuccessAction: R => Result,
                                   individualSuccessAction: R => Result,
                                   errorAction: SimpleErrorWrapper => Result,
                                   dataLog: String,
                                   extraLogContext: String)
                                  (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Result] = {
    val (result, userType) = if (isAgent)(agentSuccessAction, "agent") else (individualSuccessAction, "individual")
    val extraString: String = if(isErrorScenario) " with form errors" else ""

    val infoLogger = infoLog(
      methodLoggingContext = "handleResult",
      dataLog = dataLog,
      extraContext = Some(extraLogContext)
    )

    infoLogger(s"Request indicates that user is an $userType. Attempting to return $userType view" + extraString)

    blockWithPrePop(
      nino,
      taxYear,
      successAction = {
        infoLogger(s"Returning $userType view" + extraString)
        result
      },
      errorAction = {
        //TODO: Log
        errorAction
      },
      extraLogContext = extraLogContext,
      dataLog = dataLog,
      incomeType = "state benefits"
    )
  }
}
