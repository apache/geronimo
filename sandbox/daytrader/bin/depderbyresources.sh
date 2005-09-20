#!/bin/bash

export GERONIMO_HOME=/home/hogstrom/geronimo/geronimo/modules/assembly/target/geronimo-1.0-SNAPSHOT

#java -jar ${GERONIMO_HOME}/bin/deployer.jar --user system --password manager deploy ${GERONIMO_HOME}/repository/tranql/rars/tranql-connector-derby-embed-xa-1.0-SNAPSHOT.rar derby-plan.xml
#java -jar ${GERONIMO_HOME}/bin/deployer.jar --user system --password manager deploy ${GERONIMO_HOME}/repository/tranql/rars/tranql-connector-1.0-20050716.rar mydb2-plan.xml
#java -jar ${GERONIMO_HOME}/bin/deployer.jar --user system --password manager deploy ${GERONIMO_HOME}/repository/activemq/rars/activemq-ra-3.2-SNAPSHOT.rar jms-resource-plan.xml
#java -jar ${GERONIMO_HOME}/bin/deployer.jar --user system --password manager deploy dayTrader.ear dayTrader-plan-fixed.xml
java -jar ${GERONIMO_HOME}/bin/deployer.jar --user system --password manager deploy /home/hogstrom/.maven/repository/daytrader/ears/daytrader-ear-1.0-SNAPSHOT.ear dayTrader-plan.xml
