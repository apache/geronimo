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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.repository.Repository;

/**
 * Implementation of ConfigurationStore that loads Configurations from a repository.
 * This implementation is read-only on the assumption that a separate maven task will
 * handle installation of a built package into the repository.
 *
 * @version $Rev$ $Date$
 */
public class MavenConfigStore implements ConfigurationStore {
    private final ObjectName objectName;
    private final Repository repository;

    public MavenConfigStore(String objectName, Repository repository) throws MalformedObjectNameException {
        this.objectName = new ObjectName(objectName);
        this.repository = repository;
    }

    public String getObjectName() {
        return objectName.toString();
    }

    public GBeanData getConfiguration(URI id) throws NoSuchConfigException, IOException, InvalidConfigException {
        if (!repository.hasURI(id)) {
            throw new NoSuchConfigException("Configuration not found: " + id);
        }

        URL stateURL = new URL(getBaseURL(id), "META-INF/config.ser");
        InputStream jis = stateURL.openStream();
        try {
            ObjectInputStream ois = new ObjectInputStream(jis);
            GBeanData config = new GBeanData();
            config.readExternal(ois);
            config.setReferencePattern("ConfigurationStore", objectName);
            return config;
        } catch (ClassNotFoundException e) {
            throw new InvalidConfigException("Unable to load class from config: " + id, e);
        } finally {
            jis.close();
        }
    }

    public boolean containsConfiguration(URI configID) {
        return repository.hasURI(configID);
    }

    public File createNewConfigurationDir() {
        try {
            File tmpFile = File.createTempFile("package", ".tmpdir");
            tmpFile.delete();
            tmpFile.mkdir();
            if (!tmpFile.isDirectory()) {
                return null;
            }
            return tmpFile;
        } catch (IOException e) {
            // doh why can't I throw this?
            return null;
        }
    }

    public URL getBaseURL(URI id) throws NoSuchConfigException {
        try {
            return new URL("jar:" + repository.getURL(id).toString() + "!/");
        } catch (MalformedURLException e) {
            throw new AssertionError();
        }
    }

    public URI install(URL source) throws IOException, InvalidConfigException {
        throw new UnsupportedOperationException();
    }

    public URI install(File source) throws IOException, InvalidConfigException {
        throw new UnsupportedOperationException();
    }

    public void uninstall(URI configID) throws NoSuchConfigException, IOException {
        throw new UnsupportedOperationException();
    }

    public void updateConfiguration(Configuration configuration) throws NoSuchConfigException, Exception {
        // we don't store persistent state
    }

    public List listConfiguations() {
        throw new UnsupportedOperationException();
    }


    public static final GBeanInfo GBEAN_INFO;

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    static {
        GBeanInfoBuilder builder = new GBeanInfoBuilder(MavenConfigStore.class);
        builder.addInterface(ConfigurationStore.class);
        builder.addAttribute("objectName", String.class, false);
        builder.addReference("Repository", Repository.class);
        builder.setConstructor(new String[]{"objectName", "Repository"});
        GBEAN_INFO = builder.getBeanInfo();
    }
}
