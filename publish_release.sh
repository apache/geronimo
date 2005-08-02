#!/bin/bash
#
#  Copyright 2005 The Apache Software Foundation
# 
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
# 
#      http://www.apache.org/licenses/LICENSE-2.0
# 
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.

# --------------------------------------------------------------------
# $Rev$ $Date$
# --------------------------------------------------------------------

## This script is not supported and is here for convenience

#  If we need to SSH up to the server where the builds are published
#  These three varaibles will be used to find the host 
REMOTE_HOST='apache.org'

#  This is where we should place our release builds till we have voted
#  to accept them
RELEASE_DIR='/www/cvs.apache.org/dist/geronimo/unstable'

#  The public repo url for geronimo
SVN_URL="http://svn.apache.org/repos/asf/geronimo/tags/v1_0_M4"

#  Example value: 1.0-123456
VERSION="1.0-M4"

#  Example value: geronimo-1.0-123456
RELEASE_ID="geronimo-$VERSION"

#  Example value: /www/cvs.apache.org/dist/geronimo/unstable/geronimo-1.0-123456
VERSION_DIR="$RELEASE_DIR/$VERSION"

#  Make the DIST directory if it isn't present
DIST=$PWD/dist
test -d $DIST || mkdir $DIST

echo "$RELEASE_ID"
    
### Utility functions ########
function shash { openssl $1 < $2 > $2.$1 ;}
function fail () { echo $1 >&2; exit 1;}
function package () { 
    DEST=$1; SOURCE=$2
    tar czf $DEST.tar.gz $SOURCE
    zip -9rq $DEST.zip $SOURCE
}
function publish_build_archives {

    #  We want to checkout Geronimo into a directory that will be named
    #  just right to be the source directory, then we can just zip and tar
    #  it up before we build it.
    #
    #  The directory will be named geronimo-1.0-SVN_REVISION_NUMBER
    svn checkout $SVN_URL $RELEASE_ID
    
    #  The .svn directories contain a copy of all the files, so we should 
    #  delete them our or source zip and tar files will be twice as big 
    #  as they need to be.
    find $RELEASE_ID -name '.svn' -exec rm -rf {} \;

    #  Now let's create the source zip and tar before we build while we
    #  still have a completely clean checkout with no target directories,
    #  velocity.log files and other junk created during a build.
    package $DIST/${RELEASE_ID}-src $RELEASE_ID || fail "Unable to create source binaries"

    #  Let's go ahead and run the build to create the geronimo-foo-1.0-SVN_REVISION.jar files
    #  We don't run the tests as this is not a script for testing and reporting those test results.
    #  If the build fails to compile, the 'fail' function is called and this script will exit
    #  and nothing will be published.
    ( cd $RELEASE_ID && maven -o -Dmaven.{itest,test}.skip=true ) || fail "Build failed"

    #  During the assembly module a directory called geronimo-1.0-SVN_REVISION was created.  Let's 
    #  move in to that directory and create a geronimo-1.0-SVN_REVISION.zip and a tar.gz of the same name.
    #  When unpacked by users, these archives will extract into a directory called geronimo-1.0-SVN_REVISION/
    ( cd $RELEASE_ID/modules/assembly/target && package $DIST/${RELEASE_ID} $RELEASE_ID ) || fail "Unable to make binary archives"

    #  Let's create checksums for our source and binary tars and zips.
    for archive in $DIST/*.{zip,tar.gz}; do
	echo $archive
	shash md5 $archive
        shash sha $archive
    done || fail "Unable to sign or hash release archives"

    #  Now we want to create a directory where we will put the archives and checksums up for download.
    #  Here we setup some variables for use.  The VERSION_DIR will typically look like:
    #     /www/cvs.apache.org/dist/geronimo/unstable/1.0-SVN_REVISION/
    #  This is the directory on apache.org where non-release builds a placed.
    VERSION_DIR=$RELEASE_DIR/$VERSION

    #  At this point we are mostly done, we just need to make our release directory and copy the files.
    #  We have to do this remotely over ssh.
    echo "Making dir $VERSION_DIR"
    SSH_URL=$REMOTE_HOST
    (ssh $SSH_URL  mkdir $VERSION_DIR ) || fail "Unable to create the release dir $VERSION_DIR"
    (scp $DIST/${RELEASE_ID}* $SSH_URL:$VERSION_DIR) || fail "Unable to upload the binaries to release dir $VERSION_DIR"

    #  clean up locally
    echo rm -r $DIST/* $RELEASE_ID
}

publish_build_archives
