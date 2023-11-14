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
import forms.UkDividendsSharesAndLoansFromLimitedCompaniesFormProvider
import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.UkDividendsSharesAndLoansFromLimitedCompaniesPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.UkDividendsSharesAndLoansFromLimitedCompaniesView
import views.html.UkDividendsSharesAndLoansFromLimitedCompaniesAgentView

import scala.concurrent.{ExecutionContext, Future}

class UkDividendsSharesAndLoansFromLimitedCompaniesController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        userDataService: UserDataService,
                                        navigator: Navigator,
                                        identify: IdentifierActionProvider,
                                        getData: DataRetrievalActionProvider,
                                        requireData: DataRequiredActionProvider,
                                        formProvider: UkDividendsSharesAndLoansFromLimitedCompaniesFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: UkDividendsSharesAndLoansFromLimitedCompaniesView,
                                        agentView: UkDividendsSharesAndLoansFromLimitedCompaniesAgentView
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def form(isAgent: Boolean) = formProvider(isAgent)

  def onPageLoad(mode: Mode, taxYear: Int): Action[AnyContent] =
    (identify(taxYear) andThen taxYearAction(taxYear) andThen getData(taxYear) andThen requireData(taxYear)) {
    implicit request =>

      val preparedForm = request.userAnswers.get(UkDividendsSharesAndLoansFromLimitedCompaniesPage) match {
        case None => form(request.isAgent)
        case Some(value) => form(request.isAgent).fill(value)
      }

      if (request.isAgent) {
        Ok(agentView(preparedForm, mode, taxYear))
      } else {
        Ok(view(preparedForm, mode, taxYear))
      }
  }

  def onSubmit(mode: Mode, taxYear: Int): Action[AnyContent] =
    (identify(taxYear) andThen taxYearAction(taxYear) andThen getData(taxYear) andThen requireData(taxYear)).async {
    implicit request =>

      form(request.isAgent).bindFromRequest().fold(
        formWithErrors =>
          if (request.isAgent) {
            Future.successful(BadRequest(agentView(formWithErrors, mode, taxYear)))
          } else {
            Future.successful(BadRequest(view(formWithErrors, mode, taxYear)))
          },

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(UkDividendsSharesAndLoansFromLimitedCompaniesPage, value))
            _              <- userDataService.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(UkDividendsSharesAndLoansFromLimitedCompaniesPage, mode, updatedAnswers))
      )
  }
}
