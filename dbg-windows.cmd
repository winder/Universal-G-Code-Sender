set UGS_HOME=ugs-classic/target
%JAVA_HOME%\bin\java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5555 -Xmx512m -jar %UGS_HOME%/UniversalGcodeSender.jar