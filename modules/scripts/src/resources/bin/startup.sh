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
# Startup script file for Geronimo that starts Geronimo in the background.
#
# This script calls the geronimo.sh script passing "start" as the
# first argument followed by the arguments supplied by the caller.
#
# Refer to the documentation in the geronimo.sh file for information
# on environment variables etc.
#
# This script is based upon Tomcat's startup.sh file to enable
# those familiar with Tomcat to quickly get started with Geronimo.
# 
# Alternatively you can use the more comprehensive geronimo.sh file 
# directly.
#
# Usage:  startup.sh [geronimo.sh_args] [geronimo_args ...]
#
# $Rev$ $Date$
# --------------------------------------------------------------------

os400=false
case "`uname`" in
CYGWIN*) cygwin=true;;
OS400*) os400=true;;
esac

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
 
PRGDIR=`dirname "$PRG"`
EXECUTABLE=geronimo.sh

# Check that target executable exists
if $os400; then
  # -x will Only work on the os400 if the files are: 
  # 1. owned by the user
  # 2. owned by the PRIMARY group of the user
  # this will not work if the user belongs in secondary groups
  eval
else
  if [ ! -x "$PRGDIR"/"$EXECUTABLE" ]; then
    echo "Cannot find $PRGDIR/$EXECUTABLE"
    echo "This file is needed to run this program"
    exit 1
  fi
fi 

exec "$PRGDIR"/"$EXECUTABLE" start "$@"
