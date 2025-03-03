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

package mocks

import models.{Done, UserAnswers}
import org.scalamock.handlers.CallHandler3
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import services.UserDataService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockUserDataService extends MockFactory {this: TestSuite =>
  val mockUserDataService: UserDataService = mock[UserDataService]

  type MockType = CallHandler3[UserAnswers, UserAnswers, HeaderCarrier, Future[Done]]

  def mockSetUserData(prevAnswers: UserAnswers, newAnswers: UserAnswers): MockType =
    (mockUserDataService
      .set(_: UserAnswers, _: UserAnswers)(_: HeaderCarrier))
      .expects(prevAnswers, newAnswers, *)
      .returning(Future.successful(Done))
}

