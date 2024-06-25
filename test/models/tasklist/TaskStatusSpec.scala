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

package models.tasklist

import models.tasklist.TaskStatus._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsPath, JsSuccess, Json}

class TaskStatusSpec extends AnyFreeSpec with Matchers {

  "Completed" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(Completed)

      underTest.toString() mustBe s"\"${Completed.toString}\""
      underTest.validate[TaskStatus] mustBe JsSuccess(Completed, JsPath())
    }
  }

  "InProgress" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(InProgress)

      underTest.toString() mustBe s"\"${InProgress.toString}\""
      underTest.validate[TaskStatus] mustBe JsSuccess(InProgress, JsPath())
    }
  }

  "CheckNow" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(CheckNow)

      underTest.toString() mustBe s"\"${CheckNow.toString}\""
      underTest.validate[TaskStatus] mustBe JsSuccess(CheckNow, JsPath())
    }
  }

  "NotStarted" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(NotStarted)

      underTest.toString() mustBe s"\"${NotStarted.toString}\""
      underTest.validate[TaskStatus] mustBe JsSuccess(NotStarted, JsPath())
    }
  }

  "UnderMaintenance" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(UnderMaintenance)

      underTest.toString() mustBe s"\"${UnderMaintenance.toString}\""
      underTest.validate[TaskStatus] mustBe JsSuccess(UnderMaintenance, JsPath())
    }
  }

}
