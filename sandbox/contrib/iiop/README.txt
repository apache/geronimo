*** Software Donation/Grant to Apache Geronimo
This is a snapshot of the main contribution as of December 15, 2005. There may be some bug fixes to the contribution as we test the software.  Further fixes will come as individual contributions.

Contents:

1. src.zip
 
This zip file contains the source code of the Geronimo Corba Container (only for RMI/IIOP and simple Naming).  In this zip file there are a few packages:
 
gcc.adapter: Prototype source code written by me not taken from any Sybase product.

gcc.generator: Prototype source code written by me not taken from any Sybase product.

gcc.naming: Prototype source code that we borrowed from EAServer ME.  Any EAServer ME specific references have been removed and I have modified this so that it can later be implemented for Geronimo

gcc.repository: Prototype source code written by Mark DeLaFranier not taken from any Sybase product.  To be implemented later for Geronimo needs

gcc.properties: Prototype source code that we borrowed from EAServer ME.  Any EAServer ME specific references have been removed and we have modified this so that it can later be implemented for Geronimo
gcc.rmi: RMI/IIOP source code taken from EASME.  Any EASME specific references have been removed and we have modified for Geronimo purposes.

gcc.security: Prototype source code that we borrowed from EAServer ME.  Any EAServer ME specific references have been removed and we have modified this so that it can later be implemented for Geronimo

gcc.util: Prototype source code that we borrowed from EAServer ME.  Any EAServer ME specific references have been removed and we have modified this so that it can later be implemented for Geronimo
 
This source code represents the beginnings of a standalone Corba Container.  At the core is the RMI/IIOP protocol handler that is to be the software donation from Sybase.  All the other files are needed to allow Sybase to help provide support for J2EE interop in the Geronimo server.
 
2. corba_container.zip
 
This is the enitre directory structure for the corba container.  The src.zip is just taken from this corba_container zip file. Files other than source include:
 
- IntelliJ Projet File (tool used by Geronimo)

- gen.bat ( take the standard corba idl files and generate the helpers and holders, used by the RMI/IIOP )

- IDL files (standard corba definitions from www.omg.org )

- Test cases

