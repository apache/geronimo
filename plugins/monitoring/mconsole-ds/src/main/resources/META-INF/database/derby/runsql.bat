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
@REM $Rev$ $Date$
@REM --------------------------------------------------------------------

@REM -- This batch file is used to run sql to enable/disable monitoring
@REM -- on Default Server using ij in a NetworkServer environment.
@REM --
@REM -- REQUIREMENTS:
@REM -- You must have the Derby, Derby Client, and Derby Tools libraries in your classpath
@REM --
@REM -- You may need to modify the values below for a different
@REM -- GERONIMO_HOME
@REM --
@REM -- This file for use on Windows systems
@REM --------------------------------------------------------------------
@setlocal enableextensions

@REM -- GERONIMO_HOME may need to be changed
@REM --------------------------------------------------------------------
@REM %~dp0 is expanded pathname of the current script
set GERONIMO_HOME="%~dp0.."
for %%z in (%GERONIMO_HOME%) do set GERONIMO_HOME=%%~sz
set classpath=%GERONIMO_HOME%;%GERONIMO_HOME%\repository\org\apache\derby\derbyclient\${derbyVersion}\derbyclient-${derbyVersion}.jar;%GERONIMO_HOME%\repository\org\apache\derby\derbytools\${derbyVersion}\derbytools-${derbyVersion}.jar

@REM --------------------------------------------------------------------
@REM -- To use a different JVM with a different syntax, simply edit
@REM -- this file
@REM --------------------------------------------------------------------
if exist "%GERONIMO_HOME%\bin\setenv.bat" call "%GERONIMO_HOME%\bin\setenv.bat"

@REM --------------------------------------------------------------------
@REM -- start ij
@REM -- Arguments 5 to the script are [host], [port], [username], [password] and 
@REM -- ["enable-monitoring"|"disable-monitoring"] in that order.
@REM --------------------------------------------------------------------
"%JAVA_HOME%\bin\java.exe" %JAVA_OPTS% -Dij.driver=org.apache.derby.jdbc.ClientDriver -Dij.protocol=jdbc:derby://%1:%2/ -Dij.user=%3 -Dij.password=%4 org.apache.derby.tools.ij -f %GERONIMO_HOME%\bin\%5.sql

@endlocal
cmd /c exit /b %errorlevel%
