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
}
