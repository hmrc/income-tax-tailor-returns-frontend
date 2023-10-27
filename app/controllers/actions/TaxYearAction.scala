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

package controllers.actions

import models.requests.IdentifierRequest
import play.api.Logger
import play.api.mvc.Results.InternalServerError
import play.api.mvc._
import uk.gov.hmrc.time.TaxYear

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaxYearAction @Inject()(taxYear: Int)
                             (implicit ec: ExecutionContext) extends ActionRefiner[IdentifierRequest, IdentifierRequest] {

  implicit val executionContext: ExecutionContext = ec

  lazy val logger: Logger = Logger.apply(this.getClass)

  override def refine[A](request: IdentifierRequest[A]): Future[Either[Result, IdentifierRequest[A]]] = {
    // todo should retrieve and compare with valid taxYears from businessDetails accounting periods
    if (TaxYear.current.previous.finishYear.equals(taxYear) || TaxYear.current.finishYear.equals(taxYear)) {
      Future.successful(Right(IdentifierRequest(request.request, request.mtdItId, request.isAgent)))
    } else {
      logger.info(s"[TaxYearAction][refine] Invalid tax year, redirecting to error page")
      // todo should redirect to where user selects taxYear
      Future.successful(Left(InternalServerError))
    }
  }
}

object TaxYearAction {
  def taxYearAction(taxYear: Int)(implicit ec: ExecutionContext): TaxYearAction =
    new TaxYearAction(taxYear)
}
