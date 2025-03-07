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

package controllers.actions

import base.SpecBase
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.IncomeTaxSessionDataConnector
import models.{APIErrorBodyModel, APIErrorModel}
import models.session.SessionData
import org.mockito.ArgumentMatchers.{any, eq => mEq}
import org.mockito.{Mockito, MockitoSugar}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{Action, AnyContent, BodyParsers, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, _}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{EmptyRetrieval, Retrieval, ~}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.auth.core.{Enrolment => HMRCEnrolment}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class AuthActionSpec extends SpecBase with MockitoSugar {

  class Harness(authAction: IdentifierAction) {
    def onPageLoad(): Action[AnyContent] = authAction { _ => Results.Ok }
  }

  val mtdEnrollmentKey = "HMRC-MTD-IT"
  val mtdEnrollmentIdentifier = "MTDITID"
  private val mockAuthConnector: AuthConnector = Mockito.mock(classOf[AuthConnector])
  private type RetrievalType = Option[AffinityGroup] ~ Enrolments ~ ConfidenceLevel
  val mockSessionDataConnector: IncomeTaxSessionDataConnector = mock[IncomeTaxSessionDataConnector]
  val testMtditId = "1234567890"

  def predicate(mtdId: String): Predicate = mEq(
    HMRCEnrolment("HMRC-MTD-IT")
      .withIdentifier("MTDITID", mtdId)
      .withDelegatedAuthRule("mtd-it-auth"))

  def secondaryAgentPredicate(mtdId: String): Predicate = mEq(
    HMRCEnrolment("HMRC-MTD-IT-SUPP")
      .withIdentifier("MTDITID", mtdId)
      .withDelegatedAuthRule("mtd-it-auth-supp"))

  "Auth Action [IdentifierActionProviderImpl]" - {

    "when the user is an Organisation" - {

      "must succeed with a identifier Request when fully authenticated" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {

          val enrolments: Enrolments = Enrolments(Set(
            Enrolment(mtdEnrollmentKey, Seq(EnrolmentIdentifier(mtdEnrollmentIdentifier, testMtditId)), "Activated")
          ))

          val authResponse: Option[AffinityGroup] ~ Enrolments ~ ConfidenceLevel =
            new ~(new ~(
              Some(AffinityGroup.Organisation),
              enrolments),
              ConfidenceLevel.L250)

          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]
          val authAction = new IdentifierActionProviderImpl(new FakeSuccessfulAuthConnector(authResponse), appConfig,
            mockSessionDataConnector, bodyParsers)(ec).apply(taxYear)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe OK
        }


      }
    }
    "when the user is an individual" - {

      "must succeed with a identifier Request when fully authenticated" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {

          val enrolments: Enrolments = Enrolments(Set(
            Enrolment(mtdEnrollmentKey, Seq(EnrolmentIdentifier(mtdEnrollmentIdentifier, "1234567890")), "Activated")
          ))

          val authResponse: Option[AffinityGroup] ~ Enrolments ~ ConfidenceLevel =
            new ~(new ~(
              Some(AffinityGroup.Individual),
              enrolments),
              ConfidenceLevel.L250)

          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]
          val authAction = new IdentifierActionProviderImpl(new FakeSuccessfulAuthConnector(authResponse), appConfig,
            mockSessionDataConnector, bodyParsers)(ec).apply(taxYear)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe OK
        }
      }

      "must fail with a UNAUTHORIZED when missing mtditid enrolment" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {

          val enrolments: Enrolments = Enrolments(Set())

          val authResponse: Option[AffinityGroup] ~ Enrolments ~ ConfidenceLevel =
            new ~(new ~(
              Some(AffinityGroup.Individual),
              enrolments),
              ConfidenceLevel.L250)

          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]
          val authAction = new IdentifierActionProviderImpl(new FakeSuccessfulAuthConnector(authResponse), appConfig,
            mockSessionDataConnector, bodyParsers)(ec).apply(taxYear)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some("http://localhost:9081/report-quarterly/income-and-expenses/sign-up/eligibility")

        }
      }

      "must fail with a Redirect to ivUplift when confidence is to low" in {
        val mockSessionDataConnector: IncomeTaxSessionDataConnector = mock[IncomeTaxSessionDataConnector]
        val application = applicationBuilder(userAnswers = None).configure(
          "feature-switch.sessionCookieService" -> true
        ).overrides(bind[AuthConnector].toInstance(mockAuthConnector))
          .overrides(bind[IncomeTaxSessionDataConnector].toInstance(mockSessionDataConnector))
          .build()

        running(application) {

          val enrolments: Enrolments = Enrolments(Set(
            Enrolment(mtdEnrollmentKey, Seq(EnrolmentIdentifier(mtdEnrollmentIdentifier, testMtditId)), "Activated")
          ))

          val authResponse: Option[AffinityGroup] ~ Enrolments ~ ConfidenceLevel =
            new ~(new ~(
              Some(AffinityGroup.Individual),
              enrolments),
              ConfidenceLevel.L200)

          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]


          val authAction = new IdentifierActionProviderImpl(new FakeSuccessfulAuthConnector(authResponse), appConfig,
            mockSessionDataConnector, bodyParsers)(ec).apply(taxYear)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some("http://localhost:9302/update-and-submit-income-tax-return/iv-uplift")
        }
      }
    }
    "when the user is authorised as an agent" - {

      "must succeed with a identifier Request" in {

        val application = applicationBuilder(userAnswers = None, isAgent = true).configure(
          "feature-switch.sessionCookieService" -> true
        ).overrides(
          bind[AuthConnector].toInstance(mockAuthConnector),
          bind[IncomeTaxSessionDataConnector].toInstance(mockSessionDataConnector)
        ).build()

        val enrolments: Enrolments = Enrolments(Set(
          Enrolment(models.Enrolment.Agent.key, Seq(EnrolmentIdentifier(models.Enrolment.Agent.value, "XARN1234567")), "Activated")
        ))

        val authResponse = Future.successful(
          new ~(new ~(Some(AffinityGroup.Agent), enrolments), ConfidenceLevel.L50)
        )

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]])(any(), any()))
            .thenReturn(authResponse)

          when(mockSessionDataConnector.getSessionData(any()))
            .thenReturn(Future.successful(Right(Some(SessionData("1234567890", "nino", "utr", "test-session-id")))))

          when(mockAuthConnector.authorise(predicate(testMtditId), any[Retrieval[Unit]])(any(), any()))
            .thenReturn(Future.successful(()))

          val authAction = new IdentifierActionProviderImpl(mockAuthConnector, appConfig,
            mockSessionDataConnector, bodyParsers)(ec).apply(taxYear)

          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest().withSession("ClientMTDID" -> "1234567890", "sessionId" -> "test-session-id"))

          status(result) mustBe OK
        }
      }

      "must redirect when session data is none" in {

        val application = applicationBuilder(userAnswers = None).configure(
          "feature-switch.sessionCookieService" -> true
        ).overrides(bind[AuthConnector].toInstance(mockAuthConnector))
          .overrides(bind[IncomeTaxSessionDataConnector].toInstance(mockSessionDataConnector))
          .build()

        val enrolments: Enrolments = Enrolments(Set(
         Enrolment(models.Enrolment.Agent.key, Seq(EnrolmentIdentifier(models.Enrolment.Agent.value, "XARN1234567")), "Activated"))
        )

        val authResponse: Option[AffinityGroup] ~ Enrolments ~ ConfidenceLevel =
          new ~(new ~(
            Some(AffinityGroup.Agent),
            enrolments),
            ConfidenceLevel.L50)

        running(application) {

          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          when(mockSessionDataConnector.getSessionData(any())).thenReturn(
            Future.successful(Right(None))
          )
          val authAction = new IdentifierActionProviderImpl(new FakeSuccessfulAuthConnector(authResponse), appConfig,
            mockSessionDataConnector, bodyParsers)(ec).apply(taxYear)


          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest().withSession("ClientMTDID" -> "1234567890", "sessionId" -> "test-session-id"))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some("http://localhost:9081/report-quarterly/income-and-expenses/view/agents/client-utr")

        }
      }

      "must succeed with a identifier Request, while session cookie service feature is off" in {

        val application = applicationBuilder(userAnswers = None,isAgent = true).configure(
          "feature-switch.sessionCookieService" -> false
        ).overrides(
          bind[AuthConnector].toInstance(mockAuthConnector),
          bind[IncomeTaxSessionDataConnector].toInstance(mockSessionDataConnector)
        ).build()

        val enrolments: Enrolments = Enrolments(Set(
          Enrolment(models.Enrolment.Agent.key, Seq(EnrolmentIdentifier(models.Enrolment.Agent.value, "XARN1234567")), "Activated")
        ))

        val authResponse = Future.successful(
          new ~(new ~(Some(AffinityGroup.Agent), enrolments), ConfidenceLevel.L250)
        )

        running(application) {

          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]])(any(), any()))
            .thenReturn(authResponse)

          when(mockAuthConnector.authorise(predicate(testMtditId), any[Retrieval[Unit]])(any(), any()))
            .thenReturn(Future.successful(()))

          val authAction = new IdentifierActionProviderImpl(mockAuthConnector, appConfig,
            mockSessionDataConnector, bodyParsers)(ec).apply(taxYear)

          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest().withSession("ClientMTDID" -> testMtditId, "sessionId" -> "test-session-id"))

          status(result) mustBe OK
        }
      }
      "must succeed with a identifier Request, while session cookie service feature is on but returns error" in {

        val application = applicationBuilder(userAnswers = None, isAgent = true).configure(
          "feature-switch.sessionCookieService" -> true
        ).overrides(
          bind[AuthConnector].toInstance(mockAuthConnector),
          bind[IncomeTaxSessionDataConnector].toInstance(mockSessionDataConnector)
        ).build()

        val enrolments: Enrolments = Enrolments(Set(
          Enrolment(models.Enrolment.Agent.key, Seq(EnrolmentIdentifier(models.Enrolment.Agent.value, "XARN1234567")), "Activated")
        ))

        val authResponse = Future.successful(
          new ~(new ~(Some(AffinityGroup.Agent), enrolments), ConfidenceLevel.L250)
        )

        running(application) {

          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]])(any(), any()))
            .thenReturn(authResponse)

          when(mockAuthConnector.authorise(predicate(testMtditId), any[Retrieval[Unit]])(any(), any()))
            .thenReturn(Future.successful(()))

          when(mockSessionDataConnector.getSessionData(any())).thenReturn(
            Future.successful(Left(APIErrorModel(SERVICE_UNAVAILABLE, APIErrorBodyModel("SERVICE_UNAVAILABLE", "Internal Server error")))
            ))

          val authAction = new IdentifierActionProviderImpl(mockAuthConnector, appConfig,
            mockSessionDataConnector, bodyParsers)(ec).apply(taxYear)

          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest().withSession("ClientMTDID" -> testMtditId, "sessionId" -> "test-session-id"))

          status(result) mustBe OK
        }
      }

      "must fail with a UNAUTHORIZED when missing ClientMTDID from User Session" in {
        val application = applicationBuilder(userAnswers = None, isAgent = true).configure(
          "feature-switch.sessionCookieService" -> false
        ).overrides(
          bind[AuthConnector].toInstance(mockAuthConnector),
          bind[IncomeTaxSessionDataConnector].toInstance(mockSessionDataConnector)
        ).build()

        val enrolments: Enrolments = Enrolments(Set(
          Enrolment(models.Enrolment.Agent.key, Seq(EnrolmentIdentifier(models.Enrolment.Agent.value, "XARN1234567")), "Activated")
        ))

        val authResponse = Future.successful(
          new ~(new ~(Some(AffinityGroup.Agent), enrolments), ConfidenceLevel.L250)
        )

        running(application) {

          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]])(any(), any()))
            .thenReturn(authResponse)

          when(mockAuthConnector.authorise(predicate(testMtditId), any[Retrieval[Unit]])(any(), any()))
            .thenReturn(Future.successful(()))

          val authAction = new IdentifierActionProviderImpl(mockAuthConnector, appConfig,
            mockSessionDataConnector, bodyParsers)(ec).apply(taxYear)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some("http://localhost:9081/report-quarterly/income-and-expenses/view/agents/client-utr")
        }
      }
      "must fail with a UNAUTHORIZED when ClientMTDID from User Session does not exist in users enrolments" in {

        val application = applicationBuilder(userAnswers = None, isAgent = true).configure(
          "feature-switch.sessionCookieService" -> false
        ).overrides(
          bind[AuthConnector].toInstance(mockAuthConnector),
          bind[IncomeTaxSessionDataConnector].toInstance(mockSessionDataConnector)
        ).build()

        val enrolments: Enrolments = Enrolments(Set(
          Enrolment(models.Enrolment.Agent.key, Seq(EnrolmentIdentifier(models.Enrolment.Agent.value, "XARN1234567")), "Activated")
        ))

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          val authResponse = Future.successful(
            new ~(new ~(Some(AffinityGroup.Agent), enrolments), ConfidenceLevel.L250)
          )

          when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]])(any(), any()))
            .thenReturn(authResponse)
          when(mockAuthConnector.authorise(predicate("1234567890"), mEq(EmptyRetrieval))(any(), any()))
            .thenReturn(Future.failed(InsufficientEnrolments()))
          when(mockAuthConnector.authorise(secondaryAgentPredicate("1234567890"), mEq(EmptyRetrieval))(any(), any()))
            .thenReturn(Future.failed(InsufficientEnrolments()))

          val authAction = new IdentifierActionProviderImpl(mockAuthConnector, appConfig,
            mockSessionDataConnector, bodyParsers)(ec).apply(taxYear)

          val controller = new Harness(authAction)

          val result = controller.onPageLoad()(FakeRequest().withSession("ClientMTDID" -> "1234567890", "sessionId" -> "1234567890"))

          status(result) mustBe UNAUTHORIZED
        }
      }

      "must fail with a UNAUTHORIZED when authConnector fails to authenticate secondary agent " in {

        val application = applicationBuilder(userAnswers = None, isAgent = true).configure(
          "feature-switch.sessionCookieService" -> false
        ).overrides(
          bind[AuthConnector].toInstance(mockAuthConnector),
          bind[IncomeTaxSessionDataConnector].toInstance(mockSessionDataConnector)
        ).build()

        val enrolments: Enrolments = Enrolments(Set(
          Enrolment(models.Enrolment.Agent.key, Seq(EnrolmentIdentifier(models.Enrolment.Agent.value, "XARN1234567")), "Activated")
        ))

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]
          val authResponse = Future.successful(
            new ~(new ~(Some(AffinityGroup.Agent), enrolments), ConfidenceLevel.L250)
          )

          when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]])(any(), any()))
            .thenReturn(authResponse)
          when(mockAuthConnector.authorise(predicate("1234567890"), mEq(EmptyRetrieval))(any(), any()))
            .thenReturn(Future.failed(UnsupportedCredentialRole()))
          when(mockAuthConnector.authorise(any, mEq(EmptyRetrieval))(any(), any()))
            .thenReturn(Future.failed(InsufficientConfidenceLevel()))

          val authAction = new IdentifierActionProviderImpl(mockAuthConnector, appConfig,
            mockSessionDataConnector, bodyParsers)(ec).apply(taxYear)

          val controller = new Harness(authAction)

          val result = controller.onPageLoad()(FakeRequest().withSession("ClientMTDID" -> "1234567890", "sessionId" -> "1234567890"))

          status(result) mustBe UNAUTHORIZED
        }
      }

      "must fail with a UNAUTHORIZED when there is no AGENT REFERENCE NUMBER enrolment" in {

        val application = applicationBuilder(userAnswers = None).build()

        val enrolments: Enrolments = Enrolments(Set())

        val authResponse: Option[AffinityGroup] ~ Enrolments ~ ConfidenceLevel =
          new ~(new ~(
            Some(AffinityGroup.Agent),
            enrolments),
            ConfidenceLevel.L50)

        running(application) {

          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new IdentifierActionProviderImpl(new FakeSuccessfulAuthConnector(authResponse), appConfig,
            mockSessionDataConnector, bodyParsers)(ec).apply(taxYear)
          val controller = new Harness(authAction)

          when(mockSessionDataConnector.getSessionData(any())).thenReturn(
            Future.successful(Right(Some(SessionData("1234567890", "nino", "utr", "test-session-id"))))
          )

          val result = controller.onPageLoad()(FakeRequest().withSession("ClientMTDID" -> "1234567890", "sessionId" -> "test-session-id"))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some("https://www.gov.uk/guidance/get-an-hmrc-agent-services-account")
        }
      }
    }

    "when the user is authorised as a secondary agent" - {

      "must succeed with a identifier Request and redirect " in {

        val application = applicationBuilder(userAnswers = None, isAgent = true).configure(
          "feature-switch.sessionCookieService" -> true
        ).overrides(
          bind[AuthConnector].toInstance(mockAuthConnector),
          bind[IncomeTaxSessionDataConnector].toInstance(mockSessionDataConnector)
        ).build()

        val enrolments: Enrolments = Enrolments(Set(
          Enrolment(models.Enrolment.Agent.key, Seq(EnrolmentIdentifier(models.Enrolment.Agent.value, "XARN1234567")), "Activated"),
          Enrolment(models.Enrolment.SupportingAgent.key, Seq(EnrolmentIdentifier(models.Enrolment.SupportingAgent.value, "1234567893")), "mtd-it-auth-supp")
        ))

        val authResponse = Future.successful(
          new~(new~(Some(AffinityGroup.Agent), enrolments), ConfidenceLevel.L50)
        )

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]])(any(), any()))
            .thenReturn(authResponse)

          when(mockSessionDataConnector.getSessionData(any()))
            .thenReturn(Future.successful(Right(Some(SessionData(testMtditId, "nino", "utr", "test-session-id")))))

          when(mockAuthConnector.authorise(predicate(testMtditId), mEq(EmptyRetrieval))(any(), any()))
            .thenReturn(Future.failed(InsufficientEnrolments()))
          when(mockAuthConnector.authorise(secondaryAgentPredicate(testMtditId), any[Retrieval[Unit]])(any(), any()))
            .thenReturn(Future.successful(()))

          val authAction = new IdentifierActionProviderImpl(mockAuthConnector, appConfig,
            mockSessionDataConnector, bodyParsers)(ec).apply(taxYear)

          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest().withSession("ClientMTDID" -> testMtditId, "sessionId" -> "test-session-id"))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SupportingAgentAuthErrorController.show.url)
        }
      }

      "must redirect when session data is none" in {

        val application = applicationBuilder(userAnswers = None).configure(
            "feature-switch.sessionCookieService" -> true
          ).overrides(bind[AuthConnector].toInstance(mockAuthConnector))
          .overrides(bind[IncomeTaxSessionDataConnector].toInstance(mockSessionDataConnector))
          .build()

        val enrolments: Enrolments = Enrolments(Set(
          Enrolment(models.Enrolment.Agent.key, Seq(EnrolmentIdentifier(models.Enrolment.Agent.value, "XARN1234567")), "Activated"),
          Enrolment(models.Enrolment.SupportingAgent.key, Seq(EnrolmentIdentifier(models.Enrolment.SupportingAgent.value, "1234567893")), "mtd-it-auth-supp")
        ))

        val authResponse: Option[AffinityGroup] ~ Enrolments ~ ConfidenceLevel =
          new ~(new ~(
            Some(AffinityGroup.Agent),
            enrolments),
            ConfidenceLevel.L50)

        running(application) {

          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          when(mockSessionDataConnector.getSessionData(any())).thenReturn(
            Future.successful(Right(None))
          )
          val authAction = new IdentifierActionProviderImpl(new FakeSuccessfulAuthConnector(authResponse), appConfig,
            mockSessionDataConnector, bodyParsers)(ec).apply(taxYear)


          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest().withSession("ClientMTDID" -> testMtditId, "sessionId" -> "test-session-id"))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some("http://localhost:9081/report-quarterly/income-and-expenses/view/agents/client-utr")

        }
      }

      "must succeed with a identifier Request, while session cookie service feature is off and redirect" in {

        val application = applicationBuilder(userAnswers = None,isAgent = true).configure(
          "feature-switch.sessionCookieService" -> false
        ).overrides(
          bind[AuthConnector].toInstance(mockAuthConnector),
          bind[IncomeTaxSessionDataConnector].toInstance(mockSessionDataConnector)
        ).build()

        val enrolments: Enrolments = Enrolments(Set(
          Enrolment(models.Enrolment.Agent.key, Seq(EnrolmentIdentifier(models.Enrolment.Agent.value, "XARN1234567")), "Activated"),
          Enrolment(models.Enrolment.SupportingAgent.key, Seq(EnrolmentIdentifier(models.Enrolment.SupportingAgent.value, "1234567893")), "mtd-it-auth-supp")
        ))

        val authResponse = Future.successful(
          new ~(new ~(Some(AffinityGroup.Agent), enrolments), ConfidenceLevel.L250)
        )

        running(application) {

          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]])(any(), any()))
            .thenReturn(authResponse)

          when(mockAuthConnector.authorise(predicate(testMtditId), mEq(EmptyRetrieval))(any(), any()))
            .thenReturn(Future.failed(InsufficientEnrolments()))
          when(mockAuthConnector.authorise(secondaryAgentPredicate(testMtditId), any[Retrieval[Unit]])(any(), any()))
            .thenReturn(Future.successful(()))

          val authAction = new IdentifierActionProviderImpl(mockAuthConnector, appConfig,
            mockSessionDataConnector, bodyParsers)(ec).apply(taxYear)

          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest().withSession("ClientMTDID" -> testMtditId, "sessionId" -> "test-session-id"))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SupportingAgentAuthErrorController.show.url)
        }
      }

      "must succeed with a identifier Request, while session cookie service feature is on but returns error" in {

        val application = applicationBuilder(userAnswers = None, isAgent = true).configure(
          "feature-switch.sessionCookieService" -> true
        ).overrides(
          bind[AuthConnector].toInstance(mockAuthConnector),
          bind[IncomeTaxSessionDataConnector].toInstance(mockSessionDataConnector)
        ).build()

        val enrolments: Enrolments = Enrolments(Set(
          Enrolment(models.Enrolment.Agent.key, Seq(EnrolmentIdentifier(models.Enrolment.Agent.value, "XARN1234567")), "Activated"),
          Enrolment(models.Enrolment.SupportingAgent.key, Seq(EnrolmentIdentifier(models.Enrolment.SupportingAgent.value, "1234567893")), "mtd-it-auth-supp")
        ))

        val authResponse = Future.successful(
          new ~(new ~(Some(AffinityGroup.Agent), enrolments), ConfidenceLevel.L250)
        )

        running(application) {

          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]])(any(), any()))
            .thenReturn(authResponse)

          when(mockAuthConnector.authorise(predicate(testMtditId), mEq(EmptyRetrieval))(any(), any()))
            .thenReturn(Future.failed(InsufficientEnrolments()))
          when(mockAuthConnector.authorise(secondaryAgentPredicate(testMtditId), any[Retrieval[Unit]])(any(), any()))
            .thenReturn(Future.successful(()))

          when(mockSessionDataConnector.getSessionData(any())).thenReturn(
            Future.successful(Left(APIErrorModel(SERVICE_UNAVAILABLE, APIErrorBodyModel("SERVICE_UNAVAILABLE", "Internal Server error")))
            ))

          val authAction = new IdentifierActionProviderImpl(mockAuthConnector, appConfig,
            mockSessionDataConnector, bodyParsers)(ec).apply(taxYear)

          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest().withSession("ClientMTDID" -> testMtditId, "sessionId" -> "test-session-id"))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SupportingAgentAuthErrorController.show.url)
        }
      }

      "must fail with a redirect when missing ClientMTDID from User Session" in {
        val application = applicationBuilder(userAnswers = None, isAgent = true).configure(
          "feature-switch.sessionCookieService" -> false
        ).overrides(
          bind[AuthConnector].toInstance(mockAuthConnector),
          bind[IncomeTaxSessionDataConnector].toInstance(mockSessionDataConnector)
        ).build()

        val enrolments: Enrolments = Enrolments(Set(
          Enrolment(models.Enrolment.Agent.key, Seq(EnrolmentIdentifier(models.Enrolment.Agent.value, "XARN1234567")), "Activated"),
          Enrolment(models.Enrolment.SupportingAgent.key, Seq(EnrolmentIdentifier(models.Enrolment.SupportingAgent.value, "1234567893")), "mtd-it-auth-supp")
        ))

        val authResponse = Future.successful(
          new ~(new ~(Some(AffinityGroup.Agent), enrolments), ConfidenceLevel.L250)
        )

        running(application) {

          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]])(any(), any()))
            .thenReturn(authResponse)

          when(mockAuthConnector.authorise(predicate(testMtditId), mEq(EmptyRetrieval))(any(), any()))
            .thenReturn(Future.failed(InsufficientEnrolments()))
          when(mockAuthConnector.authorise(secondaryAgentPredicate(testMtditId), any[Retrieval[Unit]])(any(), any()))
            .thenReturn(Future.successful(()))

          val authAction = new IdentifierActionProviderImpl(mockAuthConnector, appConfig,
            mockSessionDataConnector, bodyParsers)(ec).apply(taxYear)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some("http://localhost:9081/report-quarterly/income-and-expenses/view/agents/client-utr")
        }
      }

      "must fail with a UNAUTHORIZED when ClientMTDID from User Session does not exist in users enrolments" in {

        val application = applicationBuilder(userAnswers = None, isAgent = true).configure(
          "feature-switch.sessionCookieService" -> false
        ).overrides(
          bind[AuthConnector].toInstance(mockAuthConnector),
          bind[IncomeTaxSessionDataConnector].toInstance(mockSessionDataConnector)
        ).build()

        val enrolments: Enrolments = Enrolments(Set(
          Enrolment(models.Enrolment.Agent.key, Seq(EnrolmentIdentifier(models.Enrolment.Agent.value, "XARN1234567")), "Activated"),
          Enrolment(models.Enrolment.SupportingAgent.key, Seq(EnrolmentIdentifier(models.Enrolment.SupportingAgent.value, "1234567893")), "mtd-it-auth-supp")
        ))

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          val authResponse = Future.successful(
            new ~(new ~(Some(AffinityGroup.Agent), enrolments), ConfidenceLevel.L250)
          )

          when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]])(any(), any()))
            .thenReturn(authResponse)

          when(mockAuthConnector.authorise(predicate(testMtditId), mEq(EmptyRetrieval))(any(), any()))
            .thenReturn(Future.failed(InsufficientEnrolments()))
          when(mockAuthConnector.authorise(secondaryAgentPredicate(testMtditId), mEq(EmptyRetrieval))(any(), any()))
            .thenReturn(Future.failed(InsufficientEnrolments()))

          val authAction = new IdentifierActionProviderImpl(mockAuthConnector, appConfig,
            mockSessionDataConnector, bodyParsers)(ec).apply(taxYear)

          val controller = new Harness(authAction)

          val result = controller.onPageLoad()(FakeRequest().withSession("ClientMTDID" -> testMtditId, "sessionId" -> "1234567890"))

          status(result) mustBe UNAUTHORIZED
        }
      }

      "must fail with a UNAUTHORIZED when authConnector returns any error" in {

        val application = applicationBuilder(userAnswers = None, isAgent = true).configure(
          "feature-switch.sessionCookieService" -> false
        ).overrides(
          bind[AuthConnector].toInstance(mockAuthConnector),
          bind[IncomeTaxSessionDataConnector].toInstance(mockSessionDataConnector)
        ).build()

        val enrolments: Enrolments = Enrolments(Set(
          Enrolment(models.Enrolment.Agent.key, Seq(EnrolmentIdentifier(models.Enrolment.Agent.value, "XARN1234567")), "Activated"),
          Enrolment(models.Enrolment.SupportingAgent.key, Seq(EnrolmentIdentifier(models.Enrolment.SupportingAgent.value, "1234567893")), "mtd-it-auth-supp")
        ))

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]
          val authResponse = Future.successful(
            new ~(new ~(Some(AffinityGroup.Agent), enrolments), ConfidenceLevel.L250)
          )

          when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]])(any(), any()))
            .thenReturn(authResponse)
          when(mockAuthConnector.authorise(predicate(testMtditId), mEq(EmptyRetrieval))(any(), any()))
            .thenReturn(Future.failed(UnsupportedCredentialRole()))
          when(mockAuthConnector.authorise(secondaryAgentPredicate(testMtditId), mEq(EmptyRetrieval))(any(), any()))
            .thenReturn(Future.failed(UnsupportedCredentialRole()))

          val authAction = new IdentifierActionProviderImpl(mockAuthConnector, appConfig,
            mockSessionDataConnector, bodyParsers)(ec).apply(taxYear)

          val controller = new Harness(authAction)

          val result = controller.onPageLoad()(FakeRequest().withSession("ClientMTDID" -> testMtditId, "sessionId" -> "1234567890"))

          status(result) mustBe UNAUTHORIZED
        }
      }

      "must fail with a redirect when there is no AGENT REFERENCE NUMBER enrolment" in {

        val application = applicationBuilder(userAnswers = None).build()

        val enrolments: Enrolments = Enrolments(Set())

        val authResponse: Option[AffinityGroup] ~ Enrolments ~ ConfidenceLevel =
          new ~(new ~(
            Some(AffinityGroup.Agent),
            enrolments),
            ConfidenceLevel.L50)

        running(application) {

          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new IdentifierActionProviderImpl(new FakeSuccessfulAuthConnector(authResponse), appConfig,
            mockSessionDataConnector, bodyParsers)(ec).apply(taxYear)
          val controller = new Harness(authAction)

          when(mockSessionDataConnector.getSessionData(any())).thenReturn(
            Future.successful(Right(Some(SessionData(testMtditId, "nino", "utr", "test-session-id"))))
          )

          val result = controller.onPageLoad()(FakeRequest().withSession("ClientMTDID" -> testMtditId, "sessionId" -> "test-session-id"))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some("https://www.gov.uk/guidance/get-an-hmrc-agent-services-account")
        }
      }
    }

    "must return UNAUTHORIZED when authConnector returns incorrect enrolments " in {
      val application = applicationBuilder(userAnswers = None).build()

      val enrolments: Enrolments = Enrolments(Set(
        Enrolment(mtdEnrollmentKey, Seq(EnrolmentIdentifier(mtdEnrollmentIdentifier, "7777777777")), "Activated"),
        Enrolment(mtdEnrollmentKey, Seq(EnrolmentIdentifier(mtdEnrollmentIdentifier, "8888888888")), "Activated"),
      ))

      val authResponse: Option[AffinityGroup] ~ Enrolments ~ ConfidenceLevel =
        new ~(new ~(
          None,
          enrolments),
          ConfidenceLevel.L50)

      running(application) {

        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val authAction = new IdentifierActionProviderImpl(new FakeSuccessfulAuthConnector(authResponse), appConfig,
          mockSessionDataConnector, bodyParsers)(ec).apply(taxYear)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(FakeRequest().withSession("ClientMTDID" -> testMtditId))

        status(result) mustBe UNAUTHORIZED
      }
    }

    "must redirect to sign in when the user does not have an active session " in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {

        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val authAction = new IdentifierActionProviderImpl(new FakeFailingAuthConnector(InvalidBearerToken()), appConfig,
          mockSessionDataConnector, bodyParsers)(ec).apply(taxYear)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(FakeRequest().withSession("ClientMTDID" -> testMtditId))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe "http://localhost:9949/auth-login-stub/gg-sign-in" +
          "?continue=http://localhost:10007/update-and-submit-income-tax-return/tailored-return/2024/start" +
          "&origin=income-tax-tailor-returns-frontend"
      }
    }

    "must return UNAUTHORIZED when authConnector returns an exception " in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {

        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val authAction = new IdentifierActionProviderImpl(new FakeFailingAuthConnector(InternalError()), appConfig,
          mockSessionDataConnector, bodyParsers)(ec).apply(taxYear)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(FakeRequest().withSession("ClientMTDID" -> "1234567890"))

        status(result) mustBe UNAUTHORIZED
      }
    }

  }
  "Auth Action [EarlyPrivateLaunchIdentifierActionProviderImpl]" - {

    "must succeed with a identifier Request" in {

      val application = new GuiceApplicationBuilder()
        .configure(Map("feature-switch.earlyPrivateLaunch" -> "true"))
        .build()

      running(application) {

        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val authAction = new EarlyPrivateLaunchIdentifierActionProviderImpl(new FakeSuccessfulAuthConnector(Some("internalId")),
          appConfig, bodyParsers)(ec).apply(taxYear)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(FakeRequest())

        status(result) mustBe OK
      }
    }

    "must return UNAUTHORIZED when authConnector returns incorrect enrolments " in {

      val application = new GuiceApplicationBuilder()
        .configure(Map("feature-switch.earlyPrivateLaunch" -> "true"))
        .build()

      val enrolments: Enrolments = Enrolments(Set(
        Enrolment(mtdEnrollmentKey, Seq(EnrolmentIdentifier(mtdEnrollmentIdentifier, "7777777777")), "Activated"),
        Enrolment(mtdEnrollmentKey, Seq(EnrolmentIdentifier(mtdEnrollmentIdentifier, "8888888888")), "Activated"),
      ))

      val authResponse: Option[AffinityGroup] ~ Enrolments ~ ConfidenceLevel =
        new ~(new ~(
          None,
          enrolments),
          ConfidenceLevel.L50)

      running(application) {

        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val authAction = new EarlyPrivateLaunchIdentifierActionProviderImpl(new FakeSuccessfulAuthConnector(authResponse),
          appConfig, bodyParsers)(ec).apply(taxYear)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(FakeRequest().withSession("ClientMTDID" -> "1234567890"))

        status(result) mustBe UNAUTHORIZED
      }
    }

    "must redirect to sign in when the user does not have an active session " in {

      val application = new GuiceApplicationBuilder()
        .configure(Map("feature-switch.earlyPrivateLaunch" -> "true"))
        .build()

      running(application) {

        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val authAction = new EarlyPrivateLaunchIdentifierActionProviderImpl(new FakeFailingAuthConnector(InvalidBearerToken()),
          appConfig, bodyParsers)(ec).apply(taxYear)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(FakeRequest().withSession("ClientMTDID" -> "1234567890"))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe "http://localhost:9949/auth-login-stub/gg-sign-in" +
          "?continue=http://localhost:10007/update-and-submit-income-tax-return/tailored-return/2024/start" +
          "&origin=income-tax-tailor-returns-frontend"
      }
    }

    "must return UNAUTHORIZED when authConnector returns an exception " in {

      val application = new GuiceApplicationBuilder()
        .configure(Map("feature-switch.earlyPrivateLaunch" -> "true"))
        .build()

      running(application) {

        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val authAction = new EarlyPrivateLaunchIdentifierActionProviderImpl(new FakeFailingAuthConnector(InternalError()),
          appConfig, bodyParsers)(ec).apply(taxYear)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(FakeRequest().withSession("ClientMTDID" -> "1234567890"))

        status(result) mustBe UNAUTHORIZED
      }
    }

  }
}

class FakeFailingAuthConnector @Inject()(exceptionToReturn: Throwable) extends AuthConnector {
  val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
    Future.failed(exceptionToReturn)
}

class FakeSuccessfulAuthConnector[T] @Inject()(value: T) extends AuthConnector {
  val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
    Future.fromTry(Try(value.asInstanceOf[A]))
}
