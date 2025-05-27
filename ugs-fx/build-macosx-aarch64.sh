#!/bin/bash

# ------ ENVIRONMENT --------------------------------------------------------
# The script depends on various environment variables to exist in order to
# run properly. The java version we want to use, the location of the java
# binaries (java home), and the project version as defined inside the pom.xml
# file, e.g. 1.0-SNAPSHOT.
#
# PROJECT_VERSION: version used in pom.xml, e.g. 1.0-SNAPSHOT
# APP_VERSION: the application version, e.g. 1.0.0, shown in "about" dialog

if [ -z ${PROJECT_VERSION} ]; then echo "Missing PROJECT_VERSION"; exit 1; fi
if [ -z ${APP_VERSION} ]; then echo "Missing APP_VERSION"; exit 1; fi

# Download JVM
JVM=zulu21.42.19-ca-fx-jdk21.0.7-macosx_aarch64
set -e
ZIP=$JVM.tar.gz
export JAVA_HOME=.jdks/$JVM
if test -d $JAVA_HOME/$JVM/; then
  echo "Using existing JDK from $JAVA_HOME"
else
	rm -rf $JAVA_HOME
	mkdir -p $JAVA_HOME
	curl -o $ZIP https://cdn.azul.com/zulu/bin/$ZIP
	tar -xvzf $ZIP -C $JAVA_HOME
	mv $JAVA_HOME/$JVM/* $JAVA_HOME/
fi

JAVA_VERSION=17
MAIN_JAR="ugs-fx-$PROJECT_VERSION.jar"

echo "Java home: $JAVA_HOME"
echo "Project version: $PROJECT_VERSION"
echo "App version: $APP_VERSION"
echo "Main JAR file: $MAIN_JAR"

# ------ SETUP DIRECTORIES AND FILES ----------------------------------------
# Remove previously generated java runtime and installers. Copy all required
# jar files into the input/libs folder.

rm -rfd ./target/java-runtime/
rm -rfd target/installer/

mkdir -p target/installer/input/libs/
cp target/libs/* target/installer/input/libs/
cp target/${MAIN_JAR} target/installer/input/libs/
cp installer/ugs.svg target/installer/input/libs/

# ------ REQUIRED MODULES ---------------------------------------------------
# Use jlink to detect all modules that are required to run the application.
# Starting point for the jdep analysis is the set of jars being used by the
# application.

echo "Detecting required modules"
detected_modules=`$JAVA_HOME/bin/jdeps \
  -q \
  --multi-release ${JAVA_VERSION} \
  --ignore-missing-deps \
  --print-module-deps \
  --class-path "target/installer/input/libs/*" \
    target/classes/com/willwinder/universalgcodesender/fx/Main.class`

# ------ MANUAL MODULES -----------------------------------------------------
# jdk.crypto.ec has to be added manually bound via --bind-services or
# otherwise HTTPS does not work.
#
# See: https://bugs.openjdk.java.net/browse/JDK-8221674
#
# In addition we need jdk.localedata if the application is localized.
# This can be reduced to the actually needed locales via a jlink parameter,
# e.g., --include-locales=en,de.
#
# Don't forget the leading ','!

extra_modules=,jdk.crypto.ec,jdk.localedata,jdk.dynalink,jdk.zipfs
echo "Using modules: ${detected_modules}${extra_modules}"

# ------ RUNTIME IMAGE ------------------------------------------------------
# Use the jlink tool to create a runtime image for our application. We are
# doing this in a separate step instead of letting jlink do the work as part
# of the jpackage tool. This approach allows for finer configuration and also
# works with dependencies that are not fully modularized, yet.

echo "Creating Java runtime image"
$JAVA_HOME/bin/jlink \
  --strip-native-commands \
  --no-header-files \
  --no-man-pages  \
  --strip-debug \
  --add-modules "${detected_modules}${extra_modules}" \
  --include-locales=en,de \
  --output target/java-runtime

# ------ PACKAGING ----------------------------------------------------------
# In the end we will find the package inside the target/installer directory.

$JAVA_HOME/bin/jpackage \
  --type dmg \
  --dest target/installer \
  --input target/installer/input/libs \
  --name "Universal Gcode Sender" \
  --main-class com.willwinder.universalgcodesender.fx.Main \
  --main-jar ${MAIN_JAR} \
  --resource-dir installer \
  --java-options "-XX:MaxRAMPercentage=85.0 -Dprism.forceGPU=true -Djavafx.autoproxy.disable=true -Djavafx.preloader=com.willwinder.universalgcodesender.fx.Preloader"  \
  --runtime-image target/java-runtime \
  --app-version ${APP_VERSION} \
  --copyright "Joacim Breiler" \
  --license-file ../COPYING \
  --about-url https://universalgcodesender.com/ \
  --file-associations installer/gcode.properties

mv "target/installer/Universal Gcode Sender-${APP_VERSION}.dmg" "target/installer/ugs-${APP_VERSION}-aarch64.dmg"
