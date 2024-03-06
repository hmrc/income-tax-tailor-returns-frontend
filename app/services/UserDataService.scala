/*
 * Copyright 2023 HM Revenue & Customs
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

import connectors.UserAnswersConnector
import models.{Done, UserAnswers}
import play.api.Logging
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.Future

class UserDataService @Inject()(connector: UserAnswersConnector) extends Logging {

  def get(mtdItId: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[Option[UserAnswers]] = {
    connector.get(mtdItId, taxYear)
  }

  def set(answers: UserAnswers)(implicit hc: HeaderCarrier): Future[Done] = {
    connector.set(answers.copy(data = JsObject(answers.data.fields ++ Seq("isUpdate" -> Json.toJson(true)))))
  }

  def setWithoutUpdate(answers: UserAnswers)(implicit hc: HeaderCarrier): Future[Done] = {
    connector.set(answers)
  }

  def keepAlive(mtdItId: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[Done] = {
    connector.keepAlive(mtdItId, taxYear)
  }

  def clear(mtdItId: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[Done] =
    connector.clear(mtdItId, taxYear)
}
