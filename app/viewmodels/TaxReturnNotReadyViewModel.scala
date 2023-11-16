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

package viewmodels

import models.TagStatus.Completed
import models.{SectionState, TagStatus}
import play.api.i18n.Messages
import play.twirl.api.Html

case class TaxReturnNotReadyViewModel(state: SectionState, prefix: String) {

  def getBulletItems()(implicit messages: Messages): Seq[Html] = {

    val listItems: Seq[(String, TagStatus)] = Seq[(String, TagStatus)](
      (messages(s"$prefix.aboutYou"), state.aboutYou),
      (messages(s"$prefix.incomeFromWork"), state.incomeFromWork),
      (messages(s"$prefix.incomeFromProperty"), state.incomeFromProperty),
      (messages(s"$prefix.pensions"), state.pensions)
    )

    listItems.filterNot(_._2 == Completed).map(value =>
      Html(value._1)
    )
  }

}
