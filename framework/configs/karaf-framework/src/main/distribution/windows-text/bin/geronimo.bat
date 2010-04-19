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

@REM --------------------------------------------------------------------
@REM $Rev: 931017 $ $Date: 2010-04-06 00:58:31 -0400 (Tue, 06 Apr 2010) $
@REM --------------------------------------------------------------------

@REM ---------------------------------------------------------------------------
@REM Start/Stop Batch file for Geronimo
@REM
@REM This batch file is based upon Tomcat's catalina.bat file to enable
@REM those familiar with Tomcat to quickly get started with Geronimo.
@REM
@REM This batch file can be used directly instead of startup.bat and
@REM shutdown.bat as they call this batch file anyway.
@REM
@REM You should not have to edit this file.  If you wish to have environment
@REM variables set each time you run this batch file refer to the information
@REM on the setenv.bat file below.
@REM
@REM Invocation Syntax:
@REM
@REM   geronimo command [geronimo_args]
@REM
@REM   For detailed usage information, just run geronimo.bat without any
@REM   arguments.
@REM
@REM Environment Variable Prequisites:
@REM
@REM   GERONIMO_HOME   May point at your Geronimo top-level directory.
@REM                   If not specified, this batch file will attempt to
@REM                   discover it relative to the location of this file.
@REM
@REM   GERONIMO_OPTS   (Optional) Java runtime options (in addition to
@REM                   those set in JAVA_OPTS) used when the "start",
@REM                   "stop", or "run" command is executed.
@REM
@REM   GERONIMO_TMPDIR (Optional) Directory path location of temporary directory
@REM                   the JVM should use (java.io.tmpdir).  Defaults to
@REM                   var\temp (resolved to server instance directory).
@REM
@REM   GERONIMO_WIN_START_ARGS  (Optional) additional arguments to the Windows
@REM                            START command when the "start" command
@REM                            is executed. E.G, you could set this to /MIN
@REM                            to start Geronimo in a minimized window.
@REM
@REM   JAVA_HOME       Points to your Java Development Kit installation.
@REM                   JAVA_HOME doesn't need to be set if JRE_HOME is set
@REM                   unless you use the "debug" command.
@REM                   It is mandatory either JAVA_HOME or JRE_HOME are set.
@REM
@REM   JRE_HOME        (Optional) Points to your Java Runtime Environment
@REM                   Set this if you wish to run Geronimo using the JRE
@REM                   instead of the JDK (except for the "debug" command).
@REM                   Defaults to JAVA_HOME if empty.
@REM                   It is mandatory either JAVA_HOME or JRE_HOME are set.
@REM
@REM   JAVA_OPTS       (Optional) Java runtime options used when the "start",
@REM                   "stop", or "run" command is executed.
@REM                   Also see the GERONIMO_OPTS environment variable.
@REM
@REM   JDB_SRCPATH     (Optional) The Source Path to be used by jdb debugger.
@REM                   Defaults to %GERONIMO_HOME%\src
@REM
@REM   JPDA_ADDRESS    (Optional) Java runtime options used when the "jpda start"
@REM                   command is executed. The default is "8000".
@REM
@REM   JPDA_OPTS       (Optional) JPDA command line options.
@REM                   Only set this if you need to use some unusual JPDA
@REM                   command line options.  This overrides the use of the
@REM                   other JPDA_* environment variables.
@REM                   Defaults to JPDA command line options contructed from
@REM                   the JPDA_ADDRESS, JPDA_SUSPEND and JPDA_TRANSPORT
@REM                   environment variables.
@REM
@REM   JPDA_SUSPEND    (Optional) Suspend the JVM before the main class is loaded.
@REM                   Valid values are 'y' and 'n'.  The default is "n".
@REM
@REM   JPDA_TRANSPORT  (Optional) JPDA transport used when the "jpda start"
@REM                   command is executed. The default is "dt_socket".
@REM                   Note that "dt_socket" is the default instead of "dt_shmem"
@REM                   because eclipse does not support "dt_shmem".
@REM
@REM Troubleshooting execution of this batch file:
@REM
@REM   GERONIMO_BATCH_ECHO (Optional) Environment variable that when set to
@REM                       "on" results in batch commands being echoed.
@REM
@REM   GERONIMO_BATCH_PAUSE (Optional) Environment variable that when set to
@REM                        "on" results in each batch file to pause at the
@REM                        end of execution
@REM
@REM   GERONIMO_ENV_INFO    (Optional) Environment variable that when set to
@REM                        "on" (the default) outputs the values of
@REM                        GERONIMO_HOME, GERONIMO_TMPDIR,
@REM                        JAVA_HOME and JRE_HOME before the command is
@REM                        issued. Set to "off" if you do not want this
@REM                        information displayed.
@REM
@REM Batch files called by this batch file:
@REM
@REM   %GERONIMO_HOME%\bin\setenv.bat
@REM                   (Optional) This batch file is called if it is present.
@REM                   Its contents may set one or more of the above environment
@REM                   variables. It is preferable (to simplify migration to
@REM                   future Geronimo releases) to set environment variables
@REM                   in this file rather than modifying Geronimo's script files.
@REM
@REM   %GERONIMO_HOME%\bin\setjavaenv.bat
@REM                   This batch file is called to set environment variables
@REM                   relating to the java or jdb exe file to call.
@REM                   This file should not need to be modified.
@REM
@REM Exit Codes:
@REM
@REM  0        - Success
@REM  Non-zero - Error
@REM ---------------------------------------------------------------------------

