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
import java.util.LinkedHashSet;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;

import org.apache.xmlbeans.XmlObject;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.deployment.DeploymentContext;

/**
 * @version $Rev$ $Date$
 */
public class WebModule extends Module {

    private final LinkedHashSet webClassPath = new LinkedHashSet();
    private String contextRoot;

    public WebModule(boolean standAlone, URI configId, URI parentId, JarFile moduleFile, String targetPath, XmlObject specDD, XmlObject vendorDD, String originalSpecDD) {
        super(standAlone, configId, parentId, moduleFile, targetPath, specDD, vendorDD, originalSpecDD);
    }

    public String getContextRoot() {
        return contextRoot;
    }

    public void setContextRoot(String contextRoot) {
        this.contextRoot = contextRoot;
    }

    public ConfigurationModuleType getType() {
        return ConfigurationModuleType.WAR;
    }

    public void addClass(URI location, String fqcn, byte[] bytes, DeploymentContext context) throws IOException, URISyntaxException {
        context.addClass(location, fqcn, bytes, false);
        addToWebClasspath(location);
    }

    public void addToWebClasspath(URI location) {
        webClassPath.add(location);
    }

    public URI[] getWebClasspath() {
        URI[] uris = new URI[webClassPath.size()];
        return (URI[])webClassPath.toArray(uris);
    }

}

