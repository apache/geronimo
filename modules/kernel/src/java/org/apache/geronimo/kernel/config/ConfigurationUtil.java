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
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.basic.BasicKernel;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * @version $Rev:386276 $ $Date$
 */
public final class ConfigurationUtil {
    private ConfigurationUtil() {
    }

    public static AbstractName loadBootstrapConfiguration(Kernel kernel, InputStream in, ClassLoader classLoader) throws Exception {
        ConfigurationData configurationData = readConfigurationData(in);
        return loadBootstrapConfiguration(kernel, configurationData, classLoader);
    }

    public static AbstractName loadBootstrapConfiguration(Kernel kernel, ConfigurationData configurationData, ClassLoader classLoader) throws Exception {
        if (kernel == null) throw new NullPointerException("kernel is null");
        if (configurationData == null) throw new NullPointerException("configurationData is null");
        if (classLoader == null) throw new NullPointerException("classLoader is null");

        // a bootstrap configuration can not have any dependencies
        List dependencies = configurationData.getEnvironment().getDependencies();
        if (!dependencies.isEmpty()) {
            configurationData.getEnvironment().setDependencies(Collections.EMPTY_SET);
//            throw new InvalidConfigurationException("Booststrap configuration can not have dependendencies: " + dependencies);
        }

        // build the gbean data
        Artifact configId = configurationData.getId();
        AbstractName abstractName = Configuration.getConfigurationAbstractName(configId);
        GBeanData gbeanData = new GBeanData(abstractName, Configuration.GBEAN_INFO);
        gbeanData.setAttribute("configurationData", configurationData);
        gbeanData.setAttribute("configurationResolver", new ConfigurationResolver(configurationData, null, null));

        // load and start the gbean
        kernel.loadGBean(gbeanData, classLoader);
        kernel.startGBean(gbeanData.getAbstractName());

        Configuration configuration = (Configuration) kernel.getGBean(gbeanData.getAbstractName());

        // start the gbeans
        startConfigurationGBeans(configuration.getAbstractName(), configuration, kernel, null);

        ConfigurationManager configurationManager = getConfigurationManager(kernel);
        configurationManager.loadConfiguration(configId);
        return gbeanData.getAbstractName();
    }

