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
@REM
@REM --------------------------------------------------------------------
@REM $Rev$ $Date$
@REM --------------------------------------------------------------------

@REM --------------------------------------------------------------------
@REM Set environment variables relating to the execution of java commands
@REM
@REM This batch file is called by the geronimo.bat file (which is 
@REM invoked by the startup.bat, shutdown.bat files).  This file is
@REM also invoked by the deploy.bat file.
@REM
@REM It is preferable (to simplify migration to future Geronimo releases) 
@REM to set any environment variables you need in the setenv.bat file
@REM rather than modifying Geronimo's script files.  See the documentation
@REM in the geronimo.bat file for further information.
@REM
@REM (Based upon Apache Tomcat 5.5.12's setclasspath.bat although modified
@REM to be more consistent with the shell script version's support of
@REM JDK_HOME and JRE_HOME)
@REM
@REM --------------------------------------------------------------------

@REM Begin all @REM lines with '@' in case GERONIMO_BATCH_ECHO is 'on'
@if "%GERONIMO_BATCH_ECHO%" == "on"  echo on
@if not "%GERONIMO_BATCH_ECHO%" == "on"  echo off

@REM check that either JAVA_HOME or JRE_HOME are set
set jdkOrJreHomeSet=0
if not "%JAVA_HOME%" == "" set jdkOrJreHomeSet=1
if not "%JRE_HOME%" == "" set jdkOrJreHomeSet=1
if "%jdkOrJreHomeSet%" == "1" goto gotJdkOrJreHome
echo Neither the JAVA_HOME nor the JRE_HOME environment variable is defined
echo At least one of these environment variable is needed to run this program
set ERRORLEVEL=1
goto end

@REM If we get this far we have either JAVA_HOME or JRE_HOME set
@REM now check whether the command requires the JDK and if so
@REM check that JAVA_HOME is really pointing to the JDK files.
:gotJdkOrJreHome
set _REQUIRE_JDK=0
if "%1" == "debug" set _REQUIRE_JDK=1
if "%_REQUIRE_JDK%" == "0" goto okJdkFileCheck

set jdkNotFound=0
if not exist "%JAVA_HOME%\bin\java.exe" set jdkNotFound=1
if not exist "%JAVA_HOME%\bin\javaw.exe" set jdkNotFound=1
if not exist "%JAVA_HOME%\bin\jdb.exe" set jdkNotFound=1
if not exist "%JAVA_HOME%\bin\javac.exe" set jdkNotFound=1
if %jdkNotFound% == 0 goto okJdkFileCheck
echo The JAVA_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
echo NB: JAVA_HOME should point to a JDK not a JRE
set ERRORLEVEL=1
goto end

:okJdkFileCheck
@REM default JRE_HOME to JAVA_HOME if not set.
if "%JRE_HOME%" == "" set JRE_HOME=%JAVA_HOME%

if not "%BASEDIR%" == "" goto gotBasedir
echo The BASEDIR environment variable is not defined
echo This environment variable is needed to run this program
set ERRORLEVEL=1
goto end

:gotBasedir
if exist "%BASEDIR%\bin\setjavaenv.bat" goto okBasedir
echo The BASEDIR environment variable is not defined correctly
echo This environment variable is needed to run this program
set ERRORLEVEL=1
goto end

:okBasedir
@REM Set standard command for invoking Java.
@REM Note that NT requires a window name argument when using start.
@REM Also note the quoting as JAVA_HOME may contain spaces.
set _RUNJAVA="%JRE_HOME%\bin\java"
set _RUNJAVAW="%JRE_HOME%\bin\javaw"
set _RUNJDB="%JAVA_HOME%\bin\jdb"

goto end

:end
@REM pause the batch file if GERONIMO_BATCH_PAUSE is set to 'on'
if "%GERONIMO_BATCH_PAUSE%" == "on" pause
