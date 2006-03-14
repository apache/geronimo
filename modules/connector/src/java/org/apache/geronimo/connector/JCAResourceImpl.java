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

import org.apache.geronimo.management.geronimo.JCAResource;
import org.apache.geronimo.management.geronimo.JCAResourceAdapter;
import org.apache.geronimo.connector.outbound.JCAConnectionFactoryImpl;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @version $Rev$ $Date$
 */
public class JCAResourceImpl implements JCAResource  {
    private final String objectName;

    private final Collection connectionFactories;
    private final Collection resourceAdapters;

    public JCAResourceImpl(String objectName, Collection connectionFactories, Collection resourceAdapters) {
        this.objectName = objectName;
        this.connectionFactories = connectionFactories;
        this.resourceAdapters = resourceAdapters;
    }

    public String[] getConnectionFactories() {
        Collection copy;
        synchronized(connectionFactories) {
            copy = new ArrayList(connectionFactories);
        }
        String[] result = new String[copy.size()];
        int i = 0;
        for (Iterator iterator = copy.iterator(); iterator.hasNext();) {
            JCAConnectionFactoryImpl jcaConnectionFactory = (JCAConnectionFactoryImpl) iterator.next();
            result[i++] = jcaConnectionFactory.getObjectName();
        }
        return result;
    }

    public String[] getResourceAdapterInstances() {
        Collection copy;
        synchronized(resourceAdapters) {
            copy = new ArrayList(resourceAdapters);
        }
        String[] result = new String[copy.size()];
        int i = 0;
        for (Iterator iterator = copy.iterator(); iterator.hasNext();) {
            JCAResourceAdapter resourceAdapter = (JCAResourceAdapter) iterator.next();
            result[i++] = resourceAdapter.getObjectName();
        }
        return result;
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
