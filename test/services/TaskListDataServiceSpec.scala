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
import connectors.TaskListDataConnector
import models.Done
import models.aboutyou.UkResidenceStatus
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import pages.aboutyou.UkResidenceStatusPage
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class TaskListDataServiceSpec
  extends AnyFreeSpec
    with SpecBase
    with Matchers
    with MockitoSugar
    with OptionValues
    with ScalaFutures
    with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private val mockConnector = mock[TaskListDataConnector]
  private val service = new TaskListDataService(mockConnector)

  override implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  override def beforeEach(): Unit = {
    reset(mockConnector)
    super.beforeEach()
  }

  ".set" - {

    "must write task list data to the backend when submitted" in {

      when[Future[Done]](mockConnector.set(any())(any())) thenReturn Future.successful(Done)

      service.set(emptyUserAnswers).futureValue mustEqual Done
    }

    "must write task list data to the backend when submitted with domiciled" in {

      when[Future[Done]](mockConnector.set(any())(any())) thenReturn Future.successful(Done)

      service.set(emptyUserAnswers.set(UkResidenceStatusPage, UkResidenceStatus.Domiciled).get).futureValue mustEqual Done
    }

    "must write task list data to the backend when submitted with all values present" in {

      when[Future[Done]](mockConnector.set(any())(any())) thenReturn Future.successful(Done)

      service.set(fullUserAnswers).futureValue mustEqual Done
    }

  }
}
