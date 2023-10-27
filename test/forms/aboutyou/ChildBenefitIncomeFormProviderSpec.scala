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

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class ChildBenefitIncomeFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "childBenefitIncome.error.required"
  val agentRequiredKey = "childBenefitIncome.agent.error.required"
  val invalidKey = "error.boolean"

  val form = new ChildBenefitIncomeFormProvider()(false)
  val agentForm = new ChildBenefitIncomeFormProvider()(true)

  ".value" - {

    val fieldName = "value"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".value for an agent" - {

    val fieldName = "value"

    behave like mandatoryField(
      agentForm,
      fieldName,
      requiredError = FormError(fieldName, agentRequiredKey)
    )
  }
}
