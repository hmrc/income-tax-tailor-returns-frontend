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
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{Action, AnyContent, BodyParsers, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class AuthActionSpec extends SpecBase {

  class Harness(authAction: IdentifierAction) {
    def onPageLoad(): Action[AnyContent] = authAction { _ => Results.Ok }
  }

  val mtdEnrollmentKey = "HMRC-MTD-IT"
  val mtdEnrollmentIdentifier = "MTDITID"

  "Auth Action [IdentifierActionProviderImpl]" - {

    "when the user is an Organisation" - {

      "must succeed with a identifier Request when fully authenticated" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {

          val enrolments: Enrolments = Enrolments(Set(
            Enrolment(mtdEnrollmentKey, Seq(EnrolmentIdentifier(mtdEnrollmentIdentifier, "1234567890")), "Activated")
          ))

          val authResponse: Option[AffinityGroup] ~ Enrolments ~ ConfidenceLevel =
            new ~(new ~(
              Some(AffinityGroup.Organisation),
              enrolments),
              ConfidenceLevel.L250)

          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new IdentifierActionProviderImpl(new FakeSuccessfulAuthConnector(authResponse), appConfig, bodyParsers)(ec).apply(taxYear)
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

          val authAction = new IdentifierActionProviderImpl(new FakeSuccessfulAuthConnector(authResponse), appConfig, bodyParsers)(ec).apply(taxYear)
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

          val authAction = new IdentifierActionProviderImpl(new FakeSuccessfulAuthConnector(authResponse), appConfig, bodyParsers)(ec).apply(taxYear)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe UNAUTHORIZED
        }
      }

      "must fail with a Redirect to ivUplift when confidence is to low" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {

          val enrolments: Enrolments = Enrolments(Set(
            Enrolment(mtdEnrollmentKey, Seq(EnrolmentIdentifier(mtdEnrollmentIdentifier, "1234567890")), "Activated")
          ))

          val authResponse: Option[AffinityGroup] ~ Enrolments ~ ConfidenceLevel =
            new ~(new ~(
                Some(AffinityGroup.Individual),
              enrolments),
              ConfidenceLevel.L200)

          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new IdentifierActionProviderImpl(new FakeSuccessfulAuthConnector(authResponse), appConfig, bodyParsers)(ec).apply(taxYear)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some("http://localhost:9302/update-and-submit-income-tax-return/iv-uplift")
        }
      }
    }
    "when the user is authorised as an agent" - {

      "must succeed with a identifier Request" in {

        val application = applicationBuilder(userAnswers = None).build()

        val enrolments: Enrolments = Enrolments(Set(
          Enrolment(mtdEnrollmentKey, Seq(EnrolmentIdentifier(mtdEnrollmentIdentifier, "7777777777")), "Activated"),
          Enrolment(mtdEnrollmentKey, Seq(EnrolmentIdentifier(mtdEnrollmentIdentifier, "8888888888")), "Activated"),
          Enrolment(mtdEnrollmentKey, Seq(EnrolmentIdentifier(mtdEnrollmentIdentifier, "1234567890")), "Activated")
        ) + Enrolment(models.Enrolment.Agent.key, Seq(EnrolmentIdentifier(models.Enrolment.Agent.value, "XARN1234567")), "Activated"))

        val authResponse: Option[AffinityGroup] ~ Enrolments ~ ConfidenceLevel =
          new ~(new ~(
            Some(AffinityGroup.Agent),
            enrolments),
            ConfidenceLevel.L50)

        running(application) {

          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new IdentifierActionProviderImpl(new FakeSuccessfulAuthConnector(authResponse), appConfig, bodyParsers)(ec).apply(taxYear)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest().withSession("ClientMTDID" -> "1234567890"))

          status(result) mustBe OK
        }
      }
      "must fail with a UNAUTHORIZED when missing ClientMTDID from User Session" in {

        val application = applicationBuilder(userAnswers = None).build()

        val enrolments: Enrolments = Enrolments(Set(
          Enrolment(mtdEnrollmentKey, Seq(EnrolmentIdentifier(mtdEnrollmentIdentifier, "7777777777")), "Activated"),
          Enrolment(mtdEnrollmentKey, Seq(EnrolmentIdentifier(mtdEnrollmentIdentifier, "8888888888")), "Activated"),
          Enrolment(mtdEnrollmentKey, Seq(EnrolmentIdentifier(mtdEnrollmentIdentifier, "1234567890")), "Activated")
        ) + Enrolment(models.Enrolment.Agent.key, Seq(EnrolmentIdentifier(models.Enrolment.Agent.value, "XARN1234567")), "Activated"))

        val authResponse: Option[AffinityGroup] ~ Enrolments ~ ConfidenceLevel =
          new ~(new ~(
            Some(AffinityGroup.Agent),
            enrolments),
            ConfidenceLevel.L50)

        running(application) {

          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new IdentifierActionProviderImpl(new FakeSuccessfulAuthConnector(authResponse), appConfig, bodyParsers)(ec).apply(taxYear)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe UNAUTHORIZED
        }
      }
      "must fail with a UNAUTHORIZED when ClientMTDID from User Session does not exist in users enrolments" in {

        val application = applicationBuilder(userAnswers = None).build()

        val enrolments: Enrolments = Enrolments(Set(
          Enrolment(mtdEnrollmentKey, Seq(EnrolmentIdentifier(mtdEnrollmentIdentifier, "7777777777")), "Activated"),
          Enrolment(mtdEnrollmentKey, Seq(EnrolmentIdentifier(mtdEnrollmentIdentifier, "8888888888")), "Activated"),
        ) + Enrolment(models.Enrolment.Agent.key, Seq(EnrolmentIdentifier(models.Enrolment.Agent.value, "XARN1234567")), "Activated"))

        val authResponse: Option[AffinityGroup] ~ Enrolments ~ ConfidenceLevel =
          new ~(new ~(
            Some(AffinityGroup.Agent),
            enrolments),
            ConfidenceLevel.L50)

        running(application) {

          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new IdentifierActionProviderImpl(new FakeSuccessfulAuthConnector(authResponse), appConfig, bodyParsers)(ec).apply(taxYear)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest().withSession("ClientMTDID" -> "1234567890"))

          status(result) mustBe UNAUTHORIZED
        }
      }

      "must fail with a UNAUTHORIZED when there is no AGENT REFERENCE NUMBER enrolment" in {

        val application = applicationBuilder(userAnswers = None).build()

        val enrolments: Enrolments = Enrolments(Set(
          Enrolment(mtdEnrollmentKey, Seq(EnrolmentIdentifier(mtdEnrollmentIdentifier, "7777777777")), "Activated"),
          Enrolment(mtdEnrollmentKey, Seq(EnrolmentIdentifier(mtdEnrollmentIdentifier, "8888888888")), "Activated"),
        ))

        val authResponse: Option[AffinityGroup] ~ Enrolments ~ ConfidenceLevel =
          new ~(new ~(
            Some(AffinityGroup.Agent),
            enrolments),
            ConfidenceLevel.L50)

        running(application) {

          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new IdentifierActionProviderImpl(new FakeSuccessfulAuthConnector(authResponse), appConfig, bodyParsers)(ec).apply(taxYear)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest().withSession("ClientMTDID" -> "1234567890"))

          status(result) mustBe UNAUTHORIZED
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

        val authAction = new IdentifierActionProviderImpl(new FakeSuccessfulAuthConnector(authResponse), appConfig, bodyParsers)(ec).apply(taxYear)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(FakeRequest().withSession("ClientMTDID" -> "1234567890"))

        status(result) mustBe UNAUTHORIZED
      }
    }

    "must redirect to sign in when the user does not have an active session " in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {

        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val authAction = new IdentifierActionProviderImpl(new FakeFailingAuthConnector(InvalidBearerToken()), appConfig, bodyParsers)(ec).apply(taxYear)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(FakeRequest().withSession("ClientMTDID" -> "1234567890"))

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

        val authAction = new IdentifierActionProviderImpl(new FakeFailingAuthConnector(InternalError()), appConfig, bodyParsers)(ec).apply(taxYear)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(FakeRequest().withSession("ClientMTDID" -> "1234567890"))

        status(result) mustBe UNAUTHORIZED
      }
    }

  }
  "Auth Action [EarlyPrivateLaunchIdentifierActionProviderImpl]" - {

    "must succeed with a identifier Request" in {

      val application = new GuiceApplicationBuilder()
        .configure(Map("features.earlyPrivateLaunch" -> "true"))
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
        .configure(Map("features.earlyPrivateLaunch" -> "true"))
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
        .configure(Map("features.earlyPrivateLaunch" -> "true"))
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
        .configure(Map("features.earlyPrivateLaunch" -> "true"))
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
