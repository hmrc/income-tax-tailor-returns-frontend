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

import connectors.IncomeTaxSessionDataConnector
import models.requests.{DataRequest, DataRequestWithNino}
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait DataRequiredWithNinoAction extends ActionRefiner[DataRequest, DataRequestWithNino]

trait DataRequiredWithNinoActionProvider {
  def apply(taxYear: Int): DataRequiredWithNinoAction
}

@Singleton
class DataRequiredWithNinoActionProviderImpl @Inject()(connector: IncomeTaxSessionDataConnector,
                                                       parser: BodyParsers.Default)
                                                      (implicit val executionContext: ExecutionContext)
  extends DataRequiredWithNinoActionProvider {

  override def apply(taxYear: Int): DataRequiredWithNinoAction =
    new DataRequiredWithNinoActionImpl(taxYear)(connector, parser)
}

@Singleton
class DataRequiredWithNinoActionImpl @Inject()(taxYear: Int)
                                              (incomeTaxSessionDataConnector: IncomeTaxSessionDataConnector,
                                               val parser: BodyParsers.Default)
                                              (implicit val executionContext: ExecutionContext) extends DataRequiredWithNinoAction {

  protected def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequestWithNino[A]]] = ???
}
