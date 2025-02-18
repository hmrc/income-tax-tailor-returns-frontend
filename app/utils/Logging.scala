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
  val classLoggingContext : String
  lazy val logger: LoggerWithContext = LoggerWithContext(Logger(this.getClass), classLoggingContext)

  def dataLogString(nino: String, taxYear: Int) = s" for request with NINO: $nino, and tax year: $taxYear"

  def infoLog(methodLoggingContext: String,
              dataLog: String = "",
              extraContext: Option[String] = None): String => Unit = (message: String) =>
    logger.info(methodLoggingContext, message, dataLog, extraContext)

  def warnLog(methodLoggingContext: String, dataLog: String = ""): String => Unit = (message: String) =>
    logger.warn(methodLoggingContext, message, dataLog)

  def errorLog(methodLoggingContext: String, dataLog: String = ""): String => Unit = (message: String) =>
    logger.error(methodLoggingContext, message, dataLog)

  def errorWithExceptionLog(methodLoggingContext: String, ex: Throwable, dataLog: String = ""): String => Unit = (message: String) =>
    logger.errorWithException(methodLoggingContext, message, ex, dataLog)
}

case class LoggerWithContext(underlying: Logger, classContext: String) {
  private def contextFoldOpt(additionalContext: Option[String]): String =
    additionalContext.fold("")(ctx => s"[$ctx]")

  def info(methodContext: String, message: String, dataLog: String = "", extraContext: Option[String] = None): Unit =
    underlying.info(s"[$classContext][$methodContext]${contextFoldOpt(extraContext)} - $message" + dataLog)

  def warn(methodContext: String, message: String, dataLog: String = "", extraContext: Option[String] = None): Unit =
    underlying.warn(s"[$classContext][$methodContext]${contextFoldOpt(extraContext)} - $message" + dataLog)

  def error(methodContext: String, message: String, dataLog: String = "", extraContext: Option[String] = None): Unit =
    underlying.error(s"[$classContext][$methodContext]${contextFoldOpt(extraContext)} - $message" + dataLog)

  def errorWithException(methodContext: String, message: String, ex: Throwable, dataLog: String = "", extraContext: Option[String] = None): Unit =
    underlying.error(s"[$classContext][$methodContext]${contextFoldOpt(extraContext)} - $message" + dataLog, ex)
}
