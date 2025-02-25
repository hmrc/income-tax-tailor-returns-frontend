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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Logging, PrePopulationHelper}

import scala.concurrent.{ExecutionContext, Future}

/**
 * An interface designed to provide comprehensive boiler-plating for tailoring question pages which require handling of
 * pre-population data. See JobSeekersAllowanceController for how this may be implemented.
 *
 * @param format$I$0 Implicit Play JSON Format defined for type I
 * @tparam I The relevant user answers page model associated with a given view
 * @tparam R The relevant pre-population data type associated with a given view.
 *           Must contain an inner type matching the user answers page model
 */
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

  /**
   * Any class which implements ControllerWithPrePop must define some function which can be called to retrieve
   * pre-population data R. This method returns that function for a given user's details
   * @param nino The NINO associated with the current self-assessment tax submission being made.
   *             Required to retrieve pre-population data from HMRC HODs
   * @param taxYear The tax year associated with the current self-assessment tax submission being made.
   *                Required to retrieve pre-population data from HMRC HODs
   * @param mtdItId The unique MTD IT ID associated with a given NINO.
   *                Required for authorisation when making calls to ITSASS backend microservices
   * @param hc The header carrier. Contains relevant request headers
   * @return A function which, when called, will return a Future containing either an error expressed as an instance of
   *         SimpleErrorWrapper, or pre-population data expressed as an instance of R
   */
  protected def prePopRetrievalAction(nino: String, taxYear: Int, mtdItId: String)
                                     (implicit hc: HeaderCarrier): PrePopResult

  /**
   * A method providing a generic interface for generating a view to be served to the user
   * @param form The HTML form associated with the view being provided
   * @param mode A helper trait used during view generation
   * @param taxYear The tax year associated with the current self-assessment tax submission
   * @param prePopData A data model detailing the existence of any relevant pre-existing information which may be held
   *                   by HMRC which is relevant to the view being served to the user
   * @param request A data model containing information about current ongoing request
   * @return A view to be served to the user
   */
  protected def viewProvider(form: Form[_], mode: Mode, taxYear: Int, prePopData: R)
                            (implicit request: DataRequest[_]): HtmlFormat.Appendable

  /**
   * A chain of actions to be completed before any block is processed. Can be overriden so long as the resulting
   * Request type is still a DataRequest[_].
   * @param taxYear The tax year associated with the current self-assessment tax submission
   * @return A chain of actions resulting in a DataRequest request model being created
   */
  protected def actionChain(taxYear: Int): ActionBuilder[DataRequest, AnyContent] =
    identify(taxYear) andThen
      taxYearAction(taxYear) andThen
      getData(taxYear) andThen
      requireData(taxYear)

  private def internalServerErrorResult(implicit request: Request[_]): Result = InternalServerError(
    errorHandler.internalServerErrorTemplate
  )

  private def provideViewWithLogging(view: HtmlFormat.Appendable,
                                     logger: String => Unit,
                                     hasFormErrors: Boolean)
                                    (implicit request: DataRequest[_]): HtmlFormat.Appendable = {
    def logStr(userType: String): String = s"Pre-population data successfully retrieved. Redirecting to $userType view"

    val errsString = if (hasFormErrors) " with form errors" else ""
    if (request.isAgent) {
      logger(logStr("agent") + errsString)
    } else {
      logger(logStr("individual") + errsString)
    }
    view
  }

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

    val infoLogger: String => Unit = infoLog(secondaryContext = "onPageLoad", dataLog = dataLog)

    val form: Form[I] = formProvider(dataRequest.isAgent)

    infoLogger(s"Received request to retrieve $pageName tailoring page")

    def preparedForm(prePop: R): Form[I] = dataRequest.userAnswers.get(page) match {
      case None =>
        infoLogger(s"No existing $incomeType journey answers found in request model")
        form.fill(prePop.toPageModel)
      case Some(value) =>
        infoLogger(s"Existing $incomeType journey answers found. Pre-populating form with previous user answers")
        form.fill(value)
    }

    blockWithPrePop(
      prePopulationRetrievalAction = prePopulationAction,
      successAction = (data: R) => Ok(provideViewWithLogging(
        view = viewProvider(preparedForm(data), mode, taxYear, data),
        logger = infoLogger,
        hasFormErrors = false
      )),
      errorAction = (_: SimpleErrorWrapper) => {
        logger.warn(
          secondaryContext = "onPageLoad",
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

    val infoLogger: String => Unit = infoLog(secondaryContext = "onSubmit", dataLog = dataLog)
    val warnLogger: String => Unit = warnLog(secondaryContext = "onSubmit", dataLog = dataLog)

    infoLogger(s"Request received to submit user journey answers for $pageName view")

    val form: Form[I] = formProvider(dataRequest.isAgent)

    form.bindFromRequest().fold(formWithErrors => {
      warnLogger(s"Errors found in form submission: ${formWithErrors.errors}")

      blockWithPrePop(
        prePopulationRetrievalAction = prePopulationAction,
        successAction = (data: R) => BadRequest(provideViewWithLogging(
          view = viewProvider(formWithErrors, mode, taxYear, data),
          logger = infoLogger,
          hasFormErrors = true)),
        errorAction = (_: SimpleErrorWrapper) => {
          warnLogger("Failed to load pre-population data. Returning error page");
          InternalServerError
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

