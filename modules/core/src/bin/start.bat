@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem                                                                         ##
@rem  Twiddle Proxy for Windowz                                              ##
@rem                                                                         ##
@rem ##########################################################################

@rem 
@rem $Revision: 1.2 $ $Date: 2003/08/26 10:38:26 $
@rem 

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

@rem The path of the command to execute
set COMMAND_PATH=geronimo/start

:begin
@rem Determine what directory it is in.
set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.\

:init
@rem Get command-line arguments, handling Windowz variants
if not "%OS%" == "Windows_NT" goto win9xME_args
if "%eval[2+2]" == "4" goto 4NT_args

@rem Regular WinNT shell
set CMD_LINE_ARGS=%*
goto execute

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=

:win9xME_args_slurp
if "x%1" == "x" goto execute
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto win9xME_args_slurp

:4NT_args
@rem Get arguments from the 4NT Shell from JP Software
set CMD_LINE_ARGS=%$

:execute
@rem Setup the command line
set TWIDDLE_CMD=%DIRNAME%twiddle.bat

@rem Execute Twiddle
call "%TWIDDLE_CMD%" %TWIDDLE_OPTS% "%COMMAND_PATH%" -- %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" endlocal

