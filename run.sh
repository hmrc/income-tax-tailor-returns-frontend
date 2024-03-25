#!/usr/bin/env bash

sbt run -Dconfig.resource=application.conf -Dapplication.router=testOnlyDoNotUseInAppConf.Routes -Dplay.pekko.http.server.request-timeout=60s -J-Xmx256m -J-Xms64m -Dhttp.port=10007 -Drun.mode=Dev
