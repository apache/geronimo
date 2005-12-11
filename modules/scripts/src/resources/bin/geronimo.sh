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

# -----------------------------------------------------------------------------
# Start/Stop Script for the Geronimo Server
#
# This script is based upon Tomcat's catalina.sh file to enable
# those familiar with Tomcat to quickly get started with Geronimo.
#
# For usage information, just run geronimo.sh without any arguments.
#
# Environment Variable Prequisites
#
#   GERONIMO_HOME   May point at your Geronimo top-level directory.
#
#   GERONIMO_BASE   (Optional) Base directory for resolving dynamic portions
#                   of a Geronimo installation.  If not present, resolves to
#                   the same directory that GERONIMO_HOME points to.
#
#   GERONIMO_OPTS   (Optional) Java runtime options used when the "start",
#                   "stop", or "run" command is executed.
#
#   GERONIMO_OUT    (Optional) File that Geronimo's stdout and stderr streams
#                   will be redirected to if Geronimo is started in the 
#                   background.
#                   Defaults to $GERONIMO_BASE/var/log/geronimo.out
#
#   GERONIMO_PID    (Optional) Path of the file which should contains the pid
#                   of the Geronimo java process, when start (fork) is used
#
#   GERONIMO_TMPDIR (Optional) Directory path location of temporary directory
#                   the JVM should use (java.io.tmpdir).
#                   Defaults to $GERONIMO_BASE/var/temp.
#
#   JAVA_HOME       Points to your Java Development Kit installation.
#                   JAVA_HOME doesn't need to be set if JRE_HOME is set
#                   unless you use the "debug" command.
#                   It is mandatory either JAVA_HOME or JRE_HOME are set.
#
#   JRE_HOME        Points to your Java Runtime Environment installation.
#                   Set this if you wish to run Geronimo using the JRE 
#                   instead of the JDK (except for the "debug" command).
#                   Defaults to JAVA_HOME if empty.
#                   It is mandatory either JAVA_HOME or JRE_HOME are set.
#
#   JAVA_OPTS       (Optional) Java runtime options used when the "start",
#                   "stop", or "run" command is executed.
#
#   JDB_SRCPATH     (Optional) The Source Path to be used by jdb debugger 
#                   when the "debug" command is executed.
#                   Defaults to %GERONIMO_HOME%\src
#
#   JPDA_TRANSPORT  (Optional) JPDA transport used when the "jpda start"
#                   command is executed. The default is "dt_socket".
#
#   JPDA_ADDRESS    (Optional) Java runtime options used when the "jpda start"
#                   command is executed. The default is 8000.
#
#   START_OS_CMD    (Optional) Operating system command that will be placed in
#                   front of the java command when starting Geronimo in the
#                   background.  This can be useful on operating systems where
#                   the OS provides a command that allows you to start a process
#                   with in a specified CPU or priority.
#
# Scripts called by this script:
# 
#   $GERONIMO_HOME/bin/setenv.sh
#                   (Optional) This script file is called if it is present.
#                   Its contents may set one or more of the above environment
#                   variables.
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
  if expr "$link" : '.*/.*' > /dev/null; then
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
  [ -n "$JDB_SRCPATH" ] && JDB_SRCPATH=`cygpath --unix "$JDB_SRCPATH"`  
  [ -n "$GERONIMO_HOME" ] && GERONIMO_HOME=`cygpath --unix "$GERONIMO_HOME"`
  [ -n "$GERONIMO_BASE" ] && GERONIMO_BASE=`cygpath --unix "$GERONIMO_BASE"`
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

if [ -z "$GERONIMO_BASE" ] ; then
  GERONIMO_BASE="$GERONIMO_HOME"
fi

if [ -z "$GERONIMO_TMPDIR" ] ; then
  # Define the java.io.tmpdir to use for Geronimo
  GERONIMO_TMPDIR="$GERONIMO_BASE"/var/temp
fi

if [ -z "$GERONIMO_OUT" ] ; then
  # Define the output file we are to redirect both stdout and stderr to
  # when Geronimo is started in the background
  GERONIMO_OUT="$GERONIMO_BASE"/var/log/geronimo.out
fi

if [ -z "$JDB_SRCPATH" ] ; then
  # Define the source path to be used by the JDB debugger
  JDB_SRCPATH="$GERONIMO_HOME"/src
fi

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
  JAVA_HOME=`cygpath --absolute --windows "$JAVA_HOME"`
  JRE_HOME=`cygpath --absolute --windows "$JRE_HOME"`
  JDB_SRCPATH=`cygpath --absolute --windows "$JDB_SRCPATH"`  
  GERONIMO_HOME=`cygpath --absolute --windows "$GERONIMO_HOME"`
  GERONIMO_BASE=`cygpath --absolute --windows "$GERONIMO_BASE"`
  GERONIMO_TMPDIR=`cygpath --absolute --windows "$GERONIMO_TMPDIR"`
fi

# ----- Execute The Requested Command -----------------------------------------

