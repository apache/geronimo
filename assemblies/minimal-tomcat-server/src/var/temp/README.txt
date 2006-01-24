This temp directory is used by the JVM for temporary file storage.
The JVM is configured to use this as its java.io.tmpdir in the
geronimo.sh and geronimo.bat scripts.  Geronimo is configured to use
this temporary directory rather than its default for security reasons.
The temp directory must exist for Tomcat to work correctly.

This file is also included in the distribution to prevent problems with users
who download the zip or tar.gz distribution and extract it with WinZip.
Some versions of WinZIP decide it is not necessary to create the directory 
because it is empty.