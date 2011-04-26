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
package org.apache.geronimo.kernel.basic;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.runtime.GBeanInstance;
import org.apache.geronimo.gbean.runtime.InstanceRegistry;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;

/**
 * @version $Rev$ $Date$
 */
public class BasicRegistry implements InstanceRegistry {
    private final Map<ObjectName, GBeanInstance> objectNameRegistry = new HashMap<ObjectName, GBeanInstance>();
    private final Map<AbstractName, GBeanInstance> infoRegistry = new HashMap<AbstractName, GBeanInstance>();
    private final IdentityHashMap<Object, GBeanInstance> instanceRegistry = new IdentityHashMap<Object, GBeanInstance>();
    private String kernelName = "";

    /**
     * Start the objectNameRegistry and associate it with a kernel.
     *
     * @param kernel the kernel to associate with
     */
    public void start(Kernel kernel) {
        kernelName = kernel.getKernelName();
    }

    /**
     * Shut down the objectNameRegistry and unregister any GBeans
     */
    public void stop() {
        synchronized (this) {
            objectNameRegistry.clear();
        }
        kernelName = "";
    }

    /**
     * See if there is a GBean registered with a specific name.
     *
     * @param name the name of the GBean to check for
     * @return true if there is a GBean registered with that name
     */
    public synchronized boolean isRegistered(ObjectName name) {
        return objectNameRegistry.containsKey(normalizeObjectName(name));
    }

    public synchronized boolean isRegistered(AbstractName refInfo) {
        return infoRegistry.containsKey(refInfo);
    }

    /**
     * Register a GBean instance.
     *
     * @param gbeanInstance the GBean to register
     * @throws GBeanAlreadyExistsException if there is already a GBean registered with the instance's name
     */
    public synchronized void register(GBeanInstance gbeanInstance) throws GBeanAlreadyExistsException {
        ObjectName name = normalizeObjectName(gbeanInstance.getObjectNameObject());
        if (objectNameRegistry.containsKey(name)) {
            throw new GBeanAlreadyExistsException("Cannot register GBean with abstract name: " + gbeanInstance.getAbstractName() + ", GBean with abstract name: " + objectNameRegistry.get(name).getAbstractName() + " already registered under ObjectName: " + name);
        }
        objectNameRegistry.put(name, gbeanInstance);
        infoRegistry.put(gbeanInstance.getAbstractName(), gbeanInstance);
        gbeanInstance.setInstanceRegistry(this);
    }

    public synchronized void unregister(AbstractName abstractName) throws GBeanNotFoundException {
        GBeanInstance gbeanInstance = infoRegistry.remove(abstractName);
        if (gbeanInstance == null) {
            throw new GBeanNotFoundException(abstractName);
        }
        objectNameRegistry.remove(gbeanInstance.getObjectNameObject());
    }

    public synchronized void instanceCreated(Object instance, GBeanInstance gbeanInstance) {
        instanceRegistry.put(instance, gbeanInstance);
    }

    public synchronized void instanceDestroyed(Object instance) {
        instanceRegistry.remove(instance);
    }

    public synchronized GBeanInstance getGBeanInstanceByInstance(Object instance) {
        return instanceRegistry.get(instance);
    }

    /**
     * Return the GBeanInstance registered with the supplied name.
     *
     * @param name the name of the instance to return
     * @return the GBeanInstance
     * @throws GBeanNotFoundException if there is no GBean registered with the supplied name
     */
    public synchronized GBeanInstance getGBeanInstance(ObjectName name) throws GBeanNotFoundException {
        GBeanInstance instance = objectNameRegistry.get(normalizeObjectName(name));
        if (instance == null) {
            throw new GBeanNotFoundException(name);
        }
        return instance;
    }

    public synchronized GBeanInstance getGBeanInstance(AbstractName abstractName) throws GBeanNotFoundException {
        GBeanInstance instance = infoRegistry.get(abstractName);
        if (instance == null) {
            throw new GBeanNotFoundException(abstractName);
        }
        return instance;
    }


    public synchronized GBeanInstance getGBeanInstance(String shortName, Class type) throws GBeanNotFoundException {
        if (shortName == null && type == null) throw new IllegalArgumentException("shortName and type are both null");

        AbstractNameQuery nameQuery;
        if (type == null) {
            nameQuery = new AbstractNameQuery(null, Collections.singletonMap("name", shortName));
        } else if (shortName == null) {
            nameQuery = new AbstractNameQuery(null, Collections.EMPTY_MAP, type.getName());
        } else {
            nameQuery = new AbstractNameQuery(null, Collections.singletonMap("name", shortName), type.getName());
        }
        Set<GBeanInstance> instances = listGBeans(nameQuery);

        if (instances.size() == 0) {
            throw new GBeanNotFoundException("No GBeans found", Collections.singleton(nameQuery), null);
        }

        if (instances.size() > 1) {
            if (type == null) {
                throw new GBeanNotFoundException("More then one GBean was found with shortName '" + shortName + "'", Collections.singleton(nameQuery), mapToNames(instances));
            }
            if (shortName == null) {
                throw new GBeanNotFoundException("More then one GBean was found with type '" + type.getName() + "'", Collections.singleton(nameQuery), mapToNames(instances));
            }
            throw new GBeanNotFoundException("More then one GBean was found with shortName '" + shortName + "' and type '" + type.getName() + "'", Collections.singleton(nameQuery), mapToNames(instances));
        }

        return instances.iterator().next();
    }

    private Set<AbstractName> mapToNames(Set<GBeanInstance> instances) {
        Set<AbstractName> names = new HashSet<AbstractName>(instances.size());
        for (GBeanInstance instance: instances) {
            names.add(instance.getAbstractName());
        }
        return names;
    }


    /**
     * Search the objectNameRegistry for GBeans matching a name pattern.
     *
     * @param pattern the object name pattern to search for
     * @return an unordered Set<GBeanInstance> of GBeans that matched the pattern
     */
    public Set<GBeanInstance> listGBeans(ObjectName pattern) {
        pattern = normalizeObjectName(pattern);

        // fairly dumb implementation that iterates the list of all registered GBeans
        Map<ObjectName, GBeanInstance> clone;
        synchronized (this) {
            clone = new HashMap<ObjectName, GBeanInstance>(objectNameRegistry);
        }
        Set<GBeanInstance> result = new HashSet<GBeanInstance>(clone.size());
        for (Map.Entry<ObjectName, GBeanInstance> entry : clone.entrySet()) {
            ObjectName name = entry.getKey();
            if (pattern == null || pattern.apply(name)) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    public Set<GBeanInstance> listGBeans(AbstractNameQuery query) {
        Map<AbstractName, GBeanInstance> clone;
        synchronized (this) {
            clone = new HashMap<AbstractName, GBeanInstance>(infoRegistry);
        }
        Set<GBeanInstance> result = new HashSet<GBeanInstance>(clone.size());
        for (Map.Entry<AbstractName, GBeanInstance> entry : clone.entrySet()) {
            AbstractName abstractName = entry.getKey();
            GBeanInstance gbeanData = entry.getValue();
            if (query == null || query.matches(abstractName, gbeanData.getGBeanInfo().getInterfaces())) {
                result.add(gbeanData);
            }
        }
        return result;
    }

    private ObjectName normalizeObjectName(ObjectName objectName) {
        if (objectName != null && objectName.getDomain().length() == 0) {
            try {
                return new ObjectName(kernelName, objectName.getKeyPropertyList());
            } catch (MalformedObjectNameException e) {
                throw new AssertionError(e);
            }
        }
        return objectName;
    }
}
