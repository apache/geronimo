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
package org.apache.geronimo.monitoring;

public class MonitorConstants {
    // used in SnapshotDBHelper
    public static final String SNAPSHOT_TIME = "snapshot_time";
    public static final String MBEANNAME = "mbeanName";
    public static final String STATSVALUELIST = "statsValueList";
    public static final String STATSNAMELIST = "statsNameList";

    // used in MasterRemoteControl classes
    public static final String GERONIMO_DEFAULT_DOMAIN = "geronimo";
    public static final Long DEFAULT_DURATION = new Long(300000);
    public static final int DEFAULT_RETENTION = 30;

    // used in SnapshotConfigXMLBuilder and MasterRemoteControl classes
    public static final String RETENTION = "retention";
    public static final String DURATION = "duration";
    public static final String STARTED = "started";

}
