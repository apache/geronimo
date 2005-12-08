#!/bin/bash

export GERONIMO_HOME=~/geronimo/geronimo/modules/assembly/target/geronimo-1.0-SNAPSHOT

mkdir ${GERONIMO_HOME}/repository/db2
mkdir ${GERONIMO_HOME}/repository/db2/jars
cp /home/db2inst1/sqllib/java/* ${GERONIMO_HOME}/repository/db2/jars
