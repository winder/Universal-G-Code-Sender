set JAVA_HOME=c:\PROGRA~2\Java\JDK18~1.0_9
set PATH=%PATH%;e:\_delete\apache-maven-3.6.3\bin
call mvn clean install -DskipTests=true 
call mvn package -pl ugs-core -DskipTests=true 
call mvn package assembly:assembly -DskipTests=true 

