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
import models.errors.{APIErrorBodyModel, APIErrorModel, SimpleErrorWrapper}
import models.prePopulation.EmploymentPrePopulationResponse
import models.{Done, NormalMode, SessionValues, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.aboutyou.FosterCarerPage
import pages.workandbenefits.AboutYourWorkRadioPage
import play.api.data.Form
import play.api.inject.guice.GuiceableModule
import play.api.test.Helpers._
import play.api.{Application, inject}
import services.UserDataService
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

  val baseUserAnswers: UserAnswers = emptyUserAnswers.set(FosterCarerPage, true).get

  "AboutYourWorkRadioController" -> {
    "when trying to retrieve the view with a GET" -> {
      "when pre-population is disabled" -> {
        "[GET] should return expected view when no user answers exist" in new GetWithNoPrePopTest {
          override val userAnswers: Option[UserAnswers] = Some(baseUserAnswers)

          running(application) {
            status(result) mustEqual OK

            contentAsString(result) mustEqual
              view(
                form = form,
                mode = NormalMode,
                taxYear = taxYear,
                prePopData = false
              )(request, messages(application)).toString
          }
        }

        "[GET] should return expected view when no user answers exist for an agent" in new GetWithNoPrePopAgentTest {
          override val userAnswers: Option[UserAnswers] = Some(baseUserAnswers)

          running(application) {
            status(result) mustEqual OK

            contentAsString(result) mustEqual
              agentView(
                form = form,
                mode = NormalMode,
                taxYear = taxYear,
                prePopData = false
              )(request, messages(application)).toString
          }
        }

        "[GET] should return expected view when user answers exist" in new GetWithNoPrePopTest {
          override val userAnswers: Option[UserAnswers] = Some(
            baseUserAnswers.set(
              page = AboutYourWorkRadioPage,
              value = false
            ).get
          )

          val filledForm: Form[Boolean] = form.fill(false)

          running(application) {
            status(result) mustEqual OK

            contentAsString(result) mustEqual
              view(
                form = filledForm,
                mode = NormalMode,
                taxYear = taxYear,
                prePopData = false
              )(request, messages(application)).toString
          }
        }

        "[GET] should return expected view when user answers exist for an agent" in new GetWithNoPrePopAgentTest {
          override val userAnswers: Option[UserAnswers] = Some(
            baseUserAnswers.set(
              page = AboutYourWorkRadioPage,
              value = false
            ).get
          )

          val filledForm: Form[Boolean] = form.fill(false)

          running(application) {
            status(result) mustEqual OK

            contentAsString(result) mustEqual
              agentView(
                form = filledForm,
                mode = NormalMode,
                taxYear = taxYear,
                prePopData = false
              )(request, messages(application)).toString
          }
        }
      }

      "when pre-population is enabled" -> {
        "handle errors during NINO retrieval" -> {
          "[GET] should show error page - SessionDataService: disabled, Nino in session: false" in new GetWithPrePopTest {
            override def sessionCookieServiceEnabled: Boolean = false
            override val userAnswers: Option[UserAnswers] = Some(baseUserAnswers)

            running(application) {
              status(result) mustEqual INTERNAL_SERVER_ERROR
              contentAsString(result) mustEqual mockErrorView
            }
          }

          "[GET] should show error page - SessionDataService: returns error, Nino in session: false" in new GetWithPrePopTest {
            override val userAnswers: Option[UserAnswers] = Some(baseUserAnswers)

            mockSessionDataConnectorGet(Future.successful(Left(
              APIErrorModel(IM_A_TEAPOT, APIErrorBodyModel("", "")))
            ))

            running(application) {
              status(result) mustEqual INTERNAL_SERVER_ERROR
              contentAsString(result) mustEqual mockErrorView
            }
          }

          "[GET] should show error page - SessionDataService: returns None, Nino in session: false" in new GetWithPrePopTest {
            override val userAnswers: Option[UserAnswers] = Some(baseUserAnswers)
            mockSessionDataConnectorGet(Future.successful(Right(None)))

            running(application) {
              status(result) mustEqual INTERNAL_SERVER_ERROR
              contentAsString(result) mustEqual mockErrorView
            }
          }
        }

        "handle session NINO fallback" -> {
          "[GET] should fallback to session NINO when session data service is not enabled" in new GetWithPrePopTest {
            override val sessionCookieServiceEnabled: Boolean = false
            override val userAnswers: Option[UserAnswers] = Some(baseUserAnswers)
            override val defaultSession: Seq[(String, String)] = Seq(validTaxYears, (SessionValues.CLIENT_NINO, nino))

            mockEmploymentsConnectorGet(
              result = Future.successful(Right(EmploymentPrePopulationResponse(
                hasEmployment = false
              )))
            )

            running(application) {
              status(result) mustEqual OK

              contentAsString(result) mustEqual
                view(
                  form = form,
                  mode = NormalMode,
                  taxYear = taxYear,
                  prePopData = false
                )(request, messages(application)).toString
            }
          }

          "[GET] should fallback to session NINO when session data service returns an error" in new GetWithPrePopTest {
            override val userAnswers: Option[UserAnswers] = Some(baseUserAnswers)
            mockSessionDataConnectorGet(Future.successful(Right(Some(dummySessionData))))

            override val defaultSession: Seq[(String, String)] = Seq(validTaxYears, (SessionValues.CLIENT_NINO, nino))

            mockEmploymentsConnectorGet(
              result = Future.successful(Right(EmploymentPrePopulationResponse(
                hasEmployment = false
              )))
            )

            running(application) {
              status(result) mustEqual OK

              contentAsString(result) mustEqual
                view(
                  form = form,
                  mode = NormalMode,
                  taxYear = taxYear,
                  prePopData = false
                )(request, messages(application)).toString
            }
          }

          "[GET] should fallback to session NINO when session data service returns None" in new GetWithPrePopTest {
            override val userAnswers: Option[UserAnswers] = Some(baseUserAnswers)
            mockSessionDataConnectorGet(Future.successful(Right(None)))

            override val defaultSession: Seq[(String, String)] = Seq(validTaxYears, (SessionValues.CLIENT_NINO, nino))

            mockEmploymentsConnectorGet(
              result = Future.successful(Right(EmploymentPrePopulationResponse(
                hasEmployment = false
              )))
            )

            running(application) {
              status(result) mustEqual OK

              contentAsString(result) mustEqual
                view(
                  form = form,
                  mode = NormalMode,
                  taxYear = taxYear,
                  prePopData = false
                )(request, messages(application)).toString
            }
          }
        }

        "[GET] should return an error page when pre-pop retrieval fails" in new GetWithPrePopTest {
          override val userAnswers: Option[UserAnswers] = Some(baseUserAnswers)
          mockSessionDataConnectorGet(Future.successful(Right(Some(dummySessionData))))

          mockEmploymentsConnectorGet(
            result = Future.successful(Left(SimpleErrorWrapper(IM_A_TEAPOT)))
          )

          running(application) {
            status(result) mustEqual INTERNAL_SERVER_ERROR
            contentAsString(result) mustEqual mockErrorView
          }
        }

        "[GET] should return the expected view when user answers and pre-pop exists" in new GetWithPrePopTest {
          mockSessionDataConnectorGet(Future.successful(Right(Some(dummySessionData))))

          mockEmploymentsConnectorGet(
            result = Future.successful(Right(EmploymentPrePopulationResponse(
              hasEmployment = false
            )))
          )

          override val userAnswers: Option[UserAnswers] = Some(
            baseUserAnswers.set(
              page = AboutYourWorkRadioPage,
              value = true
            ).get
          )

          running(application) {
            status(result) mustEqual OK

            contentAsString(result) mustEqual
              view(
                form = filledForm(true),
                mode = NormalMode,
                taxYear = taxYear,
                prePopData = false
              )(request, messages(application)).toString
          }
        }

        "[GET] should return the expected view when user answers and pre-pop exists for an agent" in new GetWithPrePopAgentTest {
          mockSessionDataConnectorGet(Future.successful(Right(Some(dummySessionData))))

          mockEmploymentsConnectorGet(
            result = Future.successful(Right(EmploymentPrePopulationResponse(
              hasEmployment = true
            )))
          )

          override val userAnswers: Option[UserAnswers] = Some(
            baseUserAnswers.set(
              page = AboutYourWorkRadioPage,
              value = true
            ).get
          )

          running(application) {
            status(result) mustEqual OK

            contentAsString(result) mustEqual
              agentView(
                form = filledForm(true),
                mode = NormalMode,
                taxYear = taxYear,
                prePopData = true
              )(request, messages(application)).toString
          }
        }

        "[GET] should return the expected view when only pre-pop exists" in new GetWithPrePopTest {
          override val userAnswers: Option[UserAnswers] = Some(baseUserAnswers)
          mockSessionDataConnectorGet(Future.successful(Right(Some(dummySessionData))))

          mockEmploymentsConnectorGet(
            result = Future.successful(Right(EmploymentPrePopulationResponse(
              hasEmployment = true
            )))
          )

          running(application) {
            status(result) mustEqual OK

            contentAsString(result) mustEqual
              view(
                form = filledForm(true),
                mode = NormalMode,
                taxYear = taxYear,
                prePopData = true
              )(request, messages(application)).toString
          }
        }

        "[GET] should return the expected view when only pre-pop exists for an agent" in new GetWithPrePopAgentTest {
          override val userAnswers: Option[UserAnswers] = Some(baseUserAnswers)
          mockSessionDataConnectorGet(Future.successful(Right(Some(dummySessionData))))

          mockEmploymentsConnectorGet(
            result = Future.successful(Right(EmploymentPrePopulationResponse(
              hasEmployment = true
            )))
          )

          running(application) {
            status(result) mustEqual OK

            contentAsString(result) mustEqual
              agentView(
                form = filledForm(true),
                mode = NormalMode,
                taxYear = taxYear,
                prePopData = true
              )(request, messages(application)).toString
          }
        }

        "[GET] should return the expected view when user answers and pre-pop don't exist" in new GetWithPrePopTest {
          override val userAnswers: Option[UserAnswers] = Some(baseUserAnswers)
          mockSessionDataConnectorGet(Future.successful(Right(Some(dummySessionData))))

          mockEmploymentsConnectorGet(
            result = Future.successful(Right(EmploymentPrePopulationResponse(
              hasEmployment = false
            )))
          )

          running(application) {
            status(result) mustEqual OK

            contentAsString(result) mustEqual
              view(
                form = form,
                mode = NormalMode,
                taxYear = taxYear,
                prePopData = false
              )(request, messages(application)).toString
          }
        }

        "[GET] should return the expected view when user answers and pre-pop don't exist for an agent" in new GetWithPrePopAgentTest {
          override val userAnswers: Option[UserAnswers] = Some(baseUserAnswers)
          mockSessionDataConnectorGet(Future.successful(Right(Some(dummySessionData))))

          mockEmploymentsConnectorGet(
            result = Future.successful(Right(EmploymentPrePopulationResponse(
              hasEmployment = false
            )))
          )

          running(application) {
            status(result) mustEqual OK

            contentAsString(result) mustEqual
              agentView(
                form = form,
                mode = NormalMode,
                taxYear = taxYear,
                prePopData = false
              )(request, messages(application)).toString
          }
        }
      }
    }

    "when trying to submit answers with a POST" -> {
      trait EmploymentSubmitRequest {
        def formUrlEncodedBody: (String, String) = ("value", "true")
      }

      trait SubmitEmploymentWithNoPrePopTest extends SubmitWithNoPrePopTest with EmploymentSubmitRequest

      "[POST] for a valid request should redirect to the next page" in new SubmitEmploymentWithNoPrePopTest {
        override val userAnswers: Option[UserAnswers] = Some(baseUserAnswers)
        val mockUserDataService: UserDataService = mock[UserDataService]
        when(mockUserDataService.set(any(), any())(any())) thenReturn Future.successful(Done)

        override def applicationOverrides: Seq[GuiceableModule] = Seq(
          inject.bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
          inject.bind[UserDataService].toInstance(mockUserDataService)
        )

        running(application) {
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "for a request with form errors" -> {
        "[POST] should return expected view" in new SubmitWithNoPrePopTest {
          override val userAnswers: Option[UserAnswers] = Some(baseUserAnswers)
          override def formUrlEncodedBody: (String, String) = ("value", "invalid value")

          running(application) {
            status(result) mustEqual BAD_REQUEST

            contentAsString(result) mustEqual
              view(
                form = boundForm,
                mode = NormalMode,
                taxYear = taxYear,
                prePopData = false
              )(request, messages(application)).toString
          }
        }

        "[POST] should return expected view for an agent" in new SubmitWithNoPrePopAgentTest {
          override val userAnswers: Option[UserAnswers] = Some(baseUserAnswers)
          override def formUrlEncodedBody: (String, String) = ("value", "invalid value")

          running(application) {
            status(result) mustEqual BAD_REQUEST

            contentAsString(result) mustEqual
              agentView(
                form = boundForm,
                mode = NormalMode,
                taxYear = taxYear,
                prePopData = false
              )(request, messages(application)).toString
          }
        }
      }
    }
  }
}
