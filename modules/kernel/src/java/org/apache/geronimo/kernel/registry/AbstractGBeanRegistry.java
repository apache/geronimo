/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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
package org.apache.geronimo.kernel.registry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanName;
import org.apache.geronimo.gbean.runtime.GBeanInstance;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractGBeanRegistry implements GBeanRegistry {
    protected final Map registry = new HashMap();

    public void start(Kernel kernel) {
    }

    public synchronized void stop() {
        registry.clear();
    }

    public synchronized boolean isRegistered(GBeanName name) {
        return registry.containsKey(name);
    }

    public synchronized void register(GBeanInstance gbeanInstance) throws GBeanAlreadyExistsException {
        ObjectName objectName = gbeanInstance.getObjectNameObject();
        GBeanName name = new GBeanName(objectName);
        if (registry.containsKey(name)) {
            throw new GBeanAlreadyExistsException("GBean already registered: " + name);
        }
        registry.put(name, gbeanInstance);
    }

    public synchronized void unregister(GBeanName name) throws GBeanNotFoundException, InternalKernelException {
        if (registry.remove(name) == null) {
            throw new GBeanNotFoundException("No GBean registered: " + name);
        }
    }

    public synchronized GBeanInstance getGBeanInstance(GBeanName name) throws GBeanNotFoundException {
        GBeanInstance instance = (GBeanInstance) registry.get(name);
        if (instance == null) {
            throw new GBeanNotFoundException("No GBean registered: " + name);
        }
        return instance;
    }

    public Set listGBeans(String domain, Map properties) {
        // fairly dumb implementation that iterates the list of all registered GBeans
        Map clone;
        synchronized(this) {
            clone = new HashMap(registry);
        }
        Set result = new HashSet(clone.size());
        for (Iterator i = clone.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            GBeanName name = (GBeanName) entry.getKey();
            if (name.matches(domain, properties)) {
                result.add(entry.getValue());
            }
        }
        return result;
    }
}
