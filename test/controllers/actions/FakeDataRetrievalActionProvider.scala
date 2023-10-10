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
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, when}
import org.scalatestplus.mockito.MockitoSugar
import services.UserDataService

import scala.concurrent.{ExecutionContext, Future}

//class FakeDataRetrievalAction(dataToReturn: Option[UserAnswers]) extends DataRetrievalActionProvider {
//
//  override protected def transform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] =
//    Future(OptionalDataRequest(request.request, request.userId, dataToReturn, request.isAgent))
//
//  override protected implicit val executionContext: ExecutionContext =
//    scala.concurrent.ExecutionContext.Implicits.global
//}

class FakeDataRetrievalActionProvider(taxYear: Int,
                                      dataToReturn: Option[UserAnswers],
                                      userDataService: UserDataService = mock[UserDataService]) extends DataRetrievalActionProvider with MockitoSugar {

  implicit val executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  when(userDataService.get(any())(any())).thenReturn(Future.successful(dataToReturn))

  override def apply(taxYear: Int) = new DataRetrievalActionImpl(userDataService, taxYear)
}