@20111216 Fix for https://issues.apache.org/jira/browse/GERONIMO-5991

1. Check out myfaces 1.2.8 tag code
 svn co https://svn.apache.org/repos/asf/myfaces/core/tags/1_2_8
 
2. Merge the fix of MYFACES-3166 on 1.2.x branch to the local code
 svn merge -r 1136611:1136612 https://svn.apache.org/repos/asf/myfaces/core/branches/1.2.x .
 
3. Add Geronimo unique version to myfaces pom version and build the jars
 myfaces-api-1.2.8-G2.1.8.jar
 myfaces-impl-1.2.8-G2.1.8.jar
 
4. Check in the fixed myfaces jars into 2.1 repository project

5. Update 2.1 branch root pom and repository pom
