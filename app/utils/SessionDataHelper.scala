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

import models.session.SessionData
import play.api.mvc.{Request, Result}
import services.SessionDataService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait SessionDataHelper {_: Logging =>
  val sessionDataService: SessionDataService

  def getSessionDataBlock[A](errorAction: () => Future[Result])
                            (block: SessionData => Future[Result])
                            (implicit request: Request[A], hc: HeaderCarrier, ec: ExecutionContext): Future[Result] = {
    import sessionDataService.{getSessionData, getFallbackSessionData}

    val methodLoggingContext: String = "sessionDataBlock"

    val infoLogger = infoLog(methodLoggingContext)
    val warnLogger = warnLog(methodLoggingContext)
    val errorLogger = errorLog(methodLoggingContext)

    infoLogger("Received request to handle session data block")

    getSessionData(Some(methodLoggingContext))
      .flatMap(
        _.orElse {
            warnLogger("Could not retrieve session data from session cookie service. Attempting HTTP session fallback")
            getFallbackSessionData(Some(methodLoggingContext))
          }
          .fold {
            errorLogger("Could not retrieve session data from HTTP session fallback. Processing error action")
            errorAction()
          }(data => {
            infoLogger("Successfully retrieved session data for user. Invoking block action")
            block(data)
          })
      )
  }
}
