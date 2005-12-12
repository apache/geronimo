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

package org.apache.geronimo.naming.reference;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.kernel.Kernel;

/**
 * @version $Rev$ $Date$
 */
public class ResourceReference extends SimpleAwareReference {
    private final String containerId;
    private final Class iface;

    public ResourceReference(String containerId, Class iface) {
        this.containerId = containerId;
        this.iface = iface;
    }

    public String getClassName() {
        return iface.getName();
    }

    public Object getContent() {
        Kernel kernel = getKernel();

        ObjectName target = null;
        try {
            target = ObjectName.getInstance(containerId);
        } catch (MalformedObjectNameException e) {
            throw (IllegalArgumentException) new IllegalArgumentException("Invalid object name in jmxRefAddr: " + containerId).initCause(e);
        }

        Object proxy = null;
        try {
            proxy = kernel.invoke(target, "$getResource");
        } catch (Exception e) {
            throw (IllegalStateException) new IllegalStateException("Could not get proxy").initCause(e);
        }
        if (proxy == null) {
            throw new IllegalStateException("Proxy not returned. Target " + containerId + " not started");
        }
        if (!iface.isAssignableFrom(proxy.getClass())) {
            Class proxyClass = proxy.getClass();
            Class[] interfaces = proxyClass.getInterfaces();
            StringBuffer message = new StringBuffer();
            boolean namesMatch = false;
            for (int i = 0; i < interfaces.length; i++) {
                Class anInterface = interfaces[i];
                if (iface.getName().equals(anInterface.getName())) {
                    namesMatch = true;
                    message.append("Proxy implements correct interface: ").append(iface.getName()).append(", but classloaders differ\n");
                    message.append("lookup interface classloader: ").append(iface.getClassLoader().toString()).append("\n");
                    message.append("target interface classloader: ").append(anInterface.getClassLoader().toString()).append("\n");
                    message.append("target proxy classloader: ").append(proxy.getClass().getClassLoader());
                    break;
                }
            }
            if (!namesMatch) {
                message.append("Proxy does not implement an interface named: ").append(iface.getName());
            }
            throw new ClassCastException(message.toString());
        }
        return proxy;

    }
}
