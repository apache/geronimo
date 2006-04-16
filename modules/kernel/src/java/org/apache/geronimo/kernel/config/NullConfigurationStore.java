/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.kernel.config;

import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.gbean.AbstractName;

import java.io.IOException;
import java.io.File;
import java.io.OutputStream;
import java.util.List;
import java.net.URL;
import java.net.URI;
import java.net.MalformedURLException;

/**
 * A non-functional configuration store that can be extended to be useful.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class NullConfigurationStore implements ConfigurationStore {
    public boolean isInPlaceConfiguration(Artifact configId) throws NoSuchConfigException, IOException {
        return false;
    }
    
    public void install(ConfigurationData configurationData) throws IOException, InvalidConfigException {
    }

    public void uninstall(Artifact configId) throws NoSuchConfigException, IOException {
    }

    public ConfigurationData loadConfiguration(Artifact configId) throws NoSuchConfigException, IOException, InvalidConfigException {
        return null;
    }

    public boolean containsConfiguration(Artifact configId) {
        return false;
    }

    public String getObjectName() {
        return null;
    }

    public AbstractName getAbstractName() {
        return null;
    }

    public List listConfigurations() {
        return null;
    }

    public File createNewConfigurationDir(Artifact configId) throws ConfigurationAlreadyExistsException {
        return null;
    }

    public URL resolve(Artifact configId, String moduleName, URI uri) throws NoSuchConfigException, MalformedURLException {
        return null;
    }

    public void exportConfiguration(Artifact configId, OutputStream output) throws IOException, NoSuchConfigException {
    }
}
