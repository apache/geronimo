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
package org.apache.geronimo.plugin.packaging;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.WritableListableRepository;
import org.apache.geronimo.system.configuration.ExecutableConfigurationUtil;
import org.apache.geronimo.system.configuration.RepositoryConfigurationStore;

/**
 * Implementation of ConfigurationStore that loads Configurations from a repository.
 * This implementation is read-only on the assumption that a separate maven task will
 * handle installation of a built package into the repository.
 *
 * @version $Rev$ $Date$
 */
public class MavenConfigStore extends RepositoryConfigurationStore {
    public MavenConfigStore(String objectName, WritableListableRepository repository) {
        super(null, objectName, repository);
    }

    public File createNewConfigurationDir(Artifact configId) {
        try {
            File tmpFile = File.createTempFile("package", ".tmpdir");
            tmpFile.delete();
            tmpFile.mkdir();
            if (!tmpFile.isDirectory()) {
                return null;
            }
            // create the meta-inf dir
            File metaInf = new File(tmpFile, "META-INF");
            metaInf.mkdirs();
            return tmpFile;
        } catch (IOException e) {
            // doh why can't I throw this?
            return null;
        }
    }

    public void install(ConfigurationData configurationData) throws IOException, InvalidConfigException {
        File source = configurationData.getConfigurationDir();
        if (!source.isDirectory()) {
            throw new InvalidConfigException("Source must be a directory: source=" + source);
        }
        Artifact configId = configurationData.getId();
        File targetFile = repository.getLocation(configId);
        ExecutableConfigurationUtil.createExecutableConfiguration(configurationData, null, targetFile);
    }

    public void uninstall(Artifact configID) throws NoSuchConfigException, IOException {
        throw new UnsupportedOperationException();
    }

    public List listConfigurations() {
        throw new UnsupportedOperationException();
    }

    public static final GBeanInfo GBEAN_INFO;

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    static {
        GBeanInfoBuilder builder = GBeanInfoBuilder.createStatic(MavenConfigStore.class, "ConfigurationStore");
        builder.addInterface(ConfigurationStore.class);
        builder.addInterface(ConfigurationStore.class);
        builder.addAttribute("objectName", String.class, false);
        builder.addReference("Repository", WritableListableRepository.class, "Repository");
        builder.setConstructor(new String[]{"objectName", "Repository"});
        GBEAN_INFO = builder.getBeanInfo();
    }
}
