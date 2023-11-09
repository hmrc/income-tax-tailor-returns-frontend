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

package forms.propertypensionsinvestments

import forms.mappings.Mappings
import models.propertypensionsinvestments.Pensions
import play.api.data.Form
import play.api.data.Forms.set

import javax.inject.Inject

class PensionsFormProvider @Inject() extends Mappings {

  def apply(isAgent: Boolean): Form[Set[Pensions]] = {
    val error: String =
      if (isAgent) {
        "pensions.agent.error.required"
      } else {
        "pensions.error.required"
      }
    Form(
      "value" -> set(enumerable[Pensions](error)).verifying(
        firstError(nonEmptySet(error), exclusiveItemInSet(error, Pensions.ExclusiveOption.toString))
      )
    )
  }
}
