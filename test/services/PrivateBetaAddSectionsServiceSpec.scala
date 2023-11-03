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

package services

import base.SpecBase
import models.TagStatus.{CannotStartYet, Completed, NotStarted}
import models.{SectionState, UserAnswers}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import pages.aboutyou.FosterCarerPage
import play.api.test.Helpers.running

class PrivateBetaAddSectionsServiceSpec extends AnyFreeSpec
  with SpecBase
  with Matchers
  with MockitoSugar
  with OptionValues
  with ScalaFutures
  with BeforeAndAfterEach {

  private val privateBetaEnabled = Map("features.privateBeta" -> "true")

  private val aboutYouCompleteInBeta = Some(UserAnswers(mtdItId, taxYear).set(FosterCarerPage, true).success.value)

  ".getState must" - {

    "when privateBeta is enabled" - {

      "return a model with the initial page state when no data is found" in {
        val application = applicationBuilder()
          .configure(privateBetaEnabled)
          .build()

        running(application) {

          val service = application.injector.instanceOf[PrivateBetaAddSectionsService]

          val model = service.getState(None)
          val expectedResult = SectionState(NotStarted, CannotStartYet, CannotStartYet, CannotStartYet)

          model mustBe expectedResult
        }
      }

      "return aboutYou section as Completed when data is found for last page" in {
        val application = applicationBuilder()
          .configure(privateBetaEnabled)
          .build()

        running(application) {

          val service = application.injector.instanceOf[PrivateBetaAddSectionsService]

          val model = service.getState(aboutYouCompleteInBeta)
          val expectedResult = SectionState(Completed, NotStarted, CannotStartYet, CannotStartYet)

          model mustBe expectedResult
        }
      }

      "return aboutYou section as NotStarted when no data is found for last page" in {
        val application = applicationBuilder()
          .configure(privateBetaEnabled)
          .build()

        running(application) {

          val service = application.injector.instanceOf[PrivateBetaAddSectionsService]

          val model = service.getState(aboutYouCompleteInBeta)
          val expectedResult = SectionState(Completed, NotStarted, CannotStartYet, CannotStartYet)

          model mustBe expectedResult
        }
      }

      "return incomeFromWork section as CannotStartYet when aboutYou section is incomplete" in {
        val application = applicationBuilder()
          .configure(privateBetaEnabled)
          .build()

        running(application) {

          val service = application.injector.instanceOf[PrivateBetaAddSectionsService]

          val model = service.getState(Some(emptyUserAnswers))
          val expectedResult = SectionState(NotStarted, CannotStartYet, CannotStartYet, CannotStartYet)

          model mustBe expectedResult
        }
      }

      "return incomeFromWork section as NotStarted when aboutYou section is complete" in {
        val application = applicationBuilder()
          .configure(privateBetaEnabled)
          .build()

        running(application) {

          val service = application.injector.instanceOf[PrivateBetaAddSectionsService]

          val model = service.getState(aboutYouCompleteInBeta)
          val expectedResult = SectionState(Completed, NotStarted, CannotStartYet, CannotStartYet)

          model mustBe expectedResult
        }
      }
    }
  }
}
