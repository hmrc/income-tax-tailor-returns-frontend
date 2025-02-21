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
import controllers.ControllerWithPrePop
import controllers.actions._
import forms.workandbenefits.EmploymentLumpSumsFormProvider
import handlers.ErrorHandler
import models.Mode
import models.prePopulation.EmploymentPrePopulationResponse
import navigation.Navigator
import pages.workandbenefits.EmploymentLumpSumsPage
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import play.twirl.api.HtmlFormat
import services.{PrePopulationService, SessionDataService, UserDataService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import views.html.workandbenefits.{EmploymentLumpSumsAgentView, EmploymentLumpSumsView}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class EmploymentLumpSumsController @Inject()(override val messagesApi: MessagesApi,
                                             val userDataService: UserDataService,
                                             prePopService: PrePopulationService,
                                             val ninoRetrievalService: SessionDataService,
                                             val navigator: Navigator,
                                             val identify: IdentifierActionProvider,
                                             val getData: DataRetrievalActionProvider,
                                             val requireData: DataRequiredActionProvider,
                                             val formProvider: EmploymentLumpSumsFormProvider,
                                             val controllerComponents: MessagesControllerComponents,
                                             view: EmploymentLumpSumsView,
                                             agentView: EmploymentLumpSumsAgentView,
                                             val config: FrontendAppConfig,
                                             val errorHandler: ErrorHandler)
                                            (implicit val ec: ExecutionContext)
  extends ControllerWithPrePop[EmploymentPrePopulationResponse, Boolean]
    with Logging {

  override protected val classLoggingContext: String = classOf[EmploymentLumpSumsController].getSimpleName

  override val defaultPrePopulationResponse: EmploymentPrePopulationResponse = EmploymentPrePopulationResponse.empty

  override protected def prePopRetrievalAction(nino: String, taxYear: Int, mtdItId: String)(implicit hc: HeaderCarrier): PrePopResult =
    () => prePopService.getEmployment(nino, taxYear)

  override protected def viewProvider(form: Form[_],
                                      mode: Mode,
                                      taxYear: Int,
                                      prePopData: EmploymentPrePopulationResponse)
                                     (implicit request: Request[_]): HtmlFormat.Appendable =
    view(form, mode, taxYear, prePopData)

  override protected def agentViewProvider(form: Form[_],
                                           mode: Mode,
                                           taxYear: Int,
                                           prePopData: EmploymentPrePopulationResponse)
                                          (implicit request: Request[_]): HtmlFormat.Appendable =
    agentView(form, mode, taxYear, prePopData)


  val pageName = "EmploymentLumpSums"
  val incomeType = "employment"


  def onPageLoad(mode: Mode, taxYear: Int): Action[AnyContent] = blockWithNino(
    taxYear = taxYear,
    block = onPageLoad (
      pageName = pageName,
      incomeType = incomeType,
      page = EmploymentLumpSumsPage,
      mode = mode
    )
  )


//    def onPageLoad(mode: Mode, taxYear: Int, prePopData: EmploymentPrePopulationResponse): Action[AnyContent] =
//  (identify(taxYear) andThen taxYearAction(taxYear) andThen getData(taxYear) andThen requireData(taxYear)) {
//    implicit request =>
//
//      val preparedForm = request.userAnswers.get(EmploymentLumpSumsPage) match {
//        case None => form(request.isAgent)
//        case Some(value) => form(request.isAgent).fill(value)
//      }
//
//      if (request.isAgent) {
//        Ok(agentView(preparedForm, mode, taxYear, prePopData))
//      } else {
//        Ok(view(preparedForm, mode, taxYear, prePopData))
//      }
//  }

  def onSubmit(mode: Mode, taxYear: Int): Action[AnyContent] = blockWithNino(
    taxYear = taxYear,
    block = onSubmit(
      pageName = pageName,
      incomeType = incomeType,
      page = EmploymentLumpSumsPage,
      mode = mode
    )
  )

//  def onSubmit(mode: Mode, taxYear: Int, prePopData: EmploymentPrePopulationResponse): Action[AnyContent] =
//  (identify(taxYear) andThen taxYearAction(taxYear) andThen getData(taxYear) andThen requireData(taxYear)).async {
//    implicit request =>
//
//      form(request.isAgent).bindFromRequest().fold(
//        formWithErrors =>
//          if (request.isAgent) {
//            Future.successful(BadRequest(agentView(formWithErrors, mode, taxYear, prePopData)))
//          } else {
//            Future.successful(BadRequest(view(formWithErrors, mode, taxYear, prePopData)))
//          },
//
//        value =>
//          for {
//            updatedAnswers <- Future.fromTry(request.userAnswers.set(EmploymentLumpSumsPage, value))
//            _              <- userDataService.set(updatedAnswers, request.userAnswers)
//          } yield Redirect(navigator.nextPage(EmploymentLumpSumsPage, mode, updatedAnswers))
//      )
//  }
}
