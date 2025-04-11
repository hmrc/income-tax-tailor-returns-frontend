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

import config.FrontendAppConfig
import connectors.SessionDataConnector
import models.SessionValues
import models.session.SessionData
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SessionDataService @Inject()(sessionDataConnector: SessionDataConnector, config: FrontendAppConfig)
                                  (implicit ec: ExecutionContext) extends Logging {

  override protected val primaryContext: String = "NinoRetrievalService"

  def getFallbackSessionData[A](extraLoggingContext: Option[String])
                               (implicit request: Request[A]): Option[SessionData] = {
    val methodLoggingContext: String = "fallbackSessionData"

    val infoLogger = infoLog(methodLoggingContext, extraContext = extraLoggingContext)
    val warnLogger = warnLog(methodLoggingContext, extraContext = extraLoggingContext)

    if (config.sessionFallbackEnabled) {
      infoLogger("HTTP Session fallback enabled. Attempting to retrieve session data from request")

      val optionalNino = request.session.get(SessionValues.CLIENT_NINO)
      val optionalMtdItId = request.session.get(SessionValues.CLIENT_MTDITID)
      val utr = "" //I don't think we need these right now
      val sessionId = "" //I don't think we need these right now

      (optionalNino, optionalMtdItId) match {
        case (Some(nino), Some(mtdItId)) => Some(SessionData(mtdItId, nino, utr, sessionId))
        case _ =>
          val logString: String =
            (optionalNino.fold(Seq("NINO"))(_ => Seq.empty[String]) ++
              optionalMtdItId.fold(Seq("MTD-IT-ID"))(_ => Seq.empty[String]))
              .mkString(", ")

          warnLogger(s"Could not find $logString in request session. Returning no data")
          None
      }
    } else {
      infoLogger("HTTP Session fallback disabled. Returning no data")
      None
    }
  }

  def getSessionData(extraLoggingContext: Option[String] = None)
                    (implicit hc: HeaderCarrier): Future[Option[SessionData]] = {
    val methodLoggingContext: String = "getSessionData"

    val infoLogger: String => Unit = infoLog(methodLoggingContext, extraContext = extraLoggingContext)
    val errorLogger: String => Unit = errorLog(methodLoggingContext, extraContext = extraLoggingContext)

    infoLogger("Received request to retrieve session data from session cookie service")

    if (config.sessionCookieServiceEnabled) {
      infoLogger("Session cookie service enabled. Attempting to retrieve session data")

      sessionDataConnector.getSessionData.map {
        case Right(sessionDataOpt@Some(_)) =>
          infoLogger("Session data successfully retrieved from session cookie service. Returning data")
          sessionDataOpt
        case Right(None) =>
          warnLog(methodLoggingContext)("Session cookie service returned empty data. Returning None")
          None
        case Left(err) =>
          errorLogger(
            s"Request to retrieve session data failed with error status: ${err.status} and error body: ${err.body}. " +
              s"Returning None"
          )
          None
      }
    } else {
      infoLogger("Session cookie service disabled. Returning error outcome")
      Future.successful(None)
    }
  }
}
