======================================================
Apache Geronimo milestone build M1  (Apr 28, 2004)

http://wiki.apache.org/geronimo
http://incubator.apache.org/geronimo
http://incubator.apache.org/projects/geronimo.html
------------------------------------------------------

___________________
Documentation
===================
 
 This README serves as five minute overview of Geronimo, but better
 documentation can be found here:
 
   -  http://wiki.apache.org/geronimo/Deployment
   -  http://wiki.apache.org/geronimo/Running
   
 Those are wiki pages that will be updated as we collect feedback from
 this release.
 
___________________
Release Notes
===================

 Please read the RELEASE_NOTES for a complete list of thinigs that are
 not yet functional.  We wrote those up to save you time trying to get
 things to work we know are not implemented.
 
___________________
Installation
===================

 If you've downloaded and unpacked a binary distrobution of Geronimo,
 then you are finished with installation.  If you downloaded a source
 distrobution, then see the BUILDING.txt file for more instructions
 before continuing any further.
 
___________________
Geronimo Home
===================
 
 The GERONIMO_HOME directory, is the directory where the Geronimo
 binary was unpacked or the it is the 'target' directory created by
 Maven from a source distrobution.
 
 If you unpacked the binary into the directory C:\incubator-geronimo,
 for example, than this directory is your GERONIMO_HOME directory.
 The GERONIMO_HOME directory is refered to in various parts of the
 documentation, so it's good to remeber where it is.

___________________
Deploying
===================

 The Geronimo deployment tool is packaged in the executable jar
 bin/deployer.jar under the GERONIMO_HOME and can be executed like
 this:

   C:\incubator-geronimo> java -jar bin\deployer.jar --install --module myDataSource.rar
   C:\incubator-geronimo> java -jar bin\deployer.jar --install --module myEJB.jar
   C:\incubator-geronimo> java -jar bin\deployer.jar --install --module myWebapp.war

 Notice that the deployer.jar is capable of handling a number of
 different archive types; rar, war, and ejb jar.  At this time ear
 archives are not supported.

___________________
Starting
===================

 The main server class is also packed in an executable jar,
 bin/server.jar, and can be ran in a way similar to the deploy tool.

   C:\incubator-geronimo> java -jar bin\server.jar  org/apache/geronimo/DebugConsole
   
 I know what you're thinking, "Why does 'bin\server.jar' use
 back-slashes and 'org/apache/geronimo/DebugConsole' use
 forward-slashes?"  The answer is 'org/apache/geronimo/DebugConsole'
 is a URI; a unique identifier that Geronimo uses to find the app you
 want to start.  URI's use forward-slashes only, never back-slashes.
 So, no matter what operating system your on, the URI will always look
 similar to the one above.

___________________
Support
===================
 
 Any problems with this release can be reported to the Geronimo
 mailing list or Jira issue tracker.

 Mailing list archive:
 http://nagoya.apache.org/eyebrowse/SummarizeList?listId=140

 Mailing list subscription:
 geronimo-dev-subscribe@incubator.apache.org

 Jira:
 http://issues.apache.org/jira/secure/BrowseProject.jspa?id=10220 
