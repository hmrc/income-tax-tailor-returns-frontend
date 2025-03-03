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

import scala.concurrent.{ExecutionContext, Future}

/**
 * A helper trait designed to provide some basic boilerplate functionality for handling calls which involve use of
 * pre-population data. See ControllerWithPrePop for a Controller centric implementation of this.
 *
 * @tparam R A generic pre-population data type
 */
trait PrePopulationHelper[R <: PrePopulationResponse[_]] { _: Logging =>

  /**
   * A generic method for attempting to retrieve pre-population data, and performing either an error, or success action
   * depending on whether the attempt is successful or not.
   *
   * @param prePopulationRetrievalAction Function used to attempt to retrieve pre-population data
   * @param successAction Function used to handle a success outcome. R => Result
   * @param errorAction Function used to handle errors. SimpleErrorWrapper => Result
   * @param extraLogContext String used to provide extra context for logging
   * @param dataLog String used for logging. Contains data about the current user
   * @param incomeType String used for logging. Represents the current income type being handled
   * @param ec Execution context. Required for .leftMap on Cats type EitherT to function
   * @return A Result
   */
  protected def blockWithPrePop(prePopulationRetrievalAction: () => ConnectorResponse[R],
                                successAction: R => Result,
                                errorAction: SimpleErrorWrapper => Result,
                                extraLogContext: String,
                                dataLog: String,
                                incomeType: String)
                               (implicit ec: ExecutionContext): Future[Result] = {
    val methodContext: String = "blockWithPrePop"

    val infoLogger: String => Unit = infoLog(
      secondaryContext = methodContext,
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
        secondaryContext = methodContext,
        message = s"Failed to retrieve user's pre-pop data for $incomeType. " +
          s"Received error status: ${err.status} from pre-pop service. Processing error action",
        dataLog = dataLog,
        extraContext = Some(extraLogContext)
      )

      errorAction(err)
    }.merge
  }
}
