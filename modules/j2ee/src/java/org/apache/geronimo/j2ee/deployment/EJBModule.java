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
import java.util.jar.JarFile;

import org.apache.xmlbeans.XmlObject;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;

/**
 * @version $Rev$ $Date$
 */
public class EJBModule extends Module {
    public EJBModule(String name, URI configId, URI parentId, URI moduleURI, JarFile moduleFile, String targetPath, XmlObject specDD, XmlObject vendorDD, String originalSpecDD) {
        super(name, configId, parentId, moduleURI, moduleFile, targetPath, specDD, vendorDD, originalSpecDD);
    }

    public ConfigurationModuleType getType() {
        return ConfigurationModuleType.EJB;
    }
}

