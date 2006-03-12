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

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.proxy.ProxyManager;
import org.apache.geronimo.kernel.repository.Artifact;

import javax.naming.NameNotFoundException;

/**
 * @version $Rev$ $Date$
 */
public class GBeanProxyReference extends ConfigurationAwareReference {
    private final Class type;

    public GBeanProxyReference(Artifact configId, AbstractNameQuery abstractNameQuery, Class type) {
        super(configId, abstractNameQuery);
        this.type = type;
    }

    public String getClassName() {
        return type.getName();
    }

    public Object getContent() throws IllegalStateException, NameNotFoundException {
        AbstractName target;
        try {
            target = resolveTargetName();
        } catch (GBeanNotFoundException e) {
            throw (NameNotFoundException)new NameNotFoundException("Could not resolve gbean from name query: " + abstractNameQuery).initCause(e);
        }
        Kernel kernel = getKernel();
        // todo HACK: this is a very bad idea
        ProxyManager proxyManager = kernel.getProxyManager();
        return proxyManager.createProxy(target, type);
    }
}
