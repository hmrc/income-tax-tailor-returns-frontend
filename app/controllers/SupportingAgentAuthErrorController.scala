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

package controllers

import config.FrontendAppConfig
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.SupportingAgentAuthErrorView

import java.time.LocalDate
import javax.inject.Inject

class SupportingAgentAuthErrorController @Inject()(val mcc: MessagesControllerComponents,
                                                   implicit val appConfig: FrontendAppConfig,
                                                   view: SupportingAgentAuthErrorView)
  extends FrontendController(mcc) with I18nSupport {

  def show(): Action[AnyContent] = Action {
    implicit request =>
      Unauthorized(
        view(
          taxYear = request.session.get("TAX_YEAR").map(_.toInt).getOrElse(LocalDate.now().getYear)
        )
      )
  }
}
