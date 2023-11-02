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

import controllers.actions.TaxYearAction.taxYearAction
import controllers.actions._
import models.SectionNames._
import models.NormalMode
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.AddSectionsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.{Link, Task}
import views.html.{AddSectionsAgentView, AddSectionsView}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AddSectionsController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierActionProvider,
                                       getData: DataRetrievalActionProvider,
                                       addSectionsService: AddSectionsService,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: AddSectionsView,
                                       agentView: AddSectionsAgentView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify(taxYear) andThen taxYearAction(taxYear) andThen getData(taxYear)) {
    implicit request =>

      val state = addSectionsService.getState(request.userAnswers)

      val sections = List(
        Task(Link(AboutYou.toString, controllers.aboutyou.routes.UkResidenceStatusController.onPageLoad(NormalMode, taxYear).url), state.aboutYou),
        Task(Link(IncomeFromWork.toString, controllers.aboutyou.routes.UkResidenceStatusController.onPageLoad(NormalMode, taxYear).url), state.incomeFromWork),
        Task(Link(
          IncomeFromProperty.toString, controllers.aboutyou.routes.UkResidenceStatusController.onPageLoad(NormalMode, taxYear).url), state.incomeFromProperty),
        Task(Link(Pensions.toString, controllers.aboutyou.routes.UkResidenceStatusController.onPageLoad(NormalMode, taxYear).url), state.pensions)
      )

      val completedCount: Int = sections.map(_.tag).count(_.isCompleted)

      if (request.isAgent) {
        Ok(agentView(taxYear, sections, completedCount))
      } else {
        Ok(view(taxYear, sections, completedCount))
      }
  }
}
