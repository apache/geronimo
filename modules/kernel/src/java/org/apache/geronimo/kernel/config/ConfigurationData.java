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

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;

/**
 * @version $Rev: 382645 $ $Date$
 */
public class ConfigurationData implements Serializable {
    private static final long serialVersionUID = 4324193220056650732L;

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
    private final LinkedHashSet classPath = new LinkedHashSet();

    /**
     * The gbeans contained in this configuration
     */
    private final GBeanState gbeanState;

    /**
     * Child configurations of this configuration
     */
    private final Map childConfigurations = new LinkedHashMap();

    /**
     * The base file of the configuation
     */
    private transient File configurationDir;

    /**
     * The naming system
     */
    private transient Naming naming;

    /**
     * The configuration store from which this configuration was loaded, or null if it was not loaded from a configuration store.
     */
    private transient ConfigurationStore configurationStore;

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
        this(null, null, null, null, environment, null, naming);
    }

    public ConfigurationData(ConfigurationModuleType moduleType, LinkedHashSet classPath, List gbeans, Map childConfigurations, Environment environment, File configurationDir, Naming naming) {
        if (naming == null) throw new NullPointerException("naming is null");
        this.naming = naming;
        if (moduleType != null) {
            this.moduleType = moduleType;
        } else {
            this.moduleType = ConfigurationModuleType.CAR;
        }
        if (classPath != null) {
            this.classPath.addAll(classPath);
        }
        gbeanState = ConfigurationUtil.newGBeanState(gbeans);
        if (childConfigurations != null) {
            this.childConfigurations.putAll(childConfigurations);
        }

        if (environment == null) throw new NullPointerException("environment is null");
        if (environment.getConfigId() == null) throw new NullPointerException("environment.configId is null");
        this.environment = environment;
        this.configurationDir = configurationDir;
    }

    public Artifact getId() {
        return environment.getConfigId();
    }

    public ConfigurationModuleType getModuleType() {
        return moduleType;
    }

    public List getClassPath() {
        return Collections.unmodifiableList(new ArrayList(classPath));
    }

    public List getGBeans(ClassLoader classLoader) throws InvalidConfigException {
        return gbeanState.getGBeans(classLoader);
    }

    public void addGBean(GBeanData gbeanData) {
        gbeanState.addGBean(gbeanData);
    }

    public GBeanData addGBean(String name, GBeanInfo gbeanInfo) {
        return gbeanState.addGBean(name, gbeanInfo, naming, environment);
    }

    public GBeanState getGbeanState() {
        return gbeanState;
    }

    public Map getChildConfigurations() {
        return Collections.unmodifiableMap(childConfigurations);
    }

    public void addChildConfiguration(ConfigurationData configurationData) {
        childConfigurations.put(configurationData.getId(), configurationData);
    }

    public Environment getEnvironment() {
        return environment;
    }

    public File getConfigurationDir() {
        return configurationDir;
    }

    public void setConfigurationDir(File configurationDir) {
        this.configurationDir = configurationDir;
    }

    public Naming getNaming() {
        return naming;
    }

    public void setNaming(Naming naming) {
        this.naming = naming;
    }

    public ConfigurationStore getConfigurationStore() {
        return configurationStore;
    }

    public void setConfigurationStore(ConfigurationStore configurationStore) {
        this.configurationStore = configurationStore;
    }
}
