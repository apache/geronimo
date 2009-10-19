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
package org.apache.geronimo.connector.wrapper;

import org.apache.geronimo.management.geronimo.JCAResource;
import org.apache.geronimo.management.geronimo.ResourceAdapter;

/**
 * @version $Rev$ $Date$
 */
public class ResourceAdapterImpl implements ResourceAdapter {
    private final String objectName;
    private final JCAResource jcaResource;

    public ResourceAdapterImpl(String objectName, JCAResource jcaResource) {
        this.objectName = objectName;
        this.jcaResource = jcaResource;
    }

    public String[] getJCAResources() {
        return new String[] {jcaResource.getObjectName()};
    }

    public String getObjectName() {
        return objectName;
    }

    public boolean isStateManageable() {
        return false;
    }

    public boolean isStatisticsProvider() {
        return false;
    }

    public boolean isEventProvider() {
        return false;
    }

    public JCAResource[] getJCAResourceImplementations() {
        return new JCAResource[] {jcaResource};
    }
}
