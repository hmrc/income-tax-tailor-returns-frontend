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

package models.requests

import models.UserAnswers
import play.api.mvc.{Request, WrappedRequest}

case class OptionalDataRequest[A] (request: Request[A],
                                   mtdItId: String,
                                   userAnswers: Option[UserAnswers],
                                   isAgent: Boolean) extends WrappedRequest[A](request)

case class DataRequest[A] (request: Request[A],
                           mtdItId: String,
                           userAnswers: UserAnswers,
                           isAgent: Boolean) extends WrappedRequest[A](request)

case class DataRequestWithNino[A] (request: Request[A],
                                   mtdItId: String,
                                   userAnswers: UserAnswers,
                                   isAgent: Boolean,
                                   nino: String) extends WrappedRequest[A](request)

object DataRequestWithNino {
  def apply[A](dataRequest: DataRequest[A], nino: String): DataRequestWithNino[A] =
    DataRequestWithNino[A](
      dataRequest.request,
      dataRequest.mtdItId,
      dataRequest.userAnswers,
      dataRequest.isAgent,
      nino
    )
}
