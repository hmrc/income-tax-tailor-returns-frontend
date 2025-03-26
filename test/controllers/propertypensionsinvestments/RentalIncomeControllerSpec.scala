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

package controllers.propertypensionsinvestments

import controllers.ControllerWithPrePopSpecBase
import forms.propertypensionsinvestments.RentalIncomeFormProvider
import models.errors.{APIErrorBodyModel, APIErrorModel, SimpleErrorWrapper}
import models.prePopulation.PropertyPrePopulationResponse
import models.propertypensionsinvestments.RentalIncome
import models.{Done, NormalMode, SessionValues, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.when
import pages.propertypensionsinvestments.RentalIncomePage
import play.api.data.Form
import play.api.inject.guice.GuiceableModule
import play.api.test.Helpers._
import play.api.{Application, inject}
import services.UserDataService
import views.html.propertypensionsinvestments.{RentalIncomeAgentView, RentalIncomeView}

import scala.concurrent.Future

class RentalIncomeControllerSpec extends
  ControllerWithPrePopSpecBase[RentalIncomeView, RentalIncomeAgentView, Set[RentalIncome]] {

  override def formProvider: RentalIncomeFormProvider = new RentalIncomeFormProvider()

  override val viewProvider: Application => RentalIncomeView =
    (application: Application) => application.injector.instanceOf[RentalIncomeView]

  override val agentViewProvider: Application => RentalIncomeAgentView =
    (application: Application) => application.injector.instanceOf[RentalIncomeAgentView]

  override val requestRoute: String =
    controllers
      .propertypensionsinvestments
      .routes
      .RentalIncomeController
      .onPageLoad(NormalMode, taxYear).url

  trait RentalIncomeSubmitRequest {
    def formUrlEncodedBody: (String, String) = ("value[0]", RentalIncome.values.head.toString)
  }

  "RentalIncome Controller" - {
    "when trying to retrieve the view with a GET" -> {
      "when pre-population is disabled" -> {
        "[GET] should return expected view when no user answers exist" in new GetWithNoPrePopTest {
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
            emptyUserAnswers.set(
              RentalIncomePage,
              Set[RentalIncome](RentalIncome.Uk, RentalIncome.NonUk)
            ).get
          )

          val filledForm: Form[Set[RentalIncome]] = form.fill(Set[RentalIncome](RentalIncome.Uk, RentalIncome.NonUk))

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
            emptyUserAnswers.set(
              RentalIncomePage,
              Set[RentalIncome](RentalIncome.Uk, RentalIncome.NonUk)
            ).get
          )

          val filledForm: Form[Set[RentalIncome]] = form.fill(Set[RentalIncome](RentalIncome.Uk, RentalIncome.NonUk))

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

            running(application) {
              status(result) mustEqual INTERNAL_SERVER_ERROR
              contentAsString(result) mustEqual mockErrorView
            }
          }

          "[GET] should show error page - SessionDataService: returns error, Nino in session: false" in new GetWithPrePopTest {
            mockSessionDataConnectorGet(Future.successful(Left(
              APIErrorModel(IM_A_TEAPOT, APIErrorBodyModel("", "")))
            ))

            running(application) {
              status(result) mustEqual INTERNAL_SERVER_ERROR
              contentAsString(result) mustEqual mockErrorView
            }
          }

          "[GET] should show error page - SessionDataService: returns None, Nino in session: false" in new GetWithPrePopTest {
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
            override val defaultSession: Seq[(String, String)] = Seq(validTaxYears, (SessionValues.CLIENT_NINO, nino))

            mockPropertyConnectorGet(
              result = Future.successful(Right(PropertyPrePopulationResponse(
                hasUkPropertyPrePop = false,
                hasForeignPropertyPrePop = false
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
            mockSessionDataConnectorGet(Future.successful(Right(Some(dummySessionData))))

            override val defaultSession: Seq[(String, String)] = Seq(validTaxYears, (SessionValues.CLIENT_NINO, nino))

            mockPropertyConnectorGet(
              result = Future.successful(Right(PropertyPrePopulationResponse(
                hasUkPropertyPrePop = false,
                hasForeignPropertyPrePop = false
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
            mockSessionDataConnectorGet(Future.successful(Right(None)))

            override val defaultSession: Seq[(String, String)] = Seq(validTaxYears, (SessionValues.CLIENT_NINO, nino))

            mockPropertyConnectorGet(
              result = Future.successful(Right(PropertyPrePopulationResponse(
                hasUkPropertyPrePop = false,
                hasForeignPropertyPrePop = false
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
          mockSessionDataConnectorGet(Future.successful(Right(Some(dummySessionData))))

          mockPropertyConnectorGet(
            result = Future.successful(Left(SimpleErrorWrapper(IM_A_TEAPOT)))
          )

          running(application) {
            status(result) mustEqual INTERNAL_SERVER_ERROR
            contentAsString(result) mustEqual mockErrorView
          }
        }

        "[GET] should return the expected view when user answers and pre-pop exists" in new GetWithPrePopTest {
          mockSessionDataConnectorGet(Future.successful(Right(Some(dummySessionData))))

          mockPropertyConnectorGet(
            result = Future.successful(Right(PropertyPrePopulationResponse(
              hasUkPropertyPrePop = true,
              hasForeignPropertyPrePop = false
            )))
          )

          override val userAnswers: Option[UserAnswers] = Some(
            emptyUserAnswers.set(
              RentalIncomePage,
              Set[RentalIncome](RentalIncome.NonUk)
            ).get
          )

          running(application) {
            status(result) mustEqual OK

            contentAsString(result) mustEqual
              view(
                form = filledForm(Set(RentalIncome.NonUk)),
                mode = NormalMode,
                taxYear = taxYear,
                prePopData = true
              )(request, messages(application)).toString
          }
        }

        "[GET] should return the expected view when user answers and pre-pop exists for an agent" in new GetWithPrePopAgentTest {
          mockSessionDataConnectorGet(Future.successful(Right(Some(dummySessionData))))

          mockPropertyConnectorGet(
            result = Future.successful(Right(PropertyPrePopulationResponse(
              hasUkPropertyPrePop = true,
              hasForeignPropertyPrePop = false
            )))
          )

          override val userAnswers: Option[UserAnswers] = Some(
            emptyUserAnswers.set(
              RentalIncomePage,
              Set[RentalIncome](RentalIncome.NonUk)
            ).get
          )

          running(application) {
            status(result) mustEqual OK

            contentAsString(result) mustEqual
              agentView(
                form = filledForm(Set(RentalIncome.NonUk)),
                mode = NormalMode,
                taxYear = taxYear,
                prePopData = true
              )(request, messages(application)).toString
          }
        }

        "[GET] should return the expected view when only pre-pop exists" in new GetWithPrePopTest {
          mockSessionDataConnectorGet(Future.successful(Right(Some(dummySessionData))))

          mockPropertyConnectorGet(
            result = Future.successful(Right(PropertyPrePopulationResponse(
              hasUkPropertyPrePop = true,
              hasForeignPropertyPrePop = false
            )))
          )

          running(application) {
            status(result) mustEqual OK

            contentAsString(result) mustEqual
              view(
                form = filledForm(Set(RentalIncome.Uk)),
                mode = NormalMode,
                taxYear = taxYear,
                prePopData = true
              )(request, messages(application)).toString
          }
        }

        "[GET] should return the expected view when only pre-pop exists for an agent" in new GetWithPrePopAgentTest {
          mockSessionDataConnectorGet(Future.successful(Right(Some(dummySessionData))))

          mockPropertyConnectorGet(
            result = Future.successful(Right(PropertyPrePopulationResponse(
              hasUkPropertyPrePop = true,
              hasForeignPropertyPrePop = false
            )))
          )

          running(application) {
            status(result) mustEqual OK

            contentAsString(result) mustEqual
              agentView(
                form = filledForm(Set(RentalIncome.Uk)),
                mode = NormalMode,
                taxYear = taxYear,
                prePopData = true
              )(request, messages(application)).toString
          }
        }

        "[GET] should return the expected view when user answers and pre-pop don't exist" in new GetWithPrePopTest {
          mockSessionDataConnectorGet(Future.successful(Right(Some(dummySessionData))))

          mockPropertyConnectorGet(
            result = Future.successful(Right(PropertyPrePopulationResponse(
              hasUkPropertyPrePop = false,
              hasForeignPropertyPrePop = false
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
          mockSessionDataConnectorGet(Future.successful(Right(Some(dummySessionData))))

          mockPropertyConnectorGet(
            result = Future.successful(Right(PropertyPrePopulationResponse(
              hasUkPropertyPrePop = false,
              hasForeignPropertyPrePop = false,
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
      trait SubmitRentalIncomeWithNoPrePopTest extends SubmitWithNoPrePopTest with RentalIncomeSubmitRequest
      trait SubmitRentalIncomeWithNoPrePopAgentTest extends SubmitWithNoPrePopAgentTest with RentalIncomeSubmitRequest

      "[POST] for a valid request should redirect to the next page" in new SubmitRentalIncomeWithNoPrePopTest {
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

      "[POST] must redirect to Journey Recovery if no existing data is found" in new SubmitRentalIncomeWithNoPrePopAgentTest {
        override val userAnswers: Option[UserAnswers] = None

        running(application) {
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual journeyRecoveryUrl(taxYear)
        }
      }
    }
  }
}
