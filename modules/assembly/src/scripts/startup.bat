@rem
@rem  Copyright 2005 The Apache Software Foundation
@rem
@rem   Licensed under the Apache License, Version 2.0 (the "License");
@rem   you may not use this file except in compliance with the License.
@rem   You may obtain a copy of the License at
@rem
@rem      http://www.apache.org/licenses/LICENSE-2.0
@rem
@rem   Unless required by applicable law or agreed to in writing, software
@rem   distributed under the License is distributed on an "AS IS" BASIS,
@rem   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem   See the License for the specific language governing permissions and
@rem   limitations under the License.
@rem
@rem --------------------------------------------------------------------
@rem $Rev$ $Date$
@rem --------------------------------------------------------------------
@echo off

@rem Verify we are running on Windows XP or Server
if not "%OS%"=="Windows_NT" goto FailOS
setlocal
goto Init

:FailOS
echo Error - Unrecognized OS type.
echo.
set ERRORLEVEL=1
goto End

:Init
@rem Capture any passed in arguments
set ARGS=%*
@rem Capture the current dir the script was started in
set CUR_DIR="%cd%"
for %%z in (%CUR_DIR%) do set CUR_DIR=%%~sz

@rem Set the path to the server.jar
set SERVER_JAR="%~dp0server.jar"
for %%z in (%SERVER_JAR%) do set SERVER_JAR=%%~sz

:CheckServerJar
@rem Verify the server.jar exists:
if exist "%SERVER_JAR%" goto CheckJavaHome
echo Error - Unable to locate the server jar file.
echo.
set ERRORLEVEL=1
goto End

:CheckJavaHome
for %%z in ("%JAVA_HOME%") do set JAVA_HOME=%%~sz
if not "%JAVA_HOME%"=="" goto CheckJavaExe
echo Error - The JAVA_HOME env variable MUST be set.
echo.
set ERRORLEVEL=1
goto End

:CheckJavaExe
if not exist "%JAVA_HOME%\bin" goto FailJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto FailJavaHome
set JAVA="%JAVA_HOME%\bin\java.exe"
goto StartServer

:FailJavaHome
echo Error - Unable to locate Java binary under the JAVA_HOME:
echo   JAVA_HOME: [ %JAVA_HOME%\bin\java.exe ]
echo.
set ERRORLEVEL=1
goto End

:StartServer
%JAVA% -jar %SERVER_JAR% %ARGS%

:End
echo.
@endlocal

