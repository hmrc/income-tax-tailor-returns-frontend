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

import controllers.actions.TaxYearAction.taxYearAction
import controllers.actions._
import handlers.ErrorHandler
import models.SectionNames._
import models.{Done, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{AddSectionsService, UserDataService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.{Link, Task}
import views.html.{AddSectionsAgentView, AddSectionsView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TestOnlyClearData @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierActionProvider,
                                       userDataService: UserDataService,
                                       getData: DataRetrievalActionProvider,
                                       error: ErrorHandler,
                                       val controllerComponents: MessagesControllerComponents,
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def testOnlyClear(taxYear: Int): Action[AnyContent] = (identify(taxYear) andThen taxYearAction(taxYear) andThen getData(taxYear)).async {
    implicit request =>
      userDataService.clear(request.mtdItId, taxYear).map(_ =>
        Redirect(controllers.routes.StartController.onPageLoad(taxYear))
      )
  }
}
