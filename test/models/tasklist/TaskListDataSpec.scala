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

import base.SpecBase
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsObject, JsValue, Json}

import java.time.Instant

class TaskListDataSpec extends AnyFreeSpec with Matchers with SpecBase {

  private val data: JsObject = JsObject(Seq("foo" -> Json.toJson("bar")))

  "TaskListData" - {

    "must return the correct model" in {
      val underTest: TaskListData = TaskListData(mtdItId, taxYear, data)

      underTest.mtdItId mustBe mtdItId
      underTest.taxYear mustBe taxYear
      underTest.data mustBe JsObject(Seq("foo" -> Json.toJson("bar")))
    }

    "must not read a tax year that is not valid" in {
      val underTest: JsValue = Json.toJson(TaskListData(mtdItId, taxYear + 5000, data))

      underTest.validate[TaskListData].toString mustBe "JsError(List((/taxYear,List())))"
    }

    "must read a tax year that is valid" in {
      val lastUpdated: Instant =  Instant.parse("2024-05-16T13:38:09.515Z")
      val taskListData: TaskListData = TaskListData(mtdItId, taxYear, data, lastUpdated)
      val underTest: JsValue = Json.toJson(taskListData)

      underTest.validate[TaskListData].get mustBe taskListData
    }

    "unapply to the correct values" in {
      val lastUpdated: Instant = Instant.now()
      val underTest: Option[(String, Int, JsObject, Instant)] = TaskListData.unapply(TaskListData(mtdItId, taxYear, data, lastUpdated))

      underTest mustBe Some((mtdItId, taxYear, data, lastUpdated))
    }
  }
}
