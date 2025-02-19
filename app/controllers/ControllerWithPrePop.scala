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

package controllers

import controllers.actions.TaxYearAction.taxYearAction
import controllers.actions.{DataRequiredActionProvider, DataRequiredWithNinoActionProvider, DataRetrievalActionProvider, IdentifierActionProvider}
import forms.FormProvider
import models.Mode
import models.errors.SimpleErrorWrapper
import models.prePopulation.PrePopulationResponse
import models.requests.DataRequestWithNino
import navigation.Navigator
import pages.QuestionPage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.{Reads, Writes}
import play.api.mvc.{Action, ActionBuilder, AnyContent, Request}
import play.twirl.api.HtmlFormat
import services.UserDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Logging, PrePopulationHelper}

import scala.concurrent.{ExecutionContext, Future}

trait ControllerWithPrePop[R <: PrePopulationResponse, I]
  extends FrontendBaseController
  with I18nSupport
  with PrePopulationHelper[R] { _: Logging =>

  val formProvider: FormProvider[I]
  val userDataService: UserDataService
  val navigator: Navigator
  val identify: IdentifierActionProvider
  val getData: DataRetrievalActionProvider
  val requireData: DataRequiredActionProvider
  val requireNino: DataRequiredWithNinoActionProvider

  implicit val ec: ExecutionContext

  protected def viewProvider(form: Form[_], mode: Mode, taxYear: Int, prePopData: R)
                            (implicit request: Request[_]): HtmlFormat.Appendable

  protected def agentViewProvider(form: Form[_], mode: Mode, taxYear: Int, prePopData: R)
                                 (implicit request: Request[_]): HtmlFormat.Appendable

  protected def form(isAgent: Boolean): Form[Set[I]] = formProvider(isAgent)

  // If this needs overriding simply change it to be protected
  private def actionChain(taxYear: Int): ActionBuilder[DataRequestWithNino, AnyContent] =
    identify(taxYear) andThen
      taxYearAction(taxYear) andThen
      getData(taxYear) andThen
      requireData(taxYear) andThen
      requireNino(taxYear)

  def onPageLoad(taxYear: Int,
                 pageName: String,
                 incomeType: String,
                 page: QuestionPage[Set[I]],
                 mode: Mode)
                (implicit reads: Reads[I]): Action[AnyContent] = actionChain(taxYear).async { implicit request =>
    val nino: String = request.nino
    val dataLog: String = dataLogString(nino, taxYear)

    val infoLogger: String => Unit = infoLog(
      methodLoggingContext = "onPageLoad",
      dataLog = dataLog
    )

    infoLogger(s"Received request to retrieve $pageName tailoring page")

    val preparedForm = request.userAnswers.get(page) match {
      case None =>
        infoLogger(s"No existing $incomeType journey answers found in request model")
        form(request.isAgent)
      case Some(value) =>
        infoLogger(s"Existing $incomeType journey answers found. Pre-populating form with previous user answers")
        form(request.isAgent).fill(value)
    }

    doHandleWithPrePop(
      isAgent = request.isAgent,
      isErrorScenario = false,
      prePopulationRetrievalAction = prePopRetrievalAction(nino, taxYear),
      agentSuccessAction = (data: R) =>
        Ok(agentViewProvider(preparedForm, mode, taxYear, data)),
      individualSuccessAction = (data: R) =>
        Ok(viewProvider(preparedForm, mode, taxYear, data)),
      errorAction = (err: SimpleErrorWrapper) => ???, //TODO
      extraLogContext = "onPageLoad",
      dataLog = dataLog,
      incomeType = incomeType
    )
  }

  def onSubmit(taxYear: Int,
               pageName: String,
               incomeType: String,
               page: QuestionPage[Set[I]],
               mode: Mode)
              (implicit writes: Writes[I]): Action[AnyContent] = actionChain(taxYear).async { implicit request =>
    val nino: String = request.nino
    val dataLog = dataLogString(nino, taxYear)

    val infoLogger: String => Unit = infoLog(
      methodLoggingContext = "onSubmit",
      dataLog = dataLog
    )

    infoLogger(s"Request received to submit user journey answers for $pageName view")

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
          agentSuccessAction = (data: R) =>
            BadRequest(agentViewProvider(formWithErrors, mode, taxYear, data)),
          individualSuccessAction = (data: R) =>
            BadRequest(viewProvider(formWithErrors, mode, taxYear, data)),
          errorAction = (err: SimpleErrorWrapper) => ???, //TODO
          extraLogContext = "onSubmit",
          dataLog = dataLog,
          incomeType = incomeType
        )
      },
      value =>
      {
        infoLogger("Form bound successfully from request. Attempting to update user's journey answers")
        for {
          updatedAnswers <- Future.fromTry(request.userAnswers.set(page, value))
          _ <- userDataService.set(updatedAnswers, request.userAnswers)
        } yield {
          infoLogger("Journey answers updated successfully. Proceeding to next page in the journey")
          Redirect(navigator.nextPage(page, mode, updatedAnswers))
        }
      }
    )
  }
}

