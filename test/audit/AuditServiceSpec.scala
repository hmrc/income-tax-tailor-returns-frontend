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

package audit

import base.SpecBase
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}

import scala.concurrent.Future

class AuditServiceSpec extends AnyFreeSpec
  with SpecBase
  with Matchers
  with MockitoSugar
  with OptionValues
  with ScalaFutures
  with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  val mockedAppName = "some-app-name"


  "auditing an event" - {

    val auditType = "Type"

    val transactionName = "Name"

    val eventDetails = "Details"

    val expected: Future[AuditResult] = Future.successful(Success)

    val mockAuditConnector = mock[AuditConnector]

    val mockConfig: Configuration = mock[Configuration]

    when(mockConfig.get[String](any())(any())) thenReturn mockedAppName

    val underTest = new AuditService(mockAuditConnector, mockConfig)

    "must return a successful audit result" in {

      val event = AuditModel(auditType, transactionName, eventDetails)

      when(mockAuditConnector.sendExtendedEvent(any())(any(), any())) thenReturn expected

      underTest.auditModel(event) mustBe expected

    }
  }
}



