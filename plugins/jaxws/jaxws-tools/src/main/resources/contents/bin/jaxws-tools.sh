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
# $Rev: 534454 $ $Date: 2007-05-02 09:39:49 -0400 (Wed, 02 May 2007) $
# --------------------------------------------------------------------

# -----------------------------------------------------------------------------
# You should not have to edit this file.  If you wish to have environment
# variables set each time you run this script refer to the information
# on the setenv.sh script that is called by this script below. 
#
# Invocation Syntax:
#
#   jaxws-tools.sh [general options] command [command options] 
#
#   For detailed command usage information, just run deploy.sh without any 
#   arguments.
#
# Environment Variable Prequisites:
#
#   GERONIMO_HOME   (Optional) May point at your Geronimo top-level directory.
#                   If not specified, it will default to the parent directory
#                   of the location of this script.
#
#   GERONIMO_OPTS   (Optional) Java runtime options.
#
#   GERONIMO_TMPDIR (Optional) Directory path location of temporary directory
#                   the JVM should use (java.io.tmpdir). Defaults to var/temp
#                   (resolved to server instance directory).
#
#   JAVA_HOME       Points to your Java Development Kit installation.
#                   JAVA_HOME doesn't need to be set if JRE_HOME is set.
#                   It is mandatory either JAVA_HOME or JRE_HOME are set.
#
#   JRE_HOME        Points to your Java Runtime Environment installation.
#                   Set this if you wish to run Geronimo using the JRE 
#                   instead of the JDK. Defaults to JAVA_HOME if empty.
#                   It is mandatory either JAVA_HOME or JRE_HOME are set.
#
#   JAVA_OPTS       (Optional) Java runtime options.
#
# Troubleshooting execution of this script file:
#
#  GERONIMO_ENV_INFO    (Optional) Environment variable that when set to
#                       "on" (the default) outputs the 
#                       values of GERONIMO_HOME, 
#                       GERONIMO_TMPDIR, JAVA_HOME, JRE_HOME before
#                       the command is issued. Set to "off" if you
#                       do want to see this information.
#
# Scripts called by this script:
# 
#   $GERONIMO_HOME/bin/setenv.sh
#                   (Optional) This script file is called if it is present.
#                   Its contents may set one or more of the above environment
#                   variables.  It is preferable (to simplify migration to
#                   future Geronimo releases) to set environment variables
#                   in this file rather than modifying Geronimo's script files.
#
#   $GERONIMO_HOME/bin/setjavaenv.sh
#                   This batch file is called to set environment variables
#                   relating to the java or jdb executable to invoke.
#                   This file should not need to be modified.
#
# Exit Codes:
#
#  0 - Success
#  1 - Error
# -----------------------------------------------------------------------------

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false
os400=false
case "`uname`" in
CYGWIN*) cygwin=true;;
OS400*) os400=true;;
esac

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

# Get standard environment variables
PRGDIR=`dirname "$PRG"`

# Only set GERONIMO_HOME if not already set
[ -z "$GERONIMO_HOME" ] && GERONIMO_HOME=`cd "$PRGDIR/.." ; pwd`

if [ -r "$GERONIMO_HOME"/bin/setenv.sh ]; then
  . "$GERONIMO_HOME"/bin/setenv.sh
fi

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
  [ -n "$JRE_HOME" ] && JRE_HOME=`cygpath --unix "$JRE_HOME"`
  [ -n "$GERONIMO_HOME" ] && GERONIMO_HOME=`cygpath --unix "$GERONIMO_HOME"`
fi

# For OS400
if $os400; then
  # Set job priority to standard for interactive (interactive - 6) by using
  # the interactive priority - 6, the helper threads that respond to requests
  # will be running at the same priority as interactive jobs.
  COMMAND='chgjob job('$JOBNAME') runpty(6)'
  system $COMMAND

  # Enable multi threading
  export QIBM_MULTI_THREADED=Y
fi

# Get standard Java environment variables
# (based upon Tomcat's setclasspath.sh but renamed since Geronimo's classpath 
# is set in the JAR manifest)
if $os400; then
  # -r will Only work on the os400 if the files are:
  # 1. owned by the user
  # 2. owned by the PRIMARY group of the user
  # this will not work if the user belongs in secondary groups
  BASEDIR="$GERONIMO_HOME"
  . "$GERONIMO_HOME"/bin/setjavaenv.sh 
else
  if [ -r "$GERONIMO_HOME"/bin/setjavaenv.sh ]; then
    BASEDIR="$GERONIMO_HOME"
    . "$GERONIMO_HOME"/bin/setjavaenv.sh
  else
    echo "Cannot find $GERONIMO_HOME/bin/setjavaenv.sh"
    echo "This file is needed to run this program"
    exit 1
  fi
fi

if [ -z "$GERONIMO_TMPDIR" ] ; then
  # Define the java.io.tmpdir to use for Geronimo
  GERONIMO_TMPDIR=var/temp
fi

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
  JAVA_HOME=`cygpath --absolute --windows "$JAVA_HOME"`
  JRE_HOME=`cygpath --absolute --windows "$JRE_HOME"`
  GERONIMO_HOME=`cygpath --absolute --windows "$GERONIMO_HOME"`
  GERONIMO_TMPDIR=`cygpath --absolute --windows "$GERONIMO_TMPDIR"`
fi

# ----- Execute The Requested Command -----------------------------------------
if [ "$GERONIMO_ENV_INFO" != "off" ] ; then
  echo "Using GERONIMO_HOME:   $GERONIMO_HOME"
  echo "Using GERONIMO_TMPDIR: $GERONIMO_TMPDIR"
  if [ "$1" = "debug" ] ; then
    echo "Using JAVA_HOME:       $JAVA_HOME"
    echo "Using JDB_SRCPATH:     $JDB_SRCPATH"
  else
    echo "Using JRE_HOME:        $JRE_HOME"
  fi
fi

exec "$_RUNJAVA" $JAVA_OPTS $GERONIMO_OPTS \
  -Dorg.apache.geronimo.home.dir="$GERONIMO_HOME" \
  -Djava.io.tmpdir="$GERONIMO_TMPDIR" \
  -jar "$GERONIMO_HOME"/bin/jaxws-tools.jar "$@" 
