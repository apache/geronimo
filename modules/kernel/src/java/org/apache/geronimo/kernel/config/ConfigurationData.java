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

import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.AbstractName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.io.File;

/**
 * @version $Rev: 382645 $ $Date$
 */
public class ConfigurationData {

    /**
     * Identifies the type of configuration (WAR, RAR et cetera)
     */
    private final ConfigurationModuleType moduleType;


    /**
     * List of URIs in this configuration's classpath.  These are for the classes directly included in the configuration
     */
    private final LinkedHashSet classPath = new LinkedHashSet();

    /**
     * GBeans contained in this configuration.
     */
    private final List gbeans = new ArrayList();

    /**
     * Child configurations of this configuration
     */
    private final List childConfigurations = new ArrayList();

    private final Environment environment;

    private final File configurationDir;
    private final Naming naming;

    public ConfigurationData(Artifact configId, Naming naming) {
        this(null, null, null, null, new Environment(configId), null, naming);
    }

    public ConfigurationData(ConfigurationModuleType moduleType, LinkedHashSet classPath, List gbeans, List childConfigurations, Environment environment, File configurationDir, Naming naming) {
        this.naming = naming;
        if (moduleType != null) {
            this.moduleType = moduleType;
        } else {
            this.moduleType = ConfigurationModuleType.CAR;
        }
        if (classPath != null) {
            this.classPath.addAll(classPath);
        }
        if (gbeans != null){
            this.gbeans.addAll(gbeans);
        }
        if (childConfigurations != null) {
            this.childConfigurations.addAll(childConfigurations);
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

    public List getGBeans() {
        return Collections.unmodifiableList(gbeans);
    }

    public void addGBean(GBeanData gbeanData) {
        gbeans.add(gbeanData);
    }

    public GBeanData addGBean(String name, GBeanInfo gbeanInfo) {
        String j2eeType = gbeanInfo.getJ2eeType();
        if (j2eeType == null) j2eeType = "GBean";
        AbstractName abstractName = naming.createRootName(environment.getConfigId(), name, j2eeType);
        GBeanData gBeanData = new GBeanData(abstractName, gbeanInfo);
        addGBean(gBeanData);
        return gBeanData;
    }

    public List getChildConfigurations() {
        return Collections.unmodifiableList(childConfigurations);
    }

    public Environment getEnvironment() {
        return environment;
    }

    public File getConfigurationDir() {
        return configurationDir;
    }

}
