/*
 * Copyright 2024 HM Revenue & Customs
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

package audit

import models.{Enumerable, WithName}

trait AuditDescriptors
object AuditDescriptors extends Enumerable.Implicits {

  case object UserDataCompleteType extends WithName("UserDataComplete") with AuditDescriptors
  case object UserDataCompleteTransaction extends WithName("user-data-complete") with AuditDescriptors
  case object UserDataIncompleteType extends WithName("UserDataIncomplete") with AuditDescriptors
  case object UserDataIncompleteTransaction extends WithName("user-data-incomplete") with AuditDescriptors
  case object UserDataUpdatedType extends WithName("UserDataUpdated") with AuditDescriptors
  case object UserDataUpdatedTransaction extends WithName("user-data-updated") with AuditDescriptors

  val values: Set[AuditDescriptors] = Set(
    UserDataCompleteType, UserDataCompleteTransaction, UserDataIncompleteType, UserDataIncompleteTransaction, UserDataUpdatedType, UserDataUpdatedTransaction
  )

  implicit val enumerable: Enumerable[AuditDescriptors] =
    Enumerable(values.toSeq.map(v => v.toString -> v): _*)
}
