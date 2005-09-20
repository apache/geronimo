export GERONIMO_HOME=~/geronimo/geronimo/modules/assembly/target/geronimo-1.0-SNAPSHOT
rm  ${GERONIMO_HOME}/var/log/geronimo.log

java -Xdebug -Xnoagent -Xmx512m -Xms512m  -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 -jar ${GERONIMO_HOME}/bin/server.jar $1

#java  -Xmx1024m -Xms1024m  -jar ${GERONIMO_HOME}/bin/server.jar org/apache/geronimo/DebugConsole

# java  -Xmx1024m -Xms1024m  -jar ${GERONIMO_HOME}/bin/server.jar $1
