#!/bin/bash 

sudo rm -rf target/ /tmp/jni/ src/main/clib/lib/ src/main/clib/build/ 
mvnd clean compile package install -DskipTests
PACKAGE_VERSION=$(cat pom.xml | grep version | head -n1  | sed 's/<version>//' | sed 's/<\/version>//' | xargs)
# sudo java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005 -cp target/jsysbox-${PACKAGE_VERSION}.jar ir.moke.jsysbox.MainClass
# sudo java -cp target/jsysbox-"${PACKAGE_VERSION}".jar ir.moke.jsysbox.MainClass
#
(cd target/lib ; sudo java -cp .:* -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005 ir.moke.jsysbox.MainClass)
# (cd target/lib ; sudo java -cp .:* ir.moke.jsysbox.MainClass)
