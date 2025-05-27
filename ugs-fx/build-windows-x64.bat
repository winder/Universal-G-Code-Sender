@echo off

:: ------ ENVIRONMENT --------------------------------------------------------
setlocal enabledelayedexpansion
set PROJECT_VERSION=2.0-SNAPSHOT
set APP_VERSION=2.0

:: ----------- ENVIRONMENT CHECK ------------------------------------------
if "%PROJECT_VERSION%"=="" (
    echo Missing PROJECT_VERSION
    exit /b 1
)
if "%APP_VERSION%"=="" (
    echo Missing APP_VERSION
    exit /b 1
)

:: ----------- DOWNLOAD JVM ------------------------------------------------
set JVM=zulu21.42.19-ca-fx-jdk21.0.7-win_x64
set ZIP=%JVM%.zip
set JAVA_HOME=.jdks\%JVM%

if exist "%JAVA_HOME%\" (
    echo Using existing JDK from %JAVA_HOME%
) else (
    rmdir /s /q "%JAVA_HOME%"
    curl -L -o %ZIP% https://cdn.azul.com/zulu/bin/%ZIP%
    powershell -Command "Expand-Archive -Force '%ZIP%' '.jdks'"
)

set JAVA_VERSION=17
set MAIN_JAR=ugs-fx-%PROJECT_VERSION%.jar

echo Java home: %JAVA_HOME%
echo Project version: %PROJECT_VERSION%
echo App version: %APP_VERSION%
echo Main JAR file: %MAIN_JAR%

:: ----------- SETUP DIRECTORIES AND FILES --------------------------------
rmdir /s /q target\java-runtime 2>nul
rmdir /s /q target\installer 2>nul

mkdir target\installer\input\libs
copy target\libs\* target\installer\input\libs\
copy target\%MAIN_JAR% target\installer\input\libs\
copy installer\ugs.svg target\installer\input\libs\

:: ----------- REQUIRED MODULES -------------------------------------------
echo Detecting required modules...
"%JAVA_HOME%\bin\jdeps" -q --multi-release %JAVA_VERSION% --ignore-missing-deps --print-module-deps --class-path "target\installer\input\libs\*" target\classes\com\willwinder\universalgcodesender\fx\Main.class > temp.txt

 set /p detected_modules=<temp.txt
 del temp.txt

:: ----------- EXTRA MODULES ----------------------------------------------
set extra_modules=,jdk.crypto.ec,jdk.localedata,jdk.dynalink,jdk.zipfs
echo Using modules: %detected_modules%%extra_modules%

:: ----------- RUNTIME IMAGE ----------------------------------------------
echo Creating Java runtime image...
"%JAVA_HOME%\bin\jlink" ^
  --strip-native-commands ^
  --no-header-files ^
  --no-man-pages ^
  --strip-debug ^
  --add-modules "%detected_modules%%extra_modules%" ^
  --include-locales=en,de ^
  --output target\java-runtime

:: ----------- PACKAGING --------------------------------------------------
echo Packaging application...

for %%s in ("msi" "exe") do call "%JAVA_HOME%\bin\jpackage" ^
  --type %%s ^
  --dest target\installer ^
  --input target\installer\input\libs ^
  --name "Universal G-code Sender" ^
  --main-class com.willwinder.universalgcodesender.fx.Main ^
  --main-jar %MAIN_JAR% ^
  --resource-dir installer ^
  --java-options "-XX:MaxRAMPercentage=85.0 -Dprism.forceGPU=true -Djavafx.preloader=com.willwinder.universalgcodesender.fx.Preloader" ^
  --runtime-image target\java-runtime ^
  --icon installer\ugs.ico ^
  --app-version %APP_VERSION% ^
  --win-shortcut ^
  --win-menu ^
  --vendor "Universal G-code Sender" ^
  --copyright "Joacim Breiler" ^
  --license-file ..\COPYING ^
  --about-url https://universalgcodesender.com/

move "target\installer\Universal G-code Sender*.exe" "target\installer\ugs-%APP_VERSION%-x64.exe"
move "target\installer\Universal G-code Sender*.msi" "target\installer\ugs-%APP_VERSION%-x64.msi"
echo Done.
