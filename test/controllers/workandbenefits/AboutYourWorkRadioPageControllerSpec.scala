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

import controllers.ControllerWithPrePopSpecBase
import forms.workandbenefits.AboutYourWorkRadioPageFormProvider
import models.prePopulation.EmploymentPrePopulationResponse
import models.{Done, NormalMode, SessionValues}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.guice.GuiceableModule
import play.api.test.Helpers._
import play.api.{Application, inject}
import services.UserDataService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.workandbenefits.{AboutYourWorkRadioPageAgentView, AboutYourWorkRadioPageView}

import scala.concurrent.Future

class AboutYourWorkRadioControllerSpec extends ControllerWithPrePopSpecBase[AboutYourWorkRadioPageView, AboutYourWorkRadioPageAgentView, Boolean] {

  override def formProvider: AboutYourWorkRadioPageFormProvider = new AboutYourWorkRadioPageFormProvider()

  override val viewProvider: Application => AboutYourWorkRadioPageView =
    (application: Application) => application.injector.instanceOf[AboutYourWorkRadioPageView]

  override val agentViewProvider: Application => AboutYourWorkRadioPageAgentView =
    (application: Application) => application.injector.instanceOf[AboutYourWorkRadioPageAgentView]

  override def requestRoute: String =
    controllers.workandbenefits.routes.AboutYourWorkBaseController
      .onPageLoad(NormalMode, taxYear).url

  lazy val aboutYourWorkRoute: String = requestRoute

  trait GETPrePopEnabledIndividual extends GetWithPrePopTest {
    def hasEmployment = false

    mockSessionDataConnectorGet(Future.successful(Right(Some(dummySessionData))))
    override def defaultSession: Seq[(String, String)] = Seq(validTaxYears, (SessionValues.CLIENT_NINO, nino))

    when(
      mockEmploymentConnector.getPrePopulation(nino = any, taxYear = any, mtdItId = any)(any[HeaderCarrier])
    ).thenReturn(Future.successful(Right(EmploymentPrePopulationResponse(hasEmployment))))
  }

  trait GETPrePopEnabledAgent extends GetWithPrePopAgentTest {
    mockSessionDataConnectorGet(Future.successful(Right(Some(dummySessionData))))
    override def defaultSession: Seq[(String, String)] = Seq(validTaxYears, (SessionValues.CLIENT_NINO, nino))

    when(
      mockEmploymentConnector.getPrePopulation(nino = any, taxYear = any, mtdItId = any)(any[HeaderCarrier])
    ).thenReturn(Future.successful(Right(EmploymentPrePopulationResponse(true))))
  }

  trait PostSubmitWithNoPrePopTest extends SubmitWithNoPrePopTest {
    override def formUrlEncodedBody: (String, String) = ("value", "true")

    val mockUserDataService: UserDataService = mock[UserDataService]
    when(mockUserDataService.set(any(), any())(any())) thenReturn Future.successful(Done)

    override def applicationOverrides: Seq[GuiceableModule] = Seq(
      inject.bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
      inject.bind[UserDataService].toInstance(mockUserDataService)
    )
  }

  "AboutYourWorkRadio Controller" - {
    "GET" - {
      "must return OK and the correct view for an agent" in new GetWithNoPrePopAgentTest {
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
    }

    "must return OK and the correct view for an individual" in new GETPrePopEnabledIndividual {
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
  }
}

//      "must populate the view correctly when previously answered" in new GETPrePopEnabledIndividual {
//        override val userAnswers = emptyUserAnswers.set(AboutYourWorkRadioPage, true).toOption
//
//        running(application) {
//          status(result) mustEqual OK
//          contentAsString(result) mustEqual view(
//            form.fill(true),
//            NormalMode,
//            taxYear,
//            prePopData = false
//          )(request, messages(application)).toString
//        }
//      }

//      "must redirect to Journey Recovery if no data is found" in new GETPrePopEnabledIndividual {
//        override val userAnswers = None
//
//        running(application) {
//          status(result) mustEqual SEE_OTHER
//          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad(???, taxYear).url
//        }
//      }
//    }

//    "POST" - {
      //      "must redirect on valid submission" in new PostSubmitWithNoPrePopTest {
      //        running(application) {
      //          status(result) mustEqual SEE_OTHER
      //          redirectLocation(result).value mustEqual onwardRoute.url
      //        }
      //      }

      //      "must return BAD_REQUEST on invalid submission" in new PostSubmitWithNoPrePopTest {
      //        override def formUrlEncodedBody: (String, String) = ("value", "")
      //
      //        running(application) {
      //          status(result) mustEqual BAD_REQUEST
      //          contentAsString(result) mustEqual view(
      //            boundForm,
      //            NormalMode,
      //            taxYear,
      //            prePopData = false
      //          )(request, messages(application)).toString
      //        }
      //      }

      //      "must redirect to Journey Recovery if no data is found" in new PostSubmitWithNoPrePopTest {
      //        override val userAnswers = None
      //        override def formUrlEncodedBody: (String, String) = ("value", "true")
      //
      //        running(application) {
      //          status(result) mustEqual SEE_OTHER
      //          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad(???, taxYear).url
      //        }
      //      }

      //      "must return BAD_REQUEST with errors for an agent on invalid submission" in new SubmitWithNoPrePopAgentTest {
      //        override def formUrlEncodedBody: (String, String) = ("value", "")
      //
      //        val mockUserDataService: UserDataService = mock[UserDataService]
      //        when(mockUserDataService.set(any(), any())(any())) thenReturn Future.successful(Done)
      //
      //        override def applicationOverrides: Seq[GuiceableModule] = Seq(
      //          inject.bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
      //          inject.bind[UserDataService].toInstance(mockUserDataService)
      //        )
      //
      //        running(application) {
      //          status(result) mustEqual BAD_REQUEST
      //          contentAsString(result) mustEqual agentView(
      //            boundForm,
      //            NormalMode,
      //            taxYear,
      //            prePopData = false
      //          )(request, messages(application)).toString
      //        }
      //      }
      //    }
//    }
