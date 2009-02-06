@rem 
@rem Licensed to the Apache Software Foundation (ASF) under one
@rem or more contributor license agreements.  See the NOTICE file
@rem distributed with this work for additional information
@rem regarding copyright ownership.  The ASF licenses this file
@rem to you under the Apache License, Version 2.0 (the
@rem "License"); you may not use this file except in compliance
@rem with the License.  You may obtain a copy of the License at
@rem 
@rem  http://www.apache.org/licenses/LICENSE-2.0
@rem 
@rem Unless required by applicable law or agreed to in writing,
@rem software distributed under the License is distributed on an
@rem "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@rem KIND, either express or implied.  See the License for the
@rem specific language governing permissions and limitations
@rem under the License.
@rem 

@rem 
@rem $Rev$ $Date$
@rem 

@if "%GERONIMO_BATCH_ECHO%" == "on"  echo on
@if not "%GERONIMO_BATCH_ECHO%" == "on"  echo off

if "%OS%" == "Windows_NT" goto okOsCheck
echo Cannot process command - you are running an unsupported operating system.
cmd /c exit /b 1
goto end

:okOsCheck
@setlocal enableextensions

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.\
cd /d %DIRNAME%

@REM Get standard environment variables
@REM Users can optionally create this file to set environment variables.
if exist "%DIRNAME%\setenv.bat" call "%DIRNAME%\setenv.bat"
if not %errorlevel% == 0 goto end

@REM Get standard Java environment variables
if exist "%DIRNAME%\setjavaenv.bat" goto okSetJavaEnv
echo ERROR - Cannot find %DIRNAME%\setjavaenv.bat
cmd /c exit /b 1
goto end
:okSetJavaEnv
set BASEDIR=%DIRNAME%
call "%DIRNAME%\setJavaEnv.bat"
if not %errorlevel% == 0 goto end

:check_GSHELL_HOME
if "%GSHELL_HOME%" == "" set GSHELL_HOME=%DIRNAME%..

:init
rem Get command-line arguments, handling Windows variants
if "%eval[2+2]" == "4" goto 4NT_args

rem Regular WinNT shell
set ARGS=
:setArgs
if ""%1""=="""" goto doneSetArgs
set ARGS=%ARGS% %1
shift
goto setArgs
:doneSetArgs
goto execute

:4NT_args
rem Get arguments from the 4NT Shell from JP Software
set ARGS=%$

:execute
set BOOTJAR=%GSHELL_HOME%\lib\boot\gshell-bootstrap.jar

rem Start the JVM
%_RUNJAVA% %JAVA_OPTS% -jar "%BOOTJAR%" %ARGS%

:end
@REM pause the batch file if GERONIMO_BATCH_PAUSE is set to 'on'
if "%GERONIMO_BATCH_PAUSE%" == "on" pause
@endlocal
