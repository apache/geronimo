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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.geronimo.j2ee.management.impl.Util;
import org.apache.geronimo.management.geronimo.JCAAdminObject;
import org.apache.geronimo.management.geronimo.JCAConnectionFactory;
import org.apache.geronimo.management.geronimo.JCAManagedConnectionFactory;
import org.apache.geronimo.management.geronimo.JCAResource;
import org.apache.geronimo.management.geronimo.JCAResourceAdapter;

/**
 * @version $Rev$ $Date$
 */
public class JCAResourceImpl implements JCAResource  {
    private final String objectName;

    private final Collection connectionFactories;
    private final Collection resourceAdapters;
    private final Collection adminObjects;

    public JCAResourceImpl(String objectName, Collection connectionFactories, Collection resourceAdapters, Collection adminObjects) {
        this.objectName = objectName;
        this.connectionFactories = connectionFactories;
        this.resourceAdapters = resourceAdapters;
        this.adminObjects = adminObjects;
    }

    public String[] getConnectionFactories() {
        return Util.getObjectNames(getConnectionFactoryInstances());
    }

    public String[] getResourceAdapterInstanceNames() {
        ArrayList temp = new ArrayList();
        for (Iterator iterator = resourceAdapters.iterator(); iterator.hasNext();) {
            JCAResourceAdapter resourceAdapter = (JCAResourceAdapter) iterator.next();
            temp.add(resourceAdapter.getObjectName());
        }
        return (String[])temp.toArray(new String[temp.size()]);
    }

    public JCAResourceAdapter[] getResourceAdapterInstances() {
        return (JCAResourceAdapter[])resourceAdapters.toArray(new JCAResourceAdapter[resourceAdapters.size()]);
    }

    public JCAConnectionFactory[] getConnectionFactoryInstances() {
        return (JCAConnectionFactory[])connectionFactories.toArray(new JCAConnectionFactory[connectionFactories.size()]);
    }

    public JCAManagedConnectionFactory[] getOutboundFactories() {
        return getOutboundFactories((String[]) null);
    }

    public JCAManagedConnectionFactory[] getOutboundFactories(String connectionFactoryInterface) {
        return getOutboundFactories(new String[]{connectionFactoryInterface});
    }

    public JCAManagedConnectionFactory[] getOutboundFactories(String[] connectionFactoryInterfaces) {
        Set interfaceFilter = null;
        if (connectionFactoryInterfaces != null) {
            interfaceFilter = new HashSet(Arrays.asList(connectionFactoryInterfaces));
        }

        List list = new ArrayList();
        for (Iterator iterator = connectionFactories.iterator(); iterator.hasNext();) {
            JCAConnectionFactory jcaConnectionFactory = (JCAConnectionFactory) iterator.next();
            JCAManagedConnectionFactory mcf = jcaConnectionFactory.getManagedConnectionFactoryInstance();
            if (interfaceFilter == null || interfaceFilter.contains(mcf.getConnectionFactoryInterface())) {
                list.add(mcf);
                continue;
            }
            for (int m = 0; m < mcf.getImplementedInterfaces().length; m++) {
                String iface = mcf.getImplementedInterfaces()[m];
                if (interfaceFilter == null || interfaceFilter.contains(iface)) {
                    list.add(mcf);
                    break;
                }
            }
        }
        return (JCAManagedConnectionFactory[]) list.toArray(new JCAManagedConnectionFactory[list.size()]);
    }

    public String[] getAdminObjects() {
        return Util.getObjectNames(getAdminObjectInstances());
    }

    public JCAAdminObject[] getAdminObjectInstances() {
        return (JCAAdminObject[]) adminObjects.toArray(new JCAAdminObject[adminObjects.size()]);
    }

    public JCAAdminObject[] getAdminObjectInstances(String adminObjectInterface) {
        return getAdminObjectInstances(new String[] {adminObjectInterface});
    }

    public JCAAdminObject[] getAdminObjectInstances(String[] adminObjectInterfaces) {
        Set interfaceFilter = null;
        if (adminObjectInterfaces != null) {
            interfaceFilter = new HashSet(Arrays.asList(adminObjectInterfaces));
        }

        List list = new ArrayList();

        for (Iterator iterator = adminObjects.iterator(); iterator.hasNext();) {
            JCAAdminObject adminObject = (JCAAdminObject) iterator.next();
            if (interfaceFilter == null || interfaceFilter.contains(adminObject.getAdminObjectInterface())) {
                list.add(adminObject);
            }
        }

        return (JCAAdminObject[]) list.toArray(new JCAAdminObject[list.size()]);
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
