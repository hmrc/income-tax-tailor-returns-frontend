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

package forms.aboutyou

import forms.mappings.Mappings
import models.aboutyou.CharitableDonations
import models.aboutyou.CharitableDonations.NoDonations
import play.api.data.Form
import play.api.data.Forms.set

import javax.inject.Inject

class CharitableDonationsFormProvider @Inject() extends Mappings {

  def apply(isAgent: Boolean): Form[Set[CharitableDonations]] = {
    val error: String =
      if (isAgent) {
        "charitableDonations.agent.error.required"
      } else {
        "charitableDonations.error.required"
      }

    Form(
      "value" -> set(enumerable[CharitableDonations](error)).verifying(
        firstError(nonEmptySet(error), exclusiveItemInSet(error, NoDonations.toString)))
    )
  }
}
