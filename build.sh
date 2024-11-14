#!/bin/bash 

cd $(dirname $0) # this line used inside container

sudo rm -rf target/ /tmp/jni/ src/main/clib/lib/ src/main/clib/build/ 

mvn dependency:go-offline

if [[ ! -z ${EXEC_UNIT_TEST} ]] && [[ ${EXEC_UNIT_TEST} -eq "1" ]] ; then 
  mvn clean compile package 
else 
  mvn clean compile package -DskipTests
fi 
sudo chown -R $USER:$USER $HOME/.m2 


# (cd target/lib ; sudo java -cp .:* -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005 ir.moke.jsysbox.MainClass)
# (cd target/lib ; sudo java -cp .:* ir.moke.jsysbox.MainClass)
