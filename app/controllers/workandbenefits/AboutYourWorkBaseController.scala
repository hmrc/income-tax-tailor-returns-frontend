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

import com.google.inject.{Inject, Singleton}
import controllers.actions.TaxYearAction.taxYearAction
import controllers.actions.{DataRequiredActionProvider, DataRetrievalActionProvider, IdentifierActionProvider}
import models.Mode
import models.requests.DataRequest
import pages.aboutyou.FosterCarerPage
import play.api.mvc.{Action, ActionBuilder, AnyContent}
import utils.Logging

import scala.concurrent.ExecutionContext

@Singleton
class AboutYourWorkBaseController @Inject()(controller: AboutYourWorkController,
                                            radioController: AboutYourWorkRadioController,
                                            val identify: IdentifierActionProvider,
                                            val getData: DataRetrievalActionProvider,
                                            val requireData: DataRequiredActionProvider)
                                           (implicit val ec: ExecutionContext) extends Logging {
  override protected val primaryContext: String = classOf[AboutYourWorkBaseController].getSimpleName

  private def requestToAgentString(implicit request: DataRequest[_]): String = if (request.isAgent) "agent" else ""

  private def actionChain(taxYear: Int): ActionBuilder[DataRequest, AnyContent] =
    identify(taxYear) andThen taxYearAction(taxYear) andThen getData(taxYear) andThen requireData(taxYear)

  private def isFosterCarer(dataLog: String, extraLoggingContext: String)
                           (implicit request: DataRequest[_]): Boolean = {
    val methodLoggingContext: String = "isFosterCarer"

    val infoLogger = infoLog(
      secondaryContext = methodLoggingContext,
      dataLog = dataLog,
      extraContext = Some(extraLoggingContext)
    )

    infoLogger("Attempting to retrieve user answers for FosterCarer about you page")

    val answersOpt = request.userAnswers.get(FosterCarerPage)
    answersOpt.fold {
      infoLogger("No user answers found for FosterCarer about you page. Defaulting to user answer to 'false'")
      false
    } { value =>
      infoLogger("User answers found for FosterCarer about you page. Returning answers")
      value
    }
  }

  def onPageLoad(mode: Mode, taxYear: Int): Action[AnyContent] = actionChain(taxYear).async { implicit request =>
    val methodLoggingContext: String = "onPageLoad"

    val dataLog: String = noNinoDataLogString(
      mtdItId = request.mtdItId,
      taxYear = taxYear,
      requestContext = Some(requestToAgentString)
    )

    val infoLogger: String => Unit = infoLog(methodLoggingContext, dataLog)
    infoLogger("Received request to retrieve AboutYourWork tailoring page")

    if (!isFosterCarer(dataLog, methodLoggingContext)) {
      infoLogger("Returning AboutYourWork tailoring page")
      controller.onPageLoad(mode, taxYear, Some(request))(request)
    } else {
      infoLogger("Returning AboutYourWork tailoring page for foster carers")
      radioController.onPageLoad(mode, taxYear, Some(request))(request)
    }
  }

  def onSubmit(mode: Mode, taxYear: Int): Action[AnyContent] = actionChain(taxYear).async { implicit request =>
    val methodLoggingContext: String = "onSubmit"

    val dataLog: String = noNinoDataLogString(
      mtdItId = request.mtdItId,
      taxYear = taxYear,
      requestContext = Some(requestToAgentString)
    )

    val infoLogger: String => Unit = infoLog(methodLoggingContext, dataLog)
    infoLogger("Received request to submit user journey answers for AboutYourWork view")

    if (!isFosterCarer(dataLog, methodLoggingContext)) {
      infoLogger("Submitting user journey answers for AboutYourWork view")
      controller.onSubmit(mode, taxYear, Some(request))(request)
    } else {
      infoLogger("Submitting user journey answers for AboutYourWork view for foster carers")
      radioController.onSubmit(mode, taxYear, Some(request))(request)
    }
  }
}
