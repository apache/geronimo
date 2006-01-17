#!/bin/bash

export GERONIMO_HOME=/home/hogstrom/geronimo/geronimo/modules/assembly/target/geronimo-1.1-SNAPSHOT

java -jar ${GERONIMO_HOME}/bin/deployer.jar --user system --password manager deploy dayTrader.ear dayTrader-plan-db2.xml
