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

import cats.data.EitherT
import connectors.ConnectorResponse
import models.errors.SimpleErrorWrapper
import models.prePopulation.PrePopulationResponse
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait PrePopulationHelper[R <: PrePopulationResponse] { _: Logging =>

  type PrePopResult = () => ConnectorResponse[R]

  protected def prePopRetrievalAction(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): PrePopResult

  /**
   * A generic boilerplate method to handle retrieving any pre-pop information, and process the outcome
   *
   * N.B - This method has been made private because as far as I can tell all relevant tailoring pages with pre-pop
   *       can simply instead use the doHandleWithPrePop method below which also handles the logic of switching between
   *       different views depending on whether the user is an agent or not. If that proves to be untrue this method
   *       can simply be made protected or public to open up the access.
   *
   * @param prePopulationRetrievalAction Function used to attempt to retrieve information about pre-pop
   * @param successAction Function used to handle a success outcome. Converts pre-pop data to some Result
   * @param errorAction Function used to handle an error outcome. Converts a SimpleErrorWrapper to some Result
   * @param extraLogContext String used for logging. Provides information about where this method was called
   * @param dataLog String used for logging. Contains data about the current user in a log friendly format
   * @param incomeType String used for logging. Represents the current income type being handled, i.e 'state benefits'
   * @param ec Execution context. Required for .leftMap on Cats type EitherT to function
   * @return A Result
   */
  private def blockWithPrePop(prePopulationRetrievalAction: () => ConnectorResponse[R],
                              successAction: R => Result,
                              errorAction: SimpleErrorWrapper => Result,
                              extraLogContext: String,
                              dataLog: String,
                              incomeType: String)
                             (implicit ec: ExecutionContext): Future[Result] = {
    val methodContext: String = "blockWithPrePop"

    val infoLogger = infoLog(
      methodLoggingContext = methodContext,
      dataLog = dataLog,
      extraContext = Some(extraLogContext)
    )

    infoLogger(s"Attempting to retrieve user's pre-pop data for $incomeType")

    val result = for {
      res <- EitherT(prePopulationRetrievalAction())
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

  /**
   * Boilerplate function to handle: retrieving pre-population data for an income type, the result of that retrieval
   * for both individuals and agents, logging, and error handling. Should be applicable for any controller which needs
   * to handle pre-pop. Most of the implementation is held within the blockWithPrePop method above
   *
   * @param isAgent A boolean used to flag that the user is an agent. Affects logging and the success action chosen
   * @param isErrorScenario A boolean used to flag that a request is being made with errors present. Affects logging
   * @param prePopulationRetrievalAction Function used to attempt to retrieve information about pre-pop
   * @param agentSuccessAction Function used to handle a success outcome for agents. Converts pre-pop data into a result
   * @param individualSuccessAction Function used to handle a success outcome for individuals. Same typing as above
   * @param errorAction Function used to handle an error outcome. Converts a SimpleErrorWrapper to some result
   * @param extraLogContext String used for logging. Provides information about where this method was called
   * @param dataLog String used for logging. Contains data about the current user in a log friendly format
   * @param incomeType String used for logging. Represents the current income type being handled, i.e 'state benefits'
   * @param ec Execution context. See scaladoc for blockWithPrePop for usages
   * @return A Result
   */
  protected def blockWithPrePopAndUserType(isAgent: Boolean,
                                           isErrorScenario: Boolean,
                                           prePopulationRetrievalAction: () => ConnectorResponse[R],
                                           agentSuccessAction: R => Result,
                                           individualSuccessAction: R => Result,
                                           errorAction: SimpleErrorWrapper => Result,
                                           extraLogContext: String,
                                           dataLog: String,
                                           incomeType: String)
                                          (implicit ec: ExecutionContext): Future[Result] = {
    val (result, userType) = if (isAgent)(agentSuccessAction, "agent") else (individualSuccessAction, "individual")
    val extraString: String = if(isErrorScenario) " with form errors" else ""

    val methodLoggingContext: String = "handleResult"

    val infoLogger = infoLog(
      methodLoggingContext = methodLoggingContext,
      dataLog = dataLog,
      extraContext = Some(extraLogContext)
    )

    infoLogger(s"Request indicates that user is an $userType. Attempting to return $userType view" + extraString)

    blockWithPrePop(
      prePopulationRetrievalAction = prePopulationRetrievalAction,
      successAction = result,
      errorAction = errorAction,
      extraLogContext = extraLogContext,
      dataLog = dataLog,
      incomeType = incomeType
    )
  }
}
