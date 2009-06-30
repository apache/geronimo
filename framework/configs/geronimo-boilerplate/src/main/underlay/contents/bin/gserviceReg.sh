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
# 
# This script file will invoke startup.sh , shutdown.sh and gsh.sh to 
# start/stop/restart/ server and check server status, also make server autostart
# at os boot time.
# Usage:
#  $server_install_path/bin/gserviceReg.sh add/del/list [service_name] 
#  [service_name] start/stop/restart/status
#--------------------------------------------------------------------

# Make sure prerequisite environment variables are set
if [ -z "$JAVA_HOME" -a -z "$JRE_HOME" ]; then  
    echo "ERROR:  Could not find a Java runtime."
    echo " - Neither the JAVA_HOME nor the JRE_HOME environment variable is defined"
    echo " - A Java implementation could not be found on the system PATH"
    echo "At least one of these is required for this program to execute."
    echo ""
    exit 1
fi
if [ -z "$JRE_HOME" ]; then
   if [ -d "$JAVA_HOME/jre" ]; then
     JRE_HOME="$JAVA_HOME/jre"
   else
     JRE_HOME="$JAVA_HOME"
   fi
fi
cygwin=false
os400=false
osAix=false
osSolaris=false
osRedhat=false
osSuse=false
osUbuntu=false
case "`uname`" in
CYGWIN*) cygwin=true;;
OS400*) os400=true;;
AIX*) osAix=true;;
SunOS*) osSolaris=true;;
Ubuntu*) osUbuntu=true;;
Linux*)
case "`more /proc/version`" in
*Red*Hat*) osRedhat=true;;
*SUSE*) osSuse=true;;
*Ubuntu*) osUbuntu=true;;
esac
;;
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
if [ "$#" -eq "2" ]; then
GSERVICE_NAME=${GERONIMO_HOME}/bin/$2.sh
fi

