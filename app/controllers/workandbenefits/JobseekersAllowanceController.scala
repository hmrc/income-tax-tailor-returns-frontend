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
import forms.workandbenefits.JobseekersAllowanceFormProvider
import handlers.ErrorHandler
import models.Mode
import models.prePopulation.StateBenefitsPrePopulationResponse
import models.workandbenefits.JobseekersAllowance
import navigation.Navigator
import pages.workandbenefits.JobseekersAllowancePage
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc._
import play.twirl.api.HtmlFormat
import services.{SessionDataService, PrePopulationService, UserDataService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import views.html.workandbenefits.{JobseekersAllowanceAgentView, JobseekersAllowanceView}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class JobseekersAllowanceController @Inject()(override val messagesApi: MessagesApi,
                                              val userDataService: UserDataService,
                                              prePopService: PrePopulationService,
                                              val ninoRetrievalService: SessionDataService,
                                              val navigator: Navigator,
                                              val identify: IdentifierActionProvider,
                                              val getData: DataRetrievalActionProvider,
                                              val requireData: DataRequiredActionProvider,
                                              val formProvider: JobseekersAllowanceFormProvider,
                                              val controllerComponents: MessagesControllerComponents,
                                              view: JobseekersAllowanceView,
                                              agentView: JobseekersAllowanceAgentView,
                                              val config: FrontendAppConfig,
                                              val errorHandler: ErrorHandler)
                                             (implicit val ec: ExecutionContext)
  extends ControllerWithPrePop[Set[JobseekersAllowance], StateBenefitsPrePopulationResponse]
  with Logging {

  override protected val classLoggingContext: String = "JobseekersAllowanceController"

  override val defaultPrePopulationResponse: StateBenefitsPrePopulationResponse = StateBenefitsPrePopulationResponse.empty

  override protected def prePopRetrievalAction(nino: String, taxYear: Int, mtdItId: String)
                                              (implicit hc: HeaderCarrier): PrePopResult =
    () => prePopService.getStateBenefits(nino, taxYear, mtdItId)

  override protected def viewProvider(form: Form[_],
                                      mode: Mode,
                                      taxYear: Int,
                                      prePopData: StateBenefitsPrePopulationResponse)
                                     (implicit request: Request[_]): HtmlFormat.Appendable = {
    view(form, mode, taxYear, prePopData)
  }

  override protected def agentViewProvider(form: Form[_],
                                           mode: Mode,
                                           taxYear: Int,
                                           prePopData: StateBenefitsPrePopulationResponse)
                                          (implicit request: Request[_]): HtmlFormat.Appendable =
    agentView(form, mode, taxYear, prePopData)

  val pageName = "JobseekersAllowance"
  val incomeType = "state benefits"

  def onPageLoad(mode: Mode, taxYear: Int): Action[AnyContent] = blockWithNino(
    taxYear = taxYear,
    block = onPageLoad(
      pageName = pageName,
      incomeType = incomeType,
      page = JobseekersAllowancePage,
      mode = mode
    )
  )

  def onSubmit(mode: Mode, taxYear: Int): Action[AnyContent] = blockWithNino(
    taxYear = taxYear,
    block = onSubmit(
      pageName = pageName,
      incomeType = incomeType,
      page = JobseekersAllowancePage,
      mode = mode
    )
  )
}
