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
package org.apache.geronimo.kernel.registry;

import java.util.Set;
import java.util.Map;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanName;
import org.apache.geronimo.gbean.runtime.GBeanInstance;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;

/**
 * Interface implemented by Registries that a Kernel can use to store and retrieve GBeanInstances.
 * @version $Rev$ $Date$
 */
public interface GBeanRegistry {
    /**
     * Start the registry and associate it with a kernel.
     *
     * @param kernel the kernel to associate with
     */
    void start(Kernel kernel);

    /**
     * Shut down the registry and unregister any GBeans
     */
    void stop();

    /**
     * See if there is a GBean registered with a specific name.
     *
     * @param name the name of the GBean to check for
     * @return true if there is a GBean registered with that name
     */
    boolean isRegistered(GBeanName name);

    /**
     * Register a GBean instance.
     *
     * @param gbeanInstance the GBean to register
     * @throws GBeanAlreadyExistsException if there is already a GBean registered with the instance's name
     */
    void register(GBeanInstance gbeanInstance) throws GBeanAlreadyExistsException;

    /**
     * Unregister a GBean instance.
     *
     * @param name the name of the GBean to unregister
     * @throws GBeanNotFoundException if there is no GBean registered with the supplied name
     */
    void unregister(GBeanName name) throws GBeanNotFoundException;

    /**
     * Return the GBeanInstance registered with the supplied name.
     *
     * @param name the name of the instance to return
     * @return the GBeanInstance
     * @throws GBeanNotFoundException if there is no GBean registered with the supplied name
     */
    GBeanInstance getGBeanInstance(GBeanName name) throws GBeanNotFoundException;

    /**
     * Search the registry for GBeans matching a name pattern.
     *
     * @param domain the domain to query in; null indicates all
     * @param properties the properties the GBeans must have
     * @return an unordered Set<GBeanInstance> of GBeans that matched the pattern
     */
    Set listGBeans(String domain, Map properties);
}
