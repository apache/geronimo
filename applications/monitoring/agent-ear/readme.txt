HOW TO COMPILE AND DEPLOY
    1. run `mvn clean install`
    2. Deploy the JAR file present in the MRC-ejb/target folder along with 
       MRC.xml present at the home directory into Geronimo.
       2a. Navigate to <GERONIMO_HOME>/bin/ directory
       2b. Give it this command

          java -jar ./deployer.jar -u system -p manager deploy <path-to-jar> <path-to-xml>


**The snapshot information for the server will reside under the folder
    <GERONIMO_HOME>/var/
