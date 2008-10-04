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

package org.apache.geronimo.console.jmsmanager;

import javax.jms.Queue;

public class DestinationInfo implements Comparable {

    private final String name;

    private final String physicalName;

    private final Class type;

    private final String applicationName;

    private final String moduleName;

    private final String configURI;

    public DestinationInfo(String name, String physicalName, Class type,
            String applicationName, String moduleName, String configURI) {
        this.name = name;
        this.physicalName = physicalName;
        this.type = type;
        this.applicationName = applicationName;
        this.moduleName = moduleName;
        this.configURI = configURI;
    }

    public String getName() {
        return name;
    }

    public String getPhysicalName() {
        return physicalName;
    }

    public Class getType() {
        return type;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getConfigURI() {
        return configURI;
    }

    /**
     * Determines if the destination that this objects represents is viewable
     * from the console this means that either the destination that this object
     * represents was added via the Geronimo JMS Console or it is a Queue.
     *
     * @return true if the console knows how to display this Destination
     *         otherwise returns false.
     */
    public boolean isViewable() {
        return Queue.class.isAssignableFrom(type) || isConsoleManaged();
    }

    /**
     * Determines if the destination that this objects represents is removable
     * from the console this means that the destination that this object
     * represents was added via the Geronimo JMS Console.
     *
     * @return true if the console knows how to remove this Destination
     *         otherwise returns false.
     */
    public boolean isRemovable() {
        return isConsoleManaged();
    }

    /**
     * Determines if the destination that this objects represents was added via
     * the console.
     *
     * @return true if the destination that this objects represents was added
     *         via the console otherwise returns false.
     */
    private boolean isConsoleManaged() {
        return configURI.indexOf(AbstractJMSManager.BASE_CONFIG_URI + name) > -1;
    }

    public int compareTo(Object o) {
        if (o instanceof DestinationInfo) {
            DestinationInfo rhs = (DestinationInfo) o;
            // If one of the objects is removable and the other is not, the
            // removable one is less (comes first in a descending sort).
            if (rhs.isRemovable() != this.isRemovable()) {
                if (this.isRemovable()) {
                    return -1;
                } else {
                    return 1;
                }
                // If one of the objects is viewable and the other is not the
                // viewable one is less (comes first in a descending sort).
            } else if (rhs.isViewable() != this.isViewable()) {
                if (this.isViewable()) {
                    return -1;
                } else {
                    return 1;
                }
                // Sort by name if all else fails.
            } else {
                return this.name.compareTo(rhs.getName());
            }
        }
        return 1;
    }

}
