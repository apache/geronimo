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
package org.apache.geronimo.system.plugin;

import org.apache.geronimo.kernel.repository.Version;

/**
 * SnapshotVersion is like Version but holds extra fields that appear in the
 * filename of a snapshot artifact. The toString() method is not overriden
 * because the super implementation produces the correct string for navigating
 * the directory structure of a plugin repository. The extra fields maintained
 * in this class are needed for constructing the filename portion of a URL for a
 * snapshot artifact where the qualifier and build number are replaced with a
 * snapshot timestamp and build number.
 * 
 * @version $Revision$ $Date$
 * 
 */
public class SnapshotVersion extends Version {
    private static final long serialVersionUID = -4165276456639945508L;

    private Integer buildNumber;

    private String timestamp;

    public SnapshotVersion(Version version) {
        super(version.toString());
    }

    public SnapshotVersion(String version) {
        super(version);
    }

    public int getBuildNumber() {
        return buildNumber != null ? buildNumber.intValue() : 0;
    }

    public void setBuildNumber(int buildNumber) {
        this.buildNumber = new Integer(buildNumber);
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean equals(Object other) {
        if (super.equals(other)) {
            if (other instanceof SnapshotVersion) {
                SnapshotVersion v = (SnapshotVersion) other;
                if (buildNumber == null ? v.buildNumber != null : !buildNumber.equals(v.buildNumber)) {
                    return false;
                }
                if (timestamp == null ? v.timestamp != null : !timestamp.equals(v.timestamp)) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        int hashCode = super.hashCode();
        if (buildNumber != null) {
            hashCode = 37 * hashCode + buildNumber.hashCode();
        }
        if (timestamp != null) {
            hashCode = 37 * hashCode + timestamp.hashCode();
        }
        return hashCode;
    }
}
