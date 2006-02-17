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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.jar.JarFile;

import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.common.DeploymentException;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class AppClientModule extends Module {
    private final Environment clientEnvironment;
    private JarFile earFile;
    private Collection resourceModules;


    public AppClientModule(boolean standAlone, Environment serverEnvironment, Environment clientEnvironment, JarFile moduleFile, String targetPath, XmlObject specDD, XmlObject vendorDD, String originalSpecDD) throws DeploymentException {
        super(standAlone, serverEnvironment, moduleFile, targetPath, specDD, vendorDD, originalSpecDD, null);
        this.clientEnvironment = clientEnvironment;
    }

    public ConfigurationModuleType getType() {
        return ConfigurationModuleType.CAR;
    }

    public Environment getClientEnvironment() {
        return clientEnvironment;
    }

    public JarFile getEarFile() {
        return earFile;
    }

    public void setEarFile(JarFile earFile) {
        this.earFile = earFile;
    }

    public void addClass(URI location, String fqcn, byte[] bytes, DeploymentContext context) throws IOException, URISyntaxException {
        context.addClass(location, fqcn, bytes, true);
    }

    public void setResourceModules(Collection resourceModules) {
        this.resourceModules = resourceModules;
    }

    public Collection getResourceModules() {
        return resourceModules;
    }
}


