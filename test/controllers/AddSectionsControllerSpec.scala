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

package controllers

import base.SpecBase
import models.NormalMode
import models.SectionNames._
import models.TagStatus.{CannotStartYet, NotStarted}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.{Link, Task}
import views.html.{AddSectionsAgentView, AddSectionsView}


class AddSectionsControllerSpec extends SpecBase {

  private val addSectionsKey = "addSections"
  private val addSectionsAgentKey = "addSections.agent"

  private val sections: List[Task] = List(
    Task(Link(AboutYou.toString, controllers.aboutyou.routes.UkResidenceStatusController.onPageLoad(NormalMode, taxYear).url), NotStarted, addSectionsKey),
    Task(Link(
      IncomeFromWork.toString, controllers.aboutyou.routes.UkResidenceStatusController.onPageLoad(NormalMode, taxYear).url), CannotStartYet, addSectionsKey),
    Task(Link(
      IncomeFromProperty.toString, controllers.aboutyou.routes.UkResidenceStatusController.onPageLoad(NormalMode, taxYear).url), CannotStartYet, addSectionsKey),
    Task(Link(Pensions.toString, controllers.aboutyou.routes.UkResidenceStatusController.onPageLoad(NormalMode, taxYear).url), CannotStartYet, addSectionsKey)
  )

  private val agentSections: List[Task] = List(
    Task(Link(AboutYou.toString, controllers.aboutyou.routes.UkResidenceStatusController.onPageLoad(NormalMode, taxYear).url), NotStarted, addSectionsAgentKey),
    Task(Link(
      IncomeFromWork.toString, controllers.aboutyou.routes.UkResidenceStatusController.onPageLoad(NormalMode, taxYear).url), CannotStartYet, addSectionsAgentKey),
    Task(Link(
      IncomeFromProperty.toString, controllers.aboutyou.routes.UkResidenceStatusController.onPageLoad(NormalMode, taxYear).url), CannotStartYet, addSectionsAgentKey),
    Task(Link(
      Pensions.toString, controllers.aboutyou.routes.UkResidenceStatusController.onPageLoad(NormalMode, taxYear).url), CannotStartYet, addSectionsAgentKey)
  )

  private val completedCount: Int = sections.map(_.tag).count(_.isCompleted)

  "AddSections Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.AddSectionsController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddSectionsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(taxYear, sections, completedCount)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET for an agent" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, routes.AddSectionsController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddSectionsAgentView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(taxYear, agentSections, completedCount)(request, messages(application)).toString
      }
    }
  }
}
