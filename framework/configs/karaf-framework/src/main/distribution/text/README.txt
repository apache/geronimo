======================================================
Apache Geronimo v3.0-M1

http://geronimo.apache.org/
------------------------------------------------------


______________________
Release Notes
======================

 Please read the RELEASE_NOTES-3.0-M1.txt for a complete list of new features 
 available in this release. 
 
  
______________________
Documentation
======================
 
 This README serves as quick introduction to Geronimo. More complete documentation 
 can be found here:
 
   -  http://cwiki.apache.org/GMOxDOC30/documentation.html
   OR
   -  http://cwiki.apache.org/GMOxDOC21/documentation.html
   (Geronimo 2.2 documentation until Geronimo 3.0-M1 documentation is created)


______________________
Installing Geronimo
======================

 To install Geronimo, simply unpack the .zip (Windows) or tar.gz (Unix) file containing
 Geronimo.

 Note for Windows platforms:
 The maximum path length for a directory path is 255 characters. Installing
 Geronimo into a long directory path may cause the installation or server start up to fail. 
 It is recommended that you use a short directory name, such as "c:\g\".


______________________
Starting Geronimo 
======================

 To start Geronimo in foreground type (assuming the current directory is <geronimo_home>):

   ./bin/geronimo run

 To start Geronimo in background type (assuming the current directory is <geronimo_home>):

   ./bin/geronimo start

Additional information on command environments can be found below.


______________________
Application Deployment
======================

 Applications can be deployed to a Geronimo server in several ways:

 1. Administrative command scripts -- "./bin/deploy deploy MyApp.war MyDeploymentPlan.xml"
 2. Administrative console -- login to the admin console and click "Deployer" under Applications
 3. Hot deploy -- copy your archive(s) into the "<geronimo-home>/deploy" directory. The
    hot deploy service will automatically deploy these artifacts.
 4. Maven -- applications can be installed as part of a maven build

 When you deploy an application using an administrative command, you will need
 to supply an administrator's username/password. If you do not specify the username
 and password, you will be prompted for them.


______________________
Configuration
======================

 Commonly modified configuration parameters, such as port numbers, can be set by 
 editing the file:

 <geronimo_home>/var/config/config-substitutions.properties
 
 Additional configuration attributes can be updated in the file:

 <geronimo_home>/var/config/config.xml

 Note: The server must not be running when these files are modified.

 Once the server has started, you can access the Geronimo Administration Console
 at http://localhost:8080/console/ . The default user name is "system" and the
 default password is "manager".

 
______________________
Security Configuration
======================

 The default administration user/password for the Geronimo Administration Console
 and command line deployment tool is system/manager.  You can change these defaults
 directly from the Geronimo Administration Console by accessing Security -> Users 
 and Groups and change the user name and password from the Console Realm Users portlet.

 As an alternative, you can make the same changes by editing the
 <geronimo_home>/var/security/users.properties and
 <geronimo_home>/var/security/groups.properties files.

 Access to the var/security directory should be appropriately secured on systems where
 you install Geronimo.

 Passwords in users.properties are encrypted by the server. Passwords can be changed 
 using the Geronimo Administration Console. They can also be changed using a text
 editor. While the server is stopped, simply edit the users.properties file. The 
 password(s) will be encrypted the next time the server is started. 

 To prevent potential security exposures, we strongly recommend you update the 
 default user names and passwords on your system.

______________________
Script
======================

 Geronimo provides a number of shell or batch scripts that can be used to administer Geronimo
 servers. To use most of these scripts, you must first set either the JAVA_HOME 
 or JRE_HOME environment variable:

   export JAVA_HOME=<path-to-JDK>
  or
   export JRE_HOME=<path-to-JRE>

 Unix scripts provided by Geronimo (there are .bat equivalents):

   geronimo -- used to start and stop servers; either as a foreground or background process.
   startup -- start a Geronimo server running as a background process
   shutdown -- stop a running Geronimo server
   client -- start a Geronimo application client
   deploy -- deploy, list, and undeploy plugins and applications

 For example, "./bin/deploy list-modules"

 The 'geronimo' and 'shutdown' scripts have multiple sub-commands. Executing the scripts without
 any arguments will generate usage information. Executing "help <sub-command>" will generate
 usage information for that particular command.


______________________
Support
======================
 
 Any problems with this release can be reported to the Geronimo user mailing list 
 or Jira issue tracker.

 Mailing list archive:
 http://mail-archives.apache.org/mod_mbox/geronimo-user/

 User mailing list:
 user@geronimo.apache.org

 User mailing list subscription:
 user-subscribe@geronimo.apache.org

 Jira:
 https://issues.apache.org/jira/browse/GERONIMO

 Information concerning security issues and reporting security concerns: 
 http://geronimo.apache.org/security-reports.html

 _______________________________________
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
