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

package controllers.actions

import models.requests.{DataRequest, DataRequestWithNino}
import play.api.mvc.Results.InternalServerError
import play.api.mvc._
import services.NinoRetrievalService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait DataRequiredWithNinoAction extends ActionRefiner[DataRequest, DataRequestWithNino]

trait DataRequiredWithNinoActionProvider {
  def apply(taxYear: Int): DataRequiredWithNinoAction
}

@Singleton
class DataRequiredWithNinoActionProviderImpl @Inject()(service: NinoRetrievalService,
                                                       parser: BodyParsers.Default)
                                                      (implicit val executionContext: ExecutionContext)
  extends DataRequiredWithNinoActionProvider {

  override def apply(taxYear: Int): DataRequiredWithNinoAction =
    new DataRequiredWithNinoActionImpl(taxYear)(service, parser)
}

@Singleton
class DataRequiredWithNinoActionImpl @Inject()(taxYear: Int)
                                              (service: NinoRetrievalService,
                                               val parser: BodyParsers.Default)
                                              (implicit val executionContext: ExecutionContext) extends DataRequiredWithNinoAction {

  override protected def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequestWithNino[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    for {
      serviceResponse <- service.getNinoOpt(request)
      result = serviceResponse.fold(
        Left[Result, DataRequestWithNino[A]](InternalServerError).withRight //TODO: This is probably a v&c redirect
      )(
        nino => Right(DataRequestWithNino(request, nino))
      )
    } yield result
  }
}
