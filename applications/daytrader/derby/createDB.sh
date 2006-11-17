#!/bin/bash
#=====================================================================
#  Licensed to the Apache Software Foundation (ASF) under one or more
#  contributor license agreements.  See the NOTICE file distributed with
#  this work for additional information regarding copyright ownership.
#  The ASF licenses this file to You under the Apache License, Version 2.0
#  (the "License"); you may not use this file except in compliance with
#  the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#=====================================================================

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
