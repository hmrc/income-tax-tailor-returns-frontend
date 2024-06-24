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
import models.tasklist.{ReadsWrites, TaskTitle}

object UkInterestTitles {

  case class BanksAndBuilding() extends WithName("BanksAndBuildingTitle") with TaskTitle
  object BanksAndBuilding extends ReadsWrites[BanksAndBuilding]

  case class TrustFundBond() extends WithName("TrustFundBondTitle") with TaskTitle
  object TrustFundBond extends ReadsWrites[TrustFundBond]

  case class GiltEdged() extends WithName("GiltEdgedTitle") with TaskTitle
  object GiltEdged extends ReadsWrites[GiltEdged]

}
