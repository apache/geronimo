#!/bin/bash

#export GERONIMO_HOME=~/dev/geronimo/assemblies/j2ee-tomcat-server/geronimo-1.0

java -jar ${GERONIMO_HOME}/bin/deployer.jar --user system --password manager stop Trade
java -jar ${GERONIMO_HOME}/bin/deployer.jar --user system --password manager undeploy Trade
