#!/usr/bin/env bash

"./jfrog rt config --interactive=false --url $ARTIFACTORY_URL --user $ARTIFACTORY_LOGIN --apikey $ARTIFACTORY_API_KEY"
./jfrog rt u "ugs-core/target/UniversalGcodeSender.jar" "/UGS/dist/" --build-name=$ARTIFACTORY_NAME --build-number=$TRAVIS_BUILD_NUMBER --flat=true
./jfrog rt u "ugs-platform/application/target/ugs-platform-app-*.zip" "/UGS/dist/" --build-name=$ARTIFACTORY_NAME --build-number=$TRAVIS_BUILD_NUMBER --flat=true
./jfrog rt u "ugs-platform/application/target/ugs-platform-app-*.dmg" "/UGS/dist/" --build-name=$ARTIFACTORY_NAME --build-number=$TRAVIS_BUILD_NUMBER --flat=true
./jfrog rt u "ugs-platform/application/target/site/netbeans_site/" "/UGS/update-center/" --build-name=$ARTIFACTORY_NAME --build-number=$TRAVIS_BUILD_NUMBER
"./jfrog rt bce $ARTIFACTORY_NAME $TRAVIS_BUILD_NUMBER"
"./jfrog rt bp $ARTIFACTORY_NAME $TRAVIS_BUILD_NUMBER"
