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
 * @version $Revision: 1.1 $ $Date: 2004/03/09 18:03:11 $
 *
 * */
public class JMXReferenceFactory {

    public static final String BASE_MANAGED_CONNECTION_FACTORY_NAME = "geronimo.management:J2eeType=ManagedConnectionFactory,name=";
    public static final String BASE_ADMIN_OBJECT_NAME = "geronimo.management:service=AdminObject,name=";

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

    //TODO remotable references should check for externalURI and use a LinkRef if present.
}
