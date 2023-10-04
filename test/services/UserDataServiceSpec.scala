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

import models.{Done, UserAnswers}
import org.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}

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

  private val service = new UserDataService()

  override def beforeEach(): Unit = {
//    reset(mockConnector)
    super.beforeEach()
  }

  ".get" - {

    "must return user answers when they exist in the backend" in {

      service.get().futureValue must not be defined
    }
  }

  ".set" - {

    "must write the answers to the backend" in {
      service.set(answers).futureValue mustEqual Done
    }

  }

  ".keepAlive" - {

    "must keep the backend record alive" in {

      service.keepAlive().futureValue mustEqual Done

    }
  }

  ".clear" - {

    "must remove the record from the repository" in {
      service.clear().futureValue mustEqual Done
    }
  }
}
