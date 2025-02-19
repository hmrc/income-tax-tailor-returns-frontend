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
import connectors.IncomeTaxSessionDataConnector
import models.SessionValues
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NinoRetrievalService @Inject()(sessionDataConnector: IncomeTaxSessionDataConnector,
                                     config: FrontendAppConfig) {
  //TODO: Logging

  def getNinoOpt(request: Request[_])(implicit hc: HeaderCarrier,
                                      ec: ExecutionContext): Future[Option[String]] = {
    lazy val sessionNinoOpt =
      request
        .session
        .get(SessionValues.CLIENT_NINO)

    if (config.sessionCookieServiceEnabled){
      sessionDataConnector
        .getSessionData
        .map {
          case Left(_) =>
            //TODO: a log for this
            sessionNinoOpt
          case Right(value) => value.map(_.nino).orElse(sessionNinoOpt)
        }
    } else {
      Future.successful(sessionNinoOpt)
    }
  }

}