echo "Using GERONIMO_BASE:   $GERONIMO_BASE"
echo "Using GERONIMO_HOME:   $GERONIMO_HOME"
echo "Using GERONIMO_TMPDIR: $GERONIMO_TMPDIR"
if [ "$1" = "debug" ] ; then
  echo "Using JAVA_HOME:       $JAVA_HOME"
  echo "Using JDB_SRCPATH:     $JDB_SRCPATH"
else
  echo "Using JRE_HOME:        $JRE_HOME"
fi

LONG_OPT=
if [ "$1" = "start" ] ; then
  LONG_OPT=--long
  echo "Using GERONIMO_OUT:    $GERONIMO_OUT"
fi

if [ "$1" = "jpda" ] ; then
  if [ -z "$JPDA_TRANSPORT" ]; then
    JPDA_TRANSPORT="dt_socket"
  fi
  if [ -z "$JPDA_ADDRESS" ]; then
    JPDA_ADDRESS="8000"
  fi
  if [ -z "$JPDA_OPTS" ]; then
    JPDA_OPTS="-Xdebug -Xrunjdwp:transport=$JPDA_TRANSPORT,address=$JPDA_ADDRESS,server=y,suspend=n"
  fi
  LONG_OPT=--long
  GERONIMO_OPTS="$GERONIMO_OPTS $JPDA_OPTS"
  shift
fi

if [ "$1" = "debug" ] ; then
  if $os400; then
    echo "Debug command not available on OS400"
    exit 1
  else
    echo "Note: The jdb debugger will start Geronimo in another process and connect to it."
    echo "      To terminate Geronimo when running under jdb, run the "geronimo stop" command"
    echo "      in another window.  Do not use Ctrl-C as that will terminate the jdb client"
    echo "      (the debugger itself) but will not stop the Geronimo process."
    shift
    exec "$_RUNJDB" $JAVA_OPTS $GERONIMO_OPTS \
      -sourcepath "$JDB_SRCPATH" \
      -Dorg.apache.geronimo.base.dir="$GERONIMO_BASE" \
      -Djava.io.tmpdir="$GERONIMO_TMPDIR" \
      -classpath "$GERONIMO_HOME"/bin/server.jar \
      org.apache.geronimo.system.main.Daemon $LONG_OPT "$@" 
  fi

elif [ "$1" = "run" ]; then

  shift
  exec "$_RUNJAVA" $JAVA_OPTS $GERONIMO_OPTS \
    -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" \
    -Dorg.apache.geronimo.base.dir="$GERONIMO_BASE" \
    -Djava.io.tmpdir="$GERONIMO_TMPDIR" \
    -jar "$GERONIMO_HOME"/bin/server.jar $LONG_OPT "$@" 

elif [ "$1" = "start" ] ; then

  shift
  touch $GERONIMO_OUT
  $START_OS_CMD "$_RUNJAVA" $JAVA_OPTS $GERONIMO_OPTS \
    -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" \
    -Dorg.apache.geronimo.base.dir="$GERONIMO_BASE" \
    -Djava.io.tmpdir="$GERONIMO_TMPDIR" \
    -jar "$GERONIMO_HOME"/bin/server.jar $LONG_OPT "$@" \
    >> $GERONIMO_OUT 2>&1 &

    if [ ! -z "$GERONIMO_PID" ]; then
      echo $! > $GERONIMO_PID
    fi

elif [ "$1" = "stop" ] ; then

  shift
  FORCE=0
# support -force as that is the option Tomcat uses, we will document
# --force as the option to be consistent with other Geronimo options.
  if [ "$1" = "--force" -o "$1" = "-force" ]; then
    shift
    FORCE=1
  fi

  "$_RUNJAVA" $JAVA_OPTS $GERONIMO_OPTS \
    -Dorg.apache.geronimo.base.dir="$GERONIMO_BASE" \
    -Djava.io.tmpdir="$GERONIMO_TMPDIR" \
    -jar "$GERONIMO_HOME"/bin/shutdown.jar "$@"

  if [ $FORCE -eq 1 ]; then
    if [ ! -z "$GERONIMO_PID" ]; then
       echo "Killing: `cat $GERONIMO_PID`"
       kill -9 `cat $GERONIMO_PID`
    fi
  fi

else

  echo "Usage: geronimo.sh command [geronimo_args]"
  echo "commands:"
  echo "  debug             Debug Geronimo in jdb debugger"
  echo "  jpda start        Start Geronimo under JPDA debugger"
  echo "  run               Start Geronimo in the foreground"
  echo "  start             Start Geronimo in the background"
  echo "  stop              Stop Geronimo"
  echo "  stop --force      Stop Geronimo (followed by kill -KILL)"
  echo ""
  echo "args for debug, jpda start, run and start commands:"
  echo "       --quiet       No startup progress"
  echo "       --long        Long startup progress"
  echo "  -v   --verbose     INFO log level"
  echo "  -vv  --veryverbose DEBUG log level"
  echo "       --override    Override configurations. USE WITH CAUTION!"
  echo "       --help        Detailed help."
  echo ""
  echo "args for stop command:"
  echo "       --user        Admin user"
  echo "       --password    Admin password"
  echo "       --port        RMI port to connect to"
  exit 1

fi
