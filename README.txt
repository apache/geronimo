======================================================
Apache Geronimo milestone build M3  (Nov 10, 2004)

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

 If you've downloaded and unpacked a binary distribution of Geronimo,
 then you are finished with installation.  Note that the source
 distribution is provided for reference purposes, but you may not
 actually be able to build a working server from it -- it refers to
 snapshot versions of some third-party libraries which might have
 changed since the time M3 was originally built.

 
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

 Notice that the deployer.jar is capable of handling a number of
 different archive types; rar, war, ejb jar, and EAR.

 The deployer will prompt you for a username and password; the
 default administrative account has username "system" and password
 "manager".


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
Support
===================
 
 Any problems with this release can be reported to the Geronimo
 mailing list or Jira issue tracker.

 Mailing list archive:
 http://nagoya.apache.org/eyebrowse/SummarizeList?listId=140

 Mailing list subscription:
 dev-subscribe@geronimo.apache.org

 Jira:
 http://issues.apache.org/jira/secure/BrowseProject.jspa?id=10220 
