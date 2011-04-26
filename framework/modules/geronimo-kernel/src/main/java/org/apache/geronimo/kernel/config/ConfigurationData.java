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

import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * @version $Rev$ $Date$
 */
public class ConfigurationData implements Serializable {
    private static final long serialVersionUID = 4324193220056650732L;

    /**
     * The time at which this configuration was created.
     */
    private final long created = System.currentTimeMillis();

    /**
     * Identifies the type of configuration (WAR, RAR et cetera)
     */
    private final ConfigurationModuleType moduleType;

    /**
     * Defines the configuration id, parent configurations, and classpath
     */
    private final Environment environment;

    /**
     * List of URIs in this configuration's classpath.  These are for the classes directly included in the configuration
     */
//    private final LinkedHashSet<String> classPath = new LinkedHashSet<String>();

    /**
     * The gbeans contained in this configuration
     */
    private final GBeanState gbeanState;

    /**
     * Child configurations of this configuration
     */
    private final Map<String, ConfigurationData> childConfigurations = new LinkedHashMap<String, ConfigurationData>();

    /**
     * Configurations owned by this configuration.  This is only used for cascade-uninstall.
     */
    private final Set<Artifact> ownedConfigurations = new LinkedHashSet<Artifact>();

    /**
     * The base file of the configuation
     */
    private transient File configurationDir;

    /**
     * The base file of an in-place configuration
     */
    private File inPlaceConfigurationDir;

    /**
     * Should this configuraiton be autoStarted
     */
    private boolean autoStart = true;

    /**
     * The naming system
     */
    private transient Naming naming;

    /**
     * The configuration store from which this configuration was loaded, or null if it was not loaded from a configuration store.
     */
    private transient ConfigurationStore configurationStore;

    /**
     * A transformer to transform the GBeans of this configuration.
     */
    private transient ConfigurationDataTransformer configurationDataTransformer;

    private transient BundleContext bundleContext;
    
    private transient boolean useEnvironment;

    public ConfigurationData(Artifact configId, Naming naming, GBeanState gbeanState) {
        this(new Environment(configId), naming, gbeanState);
    }

    public ConfigurationData(Environment environment, Naming naming, GBeanState gbeanState) {
        if (environment == null) throw new NullPointerException("environment is null");
        if (environment.getConfigId() == null) throw new NullPointerException("environment.configId is null");
        if (naming == null) throw new NullPointerException("naming is null");

        this.environment = environment;
        this.naming = naming;
        this.gbeanState = gbeanState;

        this.moduleType = ConfigurationModuleType.CAR;
    }

    public ConfigurationData(Artifact configId, Naming naming) {
        this(new Environment(configId), naming);
    }

    public ConfigurationData(Environment environment, Naming naming) {
        this(null, null, null, environment, null, null, naming);
    }

    public ConfigurationData(ConfigurationModuleType moduleType,
                             List<GBeanData> gbeans,
                             Map<String, ConfigurationData> childConfigurations,
                             Environment environment,
                             File configurationDir,
                             File inPlaceConfigurationDir,
                             Naming naming
    ) {
        if (naming == null) throw new NullPointerException("naming is null");
        if (environment == null) throw new NullPointerException("environment is null");
        if (environment.getConfigId() == null) throw new NullPointerException("environment.configId is null");
        this.naming = naming;
        if (moduleType != null) {
            this.moduleType = moduleType;
        } else {
            this.moduleType = ConfigurationModuleType.CAR;
        }
        gbeanState = ConfigurationUtil.newGBeanState(gbeans);
        if (childConfigurations != null) {
            this.childConfigurations.putAll(childConfigurations);
        }

        this.environment = environment;
        this.configurationDir = configurationDir;
        this.inPlaceConfigurationDir = inPlaceConfigurationDir;
    }

    public Artifact getId() {
        return environment.getConfigId();
    }

    /**
     * Gets the time at which this configuration was created (or deployed).
     *
     * @return the time at which this configuration was created (or deployed)
     */
    public long getCreated() {
        return created;
    }

    public ConfigurationModuleType getModuleType() {
        return moduleType;
    }

    public Manifest getManifest() throws ManifestException {
        return environment.getManifest();
    }

