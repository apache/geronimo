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
import javax.naming.RefAddr;

import org.apache.geronimo.kernel.Kernel;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class GBeanGetResourceRefAddr extends RefAddr implements GBeanRefAddr {

    private final static String TYPE = "org.apache.geronimo.naming.jmx.RefType";

    private final String kernelName;
    private final String containerId;
    private final Class iface;

    public GBeanGetResourceRefAddr(String kernelName, String containerId, Class iface) {
        super(TYPE);
        this.kernelName = kernelName;
        this.containerId = containerId;
        this.iface = iface;
    }


    public String getKernelName() {
        return kernelName;
    }

    public String getContainerId() {
        return containerId;
    }

    public Class getInterface() {
        return iface;
    }

    public Object getContent() {
        Kernel kernel;
        if (getKernelName() == null) {
            kernel = Kernel.getSingleKernel();
        } else {
            kernel = Kernel.getKernel(getKernelName());
        }

        ObjectName target = null;
        try {
            target = ObjectName.getInstance(getContainerId());
        } catch (MalformedObjectNameException e) {
            throw (IllegalArgumentException) new IllegalArgumentException("Invalid object name in jmxRefAddr: " + getContainerId()).initCause(e);
        }

        Object proxy = null;
        try {
            proxy = kernel.invoke(target, "$getResource");
        } catch (Exception e) {
            throw (IllegalStateException)new IllegalStateException("Could not get proxy").initCause(e);
        }
        if (proxy == null) {
            throw new IllegalStateException("Proxy not returned. Target " + getContainerId() + " not started");
        }
        if (!getInterface().isAssignableFrom(proxy.getClass())) {
            throw new ClassCastException("Proxy does not implement expected interface " + getInterface());
        }
        return proxy;

    }
}
