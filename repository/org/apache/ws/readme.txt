Private Build of Axiom 1.2.5 for Geronimo.   

How to build Axiom 1.2.5
---------------------------------
 Checkout the Axiom 1.2.5 tag
   svn co http://svn.apache.org/repos/asf/webservices/commons/tags/axiom/1_2_5
  

Apply the patch
-----------------
 cd 1_2_5
 patch -p0 -i axiom_api.patch

Build Axiom 1.2.5
---------------
 cd 1_2_5
 mvn install

Notes:
  - Use Sun 1.5.x and Maven 2.0.9 build.


Patch Information
-----------------
  axiom_api.patch  - contains fixes for AXIS2-4450

Copy patched jar files to appropriate locations
-----------------------------------------------
  cd 1_2_5
  cp modules/axiom-api/target/axiom-api-1.2.5.jar <geronimo-root>/repository/org/apache/ws/commons/axiom/axiom-api/1.2.5/axiom-api-1.2.5.jar