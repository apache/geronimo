@echo off
rem
rem  Copyright 2005 The Apache Software Foundation
rem
rem   Licensed under the Apache License, Version 2.0 (the "License");
rem   you may not use this file except in compliance with the License.
rem   You may obtain a copy of the License at
rem
rem      http://www.apache.org/licenses/LICENSE-2.0
rem
rem   Unless required by applicable law or agreed to in writing, software
rem   distributed under the License is distributed on an "AS IS" BASIS,
rem   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem   See the License for the specific language governing permissions and
rem   limitations under the License.
rem
rem --------------------------------------------------------------------
rem $Rev$ $Date$
rem --------------------------------------------------------------------
rem
rem --------------------------------------------------------------------
rem Shutdown batch file for Geronimo.
rem
rem This batch file calls the geronimo.bat script passing "stop" as the
rem first argument followed by the arguments supplied by the caller.
rem
rem Refer to the documentation in the geronimo.bat file for information
rem on environment variables etc.
rem
rem This batch file is based upon Tomcat's shutdown.bat file to enable
rem those familiar with Tomcat to easily stop Geronimo.
rem 
rem Alternatively you can use the more comprehensive geronimo.bat file 
rem directly.
rem
rem Usage:  shutdown [geronimo_args ...]
rem
rem $Rev$ $Date$
rem --------------------------------------------------------------------
if "%OS%" == "Windows_NT" setlocal

rem Guess GERONIMO_HOME if not defined
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
goto end
:okHome

set EXECUTABLE=%GERONIMO_HOME%\bin\geronimo.bat

rem Check that target executable exists
if exist "%EXECUTABLE%" goto okExec
echo Cannot find %EXECUTABLE%
echo This file is needed to run this program
goto end
:okExec

rem Get remaining unshifted command line arguments and save them in the
set CMD_LINE_ARGS=
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs
:doneSetArgs

call "%EXECUTABLE%" stop %CMD_LINE_ARGS%

:end
