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

package test.connectors

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsObject, JsValue, Json, Writes}


trait WireMockHelper extends BeforeAndAfterAll with BeforeAndAfterEach {
  this: Suite =>
  val wireMockPort: Int = 11111
  val wireMockHost: String = "localhost"
  val server = new WireMockServer(WireMockConfiguration.wireMockConfig().port(wireMockPort))

//  protected val server: WireMockServer = new WireMockServer(wireMockConfig().dynamicPort())

  override def beforeAll(): Unit = {
    server.start()
    super.beforeAll()
  }

  override def beforeEach(): Unit = {
    server.resetAll()
    super.beforeEach()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    server.stop()
  }

  def verifyPost(uri: String, optBody: Option[String] = None): Unit = {
    val uriMapping = postRequestedFor(urlEqualTo(uri))
    val postRequest = optBody match {
      case Some(body) => uriMapping.withRequestBody(equalTo(body))
      case None => uriMapping
    }
    verify(postRequest)
  }

  def verifyGet(uri: String): Unit =
    verify(getRequestedFor(urlEqualTo(uri)))

  def stubGet(url: String, returnedStatus: Int, returnedBody: String): StubMapping =
    server.stubFor(
      get(urlMatching(url))
        .willReturn(
          aResponse().withStatus(returnedStatus).withBody(returnedBody)
        ))

  /*def stubGetWithHeadersCheck(url: String, status: Int, body: String, sessionHeader: (String, String), mtdidHeader: (String, String)): StubMapping =
    stubFor(
      get(urlMatching(url))
        .withHeader(sessionHeader._1, equalTo(sessionHeader._2))
        .withHeader(mtdidHeader._1, equalTo(mtdidHeader._2))
        .willReturn(
          aResponse().withStatus(status).withBody(body)
        ))

  def stubPutWithHeadersCheck(url: String, status: Int, body: String, sessionHeader: (String, String), mtdidHeader: (String, String)): StubMapping =
    stubFor(
      put(urlMatching(url))
        .withHeader(sessionHeader._1, equalTo(sessionHeader._2))
        .withHeader(mtdidHeader._1, equalTo(mtdidHeader._2))
        .willReturn(
          aResponse().withStatus(status).withBody(body)
        ))

  def stubPutWithBodyAndHeaders[T: Writes](url: String,
                                           requestBody: T,
                                           expectedStatus: Int,
                                           responseBody: JsValue,
                                           sessionHeader: (String, String),
                                           mtdidHeader: (String, String)): StubMapping = {

    val stringReqBody = implicitly[Writes[T]]
      .writes(requestBody)
      .toString()

    stubFor(
      put(urlMatching(url))
        .withHeader(sessionHeader._1, equalTo(sessionHeader._2))
        .withHeader(mtdidHeader._1, equalTo(mtdidHeader._2))
        .withRequestBody(equalTo(stringReqBody))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
            .withBody(responseBody.toString())
            .withHeader("Content-Type", "application/json; charset=utf-8")))
  }

  def stubPost(url: String, status: Int, responseBody: String, requestHeaders: Seq[HttpHeader] = Seq.empty): StubMapping = {
    val mappingWithHeaders: MappingBuilder = requestHeaders.foldLeft(post(urlMatching(url))) { (result, nxt) =>
      result.withHeader(nxt.key(), equalTo(nxt.firstValue()))
    }

    stubFor(
      mappingWithHeaders
        .willReturn(
          aResponse().withStatus(status).withBody(responseBody)
        ))
  }

  def stubPut(url: String, status: Int, responseBody: String, requestHeaders: Seq[HttpHeader] = Seq.empty): StubMapping = {
    val mappingWithHeaders: MappingBuilder = requestHeaders.foldLeft(put(urlMatching(url))) { (result, nxt) =>
      result.withHeader(nxt.key(), equalTo(nxt.firstValue()))
    }

    stubFor(
      mappingWithHeaders
        .willReturn(
          aResponse().withStatus(status).withBody(responseBody)
        ))
  }*/
}
