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
import forms.workandbenefits.AboutYourWorkFormProvider
import handlers.ErrorHandler
import models.Mode
import models.prePopulation.EmploymentPrePopulationResponse.EmploymentPrePop
import models.requests.DataRequest
import models.workandbenefits.AboutYourWork
import navigation.Navigator
import pages.workandbenefits.AboutYourWorkPage
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc._
import play.twirl.api.HtmlFormat
import services.{PrePopulationService, SessionDataService, UserDataService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import views.html.workandbenefits.{AboutYourWorkAgentView, AboutYourWorkView}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class AboutYourWorkController @Inject()(override val messagesApi: MessagesApi,
                                        val userDataService: UserDataService,
                                        prePopService: PrePopulationService,
                                        val ninoRetrievalService: SessionDataService,
                                        val config: FrontendAppConfig,
                                        val navigator: Navigator,
                                        val identify: IdentifierActionProvider,
                                        val getData: DataRetrievalActionProvider,
                                        val requireData: DataRequiredActionProvider,
                                        val overrideRequestActionProvider: OverrideRequestActionProvider,
                                        val formProvider: AboutYourWorkFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: AboutYourWorkView,
                                        agentView: AboutYourWorkAgentView,
                                        val errorHandler: ErrorHandler
                                       )(implicit val ec: ExecutionContext)
  extends ControllerWithPrePop[Set[AboutYourWork], EmploymentPrePop]
    with Logging {

  override protected val primaryContext: String = "AboutYourWorkController"
  override val defaultPrePopulationResponse: EmploymentPrePop = EmploymentPrePop.empty

  override protected def actionChain(taxYear: Int,
                                     requestOverrideOpt: Option[DataRequest[_]] = None): ActionBuilder[DataRequest, AnyContent] =
    requestOverrideOpt.map(overrideRequestActionProvider(_)).getOrElse(
      identify(taxYear) andThen taxYearAction(taxYear) andThen getData(taxYear) andThen requireData(taxYear)
    )

  override protected def prePopRetrievalAction(nino: String, taxYear: Int, mtdItId: String)
                                              (implicit hc: HeaderCarrier): PrePopResult =
    () => prePopService.getEmployment(nino, taxYear, mtdItId).map(_.map(_.toPrePopModel))

  override protected def viewProvider(form: Form[_],
                                      mode: Mode,
                                      taxYear: Int,
                                      prePopData: EmploymentPrePop)
                                     (implicit request: DataRequest[_]): HtmlFormat.Appendable =
    if (request.isAgent) {
      agentView(form, mode, taxYear)
    } else {
      view(form, mode, taxYear)
    }

  val pageName = "Employment" //TODO: Change this
  val incomeType = "employment" //TODO: Change this

  def onPageLoad(mode: Mode,
                 taxYear: Int,
                 requestOverrideOpt: Option[DataRequest[_]]): Action[AnyContent] = onPageLoad(
    pageName = pageName,
    incomeType = incomeType,
    page = AboutYourWorkPage,
    mode = mode,
    taxYear = taxYear,
    requestOverrideOpt = requestOverrideOpt
  )

  def onSubmit(mode: Mode,
               taxYear: Int,
               requestOverrideOpt: Option[DataRequest[_]]): Action[AnyContent] = onSubmit(
    pageName = pageName,
    incomeType = incomeType,
    page = AboutYourWorkPage,
    mode = mode,
    taxYear = taxYear,
    requestOverrideOpt = requestOverrideOpt
  )
}
