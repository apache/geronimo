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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.jar.JarFile;
import java.util.Collection;
import java.util.Set;
import java.io.IOException;

import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev: 46019 $ $Date: 2004-09-14 02:56:06 -0700 (Tue, 14 Sep 2004) $
 */
public class AppClientModule extends Module {
    private JarFile earFile;
    private Collection resourceModules;

    public AppClientModule(boolean standAlone, URI configId, URI[] parentId, JarFile moduleFile, String targetPath, XmlObject specDD, XmlObject vendorDD, String originalSpecDD) {
        super(standAlone, configId, parentId, moduleFile, targetPath, specDD, vendorDD, originalSpecDD);
    }

    public ConfigurationModuleType getType() {
        return ConfigurationModuleType.CAR;
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


