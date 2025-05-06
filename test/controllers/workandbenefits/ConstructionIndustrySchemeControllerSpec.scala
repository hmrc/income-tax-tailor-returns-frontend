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

package controllers.workandbenefits

import controllers.{ControllerWithPrePopSpecBase, routes}
import forms.workandbenefits.ConstructionIndustrySchemeFormProvider
import models.prePopulation.CisPrePopulationResponse
import models.{Done, NormalMode, SessionValues, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.workandbenefits.ConstructionIndustrySchemePage
import play.api.inject.guice.GuiceableModule
import play.api.test.Helpers._
import play.api.{Application, inject}
import services.UserDataService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.workandbenefits.{ConstructionIndustrySchemeAgentView, ConstructionIndustrySchemeView}

import scala.concurrent.Future

class ConstructionIndustrySchemeControllerSpec extends
  ControllerWithPrePopSpecBase[ConstructionIndustrySchemeView, ConstructionIndustrySchemeAgentView, Boolean] {

  override def formProvider:ConstructionIndustrySchemeFormProvider = new ConstructionIndustrySchemeFormProvider()

  override val viewProvider: Application => ConstructionIndustrySchemeView =
    (application: Application) => application.injector.instanceOf[ConstructionIndustrySchemeView]

  override val agentViewProvider: Application => ConstructionIndustrySchemeAgentView =
    (application: Application) => application.injector.instanceOf[ConstructionIndustrySchemeAgentView]

  override def requestRoute: String =
    controllers
      .workandbenefits
      .routes
      .ConstructionIndustrySchemeController
      .onPageLoad(NormalMode, taxYear).url

  val expectedConditionalIndividual = "This will be added to your Income Tax Return. To remove this deduction, set it to 0."
  val expectedConditionalAgent = "This will be added to your clients Income Tax Return. To remove this deduction, set it to 0."

  lazy val constructionIndustrySchemeRoute: String = controllers.workandbenefits.routes.ConstructionIndustrySchemeController.onPageLoad(NormalMode, taxYear).url

  trait GETPrePropEnabledIndividual extends GetWithPrePopTest{
    def cis = false
    override def defaultSession: Seq[(String, String)] = Seq(validTaxYears, (SessionValues.CLIENT_NINO, nino))

    when(
      mockCisConnector.getPrePopulation(nino = any, taxYear = any, mtdItId = any)(any[HeaderCarrier])
    ).thenReturn(Future.successful(Right(CisPrePopulationResponse(cis))))

  }
  trait GETPrePropEnabledAgent extends GetWithPrePopAgentTest{
    override def defaultSession: Seq[(String, String)] = Seq(validTaxYears, (SessionValues.CLIENT_NINO, nino))

    when(
      mockCisConnector.getPrePopulation(nino = any, taxYear = any, mtdItId = any)(any[HeaderCarrier])
    ).thenReturn(Future.successful(Right(CisPrePopulationResponse(true))))

  }

  trait PostSubmitWithNoPrePopTest extends SubmitWithNoPrePopTest {

    override def formUrlEncodedBody: (String, String) = ("value", "true")
    val mockUserDataService: UserDataService = mock[UserDataService]
    when(mockUserDataService.set(any(), any())(any())) thenReturn Future
      .successful(Done)

    override def applicationOverrides: Seq[GuiceableModule] = Seq(
      inject.bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
      inject.bind[UserDataService].toInstance(mockUserDataService)
    )
  }

  "ConstructionIndustryScheme Controller" - {
    "GET" - {
      "must return OK and the correct view for a GET for an agent" in new GetWithNoPrePopAgentTest {

        running(application) {
          status(result) mustEqual OK
          contentAsString(result) mustEqual agentView(
            agentForm,
            NormalMode,
            taxYear,
            prePopData = false
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET" in new GETPrePropEnabledIndividual {

        running(application) {
          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            form,
            NormalMode,
            taxYear,
            prePopData = false
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET and isPrePopEnabled true" in new GETPrePropEnabledIndividual {

        running(application) {
          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            form,
            NormalMode,
            taxYear,
            prePopData = false
          )(request, messages(application)).toString
          contentAsString(result) mustNot include(expectedConditionalIndividual)
        }
      }

      "must return OK and the correct view for a GET for an agent and isPrePopEnabled true" in new GETPrePropEnabledIndividual {

        running(application) {
          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            agentForm,
            NormalMode,
            taxYear,
            prePopData = false
          )(request, messages(application)).toString
          contentAsString(result) mustNot include(expectedConditionalAgent)
        }

      }

      "must populate the view correctly on a GET when the question has previously been answered" in new GETPrePropEnabledIndividual {
        override val userAnswers: Option[UserAnswers] = emptyUserAnswers
          .set(ConstructionIndustrySchemePage, true)
          .toOption

        running(application) {
          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            form.fill(true),
            NormalMode,
            taxYear,
            prePopData = false
          )(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered and isPrePopEnabled true" in new GETPrePropEnabledIndividual {
        override def cis = true

        running(application) {

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            form.fill(true),
            NormalMode,
            taxYear,
            prePopData = true
          )(request, messages(application)).toString
          contentAsString(result) must include(expectedConditionalIndividual)
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered for an agent" in new GetWithNoPrePopTest {
        override val userAnswers: Option[UserAnswers] = emptyUserAnswers
          .set(ConstructionIndustrySchemePage, true)
          .toOption

        running(application) {
          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            agentForm.fill(true),
            NormalMode,
            taxYear,
            prePopData = false
          )(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered for an agent " +
        "and isPrePopEnabled true" in new GETPrePropEnabledAgent {

        running(application) {
          status(result) mustEqual OK
          contentAsString(result) mustEqual agentView(
            agentForm.fill(true),
            NormalMode,
            taxYear,
            prePopData = true
          )(request, messages(application)).toString
          contentAsString(result) must include(expectedConditionalAgent)
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in new GETPrePropEnabledIndividual {
        override val userAnswers: Option[UserAnswers] = None
        running(application) {
          status(result) mustEqual SEE_OTHER
          redirectLocation(
            result
          ).value mustEqual routes.JourneyRecoveryController
            .onPageLoad(taxYear = taxYear)
            .url
        }
      }
    }

    "POST" - {
      "must redirect to the next page when valid data is submitted" in new PostSubmitWithNoPrePopTest {

        running(application) {
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }

      }

      "must return a Bad Request and errors when invalid data is submitted" in new PostSubmitWithNoPrePopTest {
        override def formUrlEncodedBody: (String, String) = ("value", "")

        running(application) {
          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(
            boundForm,
            NormalMode,
            taxYear,
            prePopData = false
          )(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in new PostSubmitWithNoPrePopTest {
        override val userAnswers: Option[UserAnswers] = None
        override def formUrlEncodedBody: (String, String) = ("value", "")

        running(application) {
          status(result) mustEqual SEE_OTHER
          redirectLocation(
            result
          ).value mustEqual routes.JourneyRecoveryController
            .onPageLoad(taxYear = taxYear)
            .url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted for an agent" in new SubmitWithNoPrePopAgentTest {
        override def formUrlEncodedBody: (String, String) = ("value", "")
        val mockUserDataService: UserDataService = mock[UserDataService]
        when(mockUserDataService.set(any(), any())(any())) thenReturn Future
          .successful(Done)

        override def applicationOverrides: Seq[GuiceableModule] = Seq(
          inject.bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
          inject.bind[UserDataService].toInstance(mockUserDataService)
        )

        running(application) {
          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual agentView(
            boundForm,
            NormalMode,
            taxYear,
            prePopData = false
          )(request, messages(application)).toString
        }
      }
    }
  }
}
