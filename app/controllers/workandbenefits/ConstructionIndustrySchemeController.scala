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

package controllers.workandbenefits

import config.FrontendAppConfig
import controllers.actions.TaxYearAction.taxYearAction
import controllers.actions._
import forms.workandbenefits.ConstructionIndustrySchemeFormProvider
import models.Mode
import navigation.Navigator
import pages.workandbenefits.ConstructionIndustrySchemePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.workandbenefits.{ConstructionIndustrySchemeAgentView, ConstructionIndustrySchemeView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConstructionIndustrySchemeController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         config: FrontendAppConfig,
                                         userDataService: UserDataService,
                                         navigator: Navigator,
                                         identify: IdentifierActionProvider,
                                         getData: DataRetrievalActionProvider,
                                         requireData: DataRequiredActionProvider,
                                         formProvider: ConstructionIndustrySchemeFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: ConstructionIndustrySchemeView,
                                         agentView: ConstructionIndustrySchemeAgentView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def form(isAgent: Boolean): Form[Boolean] = formProvider(isAgent)

  def prePopCheck(prePopData: Boolean): Boolean = if (config.isPrePopEnabled) prePopData else false

  def onPageLoad(mode: Mode, taxYear: Int): Action[AnyContent] =
  (identify(taxYear) andThen taxYearAction(taxYear) andThen getData(taxYear) andThen requireData(taxYear)) {
    implicit request =>

      val preparedForm = request.userAnswers.get(ConstructionIndustrySchemePage) match {
        case None => form(request.isAgent)
        case Some(value) => form(request.isAgent).fill(value)
      }

      if (request.isAgent) {
        Ok(agentView(preparedForm, mode, taxYear, prePopCheck(preparedForm.value.isDefined)))
      } else {
        Ok(view(preparedForm, mode, taxYear, prePopCheck(preparedForm.value.isDefined)))
      }
  }

  def onSubmit(mode: Mode, taxYear: Int): Action[AnyContent] =
  (identify(taxYear) andThen taxYearAction(taxYear) andThen getData(taxYear) andThen requireData(taxYear)).async {
    implicit request =>

      form(request.isAgent).bindFromRequest().fold(
        formWithErrors =>
          if (request.isAgent) {
            Future.successful(BadRequest(agentView(formWithErrors, mode, taxYear, prePopCheck(formWithErrors.value.isDefined))))
          } else {
            Future.successful(BadRequest(view(formWithErrors, mode, taxYear, prePopCheck(formWithErrors.value.isDefined))))
          },

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ConstructionIndustrySchemePage, value))
            _              <- userDataService.set(updatedAnswers, request.userAnswers)
          } yield Redirect(navigator.nextPage(ConstructionIndustrySchemePage, mode, updatedAnswers))
      )
  }
}
