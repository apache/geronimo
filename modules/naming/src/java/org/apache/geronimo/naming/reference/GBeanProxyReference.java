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

import javax.management.ObjectName;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.proxy.ProxyManager;

/**
 * @version $Rev$ $Date$
 */
public class GBeanProxyReference extends SimpleAwareReference {
    private final ObjectName target;
    private final Class type;

    public GBeanProxyReference(ObjectName target, Class type) {
        this.target = target;
        this.type = type;
    }

    public String getClassName() {
        return type.getName();
    }

    public Object getContent() throws IllegalStateException {
        Kernel kernel = getKernel();
        // todo HACK: this is a very bad idea
        ProxyManager proxyManager = kernel.getProxyManager();
        return proxyManager.createProxy(target, type);
    }
}
