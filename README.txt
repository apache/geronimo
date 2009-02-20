======================================================
Apache Geronimo v2.0  (August 7, 2007)

http://geronimo.apache.org/
------------------------------------------------------


______________________
Documentation
======================
 
 This README serves as five minute overview of Geronimo, but better
 documentation can be found here:
 
   -  http://geronimo.apache.org/documentation.html


______________________
Release Notes
======================

 Please read the RELEASE NOTES for a complete list of features that are
 available in this release, as well as improvements and bug fixes. 
 
  
______________________
Installation
======================

 If you've downloaded and unpacked a binary distribution of Geronimo,
 then you are finished with installation.

 If you wish to modify the default ports that Geronimo will use, edit the file
 <geronimo_home>/var/config/config-substitutions.properties

 Note for Windows platform:
 ==========================
 Windows users keep in mind the directory path length limitation of 255 characters.
 Defaulting installation to predefined directories such as "My Documents" or 
 "Program Files" may cause the installation or the server start up to fail. Try a 
 <geronimo_home> at a root level instead.


______________________
Starting and Stopping
======================

 Geronimo comes with batch and script files to control server start and stop
 functions.  To see usage examples simply type geronimo.bat or geronimo.sh
 command as appropriate for your platform.  It is necessary to set JAVA_HOME 
 to the copy of your Sun 5 JDK/JRE prior to executing the command.

 To see the available command options type:

 <geronimo_home>/bin/geronimo.sh
 or
 <geronimo_home>\bin\geronimo.bat

 The command will display help text instructing you as to how to start and stop
 the Geronimo server.

 If you prefer to start the server without a script file you can simply type
 the following command from <geronimo_home> directory:

 java -Djava.endorsed.dirs=lib/endorsed -javaagent:bin/jpa.jar -jar bin/server.jar

 Once the server has started, you can access the welcome application by
 pointing your browser to:

   http://localhost:8080/
 
 
 To access the Geronimo Administration Console point your browser to:

   http://localhost:8080/console/ 
 
 The default user name is "system" and the default password is "manager".
 
   
______________________
Deploying
======================

 Geronimo comes with deploy scripts and batch files to deploy JEE modules or
 applications. You can either use the scripts or simply invoke the executable
 jar by running the following command:
 
 <geronimo_home>/bin/java -jar deployer.jar deploy my-web-app.war [deploy plan]

 If you use the scripts provided then the command would be as follows:

 <geronimo_home>/bin/deploy deploy my-web-app.war [deploy plan]

 You will need to use the username "system" and password "manager" unless you
 changed the defaults.  

 The deployment plan argument is optional -- you can pack a deployment plan 
 into the application module, provide it on the command line, or in some cases
 omit it entirely.

 For more information on the commands and options supported by the deploy tool,
 run from within the Geronimo directory <geronimo_home>/bin:

 java -jar deployer.jar help [command]

 As an alternative to the command-line deployer, you can copy application  
 modules into the <geronimo_home>/deploy/ directory and the hot deployer 
 service will deploy them automatically.
 
 The command-line deployer has some advantages, as it will output any
 deployment errors to its own console rather than just the server log.

 Additionally you can also graphically deploy applications and resources via 
 the Geronimo Administration Console available at:
 
   http://localhost:8080/console/


______________________
Support
======================
 
 Any problems with this release can be reported to the Geronimo
 mailing list or Jira issue tracker.

 Mailing list archive:
 http://mail-archives.apache.org/mod_mbox/geronimo-user/

 Mailing list subscription:
 user-subscribe@geronimo.apache.org

 Jira:
 http://issues.apache.org/jira/secure/BrowseProject.jspa?id=10220 
 
 Information concerning security issues and reporting security concerns: 
 http://geronimo.apache.org/security-reports.html

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