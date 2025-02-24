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

package forms.workandbenefits

import forms.FormProvider
import models.workandbenefits.JobseekersAllowance
import play.api.data.Form
import play.api.data.Forms.set

import javax.inject.Inject

class JobseekersAllowanceFormProvider @Inject() extends FormProvider[Set[JobseekersAllowance]] {

  def apply(isAgent: Boolean): Form[Set[JobseekersAllowance]] = {
    val error: String =
      if (isAgent) {
        "jobseekersAllowance.agent.error.required"
      } else {
        "jobseekersAllowance.error.required"
      }
    Form(
      "value" -> set(enumerable[JobseekersAllowance](error)).verifying(
        firstError(nonEmptySet(error), exclusiveItemInSet(error, JobseekersAllowance.No.toString))
      )
    )
  }
}
