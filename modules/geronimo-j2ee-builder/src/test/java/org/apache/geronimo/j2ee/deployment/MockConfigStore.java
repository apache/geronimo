/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

import javax.xml.namespace.QName;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilder;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.impl.J2EEServerImpl;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.IOUtil;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.NullConfigurationStore;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class MockConfigStore
    extends NullConfigurationStore
{
    protected static final Naming naming = new Jsr77Naming();
    
    protected final Map locations = new HashMap();

    public ConfigurationData loadConfiguration(Artifact configId) throws NoSuchConfigException, IOException, InvalidConfigException {
        ConfigurationData configurationData = new ConfigurationData(configId, naming);
        configurationData.setConfigurationStore(this);
        return configurationData;
    }

    public boolean containsConfiguration(Artifact configID) {
        return true;
    }

    public File createNewConfigurationDir(Artifact configId) {
        try {
            File file = DeploymentUtil.createTempDir();
            locations.put(configId, file);
            return file;
        } catch (IOException e) {
            return null;
        }
    }

    public Set resolve(Artifact configId, String moduleName, String pattern) throws NoSuchConfigException, MalformedURLException {
        File file = (File) locations.get(configId);
        if (file == null) {
            throw new NoSuchConfigException(configId);
        }
        Set matches = IOUtil.search(file, pattern);
        return matches;
    }
}
