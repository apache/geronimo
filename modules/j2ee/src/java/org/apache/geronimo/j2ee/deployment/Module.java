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
import org.apache.geronimo.deployment.util.DeploymentUtil;

/**
 * @version $Rev$ $Date$
 */
public abstract class Module {
    private final boolean standAlone;
    private final String name;
    private final URI configId;
    private final URI parentId;
    private final URI moduleURI;
    private final JarFile moduleFile;
    private final String targetPath;
    private final XmlObject specDD;
    private final XmlObject vendorDD;
    private final String originalSpecDD;

    protected Module(boolean standAlone, URI configId, URI parentId, JarFile moduleFile, String targetPath, XmlObject specDD, XmlObject vendorDD, String originalSpecDD) {
        this.standAlone = standAlone;
        this.configId = configId;
        this.parentId = parentId;
        this.moduleFile = moduleFile;
        this.targetPath = targetPath;
        this.specDD = specDD;
        this.vendorDD = vendorDD;
        this.originalSpecDD = originalSpecDD;

        if (standAlone) {
            name = configId.toString();
            moduleURI = URI.create("");
        } else {
            name = targetPath;
            moduleURI = URI.create(targetPath);
        }
    }

    public abstract ConfigurationModuleType  getType();

    public String getName() {
        return name;
    }

    public boolean isStandAlone() {
        return standAlone;
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

    public void close() {
        DeploymentUtil.close(moduleFile);
    }
}
