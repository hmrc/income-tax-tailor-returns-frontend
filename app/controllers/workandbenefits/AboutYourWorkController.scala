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

import controllers.actions.TaxYearAction.taxYearAction
import controllers.actions._
import forms.workandbenefits.AboutYourWorkFormProvider
import models.Mode
import models.requests.DataRequest
import models.workandbenefits.AboutYourWork
import navigation.Navigator
import pages.workandbenefits.AboutYourWorkPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Format.GenericFormat
import play.api.mvc._
import services.UserDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.workandbenefits.{AboutYourWorkAgentView, AboutYourWorkView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AboutYourWorkController @Inject()(override val messagesApi: MessagesApi,
                                        userDataService: UserDataService,
                                        navigator: Navigator,
                                        identify: IdentifierActionProvider,
                                        getData: DataRetrievalActionProvider,
                                        requireData: DataRequiredActionProvider,
                                        overrideRequestActionProvider: OverrideRequestActionProvider,
                                        formProvider: AboutYourWorkFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: AboutYourWorkView,
                                        agentView: AboutYourWorkAgentView)
                                       (implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def actionChain(taxYear: Int, optionalRequestOverride: Option[DataRequest[_]]): ActionBuilder[DataRequest, AnyContent] =
    optionalRequestOverride.map(overrideRequestActionProvider(_)).getOrElse(
      identify(taxYear) andThen taxYearAction(taxYear) andThen getData(taxYear) andThen requireData(taxYear)
    )

  def form(isAgent: Boolean): Form[Set[AboutYourWork]] = formProvider(isAgent)

  def onPageLoad(mode: Mode,
                 taxYear: Int,
                 optionalRequestOverride: Option[DataRequest[_]]): Action[AnyContent] =
    actionChain(taxYear, optionalRequestOverride) {
      implicit request =>

        def deriveView[A](form: Form[A], status: Status): Result = getView(mode, taxYear)(form, status)

        val preparedCheckboxForm = request.userAnswers.get(AboutYourWorkPage) match {
          case None => form(request.isAgent)
          case Some(value) => form(request.isAgent).fill(value)
        }

        deriveView(preparedCheckboxForm, Ok)
    }

  def onSubmit(mode: Mode,
               taxYear: Int,
               optionalRequestOverride: Option[DataRequest[_]]): Action[AnyContent] =
    actionChain(taxYear, optionalRequestOverride).async {
      implicit request =>

        def deriveView[A](form: Form[A], status: Status): Result = getView(mode, taxYear)(form, status)

        form(request.isAgent).bindFromRequest().fold(
          formWithErrors =>
            Future.successful(deriveView(formWithErrors, BadRequest)),

          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(AboutYourWorkPage, value))
              _ <- userDataService.set(updatedAnswers, request.userAnswers)
            } yield Redirect(navigator.nextPage(AboutYourWorkPage, mode, updatedAnswers))
        )
    }

  private def getView[A](mode: Mode, taxYear: Int)(form: Form[A], status: Status)
                        (implicit request: DataRequest[_], messages: Messages): Result =
    if (request.isAgent) {
      status(agentView(form, mode, taxYear)(request, messages))
    } else {
      status(view(form, mode, taxYear)(request, messages))
    }
}
