#!/bin/sh

#script to build geronimo, tranql, tranql-connector, and openejb in a reasonable order

echo "To deploy use the command line ./buildall.sh [clean] deploy <your apache/codehaus username>"

set -e

MAVEN_REPO=~/.maven/repository

OFFLINE=

TRANQL=tranql/tranql
TRANQLCONNECTOR=tranql/connector
OPENEJB=openejb

if [ "$1" = clean ]; then
  shift
  echo cleaning...
  (cd specs;maven $OFFLINE multiproject:clean)
  (if [ -d $TRANQL ]; then cd $TRANQL ; maven $OFFLINE  clean; fi)
  (if [ -d $TRANQLCONNECTOR ]; then cd $TRANQLCONNECTOR; maven $OFFLINE  clean; fi)
  (cd modules; maven $OFFLINE  multiproject:clean)
  (cd plugins; maven $OFFLINE multiproject:clean)
  (cd applications; maven $OFFLINE multiproject:clean)
  (if [ -d $OPENEJB ]; then cd $OPENEJB/modules; maven $OFFLINE  multiproject:clean; fi)
  (cd modules/assembly; maven $OFFLINE  clean)
  (if [ -d $OPENEJB ]; then cd openejb/modules/assembly; maven $OFFLINE  clean; fi)
  (if [ -d $OPENEJB ]; then cd openejb/modules/itests; maven $OFFLINE clean; fi )
fi

echo updating
svn up
(if [ -d $TRANQL ]; then cd $TRANQL ; cvs -q up -dP; fi)
(if [ -d $TRANQLCONNECTOR ]; then cd $TRANQLCONNECTOR; cvs -q up -dP; fi)
(if [ -d $OPENEJB ]; then cd $OPENEJB; cvs -q up -dP; fi)

echo cleaning local repo
rm -rf  $MAVEN_REPO/geronimo
rm -rf  $MAVEN_REPO/geronimo-spec
if [ -d $TRANQL ]; then rm -rf $MAVEN_REPO/tranql; fi
if [ -d $OPENEJB ]; then rm -rf $MAVEN_REPO/openejb; fi

OFFLINE=-o

echo building
(cd specs;maven $OFFLINE multiproject:install)
(if [ -d $TRANQL ]; then cd $TRANQL ; maven $OFFLINE  jar:install; fi)
(if [ -d $TRANQLCONNECTOR ]; then cd $TRANQLCONNECTOR; maven $OFFLINE  rar:install; fi)
(cd plugins/maven-xmlbeans-plugin; maven -o plugin:install);
(cd modules; maven $OFFLINE  multiproject:install)
(cd plugins; maven $OFFLINE multiproject:install)
(cd applications; maven $OFFLINE multiproject:install)
(if [ -d $OPENEJB ]; then cd $OPENEJB/modules; maven $OFFLINE  multiproject:install; fi)
(cd modules/assembly; maven $OFFLINE  jar:install)
(if [ -d $OPENEJB ]; then cd openejb/modules/assembly; maven $OFFLINE  jar:install; fi)
#(if [ -d $OPENEJB ]; then cd openejb/modules/itests; maven $OFFLINE; fi )

if [ "$1" = deploy ]; then
  echo deploying
  (cd specs;maven -o -Duser.name=$2 multiproject:deploy)
  (if [ -d $TRANQL ]; then cd $TRANQL ; maven -o -Duser.name=$2 jar:deploy; fi)
  (if [ -d $TRANQLCONNECTOR ]; then cd $TRANQLCONNECTOR; maven -o -Duser.name=$2  jar:deploy  rar:deploy; fi)
  (cd modules; maven -o -Duser.name=$2  multiproject:deploy)
  (if [ -d $OPENEJB ]; then cd $OPENEJB/modules; maven -o -Duser.name=$2   multiproject:deploy; fi)
  (cd modules/assembly; maven -o -Duser.name=$2   jar:deploy)
  (if [ -d $OPENEJB ]; then cd openejb/modules/assembly; maven -o -Duser.name=$2   jar:deploy; fi)
fi
