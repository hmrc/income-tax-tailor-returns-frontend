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

package controllers.actions

import models.UserAnswers
import models.requests.{IdentifierRequest, OptionalDataRequest}
import play.api.mvc.ActionTransformer

import scala.concurrent.{ExecutionContext, Future}

class FakeDataRetrievalActionProvider(dataToReturn: Option[UserAnswers], isAgent: Boolean)(implicit ec: ExecutionContext) extends DataRetrievalActionProvider {

  override def apply(taxYear: Int): ActionTransformer[IdentifierRequest, OptionalDataRequest] =
    new ActionTransformer[IdentifierRequest, OptionalDataRequest] {
      override protected def transform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] =
        Future.successful(OptionalDataRequest(
          request = request,
          sessionData = request.sessionData,
          userAnswers = dataToReturn,
          isAgent = isAgent
        ))

      override protected def executionContext: ExecutionContext = ec
    }
}