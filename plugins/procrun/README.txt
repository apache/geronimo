How to re-create geronimosrv.exe and geronimosrvw.exe
-----------------------------------------------------

To recreate the geronimosrv.exe and geronimosrvw.exe, you need to have
Microsoft Visual Studio 2005. You can get the source code from:
https://svn.apache.org/repos/asf/commons/proper/daemon/trunk/src/native/nt/procrun/

The patch included in https://issues.apache.org/jira/browse/DAEMON-118 is 
applied in Geronimo's build of those two exe files. You might also want to
apply this patch.

After the source code is downloaded to your local disk, open the project file
"procrun.dsw" in the root dir, and then build the subprojects "pronsrv" and 
"pronmgr". You can then rename the created exe files pronsrv.exe and 
pronmgr.exe files to geronimosrv.exe and geronimomgr.exe respectively, or to 
whatever you want to call them.
