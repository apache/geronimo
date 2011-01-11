#!/bin/sh
#
#  Licensed to the Apache Software Foundation (ASF) under one or more
#  contributor license agreements.  See the NOTICE file distributed with
#  this work for additional information regarding copyright ownership.
#  The ASF licenses this file to You under the Apache License, Version 2.0
#  (the "License"); you may not use this file except in compliance with
#  the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.

# --------------------------------------------------------------------
# $Rev$ $Date$
# --------------------------------------------------------------------

# --------------------------------------------------------------------
# -- This batch file is used to run sql to enable/disable monitoring
# -- on Default Server using ij in a NetworkServer environment.
# --
#-- REQUIREMENTS:
# -- You must have the Derby, Derby Client, and Derby Tools libraries in your classpath
# --
# --You may need to modify the values below for a different
# --GERONIMO_HOME
# --
# -- This file for use on Unix sh systems
# --------------------------------------------------------------------

ARGS=

# resolve links - $0 may be a softlink
PRG="$0"
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '.*/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`/"$link"
    fi
done

# Get standard environment variables
PRGDIR=`dirname "$PRG"`
GERONIMO_HOME=$PRGDIR/..

# --------------------------------------------------------------------
# -- To use a different JVM with a different syntax, simply edit
# -- this file
# --------------------------------------------------------------------
if [ -r "$GERONIMO_HOME"/bin/setenv.sh ]; then
  . "$GERONIMO_HOME"/bin/setenv.sh
fi

if [ -z "$JAVA_HOME" ]; then
    JAVA=`which java`
    if [ -z "$JAVA" ]; then
        echo "Unable to locate Java binary. Please add it to the PATH."
        exit 1
    fi
    JAVA_BIN=`dirname $JAVA`
    JAVA_HOME=$JAVA_BIN/..
fi

JAVA=$JAVA_HOME/bin/java
if [ ! -f "$JAVA" ]; then
    echo "Unable to locate Java"
    exit 1
fi

ENDORSED_DIR=$GERONIMO_HOME/lib/endorsed

Derby_CLASS_PATH=${GERONIMO_HOME}/repository/org/apache/derby
CLASS_PATH=${Derby_CLASS_PATH}/derbytools/${derbyVersion}/derbytools-${derbyVersion}.jar:${Derby_CLASS_PATH}/derbyclient/${derbyVersion}/derbyclient-${derbyVersion}.jar:$CLASS_PATH

# --------------------------------------------------------------------
# -- start ij
# -- Arguments 5 to the script are [host], [port], [username], [password] and
# -- ["enable-monitoring"|"disable-monitoring"] in that order.
# --------------------------------------------------------------------
$JAVA $JAVA_OPTS -cp $CLASS_PATH -Dij.driver=org.apache.derby.jdbc.ClientDriver -Dij.protocol=jdbc:derby://$1:$2/ -Dij.user=$3 -Dij.password=$4 $ARGS org.apache.derby.tools.ij -f $GERONIMO_HOME/bin/$5.sql

