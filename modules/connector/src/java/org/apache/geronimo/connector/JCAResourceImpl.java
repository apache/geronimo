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

import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.impl.Util;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.management.JCAResource;

/**
 * @version $Rev:  $ $Date:  $
 */
public class JCAResourceImpl implements JCAResource  {
    private final Kernel kernel;
    private final String objectName;
    private final J2eeContext moduleContext;

    private static final String[] CONNECTION_FACTORY_TYPES = {NameFactory.JCA_CONNECTION_FACTORY};
    private static final String[] RESOURCE_ADAPTER_INSTANCE_TYPES = {NameFactory.JCA_RESOURCE_ADAPTER};

    public JCAResourceImpl(String objectName, Kernel kernel) {
        this.objectName = objectName;
        this.kernel = kernel;

        ObjectName myObjectName = JMXUtil.getObjectName(objectName);
        moduleContext = J2eeContextImpl.newContext(myObjectName, NameFactory.JCA_RESOURCE);
    }

    public String[] getConnectionFactories() {
        try {
            return Util.getObjectNames(kernel, moduleContext, CONNECTION_FACTORY_TYPES);
        } catch (MalformedObjectNameException e) {
            throw new AssertionError();
        }
    }

    public String[] getResourceAdapterInstances() {
        try {
            return Util.getObjectNames(kernel, moduleContext, RESOURCE_ADAPTER_INSTANCE_TYPES);
        } catch (MalformedObjectNameException e) {
            throw new AssertionError();
        }
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
