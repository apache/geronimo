======================================================
Apache Geronimo milestone build M4  (August 10, 2005)

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
 then you are finished with installation.  You can also use the
 installer JAR to customize things like network ports and the
 administrative login during installation.

 
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
Starting
===================

 The main server class is also packed in an executable jar,
 bin/server.jar, and can be ran in a way similar to the deploy tool.

   C:\geronimo> java -Djava.endorsed.dirs=lib\endorsed -javaagent:bin\jpa.jar -jar bin\server.jar

 Once the server has started, you can access the welcome page by
 pointing your browser to:

   http://localhost:8080/
   
 You can also visit the web management console at:
 
   http://localhost:8080/console/
   
 To access the console, use the administrative account, which is
 username "system" and password "manager" by default.
   

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
Support
===================
 
 Any problems with this release can be reported to the Geronimo
 mailing list or Jira issue tracker.

 Mailing list archive:
 http://mail-archives.apache.org/mod_mbox/geronimo-user/

 Mailing list subscription:
 user-subscribe@geronimo.apache.org

 Jira:
 http://issues.apache.org/jira/secure/BrowseProject.jspa?id=10220 
 
 
 ---------------------------------------
 Notice Regarding Cryptographic Software
 =======================================
 This distribution includes cryptographic software.  The country in 
 which you currently reside may have restrictions on the import, 
 possession, use, and/or re-export to another country, of 
 encryption software.  BEFORE using any encryption software, please 
 check your country's laws, regulations and policies concerning the
 import, possession, or use, and re-export of encryption software, to 
 see if this is permitted.  See <http://www.wassenaar.org/> for more
 information.

 The U.S. Government Department of Commerce, Bureau of Industry and
 Security (BIS), has classified this software as Export Commodity 
 Control Number (ECCN) 5D002.C.1, which includes information security
 software using or performing cryptographic functions with asymmetric
 algorithms.  The form and manner of this Apache Software Foundation
 distribution makes it eligible for export under the License Exception
 ENC Technology Software Unrestricted (TSU) exception (see the BIS 
 Export Administration Regulations, Section 740.13) for both object 
 code and source code.

 The following provides more details on the included cryptographic
 software:
   
   Software related to cryptographic functionality is located in 
   an artifcact referred to as geronimo-util.  The source code for
   this functionality can be reviewed at:
   
   http://svn.apache.org/repos/asf/geronimo/server/
   
   The URL above is a reference to the Apache Geronimo Server source
   tree for all development (and released) source trees.
