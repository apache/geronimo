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
 * @version $Rev: 56169 $ $Date: 2004-10-31 16:05:29 -0800 (Sun, 31 Oct 2004) $
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
            throw new ClassCastException("Proxy does not implement expected interface " + iface);
        }
        return proxy;

    }
}
