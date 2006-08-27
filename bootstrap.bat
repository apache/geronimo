@echo off
rem 
rem $Rev$ $Date$
rem 

if not "%ECHO%" == "" echo %ECHO%

setlocal
set DIRNAME=%~dp0%
set PROGNAME=%~nx0%
set ARGS=%*

ant %BUILD_OPTIONS% -emacs -logger org.apache.tools.ant.NoBannerLogger -f bootstrap.xml %ARGS%

:END

endlocal

if not "%PAUSE%" == "" pause

:END_NO_PAUSE
