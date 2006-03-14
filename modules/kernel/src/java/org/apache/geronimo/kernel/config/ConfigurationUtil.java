/**
 *
 * Copyright 2005 The Apache Software Foundation
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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanQuery;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.Environment;

/**
 * @version $Rev: 384351 $ $Date$
 */
public final class ConfigurationUtil {
    private ConfigurationUtil() {
    }

    public static AbstractName loadBootstrapConfiguration(Kernel kernel, InputStream in, ClassLoader classLoader) throws Exception {
        // load and start the configuration in this jar
        GBeanData configuration = new GBeanData();
        ObjectInputStream ois = new ObjectInputStream(in);
        try {
            configuration.readExternal(ois);
        } finally {
            ois.close();
        }

        Environment environment = (Environment) configuration.getAttribute("environment");
        Artifact configId = environment.getConfigId();
        AbstractName configurationName = Configuration.getConfigurationAbstractName(configId);
        configuration.setAbstractName(configurationName);

        // for a bootstrap we should have an empty kernel, so clear the references and dependencies
        configuration.setAttribute("artifactManager", null);
        configuration.setAttribute("artifactResolver", null);
        environment.setDependencies(Collections.EMPTY_LIST);

        // load and start the gbean
        kernel.loadGBean(configuration, classLoader);
        kernel.startGBean(configurationName);

        // get the gbeans and classloader
        Map gbeans = (Map) kernel.getAttribute(configurationName, "GBeans");
        ClassLoader configurationClassLoader = (ClassLoader) kernel.getAttribute(configurationName, "configurationClassLoader");

        // register all the GBeans
        for (Iterator i = gbeans.values().iterator(); i.hasNext();) {
            GBeanData gbeanData = (GBeanData) i.next();
            gbeanData.addDependency(configurationName);

            // load the gbean into the kernel
            kernel.loadGBean(gbeanData, configurationClassLoader);

            // start the gbean
            if (kernel.isGBeanEnabled(gbeanData.getName())) {
                kernel.startRecursiveGBean(gbeanData.getName());
            }
        }

        ConfigurationManager configurationManager = getConfigurationManager(kernel);
        configurationManager.loadConfiguration(configId);
        return configurationName;
    }

    public static void storeBootstrapConfiguration(ConfigurationData configurationData, OutputStream out) throws InvalidConfigException, IOException {
        ObjectOutputStream objectOutputStream = null;
        try {
            GBeanData configurationGBeanData = toConfigurationGBeanData(configurationData, null, null, null);
            objectOutputStream = new ObjectOutputStream(out);
            configurationGBeanData.writeExternal(objectOutputStream);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidConfigException("Unable to save configuration state", e);
        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.flush();
                } catch (IOException ignored) {
                }
            }
        }

    }

    // This method is package protected in an attempt to hide how we turn ConfigurationData into a GBeanData
    // user should be using ConfigurationManager to do this work
    static GBeanData toConfigurationGBeanData(ConfigurationData configurationData, ConfigurationStore configurationStore, Collection repositories, ArtifactResolver artifactResolver) throws InvalidConfigException {
        Artifact id = configurationData.getId();
        AbstractName abstractName = Configuration.getConfigurationAbstractName(id);
        GBeanData gbeanData = new GBeanData(abstractName, Configuration.GBEAN_INFO);
        gbeanData.setAttribute("moduleType", configurationData.getModuleType());
        Environment environment = configurationData.getEnvironment();
        gbeanData.setAttribute("environment", environment);
        gbeanData.setAttribute("gBeanState", Configuration.storeGBeans(configurationData.getGBeans()));
        gbeanData.setAttribute("classPath", configurationData.getClassPath());

        ConfigurationResolver configurationResolver;
        if (configurationStore != null) {
            configurationResolver = new ConfigurationResolver(configurationData.getEnvironment().getConfigId(), configurationStore, repositories, artifactResolver);
        } else {
            configurationResolver = new ConfigurationResolver(configurationData.getEnvironment().getConfigId(), configurationData.getConfigurationDir(), repositories, artifactResolver);
        }
        gbeanData.setAttribute("configurationResolver", configurationResolver);

        return gbeanData;
    }

    /**
     * Gets a reference or proxy to the ConfigurationManager running in the specified kernel.
     *
     * @return The ConfigurationManager
     *
     * @throws IllegalStateException Occurs if a ConfigurationManager cannot be identified
     */
    public static ConfigurationManager getConfigurationManager(Kernel kernel) {
        Set names = kernel.listGBeans(new GBeanQuery(null, ConfigurationManager.class.getName()));
        for (Iterator iterator = names.iterator(); iterator.hasNext();) {
            ObjectName objectName = (ObjectName) iterator.next();
            try {
                if (kernel.getGBeanState(objectName) != State.RUNNING_INDEX) {
                    iterator.remove();
                }
            } catch (GBeanNotFoundException e) {
                // bean died
                iterator.remove();
            }
        }
        if (names.isEmpty()) {
            throw new IllegalStateException("A Configuration Manager could not be found in the kernel");
        }
        if (names.size() > 1) {
            throw new IllegalStateException("More than one Configuration Manager was found in the kernel");
        }
        ObjectName configurationManagerName = (ObjectName) names.iterator().next();
        return (ConfigurationManager) kernel.getProxyManager().createProxy(configurationManagerName, ConfigurationManager.class);
    }

    /**
     * Gets a reference or proxy to an EditableConfigurationManager running in the specified kernel, if there is one.
     *
     * @return The EdtiableConfigurationManager, or none if there is not one available.
     *
     * @throws IllegalStateException Occurs if there are multiple EditableConfigurationManagers in the kernel.
     */
    public static EditableConfigurationManager getEditableConfigurationManager(Kernel kernel) {
        Set names = kernel.listGBeans(new GBeanQuery(null, EditableConfigurationManager.class.getName()));
        for (Iterator iterator = names.iterator(); iterator.hasNext();) {
            ObjectName objectName = (ObjectName) iterator.next();
            try {
                if (kernel.getGBeanState(objectName) != State.RUNNING_INDEX) {
                    iterator.remove();
                }
            } catch (GBeanNotFoundException e) {
                // bean died
                iterator.remove();
            }
        }
        if (names.isEmpty()) {
            return null; // may be one, just not editable
        }
        if (names.size() > 1) {
            throw new IllegalStateException("More than one Configuration Manager was found in the kernel");
        }
        ObjectName configurationManagerName = (ObjectName) names.iterator().next();
        return (EditableConfigurationManager) kernel.getProxyManager().createProxy(configurationManagerName, EditableConfigurationManager.class);
    }

    public static void releaseConfigurationManager(Kernel kernel, ConfigurationManager configurationManager) {
        kernel.getProxyManager().destroyProxy(configurationManager);
    }
}
