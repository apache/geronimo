#!/bin/bash

export GERONIMO_HOME=~/geronimo/geronimo/modules/assembly/target/geronimo-1.2-SNAPSHOT

java -jar ${GERONIMO_HOME}/bin/deployer.jar --user system --password manager deploy ${GERONIMO_HOME}/repository/tranql/rars/tranql-connector-1.2-SNAPSHOT.rar mydb2-plan.xml

java -jar ${GERONIMO_HOME}/bin/deployer.jar --user system --password manager deploy ${GERONIMO_HOME}/repository/geronimo/rars/geronimo-activemq-embedded-rar-2.2-SNAPSHOT.rar jms-resource-plan.xml

java -jar ${GERONIMO_HOME}/bin/deployer.jar --user system --password manager deploy dayTrader.ear dayTrader-plan-fixed.xml
