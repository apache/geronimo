/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.monitoring.jmx.snapshot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import javax.management.MBeanServer;

import org.apache.geronimo.monitoring.jmx.MasterRemoteControlJMX;
import org.apache.geronimo.monitoring.snapshot.SnapshotConfigXMLBuilder;

/**
 * Thread that is in charge of executing every x milliseconds. Upon each
 * iteration, a snapshot of the server's information is recorded.
 */
public class SnapshotThread extends Thread {

    volatile private long SNAPSHOT_DURATION;
    private MBeanServer mbServer = null;
    int threadStatus = 1;
    // list of mbean names that we will be taking snapshots of
    private ArrayList<String> mbeanNames;

    public SnapshotThread(long snapshot_length, MBeanServer mbServer) {
        SNAPSHOT_DURATION = snapshot_length;
        this.mbServer = mbServer;
        mbeanNames = new ArrayList<String>();
    }

    /**
     * Gets the elapsed time in milliseconds between each snapshot.
     * 
     * @return long
     */
    public long getSnapshotDuration() {
        return SNAPSHOT_DURATION;
    }

    public Integer SnapshotStatus() {
        return threadStatus;

    }

    /**
     * Adds the mbean name to list in memory. To update the snapshot-config.xml
     * coder must use SnapshotConfigXMLBuilder class.
     * 
     * @param mbeanName
     */
    public void addMBeanForSnapshot(String mbeanName) {
        mbeanNames.add(mbeanName);
    }

    /**
     * Removes the mbean name to list in memory. To update the
     * snapshot-config.xml coder must use SnapshotConfigXMLBuilder class.
     * 
     * @param mbeanName
     */
    public void removeMBeanForSnapshot(String mbeanName) {
        mbeanNames.remove(mbeanName);
    }

    /**
     * Sets the elapsed time in milliseconds between each snapshot.
     * 
     * @param snapshot_length
     */
    public void setSnapshotDuration(long snapshot_length) {
        if (snapshot_length == Long.MAX_VALUE)
            threadStatus = -1;
        SNAPSHOT_DURATION = snapshot_length;
    }

    public void run() {
        // get any saved mbean names from snapshot-config.xml
        mbeanNames = SnapshotConfigXMLBuilder.getMBeanNames();
        // in the case where nothing is present, grab a set of default mbeans
        if (mbeanNames.size() <= 0) {
            mbeanNames = getDefaultMBeanList();
        }
        // pause the thread from running every SNAPSHOT_DURATION seconds
        loop: while (true) {
            try {
                // store the data
                SnapshotProcessor.takeSnapshot();
                // wait for next snapshot
                for(long nSleep = SNAPSHOT_DURATION/1000; nSleep>0; nSleep--)
                {
                   Thread.sleep(1000);
                   if(SNAPSHOT_DURATION == Long.MAX_VALUE)
                       break loop;
                }
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // flag turned on to know when the thread stops
        threadStatus = 0;
    }

    /**
     * @return A list of all default mbeans; namely, all connector or container
     *         mbean names Prereq: in order to be a connector or container mbean
     *         the name must contain "Connector"/"Container" and
     *         "Tomcat"/"Jetty"
     */
    private ArrayList<String> getDefaultMBeanList() {
        Set<String> mbeans = (new MasterRemoteControlJMX())
                .getStatisticsProviderMBeanNames();
        ArrayList<String> retval = new ArrayList<String>();
        for (Iterator it = mbeans.iterator(); it.hasNext();) {
            String name = (String) it.next();
            if (((name.contains("Connector") || name.contains("Container"))
                    && (name.contains("Jetty") || name.contains("Tomcat"))|| name.contains("JVM"))) {
                // this is a connector, so add to the list
                retval.add(name);
                // update the snapshot-config.xml to include these
                SnapshotConfigXMLBuilder.addMBeanName(name);
            }
        }
        return retval;
    }
}
