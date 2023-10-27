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

package config

import controllers.actions._
import navigation.{JourneyNavigator, Navigator, PrivateBetaNavigator}
import play.api.inject.Binding
import play.api.{Configuration, Environment}

import java.time.Clock

class Module extends play.api.inject.Module {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {

    val authBinding: Binding[_] =
      if (configuration.get[Boolean]("features.earlyPrivateLaunch")) {
          bind[IdentifierActionProvider].to[EarlyPrivateLaunchIdentifierActionProviderImpl].eagerly()
      } else {
          bind[IdentifierActionProvider].to[IdentifierActionProviderImpl].eagerly()
      }

    val navigationBinding: Binding[_] =
      if (configuration.get[Boolean]("features.privateBeta")) {
        bind[Navigator].to[PrivateBetaNavigator]
      } else {
        bind[Navigator].to[JourneyNavigator]
      }

    Seq(
      bind[DataRetrievalActionProvider].to[DataRetrievalActionProviderImpl].eagerly(),
      bind[DataRequiredActionProvider].to[DataRequiredActionProviderImpl].eagerly(),
      bind[Clock].toInstance(Clock.systemUTC()),
      authBinding,
      navigationBinding
    )


  }
}
