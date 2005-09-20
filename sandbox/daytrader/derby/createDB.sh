#!/bin/bash
export G_PATH=~/geronimo/geronimo/modules/assembly/target/geronimo-1.0-SNAPSHOT/repository/org.apache.derby/jars
export CLASSPATH=${G_PATH}/derby-10.1.1.0.jar
export CLASSPATH=${CLASSPATH}:${G_PATH}/derbynet-10.1.1.0.jar
export CLASSPATH=${CLASSPATH}:${G_PATH}/derbytools-10.1.1.0.jar
export CLASSPATH=${CLASSPATH}:${G_PATH}/derbyclient-10.1.1.0.jar
export CLASSPATH=${CLASSPATH}:/home/db2inst1/sqllib/java/db2jcc.jar
export

java -Dij.protocol=jdbc:derby:net://localhost:1527/ org.apache.derby.tools.ij < derby.txt
#java -Dij.driver=com.ibm.db2.jcc.DB2Driver -Dij.protocol=jdbc:derby:net://localhost:1527/ org.apache.derby.tools.ij 
