@if "%DEBUG%" == "" @echo off
rem 
rem Licensed to the Apache Software Foundation (ASF) under one
rem or more contributor license agreements.  See the NOTICE file
rem distributed with this work for additional information
rem regarding copyright ownership.  The ASF licenses this file
rem to you under the Apache License, Version 2.0 (the
rem "License"); you may not use this file except in compliance
rem with the License.  You may obtain a copy of the License at
rem 
rem  http://www.apache.org/licenses/LICENSE-2.0
rem 
rem Unless required by applicable law or agreed to in writing,
rem software distributed under the License is distributed on an
rem "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
rem KIND, either express or implied.  See the License for the
rem specific language governing permissions and limitations
rem under the License.
rem 

rem 
rem $Rev$ $Date$
rem 

if "%OS%"=="Windows_NT" setlocal enableextensions
set ERRORLEVEL=0

:begin

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.\

:check_JAVACMD
if not "%JAVACMD%" == "" goto check_GSHELL_HOME

:check_JAVA_HOME
if not "%JAVA_HOME%" == "" goto have_JAVA_HOME
set JAVACMD=java
goto check_GSHELL_HOME

:have_JAVA_HOME
set JAVACMD=%JAVA_HOME%\bin\java
goto check_GSHELL_HOME

:check_GSHELL_HOME
if "%GSHELL_HOME%" == "" set GSHELL_HOME=%DIRNAME%..

:init
rem Get command-line arguments, handling Windowz variants
if not "%OS%" == "Windows_NT" goto win9xME_args
if "%eval[2+2]" == "4" goto 4NT_args

rem Regular WinNT shell
set ARGS=%*
goto execute

:win9xME_args
rem Slurp the command line arguments.  This loop allows for an unlimited number
set ARGS=

:win9xME_args_slurp
if "x%1" == "x" goto execute
set ARGS=%ARGS% %1
shift
goto win9xME_args_slurp

:4NT_args
rem Get arguments from the 4NT Shell from JP Software
set ARGS=%$

:execute

set BOOTJAR=%GSHELL_HOME%\lib\boot\gshell-bootstrap.jar

rem Start the JVM
"%JAVACMD%" %JAVA_OPTS% -jar "%BOOTJAR%" %ARGS%

:end

if "%OS%"=="Windows_NT" endlocal
if "%GSHELL_BATCH_PAUSE%" == "on" pause

