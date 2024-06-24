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

package controllers.testonly

import base.SpecBase
import models.Done
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserDataService

import scala.concurrent.Future

class TestOnlyAddDataSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  "TestOnlyAddData Controller" - {

    "must return OK and the correct view for a GET" in {

      val mockUserDataService = mock[UserDataService]

      when(mockUserDataService.setWithoutUpdate(any())(any())) thenReturn Future.successful(Done)

      val application =
        applicationBuilder(userAnswers = Some(fullUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[UserDataService].toInstance(mockUserDataService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.TestOnlyAddData.testOnlyAdd(taxYear).url).withSession(validTaxYears)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        await(result).header.headers.get(LOCATION) mustBe Some(controllers.routes.StartController.onPageLoad(taxYear).url)
      }
    }
  }
}
