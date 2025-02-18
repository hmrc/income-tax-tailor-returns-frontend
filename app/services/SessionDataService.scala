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
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class SessionDataService @Inject()(sessionDataConnector: IncomeTaxSessionDataConnector,
                                   config: FrontendAppConfig) {

  def getSessionData(request: Request[_])(implicit hc: HeaderCarrier,
                                          ec: ExecutionContext) = {
    if (config.sessionCookieServiceEnabled){
      sessionDataConnector
        .getSessionData
        .map {
          case Left(_) => ??? //TODO figure out what to do when this fails
          case Right(value) => value //TODO figure out what to do when the session data is 'None'
        }
    } else {
      ???
      //TODO figure out an alternative way to acquire the user's NINO
    }
  }

}