    public List<GBeanData> getGBeans(Bundle bundle) throws InvalidConfigException {
        if (bundle == null) throw new NullPointerException("bundle is null");
        List<GBeanData> gbeans = gbeanState.getGBeans(bundle);
        if (null == configurationDataTransformer) {
            return gbeans;
        }
        //TODO transforming broken
//        return configurationDataTransformer.transformGBeans(bundle, this, gbeans);
        return gbeans;
    }

    public void addGBean(GBeanData gbeanData) {
        if (gbeanData == null) throw new NullPointerException("gbeanData is null");
        gbeanState.addGBean(gbeanData);
    }

    public GBeanData addGBean(String name, GBeanInfo gbeanInfo) {
        if (name == null) throw new NullPointerException("name is null");
        if (gbeanInfo == null) throw new NullPointerException("gbeanInfo is null");
        return gbeanState.addGBean(name, gbeanInfo, naming, environment);
    }

    public GBeanData addGBean(String name, Class gbeanClass) {
        if (name == null) throw new NullPointerException("name is null");
        if (gbeanClass == null) throw new NullPointerException("gbeanInfo is null");
        return gbeanState.addGBean(name, gbeanClass, naming, environment);
    }

    public GBeanState getGbeanState() {
        return gbeanState;
    }

    public BundleContext getBundleContext() {
        if (bundleContext == null) {
            throw new NullPointerException("bundleContext is null in configurationData: " + getId());
        }
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    /**
     * Gets a map of module name to ConfigurationData for nested
     * configurations (as in, a WAR within an EAR, not dependencies between
     * totally separate configurations).
     *
     * @return map of child configuration name to ConfigurationData for that child
     */
    public Map<String, ConfigurationData> getChildConfigurations() {
        return Collections.unmodifiableMap(childConfigurations);
    }

    public void addChildConfiguration(String moduleName, ConfigurationData configurationData) {
        if (configurationData == null) throw new NullPointerException("configurationData is null");
        childConfigurations.put(moduleName, configurationData);
    }

    /**
     * Gets the configurations owned by this configuration.  This is only used
     * for cascade-uninstall.
     *
     * @return the configurations owned by this configuration
     */
    public Set<Artifact> getOwnedConfigurations() {
        return Collections.unmodifiableSet(ownedConfigurations);
    }

    public void addOwnedConfigurations(Artifact id) {
        if (id == null) throw new NullPointerException("id is null");
        if (!id.isResolved()) throw new IllegalArgumentException("id is not resolved: " + id);
        ownedConfigurations.add(id);
    }

    public Environment getEnvironment() {
        return environment;
    }

    public File getInPlaceConfigurationDir() {
        return inPlaceConfigurationDir;
    }

    public File getConfigurationDir() {
        return configurationDir;
    }

    public void setConfigurationDir(File configurationDir) {
        if (configurationDir == null) throw new NullPointerException("configurationDir is null");
        this.configurationDir = configurationDir;
    }

    public Naming getNaming() {
        return naming;
    }

    public void setNaming(Naming naming) {
        this.naming = naming;
    }

    public boolean isAutoStart() {
        return autoStart;
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public ConfigurationStore getConfigurationStore() {
        return configurationStore;
    }

    public void setConfigurationStore(ConfigurationStore configurationStore) {
        if (configurationStore == null) throw new NullPointerException("configurationStore is null");
        this.configurationStore = configurationStore;
    }

    public ConfigurationDataTransformer getConfigurationDataTransformer() {
        return configurationDataTransformer;
    }

    public void setConfigurationDataTransformer(ConfigurationDataTransformer configurationDataTransformer) {
        this.configurationDataTransformer = configurationDataTransformer;
    }
    
    public boolean isUseEnvironment() {
        return useEnvironment;
    }

    public void setUseEnvironment(boolean useEnvironment) {
        this.useEnvironment = useEnvironment;
    }


    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("ConfigurationData [");
        buf.append("\n  Environment: ").append(environment);
        buf.append("\n  ConfigurationDir: ").append(getConfigurationDir());
        buf.append("\n  autoStart: ").append(isAutoStart());
        for (Map.Entry<String, ConfigurationData> entry: getChildConfigurations().entrySet()) {
            buf.append("\n  Child at: ").append(entry.getKey()).append(entry.getValue());
        }
        return buf.toString();
    }
}
