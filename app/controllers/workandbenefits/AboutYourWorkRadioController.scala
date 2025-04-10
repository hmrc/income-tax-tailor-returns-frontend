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
import forms.workandbenefits.AboutYourWorkRadioPageFormProvider
import handlers.ErrorHandler
import models.prePopulation.EmploymentPrePopulationResponse.EmploymentRadioPrePop
import models.requests.DataRequest
import models.workandbenefits.AboutYourWork
import models.workandbenefits.AboutYourWork.{Employed, SelfEmployed}
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.QuestionPage
import pages.workandbenefits.{AboutYourWorkPage, AboutYourWorkRadioPage}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, ActionBuilder, AnyContent, MessagesControllerComponents}
import play.twirl.api.HtmlFormat
import services.{PrePopulationService, UserDataService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import views.html.workandbenefits.{AboutYourWorkRadioPageAgentView, AboutYourWorkRadioPageView}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AboutYourWorkRadioController @Inject()(override val messagesApi: MessagesApi,
                                             val userDataService: UserDataService,
                                             prePopService: PrePopulationService,
                                             val config: FrontendAppConfig,
                                             val navigator: Navigator,
                                             val identify: IdentifierActionProvider,
                                             val getData: DataRetrievalActionProvider,
                                             val requireData: DataRequiredActionProvider,
                                             val overrideRequestActionProvider: OverrideRequestActionProvider,
                                             val formProvider: AboutYourWorkRadioPageFormProvider,
                                             val controllerComponents: MessagesControllerComponents,
                                             radioView: AboutYourWorkRadioPageView,
                                             agentRadioView: AboutYourWorkRadioPageAgentView,
                                             val errorHandler: ErrorHandler)(implicit val ec: ExecutionContext)
  extends ControllerWithPrePop[Boolean, EmploymentRadioPrePop]
    with Logging {

  override protected val primaryContext: String = classOf[AboutYourWorkRadioController].getSimpleName
  override val defaultPrePopulationResponse: EmploymentRadioPrePop = EmploymentRadioPrePop.empty

  val pageName = classOf[AboutYourWorkRadioController].getSimpleName
  val incomeType = "employment"

  override protected def actionChain(taxYear: Int,
                                     requestOverrideOpt: Option[DataRequest[_]] = None): ActionBuilder[DataRequest, AnyContent] =
    requestOverrideOpt.map(overrideRequestActionProvider(_)).getOrElse(
      identify(taxYear) andThen taxYearAction(taxYear) andThen getData(taxYear) andThen requireData(taxYear)
    )

  override protected def prePopRetrievalAction(nino: String, taxYear: Int, mtdItId: String)
                                              (implicit hc: HeaderCarrier): PrePopResult =
    () => prePopService.getEmployment(nino, taxYear, mtdItId).map(_.map(_.toPrePopRadioModel))

  override protected def viewProvider(form: Form[_],
                                      mode: Mode,
                                      taxYear: Int,
                                      prePopData: EmploymentRadioPrePop)
                                     (implicit request: DataRequest[_]): HtmlFormat.Appendable = {
    val prePopCheck = config.isPrePopEnabled && prePopData.hasEmployment

    if (request.isAgent) {
      agentRadioView(form, mode, taxYear, prePopCheck)
    } else {
      radioView(form, mode, taxYear, prePopCheck)
    }
  }

  override protected def updateAnswers(page: QuestionPage[Boolean], value: Boolean)
                                      (implicit request: DataRequest[_]): Future[UserAnswers] = {
    val aboutYourWork = if (value) {
      Set[AboutYourWork](Employed, SelfEmployed)
    } else {
      Set[AboutYourWork](SelfEmployed)
    }

    Future.fromTry(
      request.userAnswers
        .set(AboutYourWorkRadioPage, value)
        .flatMap(_.set(AboutYourWorkPage, aboutYourWork))
    )
  }

  def onPageLoad(mode: Mode,
                 taxYear: Int,
                 requestOverrideOpt: Option[DataRequest[_]]): Action[AnyContent] = onPageLoad(
    pageName = pageName,
    incomeType = incomeType,
    page = AboutYourWorkRadioPage,
    mode = mode,
    taxYear = taxYear,
    requestOverrideOpt = requestOverrideOpt
  )

  def onSubmit(mode: Mode,
               taxYear: Int,
               requestOverrideOpt: Option[DataRequest[_]]): Action[AnyContent] = onSubmit(
    pageName = pageName,
    incomeType = incomeType,
    page = AboutYourWorkRadioPage,
    mode = mode,
    taxYear = taxYear,
    requestOverrideOpt = requestOverrideOpt
  )
}
