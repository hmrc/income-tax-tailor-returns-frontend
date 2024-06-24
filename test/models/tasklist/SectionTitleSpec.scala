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

import models.tasklist.SectionTitle._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsPath, JsSuccess, Json}

class SectionTitleSpec extends AnyFreeSpec with Matchers {

  "AboutYouTitle" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(AboutYouTitle)

      underTest.toString() mustBe s"\"${AboutYouTitle.toString}\""
      underTest.validate[SectionTitle] mustBe JsSuccess(AboutYouTitle, JsPath())
    }
  }

  "CharitableDonationsTitle" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(CharitableDonationsTitle)

      underTest.toString() mustBe s"\"${CharitableDonationsTitle.toString}\""
      underTest.validate[SectionTitle] mustBe JsSuccess(CharitableDonationsTitle, JsPath())
    }
  }

  "EmploymentTitle" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(EmploymentTitle)

      underTest.toString() mustBe s"\"${EmploymentTitle.toString}\""
      underTest.validate[SectionTitle] mustBe JsSuccess(EmploymentTitle, JsPath())
    }
  }

  "SelfEmploymentTitle" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(SelfEmploymentTitle)

      underTest.toString() mustBe s"\"${SelfEmploymentTitle.toString}\""
      underTest.validate[SectionTitle] mustBe JsSuccess(SelfEmploymentTitle, JsPath())
    }
  }

  "EsaTitle" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(EsaTitle)

      underTest.toString() mustBe s"\"${EsaTitle.toString}\""
      underTest.validate[SectionTitle] mustBe JsSuccess(EsaTitle, JsPath())
    }
  }

  "JsaTitle" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(JsaTitle)

      underTest.toString() mustBe s"\"${JsaTitle.toString}\""
      underTest.validate[SectionTitle] mustBe JsSuccess(JsaTitle, JsPath())
    }
  }

  "PensionsTitle" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(PensionsTitle)

      underTest.toString() mustBe s"\"${PensionsTitle.toString}\""
      underTest.validate[SectionTitle] mustBe JsSuccess(PensionsTitle, JsPath())
    }
  }

  "InsuranceGainsTitle" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(InsuranceGainsTitle)

      underTest.toString() mustBe s"\"${InsuranceGainsTitle.toString}\""
      underTest.validate[SectionTitle] mustBe JsSuccess(InsuranceGainsTitle, JsPath())
    }
  }

  "PaymentsIntoPensionsTitle" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(PaymentsIntoPensionsTitle)

      underTest.toString() mustBe s"\"${PaymentsIntoPensionsTitle.toString}\""
      underTest.validate[SectionTitle] mustBe JsSuccess(PaymentsIntoPensionsTitle, JsPath())
    }
  }

  "InterestTitle" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(InterestTitle)

      underTest.toString() mustBe s"\"${InterestTitle.toString}\""
      underTest.validate[SectionTitle] mustBe JsSuccess(InterestTitle, JsPath())
    }
  }

  "DividendsTitle" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(DividendsTitle)

      underTest.toString() mustBe s"\"${DividendsTitle.toString}\""
      underTest.validate[SectionTitle] mustBe JsSuccess(DividendsTitle, JsPath())
    }
  }
}
