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

import controllers.actions.TaxYearAction.taxYearAction
import controllers.actions._
import forms.workandbenefits.{AboutYourWorkFormProvider, AboutYourWorkRadioPageFormProvider}
import models.Mode
import models.requests.DataRequest
import models.workandbenefits.AboutYourWork
import models.workandbenefits.AboutYourWork.{Employed, SelfEmployed}
import navigation.Navigator
import pages.aboutyou.FosterCarerPage
import pages.workandbenefits.{AboutYourWorkPage, AboutYourWorkRadioPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Format.GenericFormat
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.UserDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.workandbenefits.{AboutYourWorkAgentView, AboutYourWorkRadioPageAgentView, AboutYourWorkRadioPageView, AboutYourWorkView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AboutYourWorkController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         userDataService: UserDataService,
                                         navigator: Navigator,
                                         identify: IdentifierActionProvider,
                                         getData: DataRetrievalActionProvider,
                                         requireData: DataRequiredActionProvider,
                                         formProvider: AboutYourWorkFormProvider,
                                         radioFormProvider: AboutYourWorkRadioPageFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: AboutYourWorkView,
                                         agentView: AboutYourWorkAgentView,
                                         radioView: AboutYourWorkRadioPageView,
                                         agentRadioView: AboutYourWorkRadioPageAgentView
                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def form(isAgent: Boolean): Form[Set[AboutYourWork]] = formProvider(isAgent)

  def radioForm(isAgent: Boolean): Form[Boolean] = radioFormProvider(isAgent)

  def onPageLoad(mode: Mode, taxYear: Int): Action[AnyContent] =
    (identify(taxYear) andThen taxYearAction(taxYear) andThen getData(taxYear) andThen requireData(taxYear)) {

      implicit request =>

        val isFosterCarer: Boolean = getFosterCarerStatus(request)

        if (isFosterCarer) {

          val preparedRadioForm = request.userAnswers.get(AboutYourWorkRadioPage) match {
            case None => radioForm(request.isAgent)
            case Some(value) => radioForm(request.isAgent).fill(value)
          }

          getView(preparedRadioForm, isFosterCarer, mode, taxYear, Ok)

        } else {

          val preparedCheckboxForm = request.userAnswers.get(AboutYourWorkPage) match {
            case None => form(request.isAgent)
            case Some(value) => form(request.isAgent).fill(value)
          }

          getView(preparedCheckboxForm, isFosterCarer, mode, taxYear, Ok)
        }
    }


  def onSubmit(mode: Mode, taxYear: Int): Action[AnyContent] =
    (identify(taxYear) andThen taxYearAction(taxYear) andThen getData(taxYear) andThen requireData(taxYear)).async {
      implicit request =>

        val isFosterCarer: Boolean = getFosterCarerStatus(request)

        if (isFosterCarer) {

          radioForm(request.isAgent).bindFromRequest().fold(
            formWithErrors =>
              Future.successful(getView(formWithErrors, isFosterCarer, mode, taxYear, BadRequest)),

            value => {
              val aboutYourWork = if (value) {
                Set[AboutYourWork](Employed, SelfEmployed)
              } else {
                Set[AboutYourWork](SelfEmployed)
              }

              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(AboutYourWorkRadioPage, value)
                  .flatMap(_.set(AboutYourWorkPage, aboutYourWork)))
                _ <- userDataService.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(AboutYourWorkRadioPage, mode, updatedAnswers))
            }
          )

        } else {

          form(request.isAgent).bindFromRequest().fold(
            formWithErrors =>
              Future.successful(getView(formWithErrors, isFosterCarer, mode, taxYear, BadRequest)),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(AboutYourWorkPage, value))
                _ <- userDataService.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(AboutYourWorkPage, mode, updatedAnswers))
          )

        }
    }

  private def getFosterCarerStatus(request: DataRequest[AnyContent]): Boolean = {
    val isFosterCarer: Boolean = request.userAnswers.get(FosterCarerPage) match {
      case Some(value) => value
      case _ => false
    }
    isFosterCarer
  }

  private def getView[A](form: Form[A], isFosterCarer: Boolean, mode: Mode, taxYear: Int, status: Status)
                        (implicit request: DataRequest[_], messages: Messages): Result = {

    val refinedView = if (isFosterCarer) radioView(form, mode, taxYear)(request, messages) else view(form, mode, taxYear)(request, messages)
    val refinedAgentView = if (isFosterCarer) agentRadioView(form, mode, taxYear)(request, messages) else agentView(form, mode, taxYear)(request, messages)

    if (request.isAgent) {
      status(refinedAgentView)
    } else {
      status(refinedView)
    }
  }
}
