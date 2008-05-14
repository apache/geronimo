Private Build of Pluto for Geronimo.										       

How to build Pluto 1.1.6 (w/ locally applied PLUTO-437, PLUTO-484 and PLUTO-485 patches) for Geronimo -

Checkout pluto 1.1.6
  svn co -r 643117 https://svn.apache.org/repos/asf/portals/pluto/branches/pluto-1.1.x pluto

svn info for Pluto image:
 Path: .
 URL: http://svn.apache.org/repos/asf/portals/pluto/branches/pluto-1.1.x
 Repository Root: http://svn.apache.org/repos/asf
 Repository UUID: 13f79535-47bb-0310-9956-ffa450edef68
 Revision: 652071
 Node Kind: directory
 Schedule: normal
 Last Changed Author: esm
 Last Changed Rev: 643117
 Last Changed Date: 2008-03-31 15:57:01 -0400 (Mon, 31 Mar 2008)

Apply the following patches to the source -
  PLUTO-437 - Allow icons in navigation pane
  PLUTO-484 - Fix HTTPS problems
  PLUTO0485 - Fix to handle URIs that contain a question mark

Build pluto
  cd pluto
  mvn install

Copy to appropriate jars and rename into geronimo/repository
  cd pluto
  cp pluto-container/target/pluto-container-1.1.6-SNAPSHOT.jar                    <geronimo_root>/repository/org/apache/pluto/pluto-container/1.1.6-G643117/pluto-container-1.1.6-G643117.jar
  cp pluto-descriptor-api/target/pluto-descriptor-api-1.1.6-SNAPSHOT.jar          $geronimo_root/repository/org/apache/pluto/pluto-descriptor-api/1.1.6-G643117/pluto-descriptor-api-1.1.6-G643117.jar
  cp pluto-descriptor-impl/target/pluto-descriptor-impl-1.1.6-SNAPSHOT.jar        $geronimo_root/repository/org/apache/pluto/pluto-descriptor-impl/1.1.6-G643117/pluto-descriptor-impl-1.1.6-G643117.jar
  cp pluto-portal-driver/target/pluto-portal-driver-1.1.6-SNAPSHOT.jar            $geronimo_root/repository/org/apache/pluto/pluto-portal-driver/1.1.6-G643117/pluto-portal-driver-1.1.6-G643117.jar
  cp pluto-portal-driver-impl/target/pluto-portal-driver-impl-1.1.6-SNAPSHOT.jar  $geronimo_root/repository/org/apache/pluto/pluto-portal-driver-impl/1.1.6-G643117/pluto-portal-driver-impl-1.1.6-G643117.jar
  cp pluto-taglib/target/pluto-taglib-1.1.6-SNAPSHOT.jar                          $geronimo_root/repository//org/apache/pluto/pluto-taglib/1.1.6-G643117/pluto-taglib-1.1.6-G643117.jar

