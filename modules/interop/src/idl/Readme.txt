These files have been adapted from the Corba 3.0 specs.  New CORBA 3.0
interfaces and types have been commented out to provide the same functionality
as in 2.3.1.

Note, these files are being used for protocol marshalling (helpers and holders)

echo on

rem
rem To use this in intellij:
rem
rem 1. Add an external tool called idlj
rem    program: $JDKPath$/bin/idlj.exe
rem    parameters: -i $JDKPath$/lib -verbose -pkgPrefix IOP org.apache.geronimo.gcc.org.omg -pkgPrefix GIOP org.apache.geronimo.gcc.org.omg -pkgPrefix IIOP org.apache.geronimo.gcc.org.omg -pkgPrefix CosNaming org.apache.geronimo.gcc.org.omg -td $ProjectFileDir$/genfiles/src $FileName$
rem    working directory: $ProjectFileDir$\idl
rem
rem 2. You can right click on the .idl file and choose idlj to build it.
rem

rem
rem This is the good version of the CORBA stubs/skels
rem these files get copied into the d:\org.apache.geronimo.gcc\work\geronimo\corba_container\src
rem

setlocal

set pkgprefix=org.apache.geronimo.gcc.org.omg

set opts=
set opts=-i %java_home%\lib
set opts=%opts% -verbose
set opts=%opts% -pkgPrefix IOP %pkgprefix%
set opts=%opts% -pkgPrefix GIOP %pkgprefix%
set opts=%opts% -pkgPrefix IIOP %pkgprefix%
set opts=%opts% -pkgPrefix CosNaming %pkgprefix%


idlj -td gen %opts% IOP.idl
idlj -td gen %opts% GIOP.idl
idlj -td gen %opts% IIOP.idl
idlj -td gen %opts% CosNaming.idl

idlj -td gen %opts% org.apache.geronimo.gcc-rmi-iiop.idl

endlocal
