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

package utils

import play.api.Logger

trait Logging {
  protected val primaryContext : String
  lazy val logger: LoggerWithContext = LoggerWithContext(Logger(this.getClass), primaryContext)

  def dataLogString(nino: String, taxYear: Int) = s" for request with NINO: $nino, and tax year: $taxYear"

  def infoLog(secondaryContext: String,
              dataLog: String = "",
              extraContext: Option[String] = None): String => Unit = (message: String) =>
    logger.info(secondaryContext, message, dataLog, extraContext)

  def warnLog(secondaryContext: String,
              dataLog: String = "",
              extraContext: Option[String] = None): String => Unit = (message: String) =>
    logger.warn(secondaryContext, message, dataLog, extraContext)

  def errorLog(secondaryContext: String,
               dataLog: String = "",
               extraContext: Option[String] = None): String => Unit = (message: String) =>
    logger.error(secondaryContext, message, dataLog, extraContext)
}

case class LoggerWithContext(underlying: Logger, primaryContext: String) {
  private def contextFoldOpt(additionalContext: Option[String]): String =
    additionalContext.fold("")(ctx => s"[$ctx]")

  def info(secondaryContext: String, message: String, dataLog: String = "", extraContext: Option[String] = None): Unit =
    underlying.info(s"[$primaryContext][$secondaryContext]${contextFoldOpt(extraContext)} - $message" + dataLog)

  def warn(secondaryContext: String, message: String, dataLog: String = "", extraContext: Option[String] = None): Unit =
    underlying.warn(s"[$primaryContext][$secondaryContext]${contextFoldOpt(extraContext)} - $message" + dataLog)

  def error(secondaryContext: String, message: String, dataLog: String = "", extraContext: Option[String] = None): Unit =
    underlying.error(s"[$primaryContext][$secondaryContext]${contextFoldOpt(extraContext)} - $message" + dataLog)

  def errorWithException(secondaryContext: String, message: String, ex: Throwable, dataLog: String = "", extraContext: Option[String] = None): Unit =
    underlying.error(s"[$primaryContext][$secondaryContext]${contextFoldOpt(extraContext)} - $message" + dataLog, ex)
}
