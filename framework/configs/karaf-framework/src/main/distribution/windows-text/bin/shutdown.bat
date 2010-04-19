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
@REM $Rev: 899564 $ $Date: 2010-01-15 03:38:51 -0500 (Fri, 15 Jan 2010) $
@REM --------------------------------------------------------------------

@REM --------------------------------------------------------------------
@REM Shutdown batch file for Geronimo.
@REM
@REM This batch file calls the geronimo.bat script passing "stop" as the
@REM first argument followed by the arguments supplied by the caller.
@REM
@REM This batch file is based upon Tomcat's shutdown.bat file to enable
@REM those familiar with Tomcat to easily stop Geronimo.
@REM
@REM Alternatively you can use the more comprehensive geronimo.bat file
@REM directly.
@REM
@REM Invocation Syntax:
@REM
@REM   shutdown [stop command args ...]
@REM
@REM   Invoke the shutdown.bat file without any arguments for information
@REM   on arguments for the geronimo.bat stop command that is invoked by
@REM   this batch file.
@REM
@REM Environment Variable Prequisites:
@REM
@REM   Refer to the documentation in the geronimo.bat file for information
@REM   on environment variables etc.
@REM
@REM --------------------------------------------------------------------

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

set EXECUTABLE=%GERONIMO_HOME%\bin\geronimo.bat

@REM Check that target executable exists
if exist "%EXECUTABLE%" goto okExec
echo Cannot find %EXECUTABLE%
echo This file is needed to run this program
cmd /c exit /b 1
goto end
:okExec

@REM Get remaining unshifted command line arguments and save them in the
set CMD_LINE_ARGS=
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs
:doneSetArgs

call "%EXECUTABLE%" stop %CMD_LINE_ARGS%

:end
@REM pause the batch file if GERONIMO_BATCH_PAUSE is set to 'on'
if "%GERONIMO_BATCH_PAUSE%" == "on" pause
@endlocal
cmd /c exit /b %errorlevel%
