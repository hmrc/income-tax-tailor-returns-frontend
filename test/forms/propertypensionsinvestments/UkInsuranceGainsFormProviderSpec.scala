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

import forms.behaviours.CheckboxFieldBehaviours
import models.propertypensionsinvestments.UkInsuranceGains
import play.api.data.FormError

class UkInsuranceGainsFormProviderSpec extends CheckboxFieldBehaviours {

  val form = new UkInsuranceGainsFormProvider()(false)
  val agentForm = new UkInsuranceGainsFormProvider()(true)

  ".value" - {

    val fieldName = "value"
    val requiredKey = "ukInsuranceGains.error.required"

    behave like checkboxField[UkInsuranceGains](
      form,
      fieldName,
      validValues  = UkInsuranceGains.values,
      invalidError = FormError(s"$fieldName[0]", "error.invalid")
    )

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )

    behave like exclusiveCheckboxField(
      form,
      UkInsuranceGains.No.toString,
      fieldName,
      UkInsuranceGains.LifeInsurance.toString,
      requiredKey
    )
  }

  ".value for an agent" - {

    val fieldName = "value"
    val requiredKey = "ukInsuranceGains.agent.error.required"

    behave like mandatoryCheckboxField(
      agentForm,
      fieldName,
      requiredKey
    )

    behave like exclusiveCheckboxField(
      agentForm,
      UkInsuranceGains.No.toString,
      fieldName,
      UkInsuranceGains.LifeInsurance.toString,
      requiredKey
    )
  }
}
