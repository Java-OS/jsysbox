#!/bin/bash 

# (cd src/main/clib ; ./run_cmake.sh)
sudo rm -rf /tmp/jni/
mvn clean compile package -DskipTests
PACKAGE_VERSION=$(cat pom.xml  | grep version | head -n1  | sed 's/<version>//' | sed 's/<\/version>//' | xargs) 
# sudo java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005 -cp target/jsysbox-${PACKAGE_VERSION}.jar ir.moke.jsysbox.MainClass
sudo java -cp target/jsysbox-${PACKAGE_VERSION}.jar ir.moke.jsysbox.MainClass
