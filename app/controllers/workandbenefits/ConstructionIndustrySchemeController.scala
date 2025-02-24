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
import forms.workandbenefits.ConstructionIndustrySchemeFormProvider
import handlers.ErrorHandler
import models.Mode
import models.prePopulation.IncomeTaxCisPrePopulationResponse
import navigation.Navigator
import pages.workandbenefits.ConstructionIndustrySchemePage
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import play.twirl.api.HtmlFormat
import services.{PrePopulationService, SessionDataService, UserDataService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import views.html.workandbenefits.{ConstructionIndustrySchemeAgentView, ConstructionIndustrySchemeView}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ConstructionIndustrySchemeController @Inject()(
                                                      override val messagesApi: MessagesApi,
                                                      val userDataService: UserDataService,
                                                      prePopService: PrePopulationService,
                                                      val ninoRetrievalService: SessionDataService,
                                                      val navigator: Navigator,
                                                      val identify: IdentifierActionProvider,
                                                      val getData: DataRetrievalActionProvider,
                                                      val requireData: DataRequiredActionProvider,
                                                      val formProvider: ConstructionIndustrySchemeFormProvider,
                                                      val controllerComponents: MessagesControllerComponents,
                                                      view: ConstructionIndustrySchemeView,
                                                      agentView: ConstructionIndustrySchemeAgentView,
                                                      val config: FrontendAppConfig,
                                                      val errorHandler: ErrorHandler
                                                    )(implicit val ec: ExecutionContext)
  extends ControllerWithPrePop[Boolean, IncomeTaxCisPrePopulationResponse]
  with Logging {

  override protected val classLoggingContext: String = "ConstructionIndustrySchemeController"
  override val defaultPrePopulationResponse: IncomeTaxCisPrePopulationResponse = IncomeTaxCisPrePopulationResponse.empty
  override protected def prePopRetrievalAction(nino: String, taxYear: Int, mtdItId: String)
                                              (implicit hc: HeaderCarrier): PrePopResult =
    () => prePopService.getCis(nino, taxYear, mtdItId)

  override protected def viewProvider(form: Form[_],
                                      mode: Mode,
                                      taxYear: Int,
                                      prePopData: IncomeTaxCisPrePopulationResponse)
                                     (implicit request: Request[_]): HtmlFormat.Appendable =
    view(form, mode, taxYear, prePopData.hasCis)

  override protected def agentViewProvider(form: Form[_],
                                           mode: Mode,
                                           taxYear: Int,
                                           prePopData: IncomeTaxCisPrePopulationResponse)
                                          (implicit request: Request[_]): HtmlFormat.Appendable =
    agentView(form, mode, taxYear, prePopData.hasCis)


  val pageName = classOf[ConstructionIndustrySchemeController].getSimpleName
  val incomeType = "cis employment"

  def onPageLoad(mode: Mode, taxYear: Int): Action[AnyContent] =
    blockWithNino(
      taxYear = taxYear,
      block = onPageLoad(
        pageName = pageName,
        incomeType = incomeType,
        page = ConstructionIndustrySchemePage,
        mode = mode
      )
    )

  def onSubmit(mode: Mode, taxYear: Int): Action[AnyContent] =
    blockWithNino(
      taxYear = taxYear,
      block = onSubmit(
        pageName = pageName,
        incomeType = incomeType,
        page = ConstructionIndustrySchemePage,
        mode = mode
      )
    )
}
