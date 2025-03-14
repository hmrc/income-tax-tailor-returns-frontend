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

import config.FrontendAppConfig
import controllers.ControllerWithPrePop
import controllers.actions.TaxYearAction.taxYearAction
import controllers.actions._
import forms.workandbenefits.{AboutYourWorkFormProvider, AboutYourWorkRadioPageFormProvider}
import handlers.ErrorHandler
import models.Mode
import models.prePopulation.EmploymentPrePopulationResponse
import models.requests.DataRequest
import models.workandbenefits.AboutYourWork
import navigation.Navigator
import pages.aboutyou.FosterCarerPage
import pages.workandbenefits.AboutYourWorkPage
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, ActionBuilder, AnyContent, MessagesControllerComponents, Result}
import play.twirl.api.HtmlFormat
import services.{PrePopulationService, SessionDataService, UserDataService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import views.html.workandbenefits.{AboutYourWorkAgentView, AboutYourWorkRadioPageAgentView, AboutYourWorkRadioPageView, AboutYourWorkView}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AboutYourWorkController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         val userDataService: UserDataService,
                                         prePopService: PrePopulationService,
                                         val ninoRetrievalService: SessionDataService,
                                         val config: FrontendAppConfig,
                                         val navigator: Navigator,
                                         val identify: IdentifierActionProvider,
                                         val getData: DataRetrievalActionProvider,
                                         val requireData: DataRequiredActionProvider,
                                         val formProvider: AboutYourWorkFormProvider,
                                         val radioFormProvider: AboutYourWorkRadioPageFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: AboutYourWorkView,
                                         agentView: AboutYourWorkAgentView,
                                         radioView: AboutYourWorkRadioPageView,
                                         agentRadioView: AboutYourWorkRadioPageAgentView,
                                         val errorHandler: ErrorHandler
                                       )(implicit val ec: ExecutionContext)
  extends ControllerWithPrePop[Set[AboutYourWork], EmploymentPrePopulationResponse]
    with Logging {

  override protected val primaryContext: String = "EmploymentController"
  override val defaultPrePopulationResponse: EmploymentPrePopulationResponse = EmploymentPrePopulationResponse.empty

  override protected def actionChain(taxYear: Int): ActionBuilder[DataRequest, AnyContent] =
    identify(taxYear) andThen taxYearAction(taxYear) andThen getData(taxYear) andThen requireData(taxYear)

  override protected def prePopRetrievalAction(nino: String, taxYear: Int, mtdItId: String)
                                              (implicit hc: HeaderCarrier): PrePopResult =
    () => prePopService.getEmployment(nino, taxYear, mtdItId)

  override protected def viewProvider(form: Form[_],
                                      mode: Mode,
                                      taxYear: Int,
                                      prePopData: EmploymentPrePopulationResponse)
                                     (implicit request: DataRequest[_]): HtmlFormat.Appendable = {
    val isFosterCarer = request.userAnswers.get(FosterCarerPage).getOrElse(false)
    val prePopCheck = config.isPrePopEnabled && prePopData.hasEmploymentPrePop

    (request.isAgent, isFosterCarer) match {
      case (true, true) => agentRadioView(form, mode, taxYear, prePopCheck)
      case (true, false) => agentView(form, mode, taxYear)
      case (false, true) => radioView(form, mode, taxYear, prePopCheck)
      case (false, false) => view(form, mode, taxYear)
    }
  }

  val pageName = "Employment"
  val incomeType = "employment"

  def onPageLoad(mode: Mode, taxYear: Int): Action[AnyContent] = onPageLoad(
    pageName = pageName,
    incomeType = incomeType,
    page = AboutYourWorkPage,
    mode = mode,
    taxYear = taxYear,
  )

  def onSubmit(mode: Mode, taxYear: Int): Action[AnyContent] = onSubmit(
    pageName = pageName,
    incomeType = incomeType,
    page = AboutYourWorkPage,
    mode = mode,
    taxYear = taxYear
  )
}
