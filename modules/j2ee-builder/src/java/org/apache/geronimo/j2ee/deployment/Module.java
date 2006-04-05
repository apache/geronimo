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
import org.apache.geronimo.gbean.AbstractName;

/**
 * @version $Rev$ $Date$
 */
public abstract class Module {
    private final boolean standAlone;

    private final AbstractName moduleName;
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

    private EARContext earContext;

    private URI uniqueModuleLocation;

    protected Module(boolean standAlone, AbstractName moduleName, Environment environment, JarFile moduleFile, String targetPath, XmlObject specDD, XmlObject vendorDD, String originalSpecDD, String namespace) {
        assert targetPath != null: "targetPath is null";
        assert moduleName != null: "moduleName is null";

        this.standAlone = standAlone;
        this.moduleName = moduleName;
        this.environment = environment;
        this.moduleFile = moduleFile;
        this.targetPath = targetPath;
        this.specDD = specDD;
        this.vendorDD = vendorDD;
        this.originalSpecDD = originalSpecDD;
        this.namespace = namespace;

        if (standAlone) {
            name = environment.getConfigId().toString();
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

    public AbstractName getModuleName() {
        return moduleName;
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

    private URI getUniqueModuleLocation(DeploymentContext context) throws IOException {
        if (uniqueModuleLocation == null) {
            URI metainfUri = URI.create("META-INF/");
            File metainfDir = context.getTargetFile(metainfUri);
            if (!metainfDir.exists()) {
                metainfDir.mkdirs();
            }
            if (!metainfDir.isDirectory()) {
                throw new IOException("META-INF directory exists but is not a directory: " + metainfDir.getAbsolutePath());
            }
            if (!metainfDir.canRead()) {
                throw new IOException("META-INF directory is not readable: " + metainfDir.getAbsolutePath());
            }
            if (!metainfDir.canWrite()) {
                throw new IOException("META-INF directory is not writable: " + metainfDir.getAbsolutePath());
            }

            String suffix = "";
            URI generatedUri;
            File generatedDir;
            int i = 0;
            do {
                generatedUri = metainfUri.resolve("geronimo-generated" + suffix + "/");
                generatedDir = context.getTargetFile(generatedUri);
                suffix = "" + i++;
            } while (generatedDir.exists());
            generatedDir.mkdirs();

            // these shouldn't ever happen, but let's check anyway
            if (!generatedDir.isDirectory()) {
                throw new IOException("Geronimo generated classes directory exists but is not a directory: " + generatedDir.getAbsolutePath());
            }
            if (!generatedDir.canRead()) {
                throw new IOException("Geronimo generated classes directory is not readable: " + generatedDir.getAbsolutePath());
            }
            if (!generatedDir.canWrite()) {
                throw new IOException("Geronimo generated classes directory is not writable: " + generatedDir.getAbsolutePath());
            }

            uniqueModuleLocation = generatedUri;
        }
        return uniqueModuleLocation;
    }

    public EARContext getEarContext() {
        return earContext;
    }

    public void setEarContext(EARContext earContext) {
        this.earContext = earContext;
    }

    public abstract void addClass(URI location, String fqcn, byte[] bytes, DeploymentContext context) throws IOException, URISyntaxException;
}