GSHELL_NAME=${GERONIMO_HOME}/bin/gsh.sh
chmod +x "$GERONIMO_HOME"/bin/*

# Check that target executable exists
if $os400; then
  # -x will Only work on the os400 if the files are: 
  # 1. owned by the user
  # 2. owned by the PRIMARY group of the user
  # this will not work if the user belongs in secondary groups
  eval
else
  if [ ! -x "${GERONIMO_HOME}/bin/startup.sh" ]; then
    echo "Cannot find ${GERONIMO_HOME}/bin/startup.sh"
    echo "This file is needed to run this program"
    exit 1
  fi
  if [ ! -x "${GERONIMO_HOME}/bin/shutdown.sh" ]; then
    echo "Cannot find ${GERONIMO_HOME}/bin/shutdown.sh"
    echo "This file is needed to run this program"
    exit 1
  fi

  if [ ! -x "${GERONIMO_HOME}/bin/gsh.sh" ]; then
    if [ ! -x "${GERONIMO_HOME}/bin/gsh" ]; then
    echo "Cannot find ${GERONIMO_HOME}/bin/gsh"
    echo "This file is needed to run this program"
    exit 1
    else
      GSHELL_NAME=${GERONIMO_HOME}/bin/gsh
    fi
  fi

fi

case "$1" in
add)
if [ "$#" -lt "2" ]; then
 echo "Please input service name to be added"
 exit 1
fi
echo '#! /bin/bash'>"$GSERVICE_NAME"
echo '# chkconfig: 235 99 01'>>"$GSERVICE_NAME"
echo "# description: $2 service dameon">>"$GSERVICE_NAME"
echo "# /etc/init.d/$2">>"$GSERVICE_NAME"
echo '### BEGIN INIT INFO'>>"$GSERVICE_NAME"
echo "# Provides:          $2">>"$GSERVICE_NAME"
echo '# Required-Start:    $ALL'>>"$GSERVICE_NAME"
echo '# Required-Stop:     '>>"$GSERVICE_NAME"
echo '# Default-Start:     2 3 5'>>"$GSERVICE_NAME"
echo '# Default-Stop:      0 1 2 6'>>"$GSERVICE_NAME"
echo "# Short-Description: $2 daemon">>"$GSERVICE_NAME"
echo '### END INIT INFO'>>"$GSERVICE_NAME"
echo "JAVA_HOME=$JAVA_HOME">>"$GSERVICE_NAME"
echo "export JAVA_HOME">>"$GSERVICE_NAME"
echo "GERONIMO_HOME=$GERONIMO_HOME">>"$GSERVICE_NAME"
echo "GSHELL_NAME=$GSHELL_NAME">>"$GSERVICE_NAME"
echo 'Port_Off_Set=`grep "^PortOffset=" "$GERONIMO_HOME"/var/config/config-substitutions.properties |awk -F= '\'{print \$2}\'\`>>"$GSERVICE_NAME"
echo 'RMI_PORT=`expr $Port_Off_Set \+ 1099`'>>"$GSERVICE_NAME"
echo 'ADMIN_USER=system'>>"$GSERVICE_NAME"
echo 'ADMIN_PASS=manager'>>"$GSERVICE_NAME"


echo 'start() {'>>"$GSERVICE_NAME"

echo '"$GSHELL_NAME" -c "geronimo/wait-for-server -u ${ADMIN_USER} -w ${ADMIN_PASS} -p ${RMI_PORT} -t 10" > /dev/null 2>&1'>>"$GSERVICE_NAME"
echo 'if [ $? -eq 0 ] '>>"$GSERVICE_NAME"        
echo 'then'>>"$GSERVICE_NAME"
echo '{'>>"$GSERVICE_NAME"
echo 'echo "Server is started already on this port"'>>"$GSERVICE_NAME"
echo '}'>>"$GSERVICE_NAME"
echo 'else  '>>"$GSERVICE_NAME"
echo '{'>>"$GSERVICE_NAME"
echo 'echo "Start Server on port $RMI_PORT"'>>"$GSERVICE_NAME"
echo ' "$GERONIMO_HOME"/bin/startup.sh'>>"$GSERVICE_NAME"
echo '}'>>"$GSERVICE_NAME"
echo 'fi'>>"$GSERVICE_NAME"
echo '}'>>"$GSERVICE_NAME"

echo 'stop() {'>>"$GSERVICE_NAME"
echo 'echo "Shutting down server on port ${RMI_PORT}"'>>"$GSERVICE_NAME"
echo '"$GERONIMO_HOME"/bin/shutdown.sh --user "$ADMIN_USER" --password "$ADMIN_PASS" --port "$RMI_PORT"'>>"$GSERVICE_NAME"
echo '}'>>"$GSERVICE_NAME"

echo 'restart() {'>>"$GSERVICE_NAME"
echo 'echo "Restart server on port ${RMI_PORT}"'>>"$GSERVICE_NAME"
echo '"$GSHELL_NAME" -c "geronimo/wait-for-server -u ${ADMIN_USER} -w ${ADMIN_PASS} -p ${RMI_PORT} -t 5" > /dev/null 2>&1'>>"$GSERVICE_NAME"
echo 'if [ $? -eq 0 ] '>>"$GSERVICE_NAME"        
echo 'then'>>"$GSERVICE_NAME"
echo '{'>>"$GSERVICE_NAME"
echo '  echo -n -e "\nRestart server.\n"'>>"$GSERVICE_NAME"
echo '  stop'>>"$GSERVICE_NAME"
echo '  sleep 30'>>"$GSERVICE_NAME"
echo '  start'>>"$GSERVICE_NAME"
echo '}'>>"$GSERVICE_NAME"
echo 'else  '>>"$GSERVICE_NAME"
echo '{'>>"$GSERVICE_NAME"
echo '  echo -n -e "\nServer is down.Can not restart.\n"'>>"$GSERVICE_NAME"
echo '}'>>"$GSERVICE_NAME"
echo 'fi'>>"$GSERVICE_NAME"

echo '}'>>"$GSERVICE_NAME"

echo 'status() {'>>"$GSERVICE_NAME"
echo 'echo -n "Check server status on port ${RMI_PORT}"'>>"$GSERVICE_NAME"
echo '"$GSHELL_NAME" -c "geronimo/wait-for-server -u ${ADMIN_USER} -w ${ADMIN_PASS} -p ${RMI_PORT} -t 5" > /dev/null 2>&1'>>"$GSERVICE_NAME"
echo 'if [ $? -eq 0 ] '>>"$GSERVICE_NAME"       
echo 'then'>>"$GSERVICE_NAME"
echo 'echo -n -e "\nServer is started.\n"'>>"$GSERVICE_NAME"
echo 'else'>>"$GSERVICE_NAME"
echo '  echo -n -e "\nServer is down.\n"'>>"$GSERVICE_NAME"
echo 'fi'>>"$GSERVICE_NAME"
echo '}'>>"$GSERVICE_NAME"

echo 'case "$1" in'>>"$GSERVICE_NAME"
echo 'start)'>>"$GSERVICE_NAME"
echo 'start'>>"$GSERVICE_NAME"
echo ';;'>>"$GSERVICE_NAME"
echo 'stop)'>>"$GSERVICE_NAME"
echo 'stop'>>"$GSERVICE_NAME"
echo ';;'>>"$GSERVICE_NAME"
echo 'restart)'>>"$GSERVICE_NAME"
echo 'restart'>>"$GSERVICE_NAME"
echo ';;'>>"$GSERVICE_NAME"
echo 'status)'>>"$GSERVICE_NAME"
echo 'status'>>"$GSERVICE_NAME"
echo ';;'>>"$GSERVICE_NAME"
echo '*)'>>"$GSERVICE_NAME"
echo "echo \"Usage: $2 {start|stop|restart|status}\"">>"$GSERVICE_NAME"
echo 'exit 1'>>"$GSERVICE_NAME"
echo 'esac'>>"$GSERVICE_NAME"

chmod +x "$GSERVICE_NAME"
if [ ! -x /usr/sbin/$2 ]; then
  ln -sf "$GSERVICE_NAME" /usr/sbin/"$2"
  chmod +x /usr/sbin/"$2"
 if $osSuse; then  
  ln -sf /usr/sbin/"$2" /etc/init.d/"$2"
  insserv "$2"
  chkconfig "$2" on
 fi
 if $osRedhat; then   
    ln -sf /usr/sbin/"$2" /etc/init.d/"$2"
    chkconfig "$2" on   
 fi
 if $osSolaris; then   
   ln -sf /usr/sbin/"$2" /etc/rc3.d/S99"$2"
 fi
 if $osAix; then    
   echo "$2:2:respawn:/usr/sbin/$2 start">>/etc/inittab
 fi 
 if $osUbuntu; then    
   update-rc.d $2 defaults 99 01   
 fi
else
echo "$2 already exists"
exit 1
fi
;;
del)
if [ "$#" -lt "2" ]; then
 echo "Please input service name to be deleted"
 exit 1
else
 if [ -x /usr/sbin/"$2" ]; then
 echo "Delete service $2" 
 if $osSuse -o $osRedhat; then
  chkconfig --del $2
  rm -f /etc/init.d/"$2"  
 fi
 if $osSolaris; then   
   rm -fr /etc/rc3.d/S99"$2"
 fi
 if $osUbuntu; then    
   update-rc.d -f $2 remove   
 fi
 rm -f /usr/sbin/$2
 rm -f "$GSERVICE_NAME" 
 else
  echo "$2 doesn't exist"
  exit 1
 fi
fi
;;
list)
if [ "$#" -lt "2" ]; then
 echo "Please input service name to be listed"
 exit 1
else
  if [ -x /usr/sbin/"$2" ]; then
  serverfolder=`head -1 /usr/sbin/"$2"|awk -F= '{print $2}'`
  echo "$2 for server $serverfolder"
  else
    echo "$2 doesn't exist"
    exit 1
  fi
fi
;;
*)
echo "Usage: ./gserviceReg.sh add|del|list [required:service name]"
exit 1
;;
esac
