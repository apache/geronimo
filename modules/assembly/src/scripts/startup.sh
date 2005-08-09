#!/bin/sh
#
#  Copyright 2005 The Apache Software Foundation
# 
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
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

ARGS=

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

PRGDIR=`dirname "$PRG"`
SERVER_JAR=$PRGDIR/server.jar

if [ ! -f "$SERVER_JAR" ]; then 
    echo "Unable to locate the $SERVER_JAR jar"
    exit 1
fi

$JAVA $ARGS -jar $SERVER_JAR "$@" 
