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
import connectors.IncomeTaxSessionDataConnector
import models.SessionValues
import models.errors.APIErrorModel
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NinoRetrievalService @Inject()(sessionDataConnector: IncomeTaxSessionDataConnector,
                                     config: FrontendAppConfig)
                                    (implicit ec: ExecutionContext) extends Logging {

  override protected val classLoggingContext: String = "NinoRetrievalService"

  private def sessionDataCacheResult(implicit hc: HeaderCarrier): EitherT[Future, APIErrorModel, Option[String]] =
    for {
      sessionOpt <- EitherT(sessionDataConnector.getSessionData)
    } yield {
      sessionOpt.map(_.nino)
    }

  def getNino(extraContext: String)
             (implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): Future[Either[Unit, String]] = {
    val methodLoggingContext: String = "getNino"
    val infoLogger: String => Unit = infoLog(methodLoggingContext, extraContext = Some(extraContext))
    val errorLogger: String => Unit = errorLog(methodLoggingContext, extraContext = Some(extraContext))
    val warnLogger: String => Unit = warnLog(methodLoggingContext, extraContext = Some(extraContext))

    infoLogger("Attempting to retrieve NINO for request")

    lazy val sessionNinoOpt: Either[Unit, String] =
      request
        .session.get(SessionValues.CLIENT_NINO)
        .fold {
          errorLogger("No NINO was found in local session data")
          Left[Unit, String]().withRight
        } { nino =>
          infoLogger(s"Successfully retrieved NINO: $nino from local session data")
          Right(nino)
        }

    val result = if (config.sessionCookieServiceEnabled) {
      infoLogger("Session cookie service is enabled. Attempting to retrieve session data")

      sessionDataCacheResult
        .leftMap(err => errorLogger(
          s"Request to retrieve session data from session cookie service failed with error status: ${err.status} " +
            s"and error body: ${err.body}"
        ))
        .flatMap { ninoOpt =>
          infoLogger("Request to retrieve session data from session cookie service completed successfully")

          EitherT(Future.successful(
            ninoOpt.fold {
              warnLogger("Session cookie service returned an empty session data object")
              Left[Unit, String]().withRight
            }(nino => {
              infoLogger(s"Successfully extracted NINO: $nino from session data response")
              Right(nino)
            })
          ))
        }
        .leftFlatMap{_ =>
          infoLogger("Attempting to extract NINO from local session data as a fallback")
          EitherT(Future.successful(sessionNinoOpt))
        }
    } else {
      infoLogger("Session cookie service is disabled. Attempting to retrieve nino from local session data only")
      EitherT(Future.successful(sessionNinoOpt))
    }

    result.value
  }
}
