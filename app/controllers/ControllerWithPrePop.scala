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

import config.FrontendAppConfig
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
import play.api.libs.json.Format
import play.api.mvc.{Action, ActionBuilder, AnyContent, Request}
import play.twirl.api.HtmlFormat
import services.UserDataService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Logging, PrePopulationHelper}

import scala.concurrent.{ExecutionContext, Future}

abstract class ControllerWithPrePop[R <: PrePopulationResponse, I: Format]
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
  val config: FrontendAppConfig

  implicit val ec: ExecutionContext

  protected val defaultPrePopulationResponse: R

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

  private def prePopActionWithFeatureSwitch(nino: String, taxYear: Int)
                                           (implicit hc: HeaderCarrier): PrePopResult =
    if (config.isPrePopEnabled) {
      prePopRetrievalAction(nino, taxYear)
    } else {
      () => Future.successful(Right(defaultPrePopulationResponse))
    }

  def onPageLoad(taxYear: Int,
                 pageName: String,
                 incomeType: String,
                 page: QuestionPage[Set[I]],
                 mode: Mode): Action[AnyContent] = actionChain(taxYear).async { implicit request =>
    val nino: String = request.nino
    val dataLog: String = dataLogString(nino, taxYear)
    val infoLogger: String => Unit = infoLog(methodLoggingContext = "onPageLoad", dataLog = dataLog)

    infoLogger(s"Received request to retrieve $pageName tailoring page")

    val preparedForm = request.userAnswers.get(page) match {
      case None =>
        infoLogger(s"No existing $incomeType journey answers found in request model")
        form(request.isAgent)
      case Some(value) =>
        infoLogger(s"Existing $incomeType journey answers found. Pre-populating form with previous user answers")
        form(request.isAgent).fill(value)
    }

    def successLog(): Unit = infoLogger(
      s"Pre-population data successfully retrieved. Redirecting user to $pageName view with form errors"
    )

    blockWithPrePopAndUserType(
      isAgent = request.isAgent,
      isErrorScenario = false,
      prePopulationRetrievalAction = prePopActionWithFeatureSwitch(nino, taxYear),
      agentSuccessAction = (data: R) => {
        successLog(); Ok(agentViewProvider(preparedForm, mode, taxYear, data))
      },
      individualSuccessAction = (data: R) => {
        successLog(); Ok(viewProvider(preparedForm, mode, taxYear, data))
      },
      errorAction = (_: SimpleErrorWrapper) => {
        logger.warn(
          methodContext = "onPageLoad",
          message = "Failed to load pre-population data. Returning error page",
          dataLog = dataLog
        )
        InternalServerError
      },
      extraLogContext = "onPageLoad",
      dataLog = dataLog,
      incomeType = incomeType
    )
  }

  def onSubmit(taxYear: Int,
               pageName: String,
               incomeType: String,
               page: QuestionPage[Set[I]],
               mode: Mode): Action[AnyContent] = actionChain(taxYear).async { implicit request =>
    val nino: String = request.nino
    val dataLog = dataLogString(nino, taxYear)
    val infoLogger: String => Unit = infoLog(methodLoggingContext = "onSubmit", dataLog = dataLog)
    val warnLogger: String => Unit = warnLog(methodLoggingContext = "onSubmit", dataLog = dataLog)

    infoLogger(s"Request received to submit user journey answers for $pageName view")

    form(request.isAgent).bindFromRequest().fold(
      formWithErrors => {
        warnLogger( s"Errors found in form submission: ${formWithErrors.errors}")

        def successLog(): Unit = infoLogger(
          s"Pre-population data successfully retrieved. Redirecting user to $pageName view with form errors"
        )

        blockWithPrePopAndUserType(
          isAgent = request.isAgent,
          isErrorScenario = true,
          prePopulationRetrievalAction = prePopActionWithFeatureSwitch(nino, taxYear),
          agentSuccessAction = (data: R) => {
            successLog(); BadRequest(agentViewProvider(formWithErrors, mode, taxYear, data))
          },
          individualSuccessAction = (data: R) => {
            successLog(); BadRequest(viewProvider(formWithErrors, mode, taxYear, data))
          },
          errorAction = (_: SimpleErrorWrapper) => {
            warnLogger("Failed to load pre-population data. Returning error page")
            InternalServerError
          },
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

