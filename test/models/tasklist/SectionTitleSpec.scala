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
      val underTest = AboutYouTitle()
      Json.toJson(underTest).toString() mustBe "{}"
      Json.toJson(underTest).validate[AboutYouTitle] mustBe JsSuccess(AboutYouTitle, JsPath())
    }
  }

  "CharitableDonationsTitle" - {

    "must parse to and from json" in {
      val underTest = CharitableDonationsTitle()
      Json.toJson(underTest).toString() mustBe "{}"
      Json.toJson(underTest).validate[CharitableDonationsTitle] mustBe JsSuccess(CharitableDonationsTitle, JsPath())
    }
  }

  "EmploymentTitle" - {

    "must parse to and from json" in {
      val underTest = EmploymentTitle()
      Json.toJson(underTest).toString() mustBe "{}"
      Json.toJson(underTest).validate[EmploymentTitle] mustBe JsSuccess(EmploymentTitle, JsPath())
    }
  }

  "SelfEmploymentTitle" - {

    "must parse to and from json" in {
      val underTest = SelfEmploymentTitle()
      Json.toJson(underTest).toString() mustBe "{}"
      Json.toJson(underTest).validate[SelfEmploymentTitle] mustBe JsSuccess(SelfEmploymentTitle, JsPath())
    }
  }

  "EsaTitle" - {

    "must parse to and from json" in {
      val underTest = EsaTitle()
      Json.toJson(underTest).toString() mustBe "{}"
      Json.toJson(underTest).validate[EsaTitle] mustBe JsSuccess(EsaTitle, JsPath())
    }
  }

  "JsaTitle" - {

    "must parse to and from json" in {
      val underTest = JsaTitle()
      Json.toJson(underTest).toString() mustBe "{}"
      Json.toJson(underTest).validate[JsaTitle] mustBe JsSuccess(JsaTitle, JsPath())
    }
  }

  "PensionsTitle" - {

    "must parse to and from json" in {
      val underTest = PensionsTitle()
      Json.toJson(underTest).toString() mustBe "{}"
      Json.toJson(underTest).validate[PensionsTitle] mustBe JsSuccess(PensionsTitle, JsPath())
    }
  }

  "InsuranceGainsTitle" - {

    "must parse to and from json" in {
      val underTest = InsuranceGainsTitle()
      Json.toJson(underTest).toString() mustBe "{}"
      Json.toJson(underTest).validate[InsuranceGainsTitle] mustBe JsSuccess(InsuranceGainsTitle, JsPath())
    }
  }

  "PaymentsIntoPensionsTitle" - {

    "must parse to and from json" in {
      val underTest = PaymentsIntoPensionsTitle()
      Json.toJson(underTest).toString() mustBe "{}"
      Json.toJson(underTest).validate[PaymentsIntoPensionsTitle] mustBe JsSuccess(PaymentsIntoPensionsTitle, JsPath())
    }
  }

  "InterestTitle" - {

    "must parse to and from json" in {
      val underTest = InterestTitle()
      Json.toJson(underTest).toString() mustBe "{}"
      Json.toJson(underTest).validate[InterestTitle] mustBe JsSuccess(InterestTitle, JsPath())
    }
  }

  "DividendsTitle" - {

    "must parse to and from json" in {
      val underTest = DividendsTitle()
      Json.toJson(underTest).toString() mustBe "{}"
      Json.toJson(underTest).validate[DividendsTitle] mustBe JsSuccess(DividendsTitle, JsPath())
    }
  }
}
