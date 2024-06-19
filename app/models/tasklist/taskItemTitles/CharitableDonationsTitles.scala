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

import models.WithName
import models.tasklist.TaskTitle
import play.api.libs.json.{Json, OWrites, Reads}

object CharitableDonationsTitles {

  case class DonationsUsingGiftAid() extends WithName("DonationsUsingGiftAidTitle") with TaskTitle
  object DonationsUsingGiftAid {
    implicit val nonStrictReads: Reads[DonationsUsingGiftAid] = Reads.pure(DonationsUsingGiftAid())
    implicit val writes: OWrites[DonationsUsingGiftAid] = OWrites[DonationsUsingGiftAid](_ => Json.obj())
  }

  case class GiftsOfLandOrProperty() extends WithName("GiftsOfLandOrPropertyTitle") with TaskTitle
  object GiftsOfLandOrProperty {
    implicit val nonStrictReads: Reads[GiftsOfLandOrProperty] = Reads.pure(GiftsOfLandOrProperty())
    implicit val writes: OWrites[GiftsOfLandOrProperty] = OWrites[GiftsOfLandOrProperty](_ => Json.obj())
  }

  case class GiftsOfShares() extends WithName("GiftsOfSharesTitle") with TaskTitle
  object GiftsOfShares {
    implicit val nonStrictReads: Reads[GiftsOfShares] = Reads.pure(GiftsOfShares())
    implicit val writes: OWrites[GiftsOfShares] = OWrites[GiftsOfShares](_ => Json.obj())
  }

  case class GiftsToOverseasCharities() extends WithName("GiftsToOverseasCharities") with TaskTitle

  object GiftsToOverseasCharities{
    implicit val nonStrictReads: Reads[GiftsToOverseasCharities] = Reads.pure(GiftsToOverseasCharities())
    implicit val writes: OWrites[GiftsToOverseasCharities] = OWrites[GiftsToOverseasCharities](_ => Json.obj())
  }

}
