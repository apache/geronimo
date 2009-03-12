Private Build of Axis2 1.3 for Geronimo.   

How to build Axis2 1.3-G20090310:
---------------------------------
 Checkout the Axis2 1.3 tag
   svn co http://svn.apache.org/repos/asf/webservices/axis2/tags/java/v1.3/ axis2-1.3
  

Apply the patches
-----------------
 cd axis2-1.3
 patch -p0 -i build.patch
 patch -p0 -i saaj.patch
 patch -p0 -i metadata.patch
 patch -p0 -i jaxws.patch


Build Axis2 1.3
---------------
 cd axis2-1.3
 mvn install

Notes:
  - Use Sun 1.5.x and Maven 2.0.9 build.


Patch Information
-----------------

 build.patch    - disables broken tests
 saaj.patch     - contains fixes for AXIS2-3808, AXIS2-4027, and AXIS2-4266
 metadata.patch - contains fixes for SEI with overloaded methods
 jaxws.patch    - contains fixes for AXIS2-3343 and RESTful invocations


Copy patched jar files to appropriate locations
-----------------------------------------------
  cd axis2-1.3
  cp modules/saaj/target/axis2-saaj-1.3.jar <geronimo-root>/repository/org/apache/axis2/axis2-saaj/1.3-G20090310/axis2-saaj-1.3-G20090310.jar
  cp modules/metadata/target/axis2-metadata-1.3.jar <geronimo-root>/repository/org/apache/axis2/axis2-metadata/1.3-G20090310/axis2-metadata-1.3-G20090310.jar
  cp modules/jaxws/target/axis2-jaxws-1.3.jar <geronimo-root>/repository/org/apache/axis2/axis2-jaxws/1.3-G20090310/axis2-jaxws-1.3-G20090310.jar
