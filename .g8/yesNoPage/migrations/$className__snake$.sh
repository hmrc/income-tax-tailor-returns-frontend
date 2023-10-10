#!/bin/bash

echo ""
echo "Applying migration $className;format="snake"$"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /$className;format="decap"$/:taxYear                        controllers.$className$Controller.onPageLoad(mode: Mode = NormalMode, taxYear: Int)" >> ../conf/app.routes
echo "POST       /$className;format="decap"$/:taxYear                        controllers.$className$Controller.onSubmit(mode: Mode = NormalMode, taxYear: Int)" >> ../conf/app.routes

echo "GET        /change$className$/:taxYear                   controllers.$className$Controller.onPageLoad(mode: Mode = CheckMode, taxYear: Int)" >> ../conf/app.routes
echo "POST       /change$className$/:taxYear                   controllers.$className$Controller.onSubmit(mode: Mode = CheckMode, taxYear: Int)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "$className;format="decap"$.title = $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.agent.title = $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.heading = $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.agent.heading = $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.checkYourAnswersLabel = $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.error.required = Select yes if $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.agent.error.required = Select yes if $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.change.hidden = $className$" >> ../conf/messages.en

echo "Migration $className;format="snake"$ completed"
