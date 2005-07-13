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
package org.apache.geronimo.kernel.basic;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.gbean.GBeanName;
import org.apache.geronimo.gbean.runtime.GBeanInstance;

/**
 * @version $Rev$ $Date$
 */
public class BasicRegistry {
    private final Map registry = new HashMap();
    private String kernelName = "";

    /**
     * Start the registry and associate it with a kernel.
     *
     * @param kernel the kernel to associate with
     */
    public void start(Kernel kernel) {
        kernelName = kernel.getKernelName();
    }

    /**
     * Shut down the registry and unregister any GBeans
     */
    public synchronized void stop() {
        registry.clear();
        kernelName = "";
    }

    /**
     * See if there is a GBean registered with a specific name.
     *
     * @param name the name of the GBean to check for
     * @return true if there is a GBean registered with that name
     */
    public synchronized boolean isRegistered(GBeanName name) {
        return registry.containsKey(name);
    }

    /**
     * Register a GBean instance.
     *
     * @param gbeanInstance the GBean to register
     * @throws GBeanAlreadyExistsException if there is already a GBean registered with the instance's name
     */
    public synchronized void register(GBeanInstance gbeanInstance) throws GBeanAlreadyExistsException {
        GBeanName name = createGBeanName(gbeanInstance.getObjectNameObject());
        if (registry.containsKey(name)) {
            throw new GBeanAlreadyExistsException("GBean already registered: " + name);
        }
        registry.put(name, gbeanInstance);
    }

    /**
     * Unregister a GBean instance.
     *
     * @param name the name of the GBean to unregister
     * @throws GBeanNotFoundException if there is no GBean registered with the supplied name
     */
    public synchronized void unregister(GBeanName name) throws GBeanNotFoundException, InternalKernelException {
        if (registry.remove(name) == null) {
            try {
                throw new GBeanNotFoundException(name.getObjectName());
            } catch (MalformedObjectNameException e) {
                throw new InternalKernelException(e);
            }
        }
    }

    /**
     * Return the GBeanInstance registered with the supplied name.
     *
     * @param name the name of the instance to return
     * @return the GBeanInstance
     * @throws GBeanNotFoundException if there is no GBean registered with the supplied name
     */
    public synchronized GBeanInstance getGBeanInstance(GBeanName name) throws GBeanNotFoundException {
        GBeanInstance instance = (GBeanInstance) registry.get(name);
        if (instance == null) {
            try {
                throw new GBeanNotFoundException(name.getObjectName());
            } catch (MalformedObjectNameException e) {
                throw new InternalKernelException(e);
            }
        }
        return instance;
    }


    /**
     * Search the registry for GBeans matching a name pattern.
     *
     * @param domain the domain to query in; null indicates all
     * @param properties the properties the GBeans must have
     * @return an unordered Set<GBeanInstance> of GBeans that matched the pattern
     */
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

    private GBeanName createGBeanName(ObjectName objectName) {
        if (objectName.getDomain().length() == 0) {
            return new GBeanName(kernelName, objectName.getKeyPropertyList());
        }
        return new GBeanName(objectName);
    }
}
