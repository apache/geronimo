@REM
@REM  Copyright 2005 The Apache Software Foundation
@REM
@REM   Licensed under the Apache License, Version 2.0 (the "License");
@REM   you may not use this file except in compliance with the License.
@REM   You may obtain a copy of the License at
@REM
@REM      http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM   Unless required by applicable law or agreed to in writing, software
@REM   distributed under the License is distributed on an "AS IS" BASIS,
@REM   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM   See the License for the specific language governing permissions and
@REM   limitations under the License.

@REM --------------------------------------------------------------------
@REM $Rev$ $Date$
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
@REM   GERONIMO_BASE   (Optional) Base directory for resolving dynamic portions
@REM                   of a Geronimo installation.  If not present, resolves to
@REM                   the same directory that GERONIMO_HOME points to.
@REM
@REM   GERONIMO_OPTS   (Optional) Java runtime options (in addition to
@REM                   those set in JAVA_OPTS) used when the "start",
@REM                   "stop", or "run" command is executed.
@REM
@REM   GERONIMO_TMPDIR (Optional) Directory path location of temporary directory
@REM                   the JVM should use (java.io.tmpdir).  Defaults to
@REM                   %GERONIMO_BASE%\var\temp.
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
@REM   JPDA_TRANSPORT  (Optional) JPDA transport used when the "jpda start"
@REM                   command is executed. The default is "dt_shmem".
@REM
@REM   JPDA_ADDRESS    (Optional) Java runtime options used when the "jpda start"
@REM                   command is executed. The default is "jdbconn".
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
@REM  0 - Success
@REM  1 - Error
@REM ---------------------------------------------------------------------------

@if "%GERONIMO_BATCH_ECHO%" == "on"  echo on
@if not "%GERONIMO_BATCH_ECHO%" == "on"  echo off

if "%OS%" == "Windows_NT" goto okOsCheck
echo Cannot process Geronimo command - you are running an unsupported operating system.
set ERRORLEVEL=1
goto end

:okOsCheck
setlocal

if not "%GERONIMO_HOME%" == "" goto resolveHome
@REM %~dp0 is expanded pathname of the current script
set GERONIMO_HOME=%~dp0..

@REM resolve .. and remove any trailing slashes
:resolveHome
set CURRENT_DIR=%cd%
cd %GERONIMO_HOME%
set GERONIMO_HOME=%cd%
cd %CURRENT_DIR%

:gotHome
if exist "%GERONIMO_HOME%\bin\geronimo.bat" goto okHome
echo The GERONIMO_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
set ERRORLEVEL=1
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
set ERRORLEVEL=1
goto end
:okSetJavaEnv
set BASEDIR=%GERONIMO_HOME%
call "%GERONIMO_HOME%\bin\setJavaEnv.bat"
if not %errorlevel% == 0 goto end

if not "%GERONIMO_BASE%" == "" goto gotBase
set GERONIMO_BASE=%GERONIMO_HOME%
:gotBase

if not "%GERONIMO_TMPDIR%" == "" goto gotTmpdir
set GERONIMO_TMPDIR=%GERONIMO_BASE%\var\temp
:gotTmpdir

@REM ----- Execute The Requested Command ---------------------------------------

echo Using GERONIMO_BASE:   %GERONIMO_BASE%
echo Using GERONIMO_HOME:   %GERONIMO_HOME%
echo Using GERONIMO_TMPDIR: %GERONIMO_TMPDIR%
if "%_REQUIRE_JDK%" == "1" echo Using JAVA_HOME:       %JAVA_HOME%
if "%_REQUIRE_JDK%" == "0" echo Using JRE_HOME:        %JRE_HOME%

set _EXECJAVA=%_RUNJAVA%
@REM MAINCLASS required for jdb debugger as it requires the mainclass
@REM parameter. For other commands, the main class is obtained from
@REM the JAR manifest.
set MAINCLASS=org.apache.geronimo.system.main.Daemon
set JPDA=
set _JARFILE="%GERONIMO_HOME%"\bin\server.jar

if not ""%1"" == ""jpda"" goto noJpda
set JPDA=jpda
if not "%JPDA_TRANSPORT%" == "" goto gotJpdaTransport
set JPDA_TRANSPORT=dt_shmem
:gotJpdaTransport
if not "%JPDA_ADDRESS%" == "" goto gotJpdaAddress
set JPDA_ADDRESS=jdbconn
:gotJpdaAddress
shift
:noJpda

if ""%1"" == ""debug"" goto doDebug
if ""%1"" == ""run"" goto doRun
if ""%1"" == ""start"" goto doStart
if ""%1"" == ""stop"" goto doStop

echo Usage:  geronimo command [args]
echo commands:
echo   debug             Debug Geronimo in jdb debugger
echo   jpda start        Start Geronimo under JPDA debugger
echo   run               Start Geronimo in the current window
echo   start             Start Geronimo in a separate window
echo   stop              Stop Geronimo
echo. 
echo args for debug, jpda start, run and start commands:
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
echo        --port        RMI port to connect to
goto end

:doDebug
shift
set _EXECJAVA=%_RUNJDB%
if not "%JDB_OPTS%" == "" goto gotJdbOpts
set JDB=jdb
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
goto execCmd

:doStart
echo.
echo Starting Geronimo in a separate window...
shift
@REM use long format of startup progress to be consistent with 
@REM the unix version of the start processing
set _LONG_OPT=--long

if not "%OS%" == "Windows_NT" goto noTitle
set _EXECJAVA=start "Geronimo" %_RUNJAVA%
goto gotTitle
:noTitle
set _EXECJAVA=start %_RUNJAVA%
:gotTitle
goto execCmd

:doStop
shift
set _JARFILE="%GERONIMO_HOME%"\bin\shutdown.jar
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

@REM Execute Java with the applicable properties
if not "%JDB%" == "" goto doJDB
if not "%JPDA%" == "" goto doJpda
%_EXECJAVA% %JAVA_OPTS% %GERONIMO_OPTS% -Dorg.apache.geronimo.base.dir="%GERONIMO_BASE%" -Djava.io.tmpdir="%GERONIMO_TMPDIR%" -jar %_JARFILE% %_LONG_OPT% %CMD_LINE_ARGS%
goto end

:doJDB
%_EXECJAVA% %JAVA_OPTS% %GERONIMO_OPTS% -sourcepath "%JDB_SRCPATH%" -Dorg.apache.geronimo.base.dir="%GERONIMO_BASE%" -Djava.io.tmpdir="%GERONIMO_TMPDIR%" -classpath %_JARFILE% %MAINCLASS% %CMD_LINE_ARGS%
goto end

:doJpda
%_EXECJAVA% %JAVA_OPTS% %GERONIMO_OPTS% -Xdebug -Xrunjdwp:transport=%JPDA_TRANSPORT%,address=%JPDA_ADDRESS%,server=y,suspend=n %DEBUG_OPTS% -Dorg.apache.geronimo.base.dir="%GERONIMO_BASE%" -Djava.io.tmpdir="%GERONIMO_TMPDIR%" -jar %_JARFILE% %_LONG_OPT% %CMD_LINE_ARGS%
goto end

:end
@REM pause the batch file if GERONIMO_BATCH_PAUSE is set to 'on'
if "%GERONIMO_BATCH_PAUSE%" == "on" pause
