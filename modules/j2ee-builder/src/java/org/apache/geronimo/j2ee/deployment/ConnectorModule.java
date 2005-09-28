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
import java.util.List;
import java.io.IOException;

import org.apache.xmlbeans.XmlObject;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.deployment.DeploymentContext;

/**
 * @version $Rev$ $Date$
 */
public class ConnectorModule extends Module {
    public ConnectorModule(boolean standAlone, URI configId, List parentId, JarFile moduleFile, String targetPath, XmlObject specDD, XmlObject vendorDD, String originalSpecDD) {
        super(standAlone, configId, parentId, moduleFile, targetPath, specDD, vendorDD, originalSpecDD, null);
    }

    public ConfigurationModuleType getType() {
        return ConfigurationModuleType.RAR;
    }

    public void addClass(URI location, String fqcn, byte[] bytes, DeploymentContext context) throws IOException, URISyntaxException {
        context.addClass(location, fqcn, bytes, true);
    }

}

