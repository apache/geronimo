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

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanQuery;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.Environment;

import javax.management.ObjectName;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @version $Rev:386276 $ $Date$
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
        ConfigurationResolver configurationResolver = new ConfigurationResolver(configuration.getAbstractName().getArtifact(), null);
        configuration.setAttribute("configurationResolver", configurationResolver);

        return loadBootstrapConfiguration(kernel, configuration, classLoader);
    }

    public static AbstractName loadBootstrapConfiguration(Kernel kernel, ConfigurationData configurationData, ClassLoader classLoader) throws Exception {
        GBeanData configuration = toConfigurationGBeanData(configurationData, null, null, null);
        return loadBootstrapConfiguration(kernel, configuration, classLoader);
    }

    private static AbstractName loadBootstrapConfiguration(Kernel kernel, GBeanData configurationGBeanData, ClassLoader classLoader) throws Exception {
        Environment environment = (Environment) configurationGBeanData.getAttribute("environment");
        Artifact configId = environment.getConfigId();
        AbstractName configurationName = Configuration.getConfigurationAbstractName(configId);
        configurationGBeanData.setAbstractName(configurationName);
        configurationGBeanData.setAttribute("naming", kernel.getNaming());

        // for a bootstrap we should have an empty kernel, so clear the references and dependencies
        configurationGBeanData.setAttribute("artifactManager", null);
        configurationGBeanData.setAttribute("artifactResolver", null);
        environment.setDependencies(Collections.EMPTY_LIST);

        // load and start the gbean
        kernel.loadGBean(configurationGBeanData, classLoader);
        kernel.startGBean(configurationName);

        Configuration configuration = (Configuration) kernel.getProxyManager().createProxy(configurationName, Configuration.class);

        // get the gbeans and classloader
        Collection gbeans = configuration.getGBeans().values();

        startConfigurationGBeans(gbeans, configuration, kernel);

        ConfigurationManager configurationManager = getConfigurationManager(kernel);
        configurationManager.loadConfiguration(configId);
        return configurationName;
    }

    public static void storeBootstrapConfiguration(ConfigurationData configurationData, OutputStream out) throws InvalidConfigException, IOException {
        ObjectOutputStream objectOutputStream = null;
        try {
            GBeanData configurationGBeanData = toConfigurationGBeanData(configurationData, null, null, null);
            //TODO configid DAIN please review!!
            //configurationResolver is not serializable in principle, but is useful for local manipulation of
            //ConfigurationData/ configuration as a GBeanData.
            configurationGBeanData.setAttribute("configurationResolver", null);
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
                    //TODO really?
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

    static void preprocessGBeanData(Configuration configuration, GBeanData gbeanData) throws InvalidConfigException {
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
        gbeanData.addDependency(configuration.getAbstractName());
    }

    static void startConfigurationGBeans(Collection gbeans, Configuration configuration, Kernel kernel) throws InvalidConfigException {
        // register all the GBeans
        for (Iterator iterator = gbeans.iterator(); iterator.hasNext();) {
            GBeanData gbeanData = (GBeanData) iterator.next();

            // copy the gbeanData object as not to mutate the original
            gbeanData = new GBeanData(gbeanData);

            // preprocess the gbeanData (resolve references, set base url, declare dependency, etc.)
            preprocessGBeanData(configuration, gbeanData);
//            log.trace("Registering GBean " + gbeanData.getName());

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
            for (Iterator iterator = gbeans.iterator(); iterator.hasNext();) {
                GBeanData gbeanData = (GBeanData) iterator.next();
                AbstractName gbeanName = gbeanData.getAbstractName();
                if (State.RUNNING_INDEX != kernel.getGBeanState(gbeanName)) {
                    throw new InvalidConfigurationException("Configuration " + configuration.getId() + " failed to start because gbean " + gbeanName + " did not start");
                }
            }
        } catch (GBeanNotFoundException e) {
            throw new InvalidConfigException(e);
        }
        // todo clean up after failure
    }
}
