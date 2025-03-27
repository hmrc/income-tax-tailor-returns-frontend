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

import models.requests.DataRequest
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait OverrideRequestAction extends ActionBuilder[DataRequest, AnyContent]
  with ActionFunction[Request, DataRequest]

trait OverrideRequestActionProvider {
  def apply(overrideRequest: DataRequest[_]): ActionBuilder[DataRequest, AnyContent]
}

@Singleton
class OverrideRequestActionProviderImpl @Inject()(parser: BodyParsers.Default)
                                                 (implicit val ec: ExecutionContext) extends OverrideRequestActionProvider {
  override def apply(overrideRequest: DataRequest[_]): ActionBuilder[DataRequest, AnyContent] =
    new OverrideRequestActionImpl(overrideRequest)(ec, parser)
}

class OverrideRequestActionImpl @Inject()(overrideRequest: DataRequest[_])
                                         (implicit val executionContext: ExecutionContext,
                                          val parser: BodyParsers.Default) extends OverrideRequestAction {

  override def invokeBlock[A](request: Request[A], block: DataRequest[A] => Future[Result]): Future[Result] =
    block(overrideRequest.copy(request = request))
}
