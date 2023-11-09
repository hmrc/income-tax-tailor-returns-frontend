#!/bin/bash

echo ""
echo "Applying migration $className;format="snake"$"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /$className;format="decap"$/:taxYear                        controllers.$className$Controller.onPageLoad(mode: Mode = NormalMode, taxYear: Int)" >> ../conf/app.routes
echo "POST       /$className;format="decap"$/:taxYear                        controllers.$className$Controller.onSubmit(mode: Mode = NormalMode, taxYear: Int)" >> ../conf/app.routes

echo "GET        /change$className$/:taxYear                  controllers.$className$Controller.onPageLoad(mode: Mode = CheckMode, taxYear: Int)" >> ../conf/app.routes
echo "POST       /change$className$/:taxYear                  controllers.$className$Controller.onSubmit(mode: Mode = CheckMode, taxYear: Int)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "$className;format="decap"$.title = $title$" >> ../conf/messages.en
echo "$className;format="decap"$.agent.title = $title$" >> ../conf/messages.en
echo "$className;format="decap"$.heading = $title$" >> ../conf/messages.en
echo "$className;format="decap"$.agent.heading = $title$" >> ../conf/messages.en
echo "$className;format="decap"$.$option1key;format="decap"$ = $option1msg$" >> ../conf/messages.en
echo "$className;format="decap"$.$option2key;format="decap"$ = $option2msg$" >> ../conf/messages.en
echo "$className;format="decap"$.agent.$option1key;format="decap"$ = $option1msg$" >> ../conf/messages.en
echo "$className;format="decap"$.agent.$option2key;format="decap"$ = $option2msg$" >> ../conf/messages.en
echo "$className;format="decap"$.exclusive = Exclusive" >> ../conf/messages.en
echo "$className;format="decap"$.agent.exclusive = Exclusive" >> ../conf/messages.en
echo "$className;format="decap"$.checkYourAnswersLabel = $title$" >> ../conf/messages.en
echo "$className;format="decap"$.error.required = Select $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.agent.error.required = Select $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.change.hidden = $className$" >> ../conf/messages.en

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrary$className$: Arbitrary[$className$] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf($className$.values)";\
    print "    }";\
    next }1' ../test-utils/generators/ModelGenerators.scala > tmp && mv tmp ../test-utils/generators/ModelGenerators.scala

echo "Migration $className;format="snake"$ completed"
