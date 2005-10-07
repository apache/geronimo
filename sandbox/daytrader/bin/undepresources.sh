#!/bin/bash

export GERONIMO_HOME=/home/hogstrom/geronimo/geronimo/modules/assembly/target/geronimo-1.0-SNAPSHOT

java -jar ${GERONIMO_HOME}/bin/deployer.jar --user system --password manager stop Trade
#java -jar ${GERONIMO_HOME}/bin/deployer.jar --user system --password manager stop TradeDataSource
#java -jar ${GERONIMO_HOME}/bin/deployer.jar --user system --password manager stop TradeJMS
java -jar ${GERONIMO_HOME}/bin/deployer.jar --user system --password manager undeploy Trade
#java -jar ${GERONIMO_HOME}/bin/deployer.jar --user system --password manager undeploy TradeDataSource
#java -jar ${GERONIMO_HOME}/bin/deployer.jar --user system --password manager undeploy TradeJMS

