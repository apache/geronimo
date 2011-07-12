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

package org.apache.geronimo.monitoring.ejb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.management.ObjectName;

/**
 * Local Interface for MasterRemoteControl. Defines the operations
 * that are made available to the remote client.
 */
@Local
public interface MasterRemoteControlLocal {
    public HashMap<String, Long> getStats(String s) throws Exception;
    public void setAttribute(String s, String ss, Object o) throws Exception;
    public boolean startSnapshot(Long l);
    public boolean stopSnapshot();
    public ArrayList<HashMap<String, HashMap<String, Object>>> fetchSnapshotData(Integer i, Integer ii);
    public HashMap<String, HashMap<String, Long>> fetchMaxSnapshotData(Integer i);
    public HashMap<String, HashMap<String, Long>> fetchMinSnapshotData(Integer i);
    public Long getSnapshotDuration();
    public Long getSnapshotCount();
    public void setSnapshotDuration(Long l);
    public Set<String> getStatisticsProviderMBeanNames();
    public Set<String> getAllMBeanNames();
    public HashMap<String, ArrayList<String>> getAllSnapshotStatAttributes();
    public boolean addMBeanForSnapshot(String s);
    public boolean removeMBeanForSnapshot(String s);
    public boolean isSnapshotRunning();
    public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature);
    public TreeMap<Long, Long> getSpecificStatistics(String mbeanName, String statsName, int numberOfSnapshots, int everyNthSnapshot, boolean showArchive);
    public Set<String> getTrackedMBeans();
    public void setSnapshotRetention(int retention);
    public String getSnapshotRetention();
}
