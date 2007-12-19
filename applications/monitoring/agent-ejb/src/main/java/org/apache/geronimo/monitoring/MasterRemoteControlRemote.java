package org.apache.geronimo.monitoring;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.management.ObjectName;

/**
 * Remote Interface for MasterRemoteControl. Defines the operations
 * that are made available to the remote client.
 */
@Remote
public interface MasterRemoteControlRemote {
    @RolesAllowed("mejbuser")
    public HashMap<String, Long> getStats(String s) throws Exception;
    @RolesAllowed("mejbadmin")
    public void setAttribute(String s, String ss, Object o) throws Exception;
    @RolesAllowed("mejbuser")
    public boolean startSnapshot(Long l);
    @RolesAllowed("mejbuser")
    public boolean stopSnapshot();
    @RolesAllowed("mejbuser")
    public ArrayList<HashMap<String, HashMap<String, Object>>> fetchSnapshotData(Integer i, Integer ii);
    @RolesAllowed("mejbuser")
    public HashMap<String, HashMap<String, Long>> fetchMaxSnapshotData(Integer i);
    @RolesAllowed("mejbuser")
    public HashMap<String, HashMap<String, Long>> fetchMinSnapshotData(Integer i);
    @RolesAllowed("mejbuser")
    public Long getSnapshotDuration();
    @RolesAllowed("mejbuser")
    public Long getSnapshotCount();
    @RolesAllowed("mejbuser")
    public void setSnapshotDuration(Long l);
    @RolesAllowed("mejbuser")
    public Set<String> getStatisticsProviderMBeanNames();
    @RolesAllowed("mejbuser")
    public Set<String> getAllMBeanNames();
    @RolesAllowed("mejbuser")
    public HashMap<String, ArrayList<String>> getAllSnapshotStatAttributes();
    @RolesAllowed("mejbuser")
    public boolean addMBeanForSnapshot(String s);
    @RolesAllowed("mejbuser")
    public boolean removeMBeanForSnapshot(String s);
    @RolesAllowed("mejbuser")
    public boolean isSnapshotRunning();
    @RolesAllowed("mejbuser")
    public void setUpMEJB(String username, String password);
    @RolesAllowed("mejbadmin")
    public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature);
    @RolesAllowed("mejbuser")
    public TreeMap<Long, Long> getSpecificStatistics(String mbeanName, String statsName, int numberOfSnapshots, int everyNthSnapshot, boolean showArchive);
    @RolesAllowed("mejbuser")
    public Set<String> getTrackedMBeans();
    @RolesAllowed("mejbuser")
    public void setSnapshotRetention(int retention);
    @RolesAllowed("mejbuser")
    public String getSnapshotRetention();
}
