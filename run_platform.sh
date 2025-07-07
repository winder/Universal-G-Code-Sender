#!/usr/bin/env bash

export unameOut="$(uname -s)"

case "${unameOut}" in
    Linux*)     export platform="Linux";;
    Darwin*)    export platform="Mac";;
    CYGWIN*)    export platform="Cygwin";;
    MINGW*)     export platform="MinGw";;
    MSYS_NT*)   export platform="MSys";;
    *)          export platform="UNKNOWN:${unameOut}"
esac

if [[ "${platform}" == "Mac" ]]; then 
  if [[ -z "${JAVA_HOME}" ]]; then
    export JAVA_HOME=`/usr/libexec/java_home -v 17` ;
  fi;
fi;

/Applications/Apache\ NetBeans.app/Contents/Resources/netbeans/java/maven/bin/mvn install -DskipTests=true && \
/Applications/Apache\ NetBeans.app/Contents/Resources/netbeans/java/maven/bin/mvn nbm:run-platform -pl ugs-platform/application
