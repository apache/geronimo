@REM
@REM  Licensed to the Apache Software Foundation (ASF) under one or more
@REM  contributor license agreements.  See the NOTICE file distributed with
@REM  this work for additional information regarding copyright ownership.
@REM  The ASF licenses this file to You under the Apache License, Version 2.0
@REM  (the "License"); you may not use this file except in compliance with
@REM  the License.  You may obtain a copy of the License at
@REM
@REM      http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM  Unless required by applicable law or agreed to in writing, software
@REM  distributed under the License is distributed on an "AS IS" BASIS,
@REM  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM  See the License for the specific language governing permissions and
@REM  limitations under the License.

@REM ---------------------------------------------------------------------------
@REM $Rev$ $Date$ 
@REM ---------------------------------------------------------------------------
@if "%GERONIMO_BATCH_ECHO%" == "on"  echo on
@if not "%GERONIMO_BATCH_ECHO%" == "on"  echo off

set CONSOLE_OPTS=-Dkaraf.startLocalConsole=false -Dkaraf.startRemoteShell=false

if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1

:doneSetArgs
%_RUNJAVA% %JAVA_OPTS% %GERONIMO_OPTS% %JAVA_AGENT_OPTS% %CONSOLE_OPTS% -Dorg.apache.geronimo.home.dir="%GERONIMO_HOME%"  -Dkaraf.home="%GERONIMO_HOME%" -Dkaraf.base="%GERONIMO_HOME%" -Djava.util.logging.config.file="%GERONIMO_HOME%\etc\java.util.logging.properties" -Djava.endorsed.dirs="%GERONIMO_HOME%\lib\endorsed;%JRE_HOME%\lib\endorsed" -Djava.ext.dirs="%GERONIMO_HOME%\lib\ext;%JRE_HOME%\lib\ext" -Djava.io.tmpdir="%GERONIMO_TMPDIR%" -classpath "%CLASSPATH%" %MAINCLASS% %_LONG_OPT% %CMD_LINE_ARGS% >> "%GERONIMO_HOME%\var\log\geronimo.out" 2>&1
if not %errorlevel% == 0 pause
exit %errorlevel%
