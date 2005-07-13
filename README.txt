======================================================
Apache Geronimo milestone build M4  (July, 2005)

http://geronimo.apache.org/
------------------------------------------------------


___________________
Documentation
===================
 
 This README serves as five minute overview of Geronimo, but better
 documentation can be found here:
 
   -  http://geronimo.apache.org/documentation.html

___________________
Release Notes
===================

 Please read the RELEASE_NOTES for a complete list of things that are
 not yet functional.  We wrote those up to save you time trying to get
 things to work we know are not implemented.

___________________
Installation
===================

 If you've downloaded and unpacked a .zip or .tar.gz binary
 distribution of Geronimo, then you are finished with installation.
 These distributions are quite easy to install, but do not allow you
 to customize the server configuration (such as by changing listen
 ports).
 
 There is also an installer package available.  This is an executable
 JAR that you run to start the install routine.  This package lets
 you customize ports and the default administrative login during the
 installation process.
 
___________________
Source Code
===================
 
 Note that the source distribution is provided for reference purposes,
 but you may not actually be able to build a working server from it --
 it refers to snapshot versions of some third-party libraries which
 might have changed since the time M4 was originally built.  We are
 attempting to resolve this for future releases.
 
___________________
Geronimo Home
===================
 
 The GERONIMO_HOME directory, is the directory where the Geronimo
 binary was unpacked or the it is the 'target' directory created by
 Maven from a source distribution.
 
 If you unpacked the binary into the directory C:\geronimo,
 for example, than this directory is your GERONIMO_HOME directory.
 The GERONIMO_HOME directory is referred to in various parts of the
 documentation, so it's good to remember where it is.

___________________
Deploying
===================

 The Geronimo deployment tool is packaged in the executable jar
 bin/deployer.jar under the GERONIMO_HOME and can be executed like
 this (assuming the server is already running):

   C:\geronimo> java -jar bin\deployer.jar deploy myDataSource.rar
   C:\geronimo> java -jar bin\deployer.jar deploy myEJB.jar
   C:\geronimo> java -jar bin\deployer.jar deploy myWebapp.war
   C:\geronimo> java -jar bin\deployer.jar deploy myApp.ear

 The deployer will prompt you for a username and password; the
 default administrative account has username "system" and password
 "manager".

 Notice that the deployer.jar is capable of handling a number of
 different archive types; rar, war, ejb jar, and EAR.

___________________
Starting
===================

 The main server class is also packed in an executable jar,
 bin/server.jar, and can be ran in a way similar to the deploy tool.

   C:\geronimo> java -jar bin\server.jar

 Once the server is running, you can start the debug console with a
 command like this:

   c:\geronimo> java -jar bin\deployer.jar start \
                          org/apache/geronimo/DebugConsole

 Again, this will prompt you for a username and password, which
 default to "system" and "manager", respectively.

 Then you can access the debug console by pointing your browser to:

   http://localhost:8080/debug-tool/
   
 I know what you're thinking, "Why does 'bin\server.jar' use
 back-slashes and 'org/apache/geronimo/DebugConsole' use
 forward-slashes?"  The answer is 'org/apache/geronimo/DebugConsole'
 is a URI; a unique identifier that Geronimo uses to find the app you
 want to start.  URI's use forward-slashes only, never back-slashes.
 So, no matter what operating system you're on, the URI will always look
 similar to the one above.

___________________
Startup/Log Output
===================
 
 By default, the server provides a progress indicator while it starts,
 and generally does not provide very detailed output to the console.  If
 you'd like more detailed output, you can start the server with a
 command like this:
 
   C:\geronimo> java -jar bin\server.jar -v      (show INFO to console)
   C:\geronimo> java -jar bin\server.jar -vv     (show DEBUG to console)
   
 Note that all DEBUG and higher out will be saved to the log file
 $GERONIMO_HOME/var/log/geronimo.log in any case -- the switches above
 just alter the console output.
 
 You may also suppress the progress bar without altering the default
 console log level (WARN) by using the -quiet flag on the command line.
  
___________________
Support
===================
 
 Any problems with this release can be reported to the Geronimo
 user mailing list or Jira issue tracker.

 Mailing list archive:
 http://mail-archives.apache.org/mod_mbox/geronimo-user/

 Mailing list subscription:
 user-subscribe@geronimo.apache.org

 Jira:
 http://issues.apache.org/jira/secure/BrowseProject.jspa?id=10220 
