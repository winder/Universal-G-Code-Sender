#!/usr/bin/env bash

sudo apt-get update -qq
sudo apt-get install -qq genisoimage
sudo apt-get install -qq curl
mvn package -pl ugs-core assembly:assembly -DskipTests=true
mvn package -pl ugs-platform/application -P create-autoupdate,create-dmg -DskipTests=true
curl -fL https://getcli.jfrog.io | sh
"./jfrog rt config --interactive=false --url $ARTIFACTORY_URL --user $ARTIFACTORY_LOGIN --apikey $ARTIFACTORY_API_KEY"
./jfrog rt u "ugs-core/target/UniversalGcodeSender.jar" "/UGS/dist/" --build-name=$ARTIFACTORY_NAME --build-number=$TRAVIS_BUILD_NUMBER --flat=true
./jfrog rt u "ugs-platform/application/target/ugs-platform-app-*.zip" "/UGS/dist/" --build-name=$ARTIFACTORY_NAME --build-number=$TRAVIS_BUILD_NUMBER --flat=true
./jfrog rt u "ugs-platform/application/target/ugs-platform-app-*.dmg" "/UGS/dist/" --build-name=$ARTIFACTORY_NAME --build-number=$TRAVIS_BUILD_NUMBER --flat=true
./jfrog rt u "ugs-platform/application/target/site/netbeans_site/" "/UGS/update-center/" --build-name=$ARTIFACTORY_NAME --build-number=$TRAVIS_BUILD_NUMBER
"./jfrog rt bce $ARTIFACTORY_NAME $TRAVIS_BUILD_NUMBER"
"./jfrog rt bp $ARTIFACTORY_NAME $TRAVIS_BUILD_NUMBER"
