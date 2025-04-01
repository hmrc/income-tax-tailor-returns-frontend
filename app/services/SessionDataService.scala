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

package services

import cats.data.EitherT
import config.FrontendAppConfig
import connectors.SessionDataConnector
import models.SessionValues
import models.errors.APIErrorModel
import models.session.SessionData
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SessionDataService @Inject()(sessionDataConnector: SessionDataConnector,
                                   config: FrontendAppConfig)
                                  (implicit ec: ExecutionContext) extends Logging {

  override protected val primaryContext: String = "NinoRetrievalService"

  private def sessionDataCacheResult(implicit hc: HeaderCarrier): EitherT[Future, APIErrorModel, Option[SessionData]] =
    EitherT(sessionDataConnector.getSessionData)

  protected[services] def sessionValOpt(key: String,
                                        valName: String,
                                        infoLogger: String => Unit,
                                        errorLogger: String => Unit)
                                       (implicit request: Request[_]): Either[Unit, String] =
    request
      .session.get(key)
      .fold {
        errorLogger(s"No $valName was found in local session data")
        Left[Unit, String]().withRight
      } { sessionVal =>
        infoLogger(s"Successfully retrieved $valName: $sessionVal from local session data")
        Right(sessionVal)
      }

  // TODO: Remove this method once session data is properly integrated into request models
  def getNino()(implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): Future[Either[Unit, String]] = {
    val methodLoggingContext: String = "getNino"
    val infoLogger: String => Unit = infoLog(methodLoggingContext)
    val errorLogger: String => Unit = errorLog(methodLoggingContext)
    val warnLogger: String => Unit = warnLog(methodLoggingContext)

    infoLogger("Attempting to retrieve NINO for request")

    lazy val sessionNinoOpt: Either[Unit, String] = sessionValOpt(
      key = SessionValues.CLIENT_NINO,
      valName = "NINO",
      infoLogger = infoLogger,
      errorLogger = errorLogger
    )

    val result = if (config.sessionCookieServiceEnabled) {
      infoLogger("Session cookie service is enabled. Attempting to retrieve session data")

      sessionDataCacheResult
        .leftMap(err => errorLogger(
          s"Request to retrieve session data failed with error status: ${err.status} and error body: ${err.body}"
        ))
        .flatMap { sessionDataOpt =>
          infoLogger("Request to retrieve session data from session cookie service completed successfully")

          EitherT(Future.successful(
            sessionDataOpt.fold {
              warnLogger("Session cookie service returned an empty session data object")
              Left[Unit, String]().withRight
            }(sessionData => {
              infoLogger(s"Successfully extracted NINO: ${sessionData.nino} from session data response")
              Right(sessionData.nino)
            })
          ))
        }
        .leftFlatMap{_ =>
          infoLogger("Attempting to extract NINO from local session data as a fallback")
          EitherT(Future.successful(sessionNinoOpt))
        }
    } else {
      infoLogger("Session cookie service is disabled. Attempting to retrieve NINO from local session data only")
      EitherT(Future.successful(sessionNinoOpt))
    }

    result.value
  }

  def getSessionData()(implicit hc: HeaderCarrier): Future[Either[Unit, SessionData]] = {
    val methodLoggingContext: String = "getSessionData"
    val infoLogger: String => Unit = infoLog(methodLoggingContext)
    val errorLogger: String => Unit = errorLog(methodLoggingContext)

    infoLogger("Attempting to retrieve session data for request")

    if (config.sessionCookieServiceEnabled) {
      infoLogger("Session cookie service is enabled. Attempting to retrieve session data")

      sessionDataCacheResult
        .leftMap(err => errorLogger(
          s"Request to retrieve session data failed with error status: ${err.status} and error body: ${err.body}"
        ))
        .subflatMap(_.fold {
          warnLog(methodLoggingContext)("Session cookie service returned empty data. Returning error outcome")
          Left().withRight[SessionData]
        }(
          data => {
            infoLogger("Session data successfully retrieved from session cookie service. Returning data")
            Right(data)
          }
        )
        ).value
    } else {
      infoLogger("Session cookie service disabled. Returning error outcome")
      Future.successful(Left())
    }
  }
}
