package connectors.httpParser

class HttpParserISpec extends HttpParserBehaviours {

  "FakeParser" - {
    behave like logHttpResponse()
    behave like handleSingleError()
    behave like handleMultpleError()
    behave like returnParsingErrors()
  }
}
