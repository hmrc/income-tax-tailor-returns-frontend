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

package controllers

import base.SpecBase
import config.FrontendAppConfig
import connectors.ConnectorResponse
import forms.FormProvider
import handlers.ErrorHandler
import mocks.{MockAppConfig, MockErrorHandler, MockSessionDataService, MockUserDataService}
import models.errors.SimpleErrorWrapper
import models.prePopulation.PrePopulationResponse
import models.requests.DataRequest
import models.{Mode, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import pages.QuestionPage
import play.api.data.Form
import play.api.http.{HeaderNames, Status}
import play.api.libs.json.JsPath
import play.api.mvc.Results.Ok
import play.api.mvc._
import play.api.test.Helpers.{await, stubBodyParser, stubMessagesControllerComponents}
import play.api.test.{DefaultAwaitTimeout, FakeRequest, ResultExtractors}
import play.twirl.api.{Html, HtmlFormat}
import services.{SessionDataService, UserDataService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestLogging

import scala.concurrent.{ExecutionContext, Future}

class ControllerWithPrePopSpec extends SpecBase
  with MockSessionDataService
  with MockAppConfig
  with MockErrorHandler
  with MockUserDataService
  with HeaderNames
  with Status
  with ResultExtractors
  with DefaultAwaitTimeout {

  class DummyPrePop(data: String) extends PrePopulationResponse[String] {
    override def toPageModel: String = data
    override def toMessageString(isAgent: Boolean): String = "N/A"
    override val hasPrePop: Boolean = false
  }

  object DummyPrePop {
    val default: DummyPrePop = new DummyPrePop("prePopValue")
  }

  class DummyFormProvider extends FormProvider[String] {
    def apply(isAgent: Boolean): Form[String] = Form("formValue" -> text())
  }

  case object DummyPage extends QuestionPage[String] {
    override def path: JsPath = JsPath \ "dummyPath"
  }

  case class DummyController(prePopResult: Either[SimpleErrorWrapper, DummyPrePop],
                             userAnswers: UserAnswers) extends ControllerWithPrePop[String, DummyPrePop] with TestLogging {
    override val formProvider: FormProvider[String] = new DummyFormProvider
    override val userDataService: UserDataService = mockUserDataService
    override val navigator: Navigator = new FakeNavigator(Call("GET", "/dummyNextPage"))
    override val defaultPrePopulationResponse: DummyPrePop = DummyPrePop.default

    override def prePopRetrievalAction(nino: String, taxYear: Int, mtdItId: String)
                                      (implicit hc: HeaderCarrier): PrePopResult =
      () => Future.successful(prePopResult)

    override def viewProvider(form: Form[_], mode: Mode, taxYear: Int, prePopData: DummyPrePop)
                             (implicit request: DataRequest[_]): HtmlFormat.Appendable = {
      val errsString = if (form.errors.isEmpty) "" else s" with errors: ${form.errors.toString()}"
      HtmlFormat.raw(form.value.map(_.toString).getOrElse("N/A") + errsString)
    }

    override def controllerComponents: MessagesControllerComponents = stubMessagesControllerComponents()

    override implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
    override val ninoRetrievalService: SessionDataService = mockSessionDataService
    override val errorHandler: ErrorHandler = mockErrorHandler
    override val config: FrontendAppConfig = mockAppConfig

    override def actionChain(taxYear: Int,
                             requestOverrideOpt: Option[DataRequest[_]]): ActionBuilder[DataRequest, AnyContent] =
      new ActionBuilder[DataRequest, AnyContent] {
        override def parser: BodyParser[AnyContent] = stubBodyParser(AnyContentAsEmpty)

        override def invokeBlock[A](request: Request[A],
                                    block: DataRequest[A] => Future[Result]): Future[Result] =
          block(DataRequest[A](
            request = request,
            mtdItId = mtdItId,
            userAnswers = userAnswers,
            isAgent = false
          ))

        override def executionContext: ExecutionContext = ec
      }
  }

  trait Test {
    val taxYear: Int = 2024
    val dummyPrePopResult: Either[SimpleErrorWrapper, DummyPrePop] = Right(DummyPrePop.default)
    val dummyUserAnswers: UserAnswers = UserAnswers(mtdItId, taxYear)
    def controller: DummyController = DummyController(dummyPrePopResult, dummyUserAnswers)

    def setupStubs(): Unit = {
      mockPrePopEnabled(true)
      mockGetNino(Right("AA111111A"))
    }
  }

  "blockWithNino" -> {
    val dummyBlock = (_: String, _: () => ConnectorResponse[DummyPrePop], _: DataRequest[_]) =>
      Future.successful(Ok)

    "should process block when pre-population feature flag is off" in new Test {
      mockPrePopEnabled(false)

      val result: Result = await(controller.blockWithNino(
        taxYear = taxYear,
        extraContext = ""
      )(dummyBlock)(FakeRequest()))

      result mustBe Ok
    }

    "should process block when pre-population feature flag is on, and NINO retrieval is successful" in new Test {
      mockPrePopEnabled(true)
      mockGetNino(Right("AA111111A"))

      val result: Result = await(controller.blockWithNino(
        taxYear = taxYear,
        extraContext = ""
      )(dummyBlock)(FakeRequest()))

      result mustBe Ok
    }

    "should return an error when pre-population feature flag is on, and NINO retrieval fails" in new Test {
      mockPrePopEnabled(true)
      mockGetNino(Left())
      mockInternalServerError(Html(""))

      val result: Future[Result] = controller.blockWithNino(
        taxYear = taxYear,
        extraContext = ""
      )(dummyBlock)(FakeRequest())

      status(result) mustBe INTERNAL_SERVER_ERROR
    }

    "should handle exceptions during NINO retrieval" in new Test {
      mockPrePopEnabled(true)
      mockGetNinoException(new RuntimeException())

      val result: Future[Result] = controller.blockWithNino(
        taxYear = taxYear,
        extraContext = ""
      )(dummyBlock)(FakeRequest())

      assertThrows[RuntimeException](await(result))
    }
  }

  "onPageLoad" -> {
    "should return error result when pre-population retrieval fails" in new Test {
      setupStubs()
      mockInternalServerError(Html(""))

      override val dummyPrePopResult: Either[SimpleErrorWrapper, DummyPrePop] = Left(SimpleErrorWrapper(IM_A_TEAPOT))

      val result: Future[Result] = controller.onPageLoad(
        pageName = "N/A",
        incomeType = "N/A",
        page = DummyPage,
        mode = NormalMode,
        taxYear = taxYear
      )(FakeRequest())

      status(result) mustBe INTERNAL_SERVER_ERROR
    }

    "should use journey answers to pre-fill form when they exist and pre-population retrieval succeeds" in new Test {
      setupStubs()

      override val dummyUserAnswers: UserAnswers = UserAnswers(
        mtdItId = mtdItId,
        taxYear = taxYear
      ).set(DummyPage, "setGetValue").get

      val result: Future[Result] = controller.onPageLoad(
        pageName = "N/A",
        incomeType = "N/A",
        page = DummyPage,
        mode = NormalMode,
        taxYear = taxYear
      )(FakeRequest().withSession())

      status(result) mustBe OK
      contentAsString(result) mustBe "setGetValue"
    }

    "should use pre-population data to pre-fill form when only pre-pop exists" in new Test {
      setupStubs()

      val result: Future[Result] = controller.onPageLoad(
        pageName = "N/A",
        incomeType = "N/A",
        page = DummyPage,
        mode = NormalMode,
        taxYear = taxYear
      )(FakeRequest().withSession())

      status(result) mustBe OK
      contentAsString(result) mustBe "prePopValue"
    }
  }

  "onSubmit" -> {
    "when there are no form errors in the request" -> {
      "should redirect to next page when user data service and journey answers update successfully" in new Test {
        mockSetUserData(dummyUserAnswers.set(DummyPage, "validValue").get, dummyUserAnswers)

        val result: Future[Result] = controller.onSubmit(
          pageName = "N/A",
          incomeType = "N/A",
          page = DummyPage,
          mode = NormalMode,
          taxYear = taxYear
        )(FakeRequest().withFormUrlEncodedBody(("formValue", "validValue")))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/dummyNextPage")
      }
    }

    "when form errors exist in the request" -> {
      "should return view with form errors " in new Test {
        val result: Future[Result] = controller.onSubmit(
          pageName = "N/A",
          incomeType = "N/A",
          page = DummyPage,
          mode = NormalMode,
          taxYear = taxYear
        )(FakeRequest())

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe "N/A with errors: List(FormError(formValue,List(error.required),List()))"
      }
    }
  }

}
