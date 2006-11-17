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

package org.apache.geronimo.naming.reference;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Artifact;

import javax.naming.NameNotFoundException;

/**
 * @version $Rev$ $Date$
 */
public class ResourceReference extends ConfigurationAwareReference {
    private final Class iface;

    /**
     *
     * @param configId the configId of the configuration that holds the reference, not the resource adapter.
     * @param abstractNameQuery query for name of the resource adapter.
     * @param iface
     */
    public ResourceReference(Artifact configId, AbstractNameQuery abstractNameQuery, Class iface) {
        super(configId, abstractNameQuery);
        this.iface = iface;
    }

    public String getClassName() {
        return iface.getName();
    }

    public Object getContent() throws NameNotFoundException {
        Kernel kernel = getKernel();

        AbstractName target;
        try {
            target = resolveTargetName();
        } catch (GBeanNotFoundException e) {
            throw (NameNotFoundException) new NameNotFoundException("Could not resolve name query: " + abstractNameQueries).initCause(e);
        }

        Object proxy;
        try {
            proxy = kernel.invoke(target, "$getResource");
        } catch (Exception e) {
            throw (IllegalStateException) new IllegalStateException("Could not get proxy").initCause(e);
        }
        if (proxy == null) {
            throw new IllegalStateException("Proxy not returned. Target " + target + " not started");
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