    public static void writeConfigurationData(ConfigurationData configurationData, OutputStream out) throws IOException {
        ObjectOutputStream oout = new ObjectOutputStream(out);
        try {
            oout.writeObject(configurationData);
        } finally {
            if (oout != null) {
                try {
                    oout.flush();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static ConfigurationData readConfigurationData(InputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream oin = new ObjectInputStream(in);
        try {
            return (ConfigurationData) oin.readObject();
        } finally {
            oin.close();
        }
    }

    /**
     * Gets a reference or proxy to the ConfigurationManager running in the specified kernel.
     *
     * @return The ConfigurationManager
     * @throws IllegalStateException Occurs if a ConfigurationManager cannot be identified
     */
    public static ConfigurationManager getConfigurationManager(Kernel kernel) {
        Set names = kernel.listGBeans(new AbstractNameQuery(ConfigurationManager.class.getName()));
        for (Iterator iterator = names.iterator(); iterator.hasNext();) {
            AbstractName abstractName = (AbstractName) iterator.next();
            if (!kernel.isRunning(abstractName)) {
                iterator.remove();
            }
        }
        if (names.isEmpty()) {
            throw new IllegalStateException("A Configuration Manager could not be found in the kernel");
        }
        if (names.size() > 1) {
            throw new IllegalStateException("More than one Configuration Manager was found in the kernel");
        }
        AbstractName configurationManagerName = (AbstractName) names.iterator().next();
        return (ConfigurationManager) kernel.getProxyManager().createProxy(configurationManagerName, ConfigurationManager.class);
    }

    /**
     * Gets a reference or proxy to an EditableConfigurationManager running in the specified kernel, if there is one.
     *
     * @return The EdtiableConfigurationManager, or none if there is not one available.
     * @throws IllegalStateException Occurs if there are multiple EditableConfigurationManagers in the kernel.
     */
    public static EditableConfigurationManager getEditableConfigurationManager(Kernel kernel) {
        Set names = kernel.listGBeans(new AbstractNameQuery(EditableConfigurationManager.class.getName()));
        for (Iterator iterator = names.iterator(); iterator.hasNext();) {
            AbstractName abstractName = (AbstractName) iterator.next();
            if (!kernel.isRunning(abstractName)) {
                iterator.remove();
            }
        }
        if (names.isEmpty()) {
            return null; // may be one, just not editable
        }
        if (names.size() > 1) {
            throw new IllegalStateException("More than one Configuration Manager was found in the kernel");
        }
        AbstractName configurationManagerName = (AbstractName) names.iterator().next();
        return (EditableConfigurationManager) kernel.getProxyManager().createProxy(configurationManagerName, EditableConfigurationManager.class);
    }

    public static void releaseConfigurationManager(Kernel kernel, ConfigurationManager configurationManager) {
        kernel.getProxyManager().destroyProxy(configurationManager);
    }

    static void preprocessGBeanData(AbstractName configurationName, Configuration configuration, GBeanData gbeanData) throws InvalidConfigException {
        for (Iterator references = gbeanData.getReferencesNames().iterator(); references.hasNext();) {
            String referenceName = (String) references.next();
            GReferenceInfo referenceInfo = gbeanData.getGBeanInfo().getReference(referenceName);
            if (referenceInfo == null) {
                throw new InvalidConfigException("No reference named " + referenceName + " in gbean " + gbeanData.getAbstractName());
            }
            boolean isSingleValued = !referenceInfo.getProxyType().equals(Collection.class.getName());
            if (isSingleValued) {
                ReferencePatterns referencePatterns = gbeanData.getReferencePatterns(referenceName);
                AbstractName abstractName;
                try {
                    abstractName = configuration.findGBean(referencePatterns);
                } catch (GBeanNotFoundException e) {
                    throw new InvalidConfigException("Unable to resolve reference named " + referenceName + " in gbean " + gbeanData.getAbstractName(), e);
                }
                gbeanData.setReferencePatterns(referenceName, new ReferencePatterns(abstractName));
            }
        }

        Set newDependencies = new HashSet();
        for (Iterator dependencyIterator = gbeanData.getDependencies().iterator(); dependencyIterator.hasNext();) {
            ReferencePatterns referencePatterns = (ReferencePatterns) dependencyIterator.next();
            AbstractName abstractName;
            try {
                abstractName = configuration.findGBean(referencePatterns);
            } catch (GBeanNotFoundException e) {
                throw new InvalidConfigException("Unable to resolve dependency in gbean " + gbeanData.getAbstractName(), e);
            }
            newDependencies.add(new ReferencePatterns(abstractName));
        }
        gbeanData.setDependencies(newDependencies);

        // If the GBean has a configurationBaseUrl attribute, set it
        // todo remove this when web app cl are config. cl.
        GAttributeInfo attribute = gbeanData.getGBeanInfo().getAttribute("configurationBaseUrl");
        if (attribute != null && attribute.getType().equals("java.net.URL")) {
            try {
                URL baseURL = configuration.getConfigurationResolver().resolve(URI.create(""));
                gbeanData.setAttribute("configurationBaseUrl", baseURL);
            } catch (Exception e) {
                throw new InvalidConfigException("Unable to set attribute named " + "configurationBaseUrl" + " in gbean " + gbeanData.getAbstractName(), e);
            }
        }

        // add a dependency from the gbean to the configuration
        gbeanData.addDependency(configurationName);
    }

    static void startConfigurationGBeans(AbstractName configurationName, Configuration configuration, Kernel kernel, ManageableAttributeStore attributeStore) throws InvalidConfigException {
        Collection gbeans = configuration.getGBeans().values();
        if (attributeStore != null) {
            gbeans = attributeStore.applyOverrides(configuration.getId(), gbeans, configuration.getConfigurationClassLoader());
        }

        // register all the GBeans
        for (Iterator iterator = gbeans.iterator(); iterator.hasNext();) {
            GBeanData gbeanData = (GBeanData) iterator.next();

            // copy the gbeanData object as not to mutate the original
            gbeanData = new GBeanData(gbeanData);

            // preprocess the gbeanData (resolve references, set base url, declare dependency, etc.)
            preprocessGBeanData(configurationName, configuration, gbeanData);

            try {
                kernel.loadGBean(gbeanData, configuration.getConfigurationClassLoader());
            } catch (GBeanAlreadyExistsException e) {
                throw new InvalidConfigException(e);
            }
        }

        try {
            // start the gbeans
            for (Iterator iterator = gbeans.iterator(); iterator.hasNext();) {
                GBeanData gbeanData = (GBeanData) iterator.next();
                AbstractName gbeanName = gbeanData.getAbstractName();
                kernel.startRecursiveGBean(gbeanName);
            }

            // assure all of the gbeans are started
            List unstarted = new ArrayList();
            for (Iterator iterator = gbeans.iterator(); iterator.hasNext();) {
                GBeanData gbeanData = (GBeanData) iterator.next();
                AbstractName gbeanName = gbeanData.getAbstractName();
                if (State.RUNNING_INDEX != kernel.getGBeanState(gbeanName)) {
                    String stateReason = null;
                    if (kernel instanceof BasicKernel) {
                        stateReason = ((BasicKernel) kernel).getStateReason(gbeanName);
                    }
                    String name = gbeanName.toURI().getQuery();
                    if (stateReason != null) {
                        unstarted.add("The service " + name + " did not start because " + stateReason);
                    } else {
                        unstarted.add("The service " + name + " did not start for an unknown reason");
                    }
                }
            }
            if (!unstarted.isEmpty()) {
                StringBuffer message = new StringBuffer();
                message.append("Configuration ").append(configuration.getId()).append(" failed to start due to the following reasons:\n");
                for (Iterator iterator = unstarted.iterator(); iterator.hasNext();) {
                    String reason = (String) iterator.next();
                    message.append("  ").append(reason).append("\n");
                }
                throw new InvalidConfigurationException(message.toString());
            }
        } catch (GBeanNotFoundException e) {
            throw new InvalidConfigException(e);
        }

        for (Iterator iterator = configuration.getChildren().iterator(); iterator.hasNext();) {
            Configuration childConfiguration = (Configuration) iterator.next();
            ConfigurationUtil.startConfigurationGBeans(configurationName, childConfiguration, kernel, attributeStore);
        }
        // todo clean up after failure
    }
}