@if "%GERONIMO_BATCH_ECHO%" == "on"  echo on
@if not "%GERONIMO_BATCH_ECHO%" == "on"  echo off

@setlocal enableextensions

if not "%GERONIMO_HOME%" == "" goto resolveHome
@REM %~dp0 is expanded pathname of the current script
set GERONIMO_HOME=%~dp0..

@REM resolve .. and remove any trailing slashes
:resolveHome
set CURRENT_DIR=%cd%
cd /d %GERONIMO_HOME%
set GERONIMO_HOME=%cd%
cd /d %CURRENT_DIR%

:gotHome
if exist "%GERONIMO_HOME%\bin\geronimo.bat" goto okHome
echo The GERONIMO_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
cmd /c exit /b 1
goto end
:okHome

@REM Get standard environment variables
@REM Users can optionally create this file to set environment variables.
if exist "%GERONIMO_HOME%\bin\setenv.bat" call "%GERONIMO_HOME%\bin\setenv.bat"
if not %errorlevel% == 0 goto end

@REM Get standard Java environment variables (based upon Tomcat's setclasspath.bat
@REM but renamed since Geronimo's classpath is set in the JAR manifest)
if exist "%GERONIMO_HOME%\bin\setjavaenv.bat" goto okSetJavaEnv
echo Cannot find %GERONIMO_HOME%\bin\setjavaenv.bat
echo This file is needed to run this program
cmd /c exit /b 1
goto end
:okSetJavaEnv
set BASEDIR=%GERONIMO_HOME%
call "%GERONIMO_HOME%\bin\setJavaEnv.bat"
if not %errorlevel% == 0 goto end

@REM Using default JAVA_OPTS if it's not set
if not "%JAVA_OPTS%" == "" goto skipDefaultJavaOpts
set JAVA_OPTS=-Xmx256m -XX:MaxPermSize=128m
:skipDefaultJavaOpts

if not "%GERONIMO_TMPDIR%" == "" goto gotTmpdir
@REM A relative value will be resolved relative to each instance 
set GERONIMO_TMPDIR=var\temp
:gotTmpdir

@REM Setup the classpath
pushd "%GERONIMO_HOME%\lib"
for %%G in (*.*) do call:APPEND_TO_CLASSPATH %%G
popd
goto CLASSPATH_END

:APPEND_TO_CLASSPATH
set filename=%~1
set suffix=%filename:~-4%
if %suffix% equ .jar set CLASSPATH=%CLASSPATH%;%GERONIMO_HOME%\lib\%filename%
goto :EOF

:CLASSPATH_END

set _EXECJAVA=%_RUNJAVA%
@REM MAINCLASS required for jdb debugger as it requires the mainclass
@REM parameter. For other commands, the main class is obtained from
@REM the JAR manifest.
set MAINCLASS=org.apache.geronimo.cli.daemon.DaemonCLI
set JPDA=

if not ""%1"" == ""jpda"" goto noJpda
set JPDA=jpda
if not "%JPDA_SUSPEND%" == "" goto gotJpdaSuspend
set JPDA_SUSPEND=n
:gotJpdaSuspend
if not "%JPDA_TRANSPORT%" == "" goto gotJpdaTransport
@REM Note that "dt_socket" is the default instead of "dt_shmem"
@REM because eclipse does not support "dt_shmem".
set JPDA_TRANSPORT=dt_socket
:gotJpdaTransport
if not "%JPDA_ADDRESS%" == "" goto gotJpdaAddress
set JPDA_ADDRESS=8000
:gotJpdaAddress
if not "%JPDA_OPTS%" == "" goto gotJpdaOpts
set JPDA_OPTS=-Xdebug -Xrunjdwp:transport=%JPDA_TRANSPORT%,address=%JPDA_ADDRESS%,server=y,suspend=%JPDA_SUSPEND%
:gotJpdaOpts
set GERONIMO_OPTS=%GERONIMO_OPTS% %JPDA_OPTS%
shift
:noJpda

@REM ----- Execute The Requested Command ---------------------------------------
@if "%GERONIMO_ENV_INFO%" == "off" goto skipEnvInfo
echo Using GERONIMO_HOME:   %GERONIMO_HOME%
echo Using GERONIMO_TMPDIR: %GERONIMO_TMPDIR%
if "%_REQUIRE_JDK%" == "1" echo Using JAVA_HOME:       %JAVA_HOME%
if "%_REQUIRE_JDK%" == "0" echo Using JRE_HOME:        %JRE_HOME%
if not "%JPDA%" == "jpda" goto skipJpdaEnvInfo
@REM output JPDA info to assist diagnosing JPDA debugger config issues.
echo Using JPDA_OPTS:       %JPDA_OPTS%
:skipJpdaEnvInfo
:skipEnvInfo

if ""%1"" == ""debug"" goto doDebug
if ""%1"" == ""run"" goto doRun
if ""%1"" == ""start"" goto doStart
if ""%1"" == ""stop"" goto doStop

echo Usage:  geronimo command [args]
echo commands:
echo   debug             Debug Geronimo in jdb debugger
echo   jpda run          Start Geronimo in foreground under JPDA debugger
echo   jpda start        Start Geronimo in background under JPDA debugger
echo   run               Start Geronimo in the current window
echo   start             Start Geronimo in a separate window
echo   stop              Stop Geronimo
echo.
echo args for debug, jpda run, jpda start, run and start commands:
echo        --quiet       No startup progress
echo        --long        Long startup progress
echo   -v   --verbose     INFO log level
echo   -vv  --veryverbose DEBUG log level
echo        --override    Override configurations. USE WITH CAUTION!
echo        --help        Detailed help.
echo.
echo args for stop command:
echo        --user        Admin user
echo        --password    Admin password
echo        --host        Hostname of the server
echo        --port        RMI port to connect to
echo        --secure      Enable secure JMX communication
cmd /c exit /b 1
goto end

:doDebug
shift
set _EXECJAVA=%_RUNJDB%
set JDB=jdb
set CONSOLE_OPTS=-Dkaraf.startLocalConsole=true -Dkaraf.startRemoteShell=true
if not "%JDB_SRCPATH%" == "" goto gotJdbSrcPath
set JDB_SRCPATH=%GERONIMO_HOME%\src
:gotJdbSrcPath
echo Note: The jdb debugger will start Geronimo in another process and connect to it.
echo       To terminate Geronimo when running under jdb, run the "geronimo stop" command
echo       in another window.  Do not use Ctrl-C as that will terminate the jdb client
echo       (the debugger itself) but will not stop the Geronimo process.
goto execCmd

:doRun
shift
set CONSOLE_OPTS=-Dkaraf.startLocalConsole=true -Dkaraf.startRemoteShell=false
goto execCmd

:doStart
echo.
echo Starting Geronimo in a separate window...
shift
@REM use long format of startup progress to be consistent with
@REM the unix version of the start processing
set _LONG_OPT=--long
set _EXECJAVA=start "Geronimo Application Server" /d"%GERONIMO_HOME%\bin" %GERONIMO_WIN_START_ARGS% %_RUNJAVA%
set CONSOLE_OPTS=-Dkaraf.startLocalConsole=false -Dkaraf.startRemoteShell=true
goto execCmd

:doStop
shift
set MAINCLASS=org.apache.geronimo.cli.shutdown.ShutdownCLI
set CONSOLE_OPTS=-Dkaraf.startLocalConsole=false -Dkaraf.startRemoteShell=false
goto execCmd

:execCmd
@REM Get remaining unshifted command line arguments and save them in the
set CMD_LINE_ARGS=
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs
:doneSetArgs

@REM Setup the Java programming language agent
set JAVA_AGENT_JAR=%GERONIMO_HOME%\bin\jpa.jar
set JAVA_AGENT_OPTS=
if exist "%JAVA_AGENT_JAR%" set JAVA_AGENT_OPTS=-javaagent:"%JAVA_AGENT_JAR%"

@REM Must reset ERRORLEVEL
cmd /c exit /b 0

@REM Execute Java with the applicable properties
if not "%JDB%" == "" goto doJDB
%_EXECJAVA% %JAVA_OPTS% %GERONIMO_OPTS% %JAVA_AGENT_OPTS% %CONSOLE_OPTS% -Dorg.apache.geronimo.home.dir="%GERONIMO_HOME%"  -Dkaraf.home="%GERONIMO_HOME%" -Dkaraf.base="%GERONIMO_HOME%" -Djava.util.logging.config.file="%GERONIMO_HOME%\etc\java.util.logging.properties" -Djava.endorsed.dirs="%GERONIMO_HOME%\lib\endorsed;%JRE_HOME%\lib\endorsed" -Djava.ext.dirs="%GERONIMO_HOME%\lib\ext;%JRE_HOME%\lib\ext" -Djava.io.tmpdir="%GERONIMO_TMPDIR%" -classpath "%CLASSPATH%" %MAINCLASS% %_LONG_OPT% %CMD_LINE_ARGS%
goto end

:doJDB
%_EXECJAVA% %JAVA_OPTS% %GERONIMO_OPTS% -sourcepath "%JDB_SRCPATH%" %CONSOLE_OPTS% -Dorg.apache.geronimo.home.dir="%GERONIMO_HOME%"  -Dkaraf.home="%GERONIMO_HOME%" -Dkaraf.base="%GERONIMO_HOME%" -Djava.util.logging.config.file="%GERONIMO_HOME%\etc\java.util.logging.properties" -Djava.endorsed.dirs="%GERONIMO_HOME%\lib\endorsed;%JRE_HOME%\lib\endorsed" -Djava.ext.dirs="%GERONIMO_HOME%\lib\ext;%JRE_HOME%\lib\ext" -Djava.io.tmpdir="%GERONIMO_TMPDIR%" -classpath "%CLASSPATH%" %MAINCLASS% %CMD_LINE_ARGS%
goto end

:end
@REM pause the batch file if GERONIMO_BATCH_PAUSE is set to 'on'
if "%GERONIMO_BATCH_PAUSE%" == "on" pause
@endlocal
cmd /c exit /b %errorlevel%
