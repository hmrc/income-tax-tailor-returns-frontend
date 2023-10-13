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

import controllers.actions.{DataRetrievalActionProvider, IdentifierActionProvider}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class KeepAliveController @Inject()(
                                     val controllerComponents: MessagesControllerComponents,
                                     identify: IdentifierActionProvider,
                                     getData: DataRetrievalActionProvider,
                                     userDataService: UserDataService
                                   )(implicit ec: ExecutionContext) extends FrontendBaseController {

  def keepAlive(taxYear: Int): Action[AnyContent] = (identify(taxYear) andThen getData(taxYear)).async {
    implicit request =>
      request.userAnswers
        .map {
          answers =>
            userDataService.keepAlive(taxYear).map(_ => Ok)
        }
        .getOrElse(Future.successful(Ok))
  }
}
