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
import mocks.{MockAppConfig, MockSessionDataConnector}
import models.authorisation.Enrolment
import models.requests.IdentifierRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.{ArgumentMatchers, MockitoSugar}
import play.api.http.{HeaderNames, Status}
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test.{FakeRequest, ResultExtractors}
import services.SessionDataService
import uk.gov.hmrc.auth.core.ConfidenceLevel.{L250, L50}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, allEnrolments, confidenceLevel}
import uk.gov.hmrc.auth.core.retrieve.{EmptyRetrieval, ~}
import uk.gov.hmrc.auth.core.{Enrolment => HMRCEnrolment, _}
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}

import scala.concurrent.Future

class AuthActionSpec extends SpecBase
  with ResultExtractors
  with HeaderNames
  with Status
  with Results
  with MockitoSugar
  with MockAppConfig
  with MockSessionDataConnector {

  trait Test {
    val taxYear = 2025
    val mockParser: BodyParsers.Default = MockitoSugar.mock[BodyParsers.Default]

    type AuthReturnType = Option[AffinityGroup] ~ Enrolments ~ ConfidenceLevel

    implicit val hc: HeaderCarrier = HeaderCarrier().copy(sessionId = Some(SessionId("sessionId")))
    implicit val request: Request[AnyContent] = FakeRequest()

    val mockAuthConnector: AuthConnector = MockitoSugar.mock[AuthConnector]
    val mockSessionDataService: SessionDataService = new SessionDataService(mockSessionDataConnector, mockAppConfig)
    
    val testAuth: AuthenticatedIdentifierAction = new AuthenticatedIdentifierAction(
      taxYear = taxYear
    )(
      authConnector = mockAuthConnector,
      config = mockAppConfig,
      sessionDataService = mockSessionDataService,
      parser = mockParser
    )

    val testPrivateAuth: EarlyPrivateLaunchIdentifierAction = new EarlyPrivateLaunchIdentifierAction(
      taxYear = taxYear
    )(
      authConnector = mockAuthConnector,
      sessionDataService = mockSessionDataService,
      config = mockAppConfig,
      parser = mockParser
    )
  }
  
  "AuthenticatedIdentifierAction" - {
    "getEnrolmentValueOpt" - {
      "should return the expected value when a key exists within the given enrolments" in new Test {
        val testEnrolments: Enrolments = Enrolments(
          Set(HMRCEnrolment("dummyKey", Seq(EnrolmentIdentifier("dummyIdentifier", "value")), "activated"))
        )

        testAuth.getEnrolmentValueOpt("dummyKey", "dummyIdentifier", testEnrolments) mustBe Some("value")
      }

      "should return the expected value when multiple keys exist within the given enrolments" in new Test {
        val testEnrolments: Enrolments = Enrolments(
          Set(
            HMRCEnrolment("dummyKey", Seq(EnrolmentIdentifier("dummyIdentifier", "value")), "activated"),
            HMRCEnrolment("dummyKey", Seq(EnrolmentIdentifier("dummyIdentifier2", "value2")), "activated")
          )
        )

        testAuth.getEnrolmentValueOpt("dummyKey", "dummyIdentifier", testEnrolments) mustBe Some("value")
      }
      
      "should return none when a key doesn't exist within the given enrolment" in new Test {
        val testEnrolments: Enrolments = Enrolments(Set())
        testAuth.getEnrolmentValueOpt("dummyKey", "dummyIdentifier", testEnrolments) mustBe None
      }
    }

    "sessionIdBlock" - {
      "should process block when sessionId is present in the header carrier" in new Test {
        val result: Future[Result] = testAuth.sessionIdBlock(
          extraLoggingContext = "dummyLoggingString",
          errorLogString = "N/A",
          errorAction = Future.successful(ImATeapot("Teapot time"))
        )(
          block = (sessionId: String) => Future.successful(Ok(sessionId))
        )

        status(result) mustBe OK
        contentAsString(result) mustBe "sessionId"
      }

      "should process block when sessionId is present in the request headers" in new Test {
        override implicit val hc: HeaderCarrier = HeaderCarrier()
        override implicit val request: Request[AnyContent] = FakeRequest().withHeaders(("sessionId", "sessionId"))

        val result: Future[Result] = testAuth.sessionIdBlock(
          extraLoggingContext = "dummyLoggingString",
          errorLogString = "N/A",
          errorAction = Future.successful(ImATeapot("Teapot time"))
        )(
          block = (sessionId: String) => Future.successful(Ok(sessionId))
        )

        status(result) mustBe OK
        contentAsString(result) mustBe "sessionId"
      }

      "should return error when sessionId is not present" in new Test {
        override implicit val hc: HeaderCarrier = HeaderCarrier()

        val result: Future[Result] = testAuth.sessionIdBlock(
          extraLoggingContext = "dummyLoggingString",
          errorLogString = "N/A",
          errorAction = Future.successful(ImATeapot("Teapot time"))
        )(
          block = (sessionId: String) => Future.successful(Ok(sessionId))
        )

        status(result) mustBe IM_A_TEAPOT
        contentAsString(result) mustBe "Teapot time"
      }
    }

    "invokeBlock" - {
      "should handle appropriately when call to authorisation fails with NoActiveSession error" in new Test {
        mockLoginUrl("dummyUrl")

        when(mockAuthConnector.authorise(any(), any())(any(), any()))
          .thenReturn(Future.failed(BearerTokenExpired("AnError")))

        val result: Future[Result] = testAuth.invokeBlock(
          request = request,
          block = (_: IdentifierRequest[_]) => Future.successful(ImATeapot("Teapot time"))
        )

        status(result) mustBe SEE_OTHER
        redirectLocation(result).getOrElse("None Found") mustBe "dummyUrl"
      }

      "should handle appropriately when call to authorisation fails with any unhandled error" in new Test {
        when(mockAuthConnector.authorise(any(), any())(any(), any()))
          .thenReturn(Future.failed(new RuntimeException("Dummy exception")))

        val result: Future[Result] = testAuth.invokeBlock(
          request = request,
          block = (_: IdentifierRequest[_]) => Future.successful(ImATeapot("Teapot time"))
        )

        status(result) mustBe UNAUTHORIZED
      }

      "should handle appropriately when no affinity group is returned from authorisation call" in new Test {
        val partialAuthResponse: Option[AffinityGroup] ~ Enrolments = new ~(
          Option.empty[AffinityGroup],
          Enrolments(Set.empty[HMRCEnrolment])
        )

        val authResponse: AuthReturnType = new ~(
          partialAuthResponse,
          ConfidenceLevel.L250
        )

        when(mockAuthConnector.authorise[AuthReturnType](any(), any())(any(), any()))
          .thenReturn(Future.successful(authResponse))

        val result: Future[Result] = testAuth.invokeBlock(
          request = request,
          block = (_: IdentifierRequest[_]) => Future.successful(ImATeapot("Teapot time"))
        )

        status(result) mustBe UNAUTHORIZED
      }

      "should return expected result for Individual happy path" in new Test {
        mockLoginUrl("dummyUrl")

        val partialAuthResponse: Option[AffinityGroup] ~ Enrolments = new ~(
          Some(AffinityGroup.Individual),
          Enrolments(Set(
            HMRCEnrolment(
              key = Enrolment.Nino.key,
              identifiers = Seq(EnrolmentIdentifier(Enrolment.Nino.value, "AA111111A")),
              state = "activated"
            ),
            HMRCEnrolment(
              key = Enrolment.Individual.key,
              identifiers = Seq(EnrolmentIdentifier(Enrolment.Individual.value, "12345678")),
              state = "activated"
            )
          ))
        )

        val authResponse: Option[AffinityGroup] ~ Enrolments ~ ConfidenceLevel = new ~(
          partialAuthResponse,
          ConfidenceLevel.L250
        )

        when(mockAuthConnector.authorise[AuthReturnType](any(), any())(any(), any()))
          .thenReturn(Future.successful(authResponse))

        val result: Future[Result] = testAuth.invokeBlock(
          request = FakeRequest().withHeaders(("sessionId", "sessionId")),
          block = (_: IdentifierRequest[_]) => Future.successful(ImATeapot("Teapot time"))
        )

        status(result) mustBe IM_A_TEAPOT
        contentAsString(result) mustBe "Teapot time"
      }

      "should return expected result for Organisation happy path" in new Test {
        mockLoginUrl("dummyUrl")

        val partialAuthResponse: Option[AffinityGroup] ~ Enrolments = new ~(
          Some(AffinityGroup.Organisation),
          Enrolments(Set(
            HMRCEnrolment(
              key = Enrolment.Nino.key,
              identifiers = Seq(EnrolmentIdentifier(Enrolment.Nino.value, "AA111111A")),
              state = "activated"
            ),
            HMRCEnrolment(
              key = Enrolment.Individual.key,
              identifiers = Seq(EnrolmentIdentifier(Enrolment.Individual.value, "12345678")),
              state = "activated"
            )
          ))
        )

        val authResponse: Option[AffinityGroup] ~ Enrolments ~ ConfidenceLevel = new ~(
          partialAuthResponse,
          ConfidenceLevel.L250
        )

        when(mockAuthConnector.authorise[AuthReturnType](any(), any())(any(), any()))
          .thenReturn(Future.successful(authResponse))

        val result: Future[Result] = testAuth.invokeBlock(
          request = FakeRequest().withHeaders(("sessionId", "sessionId")),
          block = (_: IdentifierRequest[_]) => Future.successful(ImATeapot("Teapot time"))
        )

        status(result) mustBe IM_A_TEAPOT
        contentAsString(result) mustBe "Teapot time"
      }

      "should return expected result for Agent happy path" in new Test {
        val partialAuthResponse: Option[AffinityGroup] ~ Enrolments = new ~(
          Some(AffinityGroup.Agent),
          Enrolments(Set(HMRCEnrolment(
            key = Enrolment.Agent.key,
            identifiers = Seq(EnrolmentIdentifier(Enrolment.Agent.value, "value")),
            state = "activated"
          )))
        )

        val authResponse: Option[AffinityGroup] ~ Enrolments ~ ConfidenceLevel = new ~(
          partialAuthResponse,
          ConfidenceLevel.L250
        )

        when(mockAuthConnector.authorise[AuthReturnType](
          predicate = any(),
          retrieval = ArgumentMatchers.eq(affinityGroup and allEnrolments and confidenceLevel)
        )(any(), any()))
          .thenReturn(Future.successful(authResponse))

        when(mockAuthConnector.authorise[Unit](
          predicate = any(),
          retrieval = ArgumentMatchers.eq(EmptyRetrieval)
        )(any(), any()))
          .thenReturn(Future.successful())

        mockSessionServiceEnabled(true)
        mockGetSessionData(Right(Some(dummySessionData)))

        val result: Future[Result] = testAuth.invokeBlock(
          request = FakeRequest().withHeaders(("sessionId", "sessionId")),
          block = (_: IdentifierRequest[_]) => Future.successful(ImATeapot("Teapot time"))
        )

        status(result) mustBe IM_A_TEAPOT
        contentAsString(result) mustBe "Teapot time"
      }
    }

    "nonAgentAuthentication" - {
      "should return a redirect when user confidence level is below 250" in new Test {
        mockIncomeTaxSubmissionIvRedirect("dummyRedirect")

        val result: Future[Result] = testAuth.nonAgentAuthentication(
          block = (_: IdentifierRequest[_]) => Future.successful(ImATeapot("This should be impossible")),
          enrolments = Enrolments(Set.empty[HMRCEnrolment]),
          confidenceLevel = L50,
          extraLoggingContext = "N/A"
        )

        status(result) mustBe SEE_OTHER
        redirectLocation(result).getOrElse("None found") mustBe "dummyRedirect"
      }

      "should return a redirect when NINO cannot be found in enrolments" in new Test {
        mockLoginRedirect("loginUrl")

        val result: Future[Result] = testAuth.nonAgentAuthentication(
          block = (_: IdentifierRequest[_]) => Future.successful(ImATeapot("This should be impossible")),
          enrolments = Enrolments(Set(
            HMRCEnrolment(
              key = Enrolment.Individual.key,
              identifiers = Seq(EnrolmentIdentifier(Enrolment.Individual.value, "12345678")),
              state = "activated"
            )
          )),
          confidenceLevel = L250,
          extraLoggingContext = "N/A"
        )

        status(result) mustBe SEE_OTHER
        redirectLocation(result).getOrElse("None found") mustBe "loginUrl"
      }

      "should return a redirect when MTD IT ID cannot be found in enrolments" in new Test {
        mockSignUpRedirect("signUp")

        val result: Future[Result] = testAuth.nonAgentAuthentication(
          block = (_: IdentifierRequest[_]) => Future.successful(ImATeapot("This should be impossible")),
          enrolments = Enrolments(Set(
            HMRCEnrolment(
              key = Enrolment.Nino.key,
              identifiers = Seq(EnrolmentIdentifier(Enrolment.Nino.value, "AA111111A")),
              state = "activated"
            )
          )),
          confidenceLevel = L250,
          extraLoggingContext = "N/A"
        )

        status(result) mustBe SEE_OTHER
        redirectLocation(result).getOrElse("None found") mustBe "signUp"
      }

      "should return a redirect when session ID cannot be found in request" in new Test {
        mockLoginRedirect("loginUrl")
        override implicit val hc: HeaderCarrier = HeaderCarrier()

        val result: Future[Result] = testAuth.nonAgentAuthentication(
          block = (_: IdentifierRequest[_]) => Future.successful(ImATeapot("This should be impossible")),
          enrolments = Enrolments(Set(
            HMRCEnrolment(
              key = Enrolment.Nino.key,
              identifiers = Seq(EnrolmentIdentifier(Enrolment.Nino.value, "AA111111A")),
              state = "activated"
            ),
            HMRCEnrolment(
              key = Enrolment.Individual.key,
              identifiers = Seq(EnrolmentIdentifier(Enrolment.Individual.value, "12345678")),
              state = "activated"
            )
          )),
          confidenceLevel = L250,
          extraLoggingContext = "N/A"
        )

        status(result) mustBe SEE_OTHER
        redirectLocation(result).getOrElse("None found") mustBe "loginUrl"
      }

      "should return correct result for happy path" in new Test {
        mockLoginUrl("dummyUrl")

        val result: Future[Result] = testAuth.nonAgentAuthentication(
          block = (_: IdentifierRequest[_]) => Future.successful(ImATeapot("Teapot time")),
          enrolments = Enrolments(Set(
            HMRCEnrolment(
              key = Enrolment.Nino.key,
              identifiers = Seq(EnrolmentIdentifier(Enrolment.Nino.value, "AA111111A")),
              state = "activated"
            ),
            HMRCEnrolment(
              key = Enrolment.Individual.key,
              identifiers = Seq(EnrolmentIdentifier(Enrolment.Individual.value, "12345678")),
              state = "activated"
            )
          )),
          confidenceLevel = L250,
          extraLoggingContext = "N/A"
        )

        status(result) mustBe IM_A_TEAPOT
        contentAsString(result) mustBe "Teapot time"
      }
    }

    "agentAuth" - {
      "should return an error when session data service returns no data" in new Test {
        mockSessionServiceEnabled(false)
        mockFallbackEnabled(false)
        mockViewAndChangeEnterUtrUrl("dummyUrl")

        val result: Future[Result] = testAuth.agentAuth(
          block = (_: IdentifierRequest[_]) => Future.successful(ImATeapot("Should not be possible")),
          enrolments = Enrolments(Set())
        )

        status(result) mustBe SEE_OTHER
        redirectLocation(result).getOrElse("None Found") mustBe "dummyUrl"
      }

      "should return an error when agent enrolment can't be found" in new Test {
        mockSessionServiceEnabled(true)
        mockGetSessionData(Right(Some(dummySessionData)))
        mockSetUpAgentServicesAccountUrl("dummyUrl")

        val result: Future[Result] = testAuth.agentAuth(
          block = (_: IdentifierRequest[_]) => Future.successful(ImATeapot("Should not be possible")),
          enrolments = Enrolments(Set())
        )

        status(result) mustBe SEE_OTHER
        redirectLocation(result).getOrElse("None found") mustBe "dummyUrl"
      }

      "when authorisation fails" - {
        "should handle appropriately for NoActiveSession error" in new Test {
          when(mockAuthConnector.authorise(any(), any())(any(), any()))
            .thenReturn(Future.failed(MissingBearerToken("AnError")))

          mockSessionServiceEnabled(true)
          mockGetSessionData(Right(Some(dummySessionData)))
          mockViewAndChangeEnterUtrUrl("dummyUrl")

          val result: Future[Result] = testAuth.agentAuth(
            block = (_: IdentifierRequest[_]) => Future.successful(ImATeapot("Should not be possible")),
            enrolments = Enrolments(Set(
              HMRCEnrolment(
                key = Enrolment.Agent.key,
                identifiers = Seq(EnrolmentIdentifier(Enrolment.Agent.value, "value")),
                state = "activated"
              )
            ))
          )

          status(result) mustBe SEE_OTHER
          redirectLocation(result).getOrElse("None Found") mustBe "dummyUrl"
        }

        "when an AuthorisationException occurs" - {
          "should handle appropriately when secondary agent auth succeeds" in new Test {
            when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
              .thenReturn(Future.failed(InternalError("AnError")))
              .andThen(Future.successful(()))

            mockSessionServiceEnabled(true)
            mockGetSessionData(Right(Some(dummySessionData)))

            val result: Future[Result] = testAuth.agentAuth(
              block = (_: IdentifierRequest[_]) => Future.successful(ImATeapot("Should not be possible")),
              enrolments = Enrolments(Set(
                HMRCEnrolment(
                  key = Enrolment.Agent.key,
                  identifiers = Seq(EnrolmentIdentifier(Enrolment.Agent.value, "value")),
                  state = "activated"
                )
              ))
            )

            status(result) mustBe SEE_OTHER
            redirectLocation(result).getOrElse("None found") mustBe
              controllers.routes.SupportingAgentAuthErrorController.show.url
          }

          "[Secondary Agent] should handle appropriately for an AuthorisationException" in new Test {
            when(mockAuthConnector.authorise(any(), any())(any(), any()))
              .thenReturn(Future.failed(InternalError("AnError")))
              .andThen(Future.failed(InternalError("An Error")))

            mockSessionServiceEnabled(true)
            mockGetSessionData(Right(Some(dummySessionData)))

            val result: Future[Result] = testAuth.agentAuth(
              block = (_: IdentifierRequest[_]) => Future.successful(ImATeapot("Should not be possible")),
              enrolments = Enrolments(Set(
                HMRCEnrolment(
                  key = Enrolment.Agent.key,
                  identifiers = Seq(EnrolmentIdentifier(Enrolment.Agent.value, "value")),
                  state = "activated"
                )
              ))
            )

            status(result) mustBe UNAUTHORIZED
          }

          "[Secondary Agent] should handle appropriately for any unhandled errors" in new Test {
            when(mockAuthConnector.authorise(any(), any())(any(), any()))
              .thenReturn(Future.failed(InternalError("AnError")))
              .andThen(Future.failed(new RuntimeException("AnError")))

            mockSessionServiceEnabled(true)
            mockGetSessionData(Right(Some(dummySessionData)))

            val result: Future[Result] = testAuth.agentAuth(
              block = (_: IdentifierRequest[_]) => Future.successful(ImATeapot("Should not be possible")),
              enrolments = Enrolments(Set(
                HMRCEnrolment(
                  key = Enrolment.Agent.key,
                  identifiers = Seq(EnrolmentIdentifier(Enrolment.Agent.value, "value")),
                  state = "activated"
                )
              ))
            )

            status(result) mustBe INTERNAL_SERVER_ERROR
          }
        }

        "should handle appropriately for any unhandled errors" in new Test {
          when(mockAuthConnector.authorise(any(), any())(any(), any()))
            .thenReturn(Future.failed(new RuntimeException("AnError")))

          mockSessionServiceEnabled(true)
          mockGetSessionData(Right(Some(dummySessionData)))

          val result: Future[Result] = testAuth.agentAuth(
            block = (_: IdentifierRequest[_]) => Future.successful(ImATeapot("Should not be possible")),
            enrolments = Enrolments(Set(
              HMRCEnrolment(
                key = Enrolment.Agent.key,
                identifiers = Seq(EnrolmentIdentifier(Enrolment.Agent.value, "value")),
                state = "activated"
              )
            ))
          )

          status(result) mustBe INTERNAL_SERVER_ERROR
        }
      }

      "should return correct result for happy path" in new Test {
        when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
          .thenReturn(Future.successful())

        mockSessionServiceEnabled(true)
        mockGetSessionData(Right(Some(dummySessionData)))

        val result: Future[Result] = testAuth.agentAuth(
          block = (_: IdentifierRequest[_]) => Future.successful(ImATeapot("Teapot time")),
          enrolments = Enrolments(Set(
            HMRCEnrolment(
              key = Enrolment.Agent.key,
              identifiers = Seq(EnrolmentIdentifier(Enrolment.Agent.value, "value")),
              state = "activated"
            )
          ))
        )

        status(result) mustBe IM_A_TEAPOT
        contentAsString(result) mustBe "Teapot time"
      }
    }
  }

  "EarlyPrivateLaunchIdentifierAction" - {
    "invokeBlock" - {
      "should return an error when session data service returns no data" in new Test {
        mockSessionServiceEnabled(false)
        mockFallbackEnabled(false)
        mockViewAndChangeEnterUtrUrl("dummyUrl")

        val result: Future[Result] = testPrivateAuth.invokeBlock(
          request = request,
          block = (_: IdentifierRequest[_]) => Future.successful(ImATeapot("Not possible"))
        )

        status(result) mustBe SEE_OTHER
        redirectLocation(result).getOrElse("None Found") mustBe "dummyUrl"
      }

      "should handle appropriately for a NoActiveSession error" in new Test {
        mockLoginUrl("dummyUrl")
        mockSessionServiceEnabled(true)
        mockGetSessionData(Right(Some(dummySessionData)))

        when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.failed(BearerTokenExpired("AnError")))

        val result: Future[Result] = testPrivateAuth.invokeBlock(
          request = request,
          block = (_: IdentifierRequest[_]) => Future.successful(ImATeapot("Not possible"))
        )

        status(result) mustBe SEE_OTHER
        redirectLocation(result).getOrElse("None found") mustBe "dummyUrl"
      }

      "should handle appropriately for any unhandled error" in new Test {
        mockSessionServiceEnabled(true)
        mockGetSessionData(Right(Some(dummySessionData)))

        when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.failed(new RuntimeException("AnError")))

        val result: Future[Result] = testPrivateAuth.invokeBlock(
          request = request,
          block = (_: IdentifierRequest[_]) => Future.successful(ImATeapot("Not possible"))
        )

        status(result) mustBe UNAUTHORIZED
      }

      "should handle appropriately when internalId cannot be found" in new Test {
        mockSessionServiceEnabled(true)
        mockGetSessionData(Right(Some(dummySessionData)))

        when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.successful(None))

        val result: Future[Result] = testPrivateAuth.invokeBlock(
          request = request,
          block = (_: IdentifierRequest[_]) => Future.successful(ImATeapot("Not possible"))
        )

        status(result) mustBe UNAUTHORIZED
      }

      "should handle appropriately for happy path" in new Test {
        mockSessionServiceEnabled(true)
        mockGetSessionData(Right(Some(dummySessionData)))

        when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.successful(Some("someId")))

        val result: Future[Result] = testPrivateAuth.invokeBlock(
          request = request,
          block = (_: IdentifierRequest[_]) => Future.successful(ImATeapot("Teapot time"))
        )

        status(result) mustBe IM_A_TEAPOT
        contentAsString(result) mustBe "Teapot time"
      }
    }
  }
}
