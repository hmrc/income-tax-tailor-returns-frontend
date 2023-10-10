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
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}
import scala.concurrent.Future

class UserDataServiceSpec
  extends AnyFreeSpec
    with Matchers
    with MockitoSugar
    with OptionValues
    with ScalaFutures
    with BeforeAndAfterEach {

  private val instant   = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val stubClock = Clock.fixed(instant, ZoneId.systemDefault)
  private val userId    = "foo"
  private val answers   = UserAnswers(userId, Json.obj("bar" -> "baz"), lastUpdated = Instant.now(stubClock))

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val mockConnector = mock[UserAnswersConnector]

  private val service = new UserDataService(mockConnector)

  override def beforeEach(): Unit = {
    reset(mockConnector)
    super.beforeEach()
  }

  ".get" - {

    "must return user answers when they exist in the backend" in {
      when(mockConnector.get()(any())) thenReturn Future.successful(Some(answers))
      service.get().futureValue mustEqual Some(answers)
    }

    "must return None when answers do not exist in the repository" in {

      when(mockConnector.get()(any())) thenReturn Future.successful(None)

      service.get().futureValue must not be defined
    }
  }

  ".set" - {

    "must write the answers to the backend" in {
      when(mockConnector.set(any())(any())) thenReturn Future.successful(Done)

      service.set(answers).futureValue
      verify(mockConnector, times(1)).set(eqTo(answers))(any())
    }

    "must fail when writing to the repository fails" in {

      when(mockConnector.set(any())(any())) thenReturn Future.failed(new RuntimeException("foo"))

      service.set(answers).failed.futureValue
      verify(mockConnector, times(1)).set(eqTo(answers))(any())
    }

  }

  ".keepAlive" - {

    "must keep the backend record alive" in {
      when(mockConnector.keepAlive()(any())) thenReturn Future.successful(Done)

      service.keepAlive().futureValue
      verify(mockConnector, times(1)).keepAlive()(any())
    }
  }

  ".clear" - {

    "must remove the record from the repository" in {
      when(mockConnector.clear()(any())) thenReturn Future.successful(Done)

      service.clear().futureValue
      verify(mockConnector, times(1)).clear()(any())
    }
  }
}
