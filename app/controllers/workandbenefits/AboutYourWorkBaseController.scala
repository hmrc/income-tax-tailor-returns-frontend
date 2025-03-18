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

package controllers.workandbenefits

import com.google.inject.{Inject, Singleton}
import controllers.actions.TaxYearAction.taxYearAction
import controllers.actions.{DataRequiredActionProvider, DataRetrievalActionProvider, IdentifierActionProvider}
import models.Mode
import models.requests.DataRequest
import pages.aboutyou.FosterCarerPage
import play.api.mvc.{Action, ActionBuilder, AnyContent}

import scala.concurrent.ExecutionContext

@Singleton
class AboutYourWorkBaseController @Inject()(controller: AboutYourWorkController,
                                            radioController: AboutYourWorkRadioController,
                                            val identify: IdentifierActionProvider,
                                            val getData: DataRetrievalActionProvider,
                                            val requireData: DataRequiredActionProvider)
                                           (implicit val ec: ExecutionContext){

  private def actionChain(taxYear: Int): ActionBuilder[DataRequest, AnyContent] =
    identify(taxYear) andThen taxYearAction(taxYear) andThen getData(taxYear) andThen requireData(taxYear)

  private def isFosterCarer()(implicit request: DataRequest[_]): Boolean =
    request.userAnswers.get(FosterCarerPage).getOrElse(false)

  def onPageLoad(mode: Mode, taxYear: Int): Action[AnyContent] = actionChain(taxYear).async { implicit request =>
    if (!isFosterCarer()) {
      controller.onPageLoad(mode, taxYear, Some(request))(request)
    } else {
      radioController.onPageLoad(mode, taxYear, Some(request))(request)
    }
  }

  def onSubmit(mode: Mode, taxYear: Int): Action[AnyContent] = actionChain(taxYear).async { implicit request =>
    if (!isFosterCarer()) {
      controller.onSubmit(mode, taxYear, Some(request))(request)
    } else {
      radioController.onSubmit(mode, taxYear, Some(request))(request)
    }
  }
}
