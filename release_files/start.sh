
#!/bin/bash

platform='unknown'
unamestr=`uname`
case "$unamestr" in
        Linux)
                platform='linux'
                rootdir="$(dirname $(readlink -f $0))"
        ;;
        Darwin)
                platform='mac'
                rootdir="$(cd $(dirname $0); pwd -P)"
        ;;
esac

case "$platform" in
        mac)
                java -Xdock:name=UniversalGCodeSender -jar -Xmx256m $rootdir/UniversalGcodeSender*.jar
        ;;
        linux)
                java -jar -Xmx256m $rootdir/UniversalGcodeSender*.jar
        ;;
esac
        
