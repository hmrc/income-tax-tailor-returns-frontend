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

import connectors.ConnectorResponse
import controllers.PrePopulationHelper
import controllers.actions.TaxYearAction.taxYearAction
import controllers.actions._
import forms.workandbenefits.JobseekersAllowanceFormProvider
import models.Mode
import models.errors.SimpleErrorWrapper
import models.prePopulation.StateBenefitsPrePopulationResponse
import models.requests.DataRequestWithNino
import models.workandbenefits.JobseekersAllowance
import navigation.Navigator
import pages.workandbenefits.JobseekersAllowancePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import services.{PrePopulationService, UserDataService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.workandbenefits.{JobseekersAllowanceAgentView, JobseekersAllowanceView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class JobseekersAllowanceController @Inject()(override val messagesApi: MessagesApi,
                                              userDataService: UserDataService,
                                              prePopService: PrePopulationService,
                                              navigator: Navigator,
                                              identify: IdentifierActionProvider,
                                              getData: DataRetrievalActionProvider,
                                              requireData: DataRequiredActionProvider,
                                              requireNino: DataRequiredWithNinoActionProvider,
                                              formProvider: JobseekersAllowanceFormProvider,
                                              val controllerComponents: MessagesControllerComponents,
                                              view: JobseekersAllowanceView,
                                              agentView: JobseekersAllowanceAgentView)
                                             (implicit ec: ExecutionContext) extends FrontendBaseController
  with I18nSupport with Logging with PrePopulationHelper[StateBenefitsPrePopulationResponse] {
  override val classLoggingContext: String = "JobseekersAllowanceController"

  override def prePopRetrievalAction(nino: String,
                                     taxYear: Int)
                                    (implicit hc: HeaderCarrier): () => ConnectorResponse[StateBenefitsPrePopulationResponse] =
    () => prePopService.getStateBenefits(nino, taxYear)


  def form(isAgent: Boolean): Form[Set[JobseekersAllowance]] = formProvider(isAgent)

  private def actionChain(taxYear: Int): ActionBuilder[DataRequestWithNino, AnyContent] =
    identify(taxYear) andThen
      taxYearAction(taxYear) andThen
      getData(taxYear) andThen
      requireData(taxYear) andThen
      requireNino(taxYear)

  def onPageLoad(mode: Mode, taxYear: Int): Action[AnyContent] = actionChain(taxYear).async { implicit request =>
    val nino: String = request.nino
    val dataLog: String = dataLogString(nino, taxYear)

    val infoLogger: String => Unit = infoLog(
      methodLoggingContext = "onPageLoad",
      dataLog = dataLog
    )

    infoLogger("Received request to retrieve JobseekersAllowance tailoring page")

    val preparedForm = request.userAnswers.get(JobseekersAllowancePage) match {
      case None =>
        infoLogger("No existing state benefits journey answers found in request model")
        form(request.isAgent)
      case Some(value) =>
        infoLogger("Existing state benefits journey answers found. Pre-populating form with previous user answers")
        form(request.isAgent).fill(value)
    }

    doHandleWithPrePop(
      isAgent = request.isAgent,
      isErrorScenario = false,
      prePopulationRetrievalAction = prePopRetrievalAction(nino, taxYear),
      agentSuccessAction = (data: StateBenefitsPrePopulationResponse) =>
        Ok(agentView(preparedForm, mode, taxYear, data)),
      individualSuccessAction = (data: StateBenefitsPrePopulationResponse) =>
        Ok(view(preparedForm, mode, taxYear, data)),
      errorAction = ???, //TODO
      extraLogContext = "onPageLoad",
      dataLog = dataLog
    )
  }

  def onSubmit(mode: Mode, taxYear: Int): Action[AnyContent] = actionChain(taxYear).async { implicit request =>
    val nino: String = request.nino
    val dataLog = dataLogString(nino, taxYear)

    val infoLogger: String => Unit = infoLog(methodLoggingContext = "onPageLoad", dataLog = dataLog)

    infoLogger("Request received to submit user journey answers for JobseekersAllowance view")

    form(request.isAgent).bindFromRequest().fold(
      formWithErrors => {
        logger.warn(
          methodContext = "onSubmit",
          message = s"Errors found in form submission: ${formWithErrors.errors}",
          dataLog = dataLog
        )

        doHandleWithPrePop(
          isAgent = request.isAgent,
          isErrorScenario = true,
          prePopulationRetrievalAction = prePopRetrievalAction(nino, taxYear),
          agentSuccessAction = (data: StateBenefitsPrePopulationResponse) =>
            BadRequest(agentView(formWithErrors, mode, taxYear, data)),
          individualSuccessAction = (data: StateBenefitsPrePopulationResponse) =>
            BadRequest(view(formWithErrors, mode, taxYear, data)),
          errorAction = (err: SimpleErrorWrapper) => ???, //TODO
          extraLogContext = "onSubmit",
          dataLog = dataLog
        )
      },
      value =>
        {
          infoLogger("Form bound successfully from request. Attempting to update user's journey answers")
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(JobseekersAllowancePage, value))
            _ <- userDataService.set(updatedAnswers, request.userAnswers)
          } yield {
            infoLogger("Journey answers updated successfully. Proceeding to next page in the journey")
            Redirect(navigator.nextPage(JobseekersAllowancePage, mode, updatedAnswers))
          }
        }
    )
  }
}
