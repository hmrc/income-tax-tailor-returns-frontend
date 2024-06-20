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

package models.tasklist.taskItemTitles

import models.tasklist.taskItemTitles.PaymentsIntoPensionsTitles.PaymentsIntoUk
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsPath, JsSuccess, Json}

class PaymentsIntoUkSpec extends AnyFreeSpec with Matchers {

  "PaymentsIntoUk" - {

    "must parse to and from json" in {
      val underTest = PaymentsIntoUk()
      Json.toJson(underTest).toString() mustBe "{}"
      Json.toJson(underTest).validate[PaymentsIntoUk] mustBe JsSuccess(PaymentsIntoUk, JsPath())
    }
  }
}
