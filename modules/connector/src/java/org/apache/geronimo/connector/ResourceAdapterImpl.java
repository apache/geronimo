/**
 *
 * Copyright 2003-2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.connector;

import org.apache.geronimo.management.ResourceAdapter;

/**
 * @version $Rev:  $ $Date:  $
 */
public class ResourceAdapterImpl implements ResourceAdapter {
    private final String objectName;
    private final String jcaResource;

    public ResourceAdapterImpl(String objectName, String jcaResource) {
        this.objectName = objectName;
        this.jcaResource = jcaResource;
    }

    public String[] getJCAResources() {
        return new String[] {jcaResource};
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
}
