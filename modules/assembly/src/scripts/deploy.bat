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

@rem Set the path to the deployer.jar
set DEPLOYER_JAR="%~dp0deployer.jar"
for %%z in (%DEPLOYER_JAR%) do set DEPLOYER_JAR=%%~sz

:CheckDeployerJar
@rem Verify the deployer.jar exists:
if exist "%DEPLOYER_JAR%" goto CheckJavaHome
echo Error - Unable to locate the deployer jar file.
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
goto StartDeployer

:FailJavaHome
echo Error - Unable to locate Java binary under the JAVA_HOME:
echo   JAVA_HOME: [ %JAVA_HOME%\bin\java.exe ]
echo.
set ERRORLEVEL=1
goto End

:StartDeployer
%JAVA% -jar %DEPLOYER_JAR% %ARGS%

:End
echo.
@endlocal

