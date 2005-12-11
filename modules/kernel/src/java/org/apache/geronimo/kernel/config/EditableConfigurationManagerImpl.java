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
import java.net.URI;
import javax.management.ObjectName;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;

/**
 * Standard implementation of an editable ConfigurationManager.
 *
 * @version $Rev$ $Date$
 */
public class EditableConfigurationManagerImpl extends ConfigurationManagerImpl implements EditableConfigurationManager {
    public EditableConfigurationManagerImpl(Kernel kernel, Collection stores, ManageableAttributeStore attributeStore,
                                            PersistentConfigurationList configurationList) {
        super(kernel, stores, attributeStore, configurationList);
    }

    public void addGBeanToConfiguration(URI configID, GBeanData gbean, boolean start) throws InvalidConfigException {
        //todo: when able to, save new GBean data in attribute store rather than in serialized state
        //that is, replace the code below with only calls to the attribute store
        try {
            ObjectName name = Configuration.getConfigurationObjectName(configID);
            kernel.invoke(name, "addGBean", new Object[]{gbean, start ? Boolean.TRUE : Boolean.FALSE}, new String[]{GBeanData.class.getName(), boolean.class.getName()});
        } catch (Exception e) {
            throw new InvalidConfigException("Unable to add GBean to configuration", e);
        }

        attributeStore.addGBean(configID.toString(), gbean);
    }

    public void removeGBeanFromConfiguration(URI configID, ObjectName gbean) throws GBeanNotFoundException, InvalidConfigException {
        // Make sure the specified configuration has the specified GBean
        try {
            ObjectName name = Configuration.getConfigurationObjectName(configID);
            Boolean result = (Boolean) kernel.invoke(name, "containsGBean", new Object[]{gbean}, new String[]{ObjectName.class.getName()});
            if(!result.booleanValue()) {
                throw new GBeanNotFoundException(gbean);
            }
        } catch(GBeanNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidConfigException("Unable to add GBean to configuration", e);
        }

        // Stop and unload the GBean if necessary
        try {
            if (kernel.getGBeanState(gbean) == State.RUNNING_INDEX) {
                kernel.stopGBean(gbean);
            }
            kernel.unloadGBean(gbean);
        } catch (GBeanNotFoundException e) {
            // Bean is no longer loaded
        }

        // Make sure it's not loaded next time the configuration is loaded
        attributeStore.setShouldLoad(configID.toString(), gbean, false);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(EditableConfigurationManagerImpl.class, ConfigurationManagerImpl.GBEAN_INFO, "ConfigurationManager");
        infoFactory.addInterface(EditableConfigurationManager.class);
        infoFactory.setConstructor(new String[]{"kernel", "Stores", "AttributeStore", "PersistentConfigurationList"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
