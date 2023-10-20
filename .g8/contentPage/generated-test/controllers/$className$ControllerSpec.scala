package controllers

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.$className$View
import views.html.$className$AgentView


class $className$ControllerSpec extends SpecBase {

  "$className$ Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.$className$Controller.onPageLoad(taxYear).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[$className$View]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(taxYear)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET for an agent" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, routes.$className$Controller.onPageLoad(taxYear).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[$className$AgentView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(taxYear)(request, messages(application)).toString
      }
    }
  }
}
