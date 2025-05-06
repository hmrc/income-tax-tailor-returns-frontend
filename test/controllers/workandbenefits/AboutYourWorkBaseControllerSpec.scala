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

package controllers.workandbenefits

import base.SpecBase
import controllers.actions._
import models.{CheckMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, when}
import pages.aboutyou.FosterCarerPage
import play.api.Application
import play.api.http.{HeaderNames, Status}
import play.api.mvc.Results.ImATeapot
import play.api.mvc._
import play.api.test.Helpers.stubBodyParser
import play.api.test.{DefaultAwaitTimeout, FakeRequest, ResultExtractors}

import scala.concurrent.{ExecutionContext, Future}

class AboutYourWorkBaseControllerSpec extends SpecBase
  with HeaderNames
  with Status
  with ResultExtractors
  with DefaultAwaitTimeout {

  trait Test {
    val taxYear: Int = 2023
    val mtdItId: String = "12345678"
    val nonRadioResult: String = "Some string result for non foster carers"
    val radioResult: String = "Some string result for foster carers"

    val mockController: AboutYourWorkController = mock[AboutYourWorkController]
    val mockRadioController: AboutYourWorkRadioController = mock[AboutYourWorkRadioController]

    implicit val ec: ExecutionContext = ExecutionContext.global
    val userAnswers: UserAnswers = UserAnswers(mtdItId, taxYear)

    lazy val application: Application = applicationBuilder(Some(userAnswers))
      .build()

    def testController: AboutYourWorkBaseController = new AboutYourWorkBaseController(
      controller = mockController,
      radioController = mockRadioController,
      identify = application.injector.instanceOf[IdentifierActionProvider],
      getData = application.injector.instanceOf[DataRetrievalActionProvider],
      requireData = application.injector.instanceOf[DataRequiredActionProvider]
    )

    val testParser: BodyParser[AnyContent] = stubBodyParser()

    val nonRadioAction: Action[AnyContent] = new Action[AnyContent] {
      override def parser: BodyParser[AnyContent] = testParser
      override def apply(request: Request[AnyContent]): Future[Result] = Future.successful(ImATeapot(nonRadioResult))
      override def executionContext: ExecutionContext = ec
    }

    val radioAction: Action[AnyContent] = new Action[AnyContent] {
      override def parser: BodyParser[AnyContent] = testParser
      override def apply(request: Request[AnyContent]): Future[Result] = Future.successful(ImATeapot(radioResult))
      override def executionContext: ExecutionContext = ec
    }

    val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      .withSession("validTaxYears" -> taxYears.mkString(","))
  }

  "onPageLoad" - {
    "FosterCarer user answers exist and have a value of true" - {
      "should redirect to AboutYourWorkRadioController.onPageLoad for foster carers" in new Test {
        override val userAnswers: UserAnswers = UserAnswers(
          mtdItId = mtdItId,
          taxYear = taxYear
        ).set(FosterCarerPage, true).get

        when(mockRadioController.onPageLoad(any(), any(), any()))
          .thenReturn(radioAction)

        val result: Future[Result] = testController.onPageLoad(CheckMode, taxYear)(request)
        status(result) mustBe IM_A_TEAPOT
        contentAsString(result) mustBe radioResult
      }
    }

    "FosterCarer user answers exist and have a value of false" - {
      "should redirect to AboutYourWorkController.onPageLoad for non foster carers" in new Test {
        override val userAnswers: UserAnswers = UserAnswers(
          mtdItId = mtdItId,
          taxYear = taxYear
        ).set(FosterCarerPage, false).get

        when(mockController.onPageLoad(any(), any(), any()))
          .thenReturn(nonRadioAction)

        val result: Future[Result] = testController.onPageLoad(CheckMode, taxYear)(request)
        status(result) mustBe IM_A_TEAPOT
        contentAsString(result) mustBe nonRadioResult
      }
    }

    "FosterCarer user answers don't exist" - {
      "should redirect to AboutYourWorkController.onPageLoad" in new Test {
        when(mockController.onPageLoad(any(), any(), any()))
          .thenReturn(nonRadioAction)

        val result: Future[Result] = testController.onPageLoad(CheckMode, taxYear)(request)
        status(result) mustBe IM_A_TEAPOT
        contentAsString(result) mustBe nonRadioResult
      }
    }
  }

  "onSubmit" - {
    "FosterCarer user answers exist and have a value of true for a submission" - {
      "should redirect to AboutYourWorkRadioController.onSubmit for a foster carer" in new Test {
        override val userAnswers: UserAnswers = UserAnswers(
          mtdItId = mtdItId,
          taxYear = taxYear
        ).set(FosterCarerPage, true).get

        when(mockRadioController.onSubmit(any(), any(), any()))
          .thenReturn(radioAction)

        val result: Future[Result] = testController.onSubmit(CheckMode, taxYear)(request)
        status(result) mustBe IM_A_TEAPOT
        contentAsString(result) mustBe radioResult
      }
    }

    "FosterCarer user answers exist and have a value of false " - {
      "should redirect to AboutYourWorkController.onSubmit for a non foster carer" in new Test {
        override val userAnswers: UserAnswers = UserAnswers(
          mtdItId = mtdItId,
          taxYear = taxYear
        ).set(FosterCarerPage, false).get

        when(mockController.onSubmit(any(), any(), any()))
          .thenReturn(nonRadioAction)

        val result: Future[Result] = testController.onSubmit(CheckMode, taxYear)(request)
        status(result) mustBe IM_A_TEAPOT
        contentAsString(result) mustBe nonRadioResult
      }
    }

    "FosterCarer user answers don't exist" - {
      "should redirect to AboutYourWorkController.onSubmit" in new Test {
        when(mockController.onSubmit(any(), any(), any()))
          .thenReturn(nonRadioAction)

        val result: Future[Result] = testController.onSubmit(CheckMode, taxYear)(request)
        status(result) mustBe IM_A_TEAPOT
        contentAsString(result) mustBe nonRadioResult
      }
    }
  }

}
