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
@REM Windows Service Install/Uninstall batch file based on procrun for Geronimo
@REM
@REM You should not have to edit this file.  If you wish to have environment
@REM variables set each time you run this batch file refer to the information
@REM on the setenv.bat file.
@REM
@REM
@REM Invocation Syntax:
@REM
@REM   service_pr command [stop args]
@REM
@REM Commands
@REM   install      Install the service of the name "geronimosrv".
@REM   remove       Remove the service "geronimosrv" from the System.
@REM
@REM For detailed usage information, just run service_pr.bat without any
@REM arguments.
@REM
@REM Environment Variable Prequisites:
@REM
@REM   GERONIMO_HOME   May point at your Geronimo top-level directory.
@REM                   If not specified, this batch file will attempt to
@REM                   discover it relative to the location of this file.
@REM
@REM   GERONIMO_TMPDIR (Optional) Directory path location of temporary directory
@REM                   the JVM should use (java.io.tmpdir).  Defaults to
@REM                   var\temp (resolved to server instance directory).
@REM
@REM   JAVA_HOME       Points to your Java Development Kit installation.
@REM                   JAVA_HOME doesn't need to be set if JRE_HOME is set
@REM                   unless you use the "debug" command.
@REM                   It is mandatory either JAVA_HOME or JRE_HOME are set.
@REM
@REM   JRE_HOME        (Optional) Points to your Java Runtime Environment
@REM                   Set this if you wish to run Geronimo using the JRE
@REM                   instead of the JDK.
@REM                   Defaults to JAVA_HOME if empty.
@REM                   It is mandatory either JAVA_HOME or JRE_HOME are set.
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
@REM                        JAVA_RUNTIME before the command is
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

if "%OS%" == "Windows_NT" goto okOsCheck
echo Cannot process Geronimo command - you are running an unsupported operating system.
cmd /c exit /b 1
goto end

:okOsCheck
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

if not "%GERONIMO_TMPDIR%" == "" goto gotTmpdir
@REM A relative value will be resolved relative to each instance 
set GERONIMO_TMPDIR=var\temp
:gotTmpdir

set EXECUTABLE=%GERONIMO_HOME%\bin\geronimosrv.exe
set JAVA_EXE=%_RUNJAVA%.exe

if "%1" == "" goto showUsage
set SERVICE_NAME=geronimosrv

if ""%1"" == ""install"" goto doInstall
if ""%1"" == ""remove"" goto doRemove

:showUsage
echo.
echo Usage:  service_pr command [stop args]
echo.
echo Commands:
echo   install     Install the Geronimo service with the service name "geronimosrv"
echo   remove      Uninstall the Geronimo service "geronimosrv"
echo.
echo Optional stop args are used when stopping the service.
echo Specify all necessary args or specify none of them.
echo   --user      Admin user, default to "system"
echo   --password  Admin password, default to "manager"
echo   --port      RMI port to connect to, default to 1099
echo   --secure    Optional flag for enabling secure JMX communication
cmd /c exit /b 1
goto end

:doRemove
@REM Remove the service
"%EXECUTABLE%" //DS//%SERVICE_NAME%
if not errorlevel 1 goto removed
echo Failed to remove "%SERVICE_NAME%" service.
goto end

:removed
echo The service '%SERVICE_NAME%' has been removed.
goto end

:doInstall
@REM ----- Install the service ---------------------------------------
@if "%GERONIMO_ENV_INFO%" == "off" goto skipEnvInfo
echo Using GERONIMO_HOME:   %GERONIMO_HOME%
echo Using GERONIMO_TMPDIR: %GERONIMO_TMPDIR%
echo Using JRE_HOME:        %JRE_HOME%
:skipEnvInfo

@REM Set default startup arguments
set STARTUP_ARGS=--long

@REM Get remaining unshifted shutdown command line arguments
if "%2" == "" goto setDefaultArgs
set SHUTDOWN_ARGS="%2"
:setArgs
shift
if "%2"=="" goto doneSetArgs
set SHUTDOWN_ARGS=%SHUTDOWN_ARGS%#%2
goto setArgs
:setDefaultArgs
set SHUTDOWN_ARGS=--user#system#--password#manager
:doneSetArgs

@REM Setup the Java programming language agent
set JAVA_AGENT_JAR=%GERONIMO_HOME%\bin\jpa.jar
set JAVA_AGENT_OPTS=
if exist "%JAVA_AGENT_JAR%" set JAVA_AGENT_OPTS=-javaagent:^"%JAVA_AGENT_JAR%^"

set PR_DISPLAYNAME=Apache Geronimo Service - %SERVICE_NAME%
set PR_DESCRIPTION=Apache Geronimo Server - http://geronimo.apache.org/
set PR_INSTALL=%EXECUTABLE%
set PR_LOGPATH=%GERONIMO_HOME%\var\log
set PR_LOGLEVEL=INFO
set PR_LOGPREFIX=geronimosrv.log
set PR_STDOUTPUT=%PR_LOGPATH%\geronimosrv.out
set PR_STDERROR=%PR_LOGPATH%\geronimosrv.err

"%EXECUTABLE%" //IS//%SERVICE_NAME% --StartImage %JAVA_EXE% --StartPath "%GERONIMO_HOME%" --StartMode exe --StartParams %JAVA_AGENT_OPTS%#-Djava.endorsed.dirs="%GERONIMO_HOME%\lib\endorsed';'%JRE_HOME%\lib\endorsed"#-Djava.ext.dirs="%GERONIMO_HOME%\lib\ext';'%JRE_HOME%\lib\ext"#-Dorg.apache.geronimo.home.dir="%GERONIMO_HOME%"#-Djava.io.tmpdir="%GERONIMO_TMPDIR%"#-jar#"%GERONIMO_HOME%\bin\server.jar"#%STARTUP_ARGS% --StopImage %JAVA_EXE% --StopPath "%GERONIMO_HOME%" --StopMode exe --StopParams %JAVA_AGENT_OPTS%#-Djava.endorsed.dirs="%GERONIMO_HOME%\lib\endorsed';'%JRE_HOME%\lib\endorsed"#-Djava.ext.dirs="%GERONIMO_HOME%\lib\ext';'%JRE_HOME%\lib\ext"#-Dorg.apache.geronimo.home.dir="%GERONIMO_HOME%"#-Djava.io.tmpdir="%GERONIMO_TMPDIR%"#-jar#"%GERONIMO_HOME%\bin\shutdown.jar"#%SHUTDOWN_ARGS%
if not errorlevel 1 goto installed
echo Failed to install "%SERVICE_NAME%" service.
goto end

:installed
echo The service "%SERVICE_NAME%" has been installed.

:end
@REM pause the batch file if GERONIMO_BATCH_PAUSE is set to 'on'
if "%GERONIMO_BATCH_PAUSE%" == "on" pause
@endlocal
