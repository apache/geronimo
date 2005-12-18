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
@REM Startup batch file for Geronimo that starts Geronimo in a new window.
@REM
@REM This batch file calls the geronimo.bat script passing "start" as the
@REM first argument followed by the arguments supplied by the caller.
@REM
@REM Refer to the documentation in the geronimo.bat file for information
@REM on environment variables etc.
@REM
@REM This batch file is based upon Tomcat's startup.bat file to enable
@REM those familiar with Tomcat to quickly get started with Geronimo.
@REM 
@REM Alternatively you can use the more comprehensive geronimo.bat file 
@REM directly.
@REM
@REM Usage:  startup [geronimo.bat_args] [geronimo_args ...]
@REM
@REM $Rev$ $Date$
@REM --------------------------------------------------------------------

@if "%GERONIMO_BATCH_ECHO%" == "on"  echo on
@if not "%GERONIMO_BATCH_ECHO%" == "on"  echo off

if "%OS%" == "Windows_NT" goto okOsCheck
echo Cannot process Geronimo command - you are running an unsupported operating system.
set ERRORLEVEL=1
goto end

:okOsCheck
setlocal

@REM Guess GERONIMO_HOME if not defined
set CURRENT_DIR=%cd%
if not "%GERONIMO_HOME%" == "" goto gotHome
set GERONIMO_HOME=%CURRENT_DIR%
if exist "%GERONIMO_HOME%\bin\geronimo.bat" goto okHome
cd ..
set GERONIMO_HOME=%cd%
cd %CURRENT_DIR%
:gotHome
if exist "%GERONIMO_HOME%\bin\geronimo.bat" goto okHome
echo The GERONIMO_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
set ERRORLEVEL=1
goto end
:okHome

set EXECUTABLE=%GERONIMO_HOME%\bin\geronimo.bat

@REM Check that target executable exists
if exist "%EXECUTABLE%" goto okExec
echo Cannot find %EXECUTABLE%
echo This file is needed to run this program
set ERRORLEVEL=1
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

call "%EXECUTABLE%" start %CMD_LINE_ARGS%

:end

@REM pause the batch file if GERONIMO_BATCH_PAUSE is set to 'on'
if "%GERONIMO_BATCH_PAUSE%" == "on" pause
