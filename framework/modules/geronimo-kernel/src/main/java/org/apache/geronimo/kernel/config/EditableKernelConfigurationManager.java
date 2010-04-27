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
package org.apache.geronimo.kernel.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.management.State;
import org.apache.xbean.osgi.bundle.util.BundleClassLoader;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.osgi.framework.BundleContext;

/**
 * Standard implementation of an editable ConfigurationManager.
 *
 * @version $Rev:386276 $ $Date$
 */
@GBean(j2eeType = "ConfigurationManager")
public class EditableKernelConfigurationManager extends KernelConfigurationManager implements EditableConfigurationManager {
    private final AbstractNameQuery defaultStoreNameQuery;

    public EditableKernelConfigurationManager(@ParamSpecial(type = SpecialAttributeType.kernel) Kernel kernel,
                                              @ParamAttribute(name = GBEAN_ATTR_DEFAULT_STORE_NAME_QUERY) AbstractNameQuery defaultStoreNameQuery,
                                              @ParamReference(name = "Stores", namingType = "ConfigurationStore") Collection<ConfigurationStore> stores,
                                              @ParamReference(name = "AttributeStore", namingType = "AttributeStore") ManageableAttributeStore attributeStore,
                                              @ParamReference(name = "PersistentConfigurationList") PersistentConfigurationList configurationList,
                                              @ParamReference(name = "ArtifactManager", namingType = "ArtifactManager") ArtifactManager artifactManager,
                                              @ParamReference(name = "ArtifactResolver", namingType = "ArtifactResolver") ArtifactResolver artifactResolver,
                                              @ParamReference(name = "Repositories", namingType = "Repository") Collection<ListableRepository> repositories,
                                              @ParamReference(name = "Watchers") Collection<DeploymentWatcher> watchers,
                                              @ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext) {
        super(kernel,
                stores,
                attributeStore,
                configurationList,
                artifactManager,
                artifactResolver,
                repositories,
                watchers,
                bundleContext);
        this.defaultStoreNameQuery = defaultStoreNameQuery;        
    }

    @Override
    protected List getStoreList() {
        if (null == defaultStoreNameQuery) {
            return super.getStoreList();
        }

        List<ConfigurationStore> storeList = new ArrayList<ConfigurationStore>();
        for (Iterator iterator = stores.iterator(); iterator.hasNext();) {
            ConfigurationStore configurationStore = (ConfigurationStore) iterator.next();
            if (defaultStoreNameQuery.matches(configurationStore.getAbstractName(), Collections.EMPTY_SET)) {
                storeList.add(0, configurationStore);
            } else {
                storeList.add(configurationStore);
            }
        }
        return storeList;
    }

    public void addGBeanToConfiguration(Artifact configurationId, GBeanData gbean, boolean start) throws InvalidConfigException {
        Configuration configuration = getConfiguration(configurationId);

        try {
            // add the gbean to the configuration
            configuration.addGBean(gbean);
        } catch (GBeanAlreadyExistsException e) {
            throw new InvalidConfigException("Cound not add GBean " + gbean.getAbstractName() + " to configuration " + configurationId, e);
        }

        addGBeanToConfiguration(configuration, gbean, start);
    }

    public void addGBeanToConfiguration(Artifact configurationId, String name, GBeanData gbean, boolean start) throws InvalidConfigException {
        Configuration configuration = getConfiguration(configurationId);

        try {
            // add the gbean to the configuration
            configuration.addGBean(name, gbean);
        } catch (GBeanAlreadyExistsException e) {
            throw new InvalidConfigException("Cound not add GBean " + gbean.getAbstractName() + " to configuration " + configurationId, e);
        }

        addGBeanToConfiguration(configuration, gbean, start);
    }

    private void addGBeanToConfiguration(Configuration configuration, GBeanData gbean, boolean start) throws InvalidConfigException {
        ClassLoader configurationClassLoader = new BundleClassLoader(configuration.getBundle());
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(configurationClassLoader);

            log.trace("Registering GBean " + gbean.getAbstractName());


            // preprocess the gbean data before loading it into the kernel
            ConfigurationUtil.preprocessGBeanData(configuration.getAbstractName(), configuration, gbean);

            // register the bean with the kernel
            kernel.loadGBean(gbean, configuration.getBundleContext());

            // start the configuration
            if (start) {
                try {
                    kernel.startRecursiveGBean(gbean.getAbstractName());
                } catch (GBeanNotFoundException e) {
                    throw new InvalidConfigException("How could we not find a GBean that we just loaded ('" + gbean.getAbstractName() + "')?", e);
                }
            }

        } catch(Exception e) {
            // clean up failed gbean
            try {
                configuration.removeGBean(gbean.getAbstractName());
            } catch (GBeanNotFoundException e1) {
                // this is good
            }
            try {
                kernel.stopGBean(gbean.getAbstractName());
            } catch (GBeanNotFoundException e1) {
                // this is good
            }
            try {
                kernel.unloadGBean(gbean.getAbstractName());
            } catch (GBeanNotFoundException e1) {
                // this is good
            }

            if (e instanceof InvalidConfigException) {
                throw (InvalidConfigException) e;
            }
            throw new InvalidConfigException("Cound not add GBean " + gbean.getAbstractName() + " to configuration " + configuration.getId(), e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }

        if (attributeStore != null) {
            attributeStore.addGBean(configuration.getId(), gbean, configuration.getBundle());
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

        // Make sure it's not loaded next time the configuration is loaded
        if (attributeStore != null) {
            attributeStore.setShouldLoad(configurationId, gbeanName, false);
        }
    }


    public static final String GBEAN_ATTR_DEFAULT_STORE_NAME_QUERY = "defaultStoreNameQuery";

}
