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
package org.apache.geronimo.j2ee.deployment;

import java.util.jar.JarFile;
import java.net.URI;

import org.apache.xmlbeans.XmlObject;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;

/**
 * @version $Rev$ $Date$
 */
public abstract class Module {
    private String name;
    private final URI configId;
    private final URI parentId;
    private final URI moduleURI;
    private final JarFile moduleFile;
    private final String targetPath;
    private final XmlObject specDD;
    private final XmlObject vendorDD;
    private final String originalSpecDD;

    public Module(String name, URI configId, URI parentId, URI moduleURI, JarFile moduleFile, String targetPath, XmlObject specDD, XmlObject vendorDD, String originalSpecDD) {
        this.name = name;
        this.configId = configId;
        this.parentId = parentId;
        this.moduleURI = moduleURI;
        this.moduleFile = moduleFile;
        this.targetPath = targetPath;
        this.specDD = specDD;
        this.vendorDD = vendorDD;
        this.originalSpecDD = originalSpecDD;
    }

    public abstract ConfigurationModuleType  getType();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public URI getConfigId() {
        return configId;
    }

    public URI getParentId() {
        return parentId;
    }

    public URI getModuleURI() {
        return moduleURI;
    }

    public JarFile getModuleFile() {
        return moduleFile;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public XmlObject getSpecDD() {
        return specDD;
    }

    public XmlObject getVendorDD() {
        return vendorDD;
    }

    public String getOriginalSpecDD() {
        return originalSpecDD;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Module) {
            Module module = (Module) obj;
            return name.equals(module.name);
        }
        return false;
    }
}
