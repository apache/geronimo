/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.j2ee;

import java.util.Set;

import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Revision$ $Date$
 */
public class ApplicationInfo {
    private ConfigurationModuleType type;
    private Environment environment;
    private String applicationName;
    private XmlObject specDD;
    private XmlObject vendorDD;
    private Set modules;
    private Set moduleLocations;
    private String originalSpecDD;

    public ApplicationInfo() {
    }

    public ApplicationInfo(ConfigurationModuleType type, Environment environment, String applicationName, XmlObject specDD, XmlObject vendorDD, Set modules, Set moduleLocations, String originalSpecDD) {
        assert type != null;
        assert environment != null;
        assert modules != null;
        assert moduleLocations != null;

        this.type = type;
        this.environment = environment;
        this.applicationName = applicationName;
        this.specDD = specDD;
        this.vendorDD = vendorDD;
        this.modules = modules;
        this.moduleLocations = moduleLocations;
        this.originalSpecDD = originalSpecDD;
    }

    public ConfigurationModuleType getType() {
        return type;
    }

    public void setType(ConfigurationModuleType type) {
        this.type = type;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public XmlObject getVendorDD() {
        return vendorDD;
    }

    public void setVendorDD(XmlObject vendorDD) {
        this.vendorDD = vendorDD;
    }

    public XmlObject getSpecDD() {
        return specDD;
    }

    public void setSpecDD(XmlObject specDD) {
        this.specDD = specDD;
    }

    public Set getModules() {
        return modules;
    }

    public void setModules(Set modules) {
        this.modules = modules;
    }

    public Set getModuleLocations() {
        return moduleLocations;
    }

    public void setModuleLocations(Set moduleLocations) {
        this.moduleLocations = moduleLocations;
    }

    public String getOriginalSpecDD() {
        return originalSpecDD;
    }

    public void setOriginalSpecDD(String originalSpecDD) {
        this.originalSpecDD = originalSpecDD;
    }
}
