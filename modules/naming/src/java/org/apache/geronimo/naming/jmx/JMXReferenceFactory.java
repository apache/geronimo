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

import org.apache.geronimo.naming.ReferenceFactory;
import org.apache.geronimo.xbeans.geronimo.naming.GerLocalRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerRemoteRefType;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class JMXReferenceFactory implements ReferenceFactory {

    //TODO these names are constructed in a more generic fashion in ConnectorModuleBuilder.
    public static final String BASE_MANAGED_CONNECTION_FACTORY_NAME = ",j2eeType=JCAManagedConnectionFactory,name=";
    public static final String BASE_ADMIN_OBJECT_NAME = ",j2eeType=JCAAdminObject,name=";

    private final String baseName;

    public JMXReferenceFactory(String domainName, String serverName) {
        baseName = domainName + ":J2EEServer=" + serverName;
    }

    public Reference buildResourceLinkReference(GerLocalRefType localRef, Class iface) throws MalformedObjectNameException {
        Reference ref = new Reference(null, JMXObjectFactory.class.getName(), null);

        ref.add(new JMXRefAddr(null, localRef.getKernelName(), ObjectName.getInstance(localRef.getResourceLink()), iface));
        return ref;
    }

    public Reference buildConnectionFactoryReference(GerLocalRefType localRef, Class iface) throws MalformedObjectNameException {
        Reference ref = new Reference(null, JMXObjectFactory.class.getName(), null);

        String targetName = localRef.getTargetName();
        ref.add(new JMXRefAddr(null, localRef.getKernelName(), createManagedConnectionFactoryObjectName(targetName), iface));
        return ref;
    }

    public ObjectName createManagedConnectionFactoryObjectName(String targetName) throws MalformedObjectNameException {
        if (targetName.indexOf(':') < 0) {
            return ObjectName.getInstance(baseName + BASE_MANAGED_CONNECTION_FACTORY_NAME + targetName);
        } else {
            return ObjectName.getInstance(targetName);
        }
    }

    public Reference buildAdminObjectReference(GerLocalRefType localRef, Class iface) throws MalformedObjectNameException {
        Reference ref = new Reference(null, JMXObjectFactory.class.getName(), null);
        String targetName = localRef.getTargetName();
        ref.add(new JMXRefAddr(null, localRef.getKernelName(), createAdminObjectObjectName(targetName), iface));
        return ref;
    }

    public ObjectName createAdminObjectObjectName(String targetName) throws MalformedObjectNameException {
        return ObjectName.getInstance(baseName + BASE_ADMIN_OBJECT_NAME + targetName);
    }

    //TODO warning: this only works if there is only one kernel!
    public Reference buildMessageDestinationReference(String linkName, Class iface) throws MalformedObjectNameException {
        Reference ref = new Reference(null, JMXObjectFactory.class.getName(), null);
        ref.add(new JMXRefAddr(null, null, createAdminObjectObjectName(linkName), iface));
        return ref;
    }

    public Reference buildEjbReference(GerRemoteRefType remoteRef, Class iface) throws MalformedObjectNameException {
        Reference ref = new Reference(null, JMXObjectFactory.class.getName(), null);
        ref.add(new JMXRefAddr(remoteRef.getServer(), remoteRef.getKernelName(), ObjectName.getInstance(remoteRef.getTargetName()), iface));
        return ref;
    }

    public Reference buildEjbLocalReference(GerLocalRefType localRef, Class iface) throws MalformedObjectNameException {
        Reference ref = new Reference(null, JMXObjectFactory.class.getName(), null);
        ref.add(new JMXRefAddr(null, localRef.getKernelName(), ObjectName.getInstance(localRef.getTargetName()), iface));
        return ref;
    }

}
