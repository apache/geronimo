#!/bin/bash

export GERONIMO_HOME=~/geronimo/geronimo/modules/assembly/target/geronimo-1.0-SNAPSHOT

java -jar ${GERONIMO_HOME}/bin/deployer.jar --user system --password manager start  org/apache/geronimo/DebugConsole

