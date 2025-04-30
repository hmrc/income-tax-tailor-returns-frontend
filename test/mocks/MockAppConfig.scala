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

package mocks

import config.FrontendAppConfig
import org.scalamock.handlers.{CallHandler0, CallHandler1}
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite

trait MockAppConfig extends MockFactory {this: TestSuite =>

  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  def mockStateBenefitsBaseUrl(response: String): CallHandler0[String] =
    (() => mockAppConfig.stateBenefitsBaseUrl)
      .expects()
      .returning(response)

  def mockCisBaseUrl(response: String): CallHandler0[String] =
    (() => mockAppConfig.cisBaseUrl)
      .expects()
      .returning(response)

  def mockEmploymentBaseUrl(response: String): CallHandler0[String] =
    (() => mockAppConfig.employmentBackendBaseUrl)
      .expects()
      .returning(response)

  def mockPropertyBaseUrl(response: String): CallHandler0[String] =
    (() => mockAppConfig.propertyBaseUrl)
      .expects()
      .returning(response)

  def mockSessionServiceBaseUrl(response: String): CallHandler0[String] =
    (() => mockAppConfig.vcSessionServiceBaseUrl)
      .expects()
      .returning(response)

  def mockSessionServiceEnabled(response: Boolean): CallHandler0[Boolean] =
    (() => mockAppConfig.sessionCookieServiceEnabled)
      .expects()
      .returning(response)

  def mockPrePopEnabled(response: Boolean): CallHandler0[Boolean] = {
    (() => mockAppConfig.isPrePopEnabled)
      .expects()
      .returning(response)
  }

  def mockIncomeTaxSubmissionIvRedirect(response: String): CallHandler0[String] = {
    (() => mockAppConfig.incomeTaxSubmissionIvRedirect)
      .expects()
      .returning(response)
  }

  def mockLoginRedirect(response: String): CallHandler1[Int, String] = {
    (mockAppConfig.loginUrl(_: Int))
      .expects(*)
      .returning(response)
  }

  def mockSignUpRedirect(response: String): CallHandler0[String] = {
    (() => mockAppConfig.signUpUrlIndividual)
      .expects()
      .returning(response)
  }

    def mockSetUpAgentServicesAccountUrl(response: String): CallHandler0[String] = {
      (() => mockAppConfig.setUpAgentServicesAccountUrl())
        .expects()
        .returning(response)
  }

  def mockViewAndChangeEnterUtrUrl(response: String): CallHandler0[String] = {
    (() => mockAppConfig.viewAndChangeEnterUtrUrl)
      .expects()
      .returning(response)
  }

  def mockLoginUrl(response: String): CallHandler1[Int, String] = {
    (mockAppConfig.loginUrl(_: Int))
      .expects(*)
      .returning(response)
  }
}
