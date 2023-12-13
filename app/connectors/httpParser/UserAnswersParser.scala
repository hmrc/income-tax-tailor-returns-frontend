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

package connectors.httpParser

import models.UserAnswers
import models.errors.HttpError
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object UserAnswersParser extends HttpParser {
  type UserAnswersResponse = Either[HttpError, UserAnswers]

  override val parserName: String = "UserAnswersParser"

  implicit object UserAnswersHttpReads extends HttpReads[UserAnswersResponse] {

    override def read(method: String, url: String, response: HttpResponse): UserAnswersResponse =
      response.status match {
        case OK =>
          response.json
            .validate[UserAnswers]
            .fold[UserAnswersResponse](_ => Left(nonModelValidatingJsonFromAPI), parsedModel => Right(parsedModel))

        case _ => Left(pagerDutyError(response))
      }
  }
}
