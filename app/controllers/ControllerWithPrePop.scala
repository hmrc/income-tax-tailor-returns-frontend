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
import connectors.ConnectorResponse
import forms.FormProvider
import handlers.ErrorHandler
import models.{Mode, UserAnswers}
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

  type PrePopResult = () => ConnectorResponse[R]

  val formProvider: FormProvider[I]
  val userDataService: UserDataService
  val navigator: Navigator
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
   * @param requestOverrideOpt An optional pre-existing data request model to be used for this request
   * @return A chain of actions resulting in a DataRequest request model being created
   */
  protected def actionChain(taxYear: Int,
                            requestOverrideOpt: Option[DataRequest[_]] = None): ActionBuilder[DataRequest, AnyContent]

  /**
   * This method wraps around some block which requires a NINO. It first attempts to retrieve the NINO from the session
   * data service and then uses this NINO to build a log string, and prePopRetrievalAction function to be passed into
   * the block. It then processes the block with these values and returns the result.
   * If the pre-pop feature switch is disabled it instead passes a dummy prePopRetrievalAction into the block and does
   * not attempt to retrieve the NINO.
   * The actionChain this method uses converts a regular Request[_] into a DataRequest[_] to provide access to certain
   * user details. This may also handle things like authentication, or updating the current session.
   *
   * @param taxYear The tax year associated with the current self-assessment tax submission
   * @param block   An action to be completed once the NINO for the request is retrieved
   * @return An action resulting from the provided block
   */
  protected[controllers] def blockWithNino(taxYear: Int,
                                           extraContext: String,
                                           requestOverrideOpt: Option[DataRequest[_]] = None)
                                          (block: (String, PrePopResult, DataRequest[_]) => Future[Result]): Action[AnyContent] = {
    val methodContext: String = "blockWithNino"

    logger.info(
      secondaryContext = methodContext,
      message = "Received request to handle block with NINO, and pre-population actions. Processing action chain",
      extraContext = Some(extraContext)
    )

    actionChain(taxYear, requestOverrideOpt).async { implicit request =>
      val dataLog: String = noNinoDataLogString(request.mtdItId, taxYear, Some(requestToAgentString))
      val infoLogger = infoLog(methodContext, dataLog, Some(extraContext))
      val errorLogger = errorLog(methodContext, dataLog, Some(extraContext))

      infoLogger("Action chain completed successfully")

      def result: EitherT[Future, Unit, Result] = for {
        nino <- EitherT(ninoRetrievalService.getNino())
        result <- EitherT.right {
          infoLogger(s"Successfully retrieved NINO from session data service. Processing block with NINO: $nino")

          block(
            dataLogString(nino, taxYear, Some(requestToAgentString)),
            prePopRetrievalAction(nino, taxYear, request.mtdItId),
            request
          )
        }
      } yield result

      if (config.isPrePopEnabled) {
        infoLogger("Pre-population feature switch enabled. Attempting to retrieve NINO from session data service")
        result
          .leftMap(_ => {
            errorLogger("Failed to retrieve NINO from session data service. Returning an error page")
            InternalServerError(errorHandler.internalServerErrorTemplate)
          })
          .merge
      } else {
        infoLogger("Pre-population feature switch disabled. Processing block without NINO, or pre-population actions")
        block(
          dataLog,
          () => Future.successful(Right(defaultPrePopulationResponse)),
          request
        )
      }
    }
  }

  /**
   * A generic boilerplate method for loading tailoring question pages with pre-pop actions
   *
   * @param pageName   String used for logging. Represents the current tailoring page being handled
   * @param incomeType String used for logging. Represents the current income type being handled
   * @param page       The relevant page model for the current tailoring page being handled
   * @param mode       A helper trait used during view generation
   * @param taxYear    The tax year associated with the current self-assessment tax submission
   * @param requestOverrideOpt An optional pre-existing data request model to be used for this request
   * @return Some action. Typically, either an error view or the current tailoring question page is served
   */
  protected[controllers] def onPageLoad(pageName: String,
                                        incomeType: String,
                                        page: QuestionPage[I],
                                        mode: Mode,
                                        taxYear: Int,
                                        requestOverrideOpt: Option[DataRequest[_]] = None): Action[AnyContent] =
    blockWithNino(taxYear, "onPageLoad", requestOverrideOpt) {
      (dataLog: String, prePopulationAction: PrePopResult, dataRequest: DataRequest[_]) =>
        implicit val request: DataRequest[_] = dataRequest

        val methodContext: String = "onPageLoad"
        val infoLogger: String => Unit = infoLog(secondaryContext = methodContext, dataLog = dataLog)
        val errorLogger: String => Unit = errorLog(secondaryContext = methodContext, dataLog = dataLog)

        infoLogger(s"Received request to retrieve $pageName tailoring page")

        def preparedForm(prePop: R): Form[I] = dataRequest.userAnswers.get(page) match {
          case None if prePop.hasPrePop =>
            infoLogger(s"No existing $incomeType journey answers found. Pre-filling form with pre-pop data")
            fillFormFromPageModel(form, prePop.toPageModel)
          case None =>
            infoLogger(s"No existing $incomeType journey answers or pre-pop data found. Returning empty form")
            form
          case Some(value) =>
            infoLogger(s"Existing $incomeType journey answers found. Pre-filling form with previous user answers")
            form.fill(value)
        }

        blockWithPrePop(
          prePopulationRetrievalAction = prePopulationAction,
          successAction = (data: R) => {
            infoLogger(prePopSuccessMessage)
            Ok(viewProvider(preparedForm(data), mode, taxYear, data))
          },
          errorAction = prePopErrorResult(errorLogger),
          extraLogContext = "onPageLoad",
          dataLog = dataLog,
          incomeType = incomeType
        )
    }

  /**
   * A generic boilerplate method for submitting tailoring question pages
   *
   * @param pageName   String used for logging. Represents the current tailoring page being handled
   * @param incomeType String used for logging. Represents the current income type being handled
   * @param page       The relevant page model for the current tailoring page being handled
   * @param mode       A helper trait used during view generation
   * @param taxYear    The tax year associated with the current self-assessment tax submission
   * @param requestOverrideOpt An optional pre-existing data request model to be used for this request
   * @return Some action. This may be an error view, the current tailoring question page if there are form errors
   *         present in the request, or the next page in the journey if there are not
   */
  protected[controllers] def onSubmit(pageName: String,
                                      incomeType: String,
                                      page: QuestionPage[I],
                                      mode: Mode,
                                      taxYear: Int,
                                      requestOverrideOpt: Option[DataRequest[_]] = None): Action[AnyContent] =
    actionChain(taxYear, requestOverrideOpt).async { implicit request =>
      val methodLogString: String = "onSubmit"
      val dataLog: String = noNinoDataLogString(request.mtdItId, taxYear, Some(requestToAgentString))
      val infoLogger: String => Unit = infoLog(secondaryContext = methodLogString, dataLog = dataLog)
      val warnLogger: String => Unit = warnLog(secondaryContext = methodLogString, dataLog = dataLog)

      infoLogger(s"Request received to submit user journey answers for $pageName view")

      form.bindFromRequest().fold(formWithErrors => {
        warnLogger(s"Errors found in form submission. Returning $pageName view with errors: ${formWithErrors.errors}")
        Future.successful(BadRequest(viewProvider(formWithErrors, mode, taxYear, defaultPrePopulationResponse)))
      },
        value => {
          infoLogger("Form bound successfully from request. Attempting to update user's journey answers")
          for {
            updatedAnswers <- updateAnswers(page, value)
            _ <- userDataService.set(updatedAnswers, request.userAnswers)
          } yield {
            infoLogger("Journey answers updated successfully. Proceeding to next page in the journey")
            Redirect(navigator.nextPage(page, mode, updatedAnswers))
          }
        }
      )
    }

  protected def updateAnswers(page: QuestionPage[I], value: I)
                             (implicit request: DataRequest[_]): Future[UserAnswers] =
    Future.fromTry(request.userAnswers.set(page, value))

  protected def fillFormFromPageModel(form: Form[I], pageModel: I): Form[I]  = form.fill(pageModel)

  private def internalServerErrorResult(implicit request: Request[_]): Result = InternalServerError(
    errorHandler.internalServerErrorTemplate
  )

  private def requestToAgentString(implicit request: DataRequest[_]): String = if (request.isAgent) "agent" else ""

  private def prePopSuccessMessage(implicit request: DataRequest[_]) = {
    s"Pre-population data successfully retrieved. Redirecting to $requestToAgentString view"
  }

  private def prePopErrorResult(errLogger: String => Unit)
                               (implicit request: DataRequest[_]): SimpleErrorWrapper => Result = {
    _: SimpleErrorWrapper =>
      errLogger("Failed to load pre-population data. Returning error page")
      internalServerErrorResult
  }

  private def form(implicit request: DataRequest[_]): Form[I] = formProvider(request.isAgent)
}

