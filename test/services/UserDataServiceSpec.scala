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

import base.SpecBase
import connectors.UserAnswersConnector
import models.{Done, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}
import scala.concurrent.{ExecutionContext, Future}

class UserDataServiceSpec
  extends AnyFreeSpec
    with SpecBase
    with Matchers
    with MockitoSugar
    with OptionValues
    with ScalaFutures
    with BeforeAndAfterEach {

  private val instant   = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val stubClock = Clock.fixed(instant, ZoneId.systemDefault)
  private val userId    = "foo"
  private def answers(taxYear: Int)   = UserAnswers(userId, taxYear, Json.obj("bar" -> "baz"), lastUpdated = Instant.now(stubClock))

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private val mockConnector = mock[UserAnswersConnector]
  private val service = new UserDataService(mockConnector)

  override def beforeEach(): Unit = {
    reset(mockConnector)
    super.beforeEach()
  }

  ".get" - {

    "must return user answers when they exist in the backend" in {

      when[Future[Option[UserAnswers]]](mockConnector.get(any())(any()))
        .thenReturn(Future.successful(Some(UserAnswers("anMtdItId", taxYear, Json.obj("aPage" -> "aValue")))))

      service.get(taxYear).futureValue mustBe defined
    }
  }

  ".set" - {

    "must write the answers to the backend" in {

      when[Future[Done]](mockConnector.set(any())(any())) thenReturn Future.successful(Done)

      service.set(answers(taxYear)).futureValue mustEqual Done
    }

  }

  ".keepAlive" - {

    "must keep the backend record alive" in {

      when[Future[Done]](mockConnector.keepAlive(any())(any())) thenReturn Future.successful(Done)

      service.keepAlive(taxYear).futureValue mustEqual Done
    }
  }

  ".clear" - {

    "must remove the record from the repository" in {

      when[Future[Done]](mockConnector.clear(any())(any())) thenReturn Future.successful(Done)

      service.clear(taxYear).futureValue mustEqual Done
    }
  }
  override implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
}
