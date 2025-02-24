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

import cats.data.EitherT
import config.FrontendAppConfig
import controllers.actions.TaxYearAction.taxYearAction
import controllers.actions.{DataRequiredActionProvider, DataRetrievalActionProvider, IdentifierActionProvider}
import forms.FormProvider
import handlers.ErrorHandler
import models.Mode
import models.errors.SimpleErrorWrapper
import models.prePopulation.PrePopulationResponse
import models.requests.DataRequest
import navigation.Navigator
import pages.QuestionPage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Format
import play.api.mvc._
import play.twirl.api.HtmlFormat
import services.{SessionDataService, UserDataService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Logging, PrePopulationHelper}

import scala.concurrent.{ExecutionContext, Future}

abstract class ControllerWithPrePop[I: Format, R <: PrePopulationResponse[I]]
  extends FrontendBaseController
  with I18nSupport
  with PrePopulationHelper[R] { _: Logging =>

  val formProvider: FormProvider[I]
  val userDataService: UserDataService
  val navigator: Navigator
  val identify: IdentifierActionProvider
  val getData: DataRetrievalActionProvider
  val requireData: DataRequiredActionProvider
  val config: FrontendAppConfig
  val errorHandler: ErrorHandler
  val ninoRetrievalService: SessionDataService

  implicit val ec: ExecutionContext

  protected val defaultPrePopulationResponse: R

  private def internalServerErrorResult(implicit request: Request[_]): Result = InternalServerError(
    errorHandler.internalServerErrorTemplate
  )

  protected def viewProvider(form: Form[_], mode: Mode, taxYear: Int, prePopData: R)
                            (implicit request: Request[_]): HtmlFormat.Appendable

  protected def agentViewProvider(form: Form[_], mode: Mode, taxYear: Int, prePopData: R)
                                 (implicit request: Request[_]): HtmlFormat.Appendable

  protected def form(isAgent: Boolean): Form[I] = formProvider(isAgent)

  // If this needs overriding simply change it to be protected
  private def actionChain(taxYear: Int): ActionBuilder[DataRequest, AnyContent] =
    identify(taxYear) andThen
      taxYearAction(taxYear) andThen
      getData(taxYear) andThen
      requireData(taxYear)

  def blockWithNino(taxYear: Int,
                    block: (String, Int, PrePopResult, DataRequest[_]) => Future[Result]): Action[AnyContent] =
    actionChain(taxYear).async { implicit request =>
      def result: EitherT[Future, Unit, Result] = for {
        nino <- EitherT(ninoRetrievalService.getNino("blockWithNino"))
        result <- EitherT.right(
          block(
            dataLogString(nino, taxYear),
            taxYear,
            prePopRetrievalAction(nino, taxYear, request.mtdItId),
            request
          )
        )
      } yield result

      if (config.isPrePopEnabled) {
        result
          .leftMap(_ => InternalServerError(errorHandler.internalServerErrorTemplate))
          .merge
      } else {
        block(
          dataLogString("N/A", taxYear),
          taxYear,
          () => Future.successful(Right(defaultPrePopulationResponse)),
          request
        )
      }
  }

  protected def onPageLoad(pageName: String,
                           incomeType: String,
                           page: QuestionPage[I],
                           mode: Mode)
                          (dataLog: String,
                           taxYear: Int,
                           prePopulationAction: PrePopResult,
                           dataRequest: DataRequest[_]): Future[Result] = {
    implicit val request: DataRequest[_] = dataRequest

    val infoLogger: String => Unit = infoLog(methodLoggingContext = "onPageLoad", dataLog = dataLog)

    infoLogger(s"Received request to retrieve $pageName tailoring page")

    def preparedForm(prePop: R): Form[I] = dataRequest.userAnswers.get(page) match {
      case None =>
        infoLogger(s"No existing $incomeType journey answers found in request model")
        form(dataRequest.isAgent).fill(prePop.toPageModel)
      case Some(value) =>
        infoLogger(s"Existing $incomeType journey answers found. Pre-populating form with previous user answers")
        form(dataRequest.isAgent).fill(value)
    }

    def successLog(): Unit = infoLogger(
      s"Pre-population data successfully retrieved. Redirecting user to $pageName view with form errors"
    )

    blockWithPrePopAndUserType(
      isAgent = dataRequest.isAgent,
      isErrorScenario = false,
      prePopulationRetrievalAction = prePopulationAction,
      agentSuccessAction = (data: R) => {
        successLog()
        Ok(agentViewProvider(preparedForm(data), mode, taxYear, data))
      },
      individualSuccessAction = (data: R) => {
        successLog()
        Ok(viewProvider(preparedForm(data), mode, taxYear, data))
      },
      errorAction = (_: SimpleErrorWrapper) => {
        logger.warn(
          methodContext = "onPageLoad",
          message = "Failed to load pre-population data. Returning error page",
          dataLog = dataLog
        )
        internalServerErrorResult
      },
      extraLogContext = "onPageLoad",
      dataLog = dataLog,
      incomeType = incomeType
    )
  }

  protected def onSubmit(pageName: String,
                         incomeType: String,
                         page: QuestionPage[I],
                         mode: Mode)
                        (dataLog: String,
                         taxYear: Int,
                         prePopulationAction: PrePopResult,
                         dataRequest: DataRequest[_]): Future[Result] = {
    implicit val request: DataRequest[_] = dataRequest

    val infoLogger: String => Unit = infoLog(methodLoggingContext = "onSubmit", dataLog = dataLog)
    val warnLogger: String => Unit = warnLog(methodLoggingContext = "onSubmit", dataLog = dataLog)

    infoLogger(s"Request received to submit user journey answers for $pageName view")

    form(dataRequest.isAgent).bindFromRequest().fold(formWithErrors => {
        warnLogger( s"Errors found in form submission: ${formWithErrors.errors}")

        def successLog(): Unit = infoLogger(s"Pre-population data successfully retrieved. " +
          s"Redirecting user to $pageName view with form errors")

        blockWithPrePopAndUserType(
          isAgent = dataRequest.isAgent,
          isErrorScenario = true,
          prePopulationRetrievalAction = prePopulationAction,
          agentSuccessAction = (data: R) => {
            successLog(); BadRequest(agentViewProvider(formWithErrors, mode, taxYear, data))
          },
          individualSuccessAction = (data: R) => {
            successLog(); BadRequest(viewProvider(formWithErrors, mode, taxYear, data))
          },
          errorAction = (_: SimpleErrorWrapper) => {
            warnLogger("Failed to load pre-population data. Returning error page"); InternalServerError
          },
          extraLogContext = "onSubmit",
          dataLog = dataLog,
          incomeType = incomeType
        )
      },
      value => {
        infoLogger("Form bound successfully from request. Attempting to update user's journey answers")
        for {
          updatedAnswers <- Future.fromTry(dataRequest.userAnswers.set(page, value))
          _ <- userDataService.set(updatedAnswers, dataRequest.userAnswers)
        } yield {
          infoLogger("Journey answers updated successfully. Proceeding to next page in the journey")
          Redirect(navigator.nextPage(page, mode, updatedAnswers))
        }
      }
    )
  }
}

