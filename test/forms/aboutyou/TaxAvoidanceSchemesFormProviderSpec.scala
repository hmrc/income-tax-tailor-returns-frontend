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

import forms.behaviours.CheckboxFieldBehaviours
import models.aboutyou.TaxAvoidanceSchemes
import models.aboutyou.TaxAvoidanceSchemes.{NoAvoidance, TaxAvoidance}
import play.api.data.FormError

class TaxAvoidanceSchemesFormProviderSpec extends CheckboxFieldBehaviours {

  val form = new TaxAvoidanceSchemesFormProvider()(false)
  val agentForm = new TaxAvoidanceSchemesFormProvider()(true)

  ".value" - {

    val fieldName = "value"
    val requiredKey = "taxAvoidanceSchemes.error.required"

    behave like checkboxField[TaxAvoidanceSchemes](
      form,
      fieldName,
      validValues  = TaxAvoidanceSchemes.values,
      invalidError = FormError(s"$fieldName[0]", "error.invalid")
    )

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )

    behave like exclusiveCheckboxField(
      form,
      NoAvoidance.toString,
      fieldName,
      TaxAvoidance.toString,
      requiredKey
    )
  }

  ".value for an agent" - {

    val fieldName = "value"
    val requiredKey = "taxAvoidanceSchemes.agent.error.required"

    behave like mandatoryCheckboxField(
      agentForm,
      fieldName,
      requiredKey
    )

    behave like exclusiveCheckboxField(
      agentForm,
      NoAvoidance.toString,
      fieldName,
      TaxAvoidance.toString,
      requiredKey
    )
  }
}
