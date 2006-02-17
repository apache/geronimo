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
import java.net.URISyntaxException;
import java.io.IOException;
import java.io.File;

import org.apache.xmlbeans.XmlObject;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.common.DeploymentException;

/**
 * @version $Rev$ $Date$
 */
public abstract class Module {
    private final boolean standAlone;
    private final String name;
    private final Environment environment;
    private final URI moduleURI;
    private final JarFile moduleFile;
    private final String targetPath;
    private final URI targetPathURI;
    private final XmlObject specDD;
    private final XmlObject vendorDD;
    private final String originalSpecDD;
    private final String namespace;

    private URI uniqueModuleLocation;

    protected Module(boolean standAlone, Environment environment, JarFile moduleFile, String targetPath, XmlObject specDD, XmlObject vendorDD, String originalSpecDD, String namespace) throws DeploymentException {
        assert targetPath != null: "targetPath is null";

        this.standAlone = standAlone;
        this.environment = environment;
        this.moduleFile = moduleFile;
        this.targetPath = targetPath;
        this.specDD = specDD;
        this.vendorDD = vendorDD;
        this.originalSpecDD = originalSpecDD;
        this.namespace = namespace;

        if (standAlone) {
            //TODO configid
            try {
                name = environment.getConfigId().toURI().toString();
            } catch (URISyntaxException e) {
                throw new DeploymentException("Could not construct module name from environment configId");
            }
            moduleURI = URI.create("");
        } else {
            name = targetPath;
            moduleURI = URI.create(targetPath);
        }

        targetPathURI = URI.create(targetPath + "/");
    }

    public abstract ConfigurationModuleType getType();

    public String getName() {
        return name;
    }

    public boolean isStandAlone() {
        return standAlone;
    }

    public Environment getEnvironment() {
        return environment;
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

    public URI getTargetPathURI() {
        return targetPathURI;
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

    public String getNamespace() {
        return namespace;
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

    public void addClass(String fqcn, byte[] bytes, DeploymentContext context) throws IOException, URISyntaxException {
        URI location = getUniqueModuleLocation(context);
        addClass(location, fqcn, bytes, context);
    }

    private URI getUniqueModuleLocation(DeploymentContext context) {
        if (uniqueModuleLocation == null) {
            String suffix = "";
            URI candidateURI;
            File candidateFile;
            int i = 1;
            do {
                candidateURI = URI.create(targetPath + "-generated" + suffix + "/");
                candidateFile = context.getTargetFile(candidateURI);
                suffix = "" + i++;
            } while (candidateFile.exists());
            candidateFile.mkdirs();
            uniqueModuleLocation = candidateURI;
        }
        return uniqueModuleLocation;
    }

    public abstract void addClass(URI location, String fqcn, byte[] bytes, DeploymentContext context) throws IOException, URISyntaxException;
}
