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
@REM $Rev: 534454 $ $Date: 2007-05-02 09:39:49 -0400 (Wed, 02 May 2007) $
@REM --------------------------------------------------------------------

@REM ---------------------------------------------------------------------------
@REM You should not have to edit this file.  If you wish to have
@REM environment variables set each time you run this batch file
@REM refer to the information on the setenv.bat file below.
@REM
@REM Invocation Syntax:
@REM
@REM   jaxws-tools [general options] command [command options]
@REM
@REM   For detailed usage information, just run deploy without
@REM   arguments.
@REM
@REM Environment Variable Prequisites
@REM
@REM   GERONIMO_HOME   May point at your Geronimo top-level directory.
@REM                   If not specified, this batch file will attempt to
@REM                   discover it relative to the location of this file.
@REM
@REM   GERONIMO_OPTS   (Optional) Java runtime options (in addition to
@REM                   those set in JAVA_OPTS).
@REM
@REM   GERONIMO_TMPDIR (Optional) Directory path location of temporary directory
@REM                   the JVM should use (java.io.tmpdir).  Defaults to
@REM                   var\temp (resolved to server instance directory).
@REM
@REM   JAVA_HOME       Points to your Java Development Kit installation.
@REM                   JAVA_HOME doesn't need to be set if JRE_HOME is set.
@REM                   It is mandatory either JAVA_HOME or JRE_HOME are set.
@REM
@REM   JRE_HOME        (Optional) Points to your Java Runtime Environment
@REM                   Set this if you wish to run Geronimo using the JRE
@REM                   instead of the JDK.
@REM                   Defaults to JAVA_HOME if empty.
@REM                   It is mandatory either JAVA_HOME or JRE_HOME are set.
@REM
@REM   JAVA_OPTS       (Optional) Java runtime options used.
@REM                   Also see the GERONIMO_OPTS environment variable.
@REM
@REM Troubleshooting execution of this batch file:
@REM
@REM   GERONIMO_BATCH_ECHO  (Optional) Environment variable that when set to
@REM                        "on" results in batch commands being echoed.
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
@REM                   in this file rather than modifying Geronimo's batch files.
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
if exist "%GERONIMO_HOME%\bin\deploy.bat" goto okHome
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
@REM but renamed since Deployer's classpath is set in the JAR manifest)
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
set GERONIMO_TMPDIR=var\temp
:gotTmpdir

@REM ----- Execute The Requested Command ---------------------------------------
@if "%GERONIMO_ENV_INFO%" == "off" goto skipEnvInfo
echo Using GERONIMO_HOME:   %GERONIMO_HOME%
echo Using GERONIMO_TMPDIR: %GERONIMO_TMPDIR%
if "%_REQUIRE_JDK%" == "1" echo Using JAVA_HOME:       %JAVA_HOME%
if "%_REQUIRE_JDK%" == "0" echo Using JRE_HOME:        %JRE_HOME%

:skipEnvInfo

@REM Capture any passed in arguments
set CMD_LINE_ARGS=%*
set _JARFILE="%GERONIMO_HOME%"\bin\jaxws-tools.jar

%_RUNJAVA% %JAVA_OPTS% %GERONIMO_OPTS% -Dorg.apache.geronimo.home.dir="%GERONIMO_HOME%" -Djava.io.tmpdir="%GERONIMO_TMPDIR%" -jar %_JARFILE% %CMD_LINE_ARGS%
goto end

:end
echo.
@REM pause the batch file if GERONIMO_BATCH_PAUSE is set to 'on'
if "%GERONIMO_BATCH_PAUSE%" == "on" pause
@endlocal
cmd /c exit /b %errorlevel%
