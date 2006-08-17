#!/bin/sh

DIRNAME=`dirname $0`
for x in `cat $DIRNAME/all_changes.log | egrep '^Not Merged' | awk -F, '{ print $2 }'`; do
    rev=`echo $x | cut -c2-`
    svn log -vr $rev http://svn.apache.org/repos/asf/geronimo/
done