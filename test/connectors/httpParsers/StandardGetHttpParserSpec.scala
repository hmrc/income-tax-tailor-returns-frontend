/*
 * Copyright 2025 HM Revenue & Customs
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

package connectors.httpParsers

import base.SpecBase
import models.errors.SimpleErrorWrapper
import play.api.http.Status.{BAD_REQUEST, IM_A_TEAPOT, INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.{Json, Reads}
import uk.gov.hmrc.http.HttpResponse
import utils.Logging

class StandardGetHttpParserSpec extends SpecBase {

  case class DummyDataModel(someData: String)

  object DummyDataModel {
    implicit val reads: Reads[DummyDataModel] = Json.reads[DummyDataModel]
  }

  object DummyGetHttpParser extends StandardGetHttpParser[DummyDataModel] with Logging {
    override protected val primaryContext: String = "DummyLogString"
  }

  "parseToDataModel" -> {
    val dummyError: SimpleErrorWrapper = SimpleErrorWrapper(IM_A_TEAPOT)
    "should return an error when body string cannot be parsed to JSON" -> {
      val result = DummyGetHttpParser.parseToDataModel("")
      result mustBe a[Left[_, _]]
      result.swap.getOrElse(dummyError) mustBe SimpleErrorWrapper(INTERNAL_SERVER_ERROR)
    }

    "should return an error when body string cannot be parsed to data model" -> {
      val result = DummyGetHttpParser.parseToDataModel("""{}""")
      result mustBe a[Left[_, _]]
      result.swap.getOrElse(dummyError) mustBe SimpleErrorWrapper(INTERNAL_SERVER_ERROR)
    }

    "should return a data model when body string can be parsed to data model" -> {
      val result = DummyGetHttpParser.parseToDataModel("""{"someData": "data"}""")
      result mustBe a[Right[_, _]]
      result.getOrElse(DummyDataModel("nonsense")).someData mustBe "data"
    }
  }

  "reads" -> {
    "should return an error when HttpResponse status is not 200" in {
      val result = DummyGetHttpParser.StandardGetHttpReads.read("", "", HttpResponse(BAD_REQUEST, ""))
      result mustBe a[Left[_, _]]
      result.swap.getOrElse(SimpleErrorWrapper(IM_A_TEAPOT)).status mustBe BAD_REQUEST
    }

    "should parse a valid response body when HttpResponse status is 200" in {
      val result = DummyGetHttpParser.StandardGetHttpReads.read("", "", HttpResponse(OK, """{"someData": "data"}"""))
      result mustBe a[Right[_, _]]
      result.getOrElse(DummyDataModel("dummy")).someData mustBe "data"
    }

    "should not should parse an invalid response body when HttpResponse status is 200" in {
      val result = DummyGetHttpParser.StandardGetHttpReads.read("", "", HttpResponse(OK, """{"someData": 1}"""))
      result mustBe a[Left[_, _]]
      result.swap.getOrElse(SimpleErrorWrapper(IM_A_TEAPOT)).status mustBe INTERNAL_SERVER_ERROR
    }
  }

}
