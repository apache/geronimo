Private Build of commons-httpclient  for Geronimo.   

How to build commons-httpclient 3.0.1-G20111008:
---------------------------------
 Checkout the commons-httpclient 3.0.1 tag
   svn co http://svn.apache.org/repos/asf/httpcomponents/oac.hc3x/tags/HTTPCLIENT_3_0_1/
  

Apply the patches
-----------------
 cd HTTPCLIENT_3_0_1
 patch -p0 -i HTTPCLIENT-644.patch

Build commons-httpclient 3.0.1-G20111008
---------------
 cd HTTPCLIENT_3_0_1
 ant dist

Notes:
  - Use Sun 1.5.x and Maven 2.0.9 build.


Patch Information
-----------------

 HTTPCLIENT-644.patch    - Support to configure max header count and max line length property
                                             To make the life easier, and avoid updating those client codes (e.g. Axis2 1.3 etc.). These two options could be configured while starting the server,
                                             -Dhttp.connection.max-line-length=100 -Dhttp.connection.max-header-count=10

Copy patched jar files to appropriate locations
-----------------------------------------------
  cd HTTPCLIENT_3_0_1
  cp dist/commons-httpclient.jar <geronimo-root>/repository/commons-httpclient/commons-httpclient/3.0.1-G20111008/commons-httpclient-3.0.1-G20111008.jar
