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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.geronimo.gbean.GBeanData;

/**
 * @version $Rev$ $Date$
 */
public class ConfigurationData {
    /**
     * URI used to referr to this configuration in the configuration manager
     */
    private URI id;

    /**
     * Identifies the type of configuration (WAR, RAR et cetera)
     */
    private ConfigurationModuleType moduleType;

    /**
     * The uri of the parent of this configuration.  May be null.
     */
    private List parentId;

    /**
     * The domain name of the configurations.  This is used to autogenerate names for sub components.
     */
    private String domain;

    /**
     * The server name of the configurations.  This is used to autogenerate names for sub components.
     */
    private String server;

    /**
     * List of URIs of jar files on which this configuration is dependent on.
     */
    private final LinkedHashSet dependencies = new LinkedHashSet();

    /**
     * List of URIs in this configuration's classpath.
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

    /**
     * If true, then inverse the standard class loading delegation model.
     */
    private boolean inverseClassLoading;
    
    /**
     * Class filters defining the classes hidden from the configuration.
     */
    private final Set hiddenClasses = new HashSet();

    /**
     * Class filters defining the classes that the configuration cannot
     * override.  
     */
    private final Set nonOverridableClasses = new HashSet();
    
    public ConfigurationData() {
    }

    public ConfigurationData(ConfigurationData configurationData) {
        id = configurationData.id;
        moduleType = configurationData.moduleType;
        parentId = configurationData.getParentId();
        domain = configurationData.domain;
        server = configurationData.server;
        setDependencies(new ArrayList(configurationData.dependencies));
        setClassPath(new ArrayList(configurationData.classPath));
        setGBeans(configurationData.gbeans);
        setChildConfigurations(configurationData.childConfigurations);
        inverseClassLoading = configurationData.inverseClassLoading;
        hiddenClasses.addAll(configurationData.hiddenClasses);
        nonOverridableClasses.addAll(configurationData.nonOverridableClasses);
    }

    public URI getId() {
        return id;
    }

    public void setId(URI id) {
        this.id = id;
    }

    public ConfigurationModuleType getModuleType() {
        return moduleType;
    }

    public void setModuleType(ConfigurationModuleType moduleType) {
        this.moduleType = moduleType;
    }

    public List getParentId() {
        return parentId;
    }

    public void setParentId(List parentId) {
        this.parentId = parentId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public List getDependencies() {
        return Collections.unmodifiableList(new ArrayList(dependencies));
    }

    public void setDependencies(List dependencies) {
        this.dependencies.clear();
        for (Iterator iterator = dependencies.iterator(); iterator.hasNext();) {
            URI dependency = (URI) iterator.next();
            addDependency(dependency);
        }
    }

    public void addDependency(URI dependency) {
        assert dependency != null;
        this.dependencies.add(dependency);
    }

    public List getClassPath() {
        return Collections.unmodifiableList(new ArrayList(classPath));
    }

    public void setClassPath(List classPath) {
        this.classPath.clear();
        for (Iterator iterator = classPath.iterator(); iterator.hasNext();) {
            URI location = (URI) iterator.next();
            addClassPathLocation(location);
        }
    }

    public void addClassPathLocation(URI location) {
        assert location != null;
        this.classPath.add(location);
    }

    public List getGBeans() {
        return Collections.unmodifiableList(gbeans);
    }

    public void setGBeans(List gbeans) {
        this.gbeans.clear();
        for (Iterator iterator = gbeans.iterator(); iterator.hasNext();) {
            GBeanData gbeanData = (GBeanData) iterator.next();
            addGBean(gbeanData);
        }
    }

    public void addGBean(GBeanData gbeanData) {
        assert gbeanData != null;
        gbeans.add(gbeanData);
    }

    public List getChildConfigurations() {
        return Collections.unmodifiableList(childConfigurations);
    }

    public void setChildConfigurations(List childConfigurations) {
        this.childConfigurations.clear();
        for (Iterator iterator = childConfigurations.iterator(); iterator.hasNext();) {
            ConfigurationData configurationData = (ConfigurationData) iterator.next();
            addChildConfiguration(configurationData);
        }
    }
    public void addChildConfiguration(ConfigurationData configurationData) {
        assert configurationData != null;
        childConfigurations.add(configurationData);
    }

    public boolean isInverseClassloading() {
        return inverseClassLoading;
    }
    
    public void setInverseClassloading(boolean inverseClassLoading) {
        this.inverseClassLoading = inverseClassLoading;
    }
    
    public Set getHiddenClasses() {
        return hiddenClasses;
    }
    
    public Set getNonOverridableClasses() {
        return nonOverridableClasses;
    }
}
