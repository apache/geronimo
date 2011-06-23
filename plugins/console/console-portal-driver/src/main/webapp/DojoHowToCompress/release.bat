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
@REM $Rev: 931017 $ $Date: 2011-06-26 00:58:31 -0400 (Thur, 26 Jun 2011) $
@REM -


@echo come to release
@set MYPROJECT_DOJO=%cd%
@cd /d ../../dojo-release-1.5.1-src/util/buildscripts
build.bat action=clean,release optimize=shrinksafe layerOptimize=shrinksafe copyTests=false stripConsole=all releaseDir=%MYPROJECT_DOJO%\release  profileFile=%MYPROJECT_DOJO%\app\index.profile.js version=1.0 cssOptimize=comments releaseName=dojo
