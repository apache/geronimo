/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.naming.jmx;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.Reference;

import org.apache.geronimo.naming.deployment.RefAdapter;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/06/25 21:33:27 $
 *
 * */
public class JMXReferenceFactory {

    //TODO these names are constructed in a more generic fashion in ConnectorModuleBuilder.
    public static final String BASE_MANAGED_CONNECTION_FACTORY_NAME = "geronimo.server:J2EEServer=geronimo,j2eeType=JCAManagedConnectionFactory,name=";
    public static final String BASE_ADMIN_OBJECT_NAME = "geronimo.server:J2EEServer=geronimo,j2eeType=JCAAdminObject,name=";

    public JMXReferenceFactory() {
    }

    public Reference buildConnectionFactoryReference(RefAdapter refAdapter, Class iface) throws MalformedObjectNameException {
        Reference ref = new Reference(null, JMXObjectFactory.class.getName(), null);
        ref.add(new JMXRefAddr(refAdapter.getServerName(), refAdapter.getKernelName(), ObjectName.getInstance(BASE_MANAGED_CONNECTION_FACTORY_NAME + refAdapter.getTargetName()), iface));
        return ref;
    }

    public Reference buildAdminObjectReference(RefAdapter refAdapter, Class iface) throws MalformedObjectNameException {
        Reference ref = new Reference(null, JMXObjectFactory.class.getName(), null);
        ref.add(new JMXRefAddr(refAdapter.getServerName(), refAdapter.getKernelName(), ObjectName.getInstance(BASE_ADMIN_OBJECT_NAME + refAdapter.getTargetName()), iface));
        return ref;
    }

    //TODO warning: this only works if there is only one kernel!
    public Reference buildMessageDestinationReference(String linkName, Class iface) throws MalformedObjectNameException {
        Reference ref = new Reference(null, JMXObjectFactory.class.getName(), null);
        ref.add(new JMXRefAddr(null, null, ObjectName.getInstance(BASE_ADMIN_OBJECT_NAME + linkName), iface));
        return ref;
    }

    //TODO remotable references should check for externalURI and use a LinkRef if present.
}
