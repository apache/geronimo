#!/bin/bash
if [ "${GERONIMO_HOME}" = "" ]
then
  echo Please define the GERONIMO_HOME environment variable.
  exit
fi
export G_PATH=${GERONIMO_HOME}/repository/org.apache.derby/jars
export CLASSPATH=${G_PATH}/derby-10.1.1.0.jar
export CLASSPATH=${CLASSPATH}:${G_PATH}/derbynet-10.1.1.0.jar
export CLASSPATH=${CLASSPATH}:${G_PATH}/derbytools-10.1.1.0.jar
export CLASSPATH=${CLASSPATH}:${G_PATH}/derbyclient-10.1.1.0.jar
export
echo "Invoking IJ command line tool to create the database and tables...please wait"
java -Dij.driver=org.apache.derby.jdbc.ClientDriver -Dij.protocol=jdbc:derby://localhost:1527/ org.apache.derby.tools.ij < derby.txt
echo "Table creation complete"
