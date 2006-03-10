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
package org.apache.geronimo.kernel.config;

import java.util.Collection;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.AbstractName;

/**
 * Standard implementation of an editable ConfigurationManager.
 *
 * @version $Rev$ $Date$
 */
public class EditableConfigurationManagerImpl extends ConfigurationManagerImpl implements EditableConfigurationManager {
    public EditableConfigurationManagerImpl(Kernel kernel,
            Collection stores,
            ManageableAttributeStore attributeStore,
            PersistentConfigurationList configurationList,
            ArtifactManager artifactManager,
            ArtifactResolver artifactResolver,
            ClassLoader classLoader) {
        super(kernel, stores, attributeStore, configurationList, artifactManager, artifactResolver, classLoader);
    }

    public void addGBeanToConfiguration(Artifact configurationId, GBeanData gbean, boolean start) throws InvalidConfigException {
        Configuration configuration = getConfiguration(configurationId);
        ClassLoader configurationClassLoader = configuration.getConfigurationClassLoader();

        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(configurationClassLoader);

            log.trace("Registering GBean " + gbean.getName());

            // add a dependency on the configuration
            gbean.addDependency(configuration.getAbstractName());

            // register the bean with the kernel
            kernel.loadGBean(gbean, configurationClassLoader);

            // start the configuration
            if (start) {
                try {
                    kernel.startRecursiveGBean(gbean.getName());
                } catch (GBeanNotFoundException e) {
                    throw new InvalidConfigException("How could we not find a GBean that we just loaded ('" + gbean.getName() + "')?");
                }
            }

            configuration.addGBean(gbean);
        } catch(InvalidConfigException e) {
            throw e;
        } catch(Exception e) {
            throw new InvalidConfigException("Cound not add GBean " + gbean.getName() + " to configuration " + configurationId, e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }

        if (attributeStore != null) {
            attributeStore.addGBean(configurationId.toString(), gbean);
        }
    }

    public void removeGBeanFromConfiguration(Artifact configurationId, AbstractName gbeanName) throws GBeanNotFoundException, InvalidConfigException {
        Configuration configuration = getConfiguration(configurationId);
        if (!configuration.containsGBean(gbeanName)) {
            throw new GBeanNotFoundException(gbeanName);
        }
        configuration.removeGBean(gbeanName);

        try {
            if (kernel.getGBeanState(gbeanName) == State.RUNNING_INDEX) {
                kernel.stopGBean(gbeanName);
            }
            kernel.unloadGBean(gbeanName);
        } catch (GBeanNotFoundException e) {
            // Bean is no longer loaded
        }

        configuration.removeGBean(gbeanName);

        // Make sure it's not loaded next time the configuration is loaded
        if (attributeStore != null) {
            attributeStore.setShouldLoad(configurationId.toString(), gbeanName, false);
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(EditableConfigurationManagerImpl.class, ConfigurationManagerImpl.GBEAN_INFO, "ConfigurationManager");
        infoFactory.addInterface(EditableConfigurationManager.class);
        infoFactory.setConstructor(new String[]{"kernel", "Stores", "AttributeStore", "PersistentConfigurationList", "ArtifactManager", "ArtifactResolver", "classLoader"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
