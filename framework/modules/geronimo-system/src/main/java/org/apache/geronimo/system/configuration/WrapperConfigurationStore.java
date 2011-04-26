/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.system.configuration;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.gbean.wrapper.AbstractServiceWrapper;
import org.apache.geronimo.kernel.config.ConfigurationAlreadyExistsException;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.osgi.framework.Bundle;

/**
 * @version $Rev:$ $Date:$
 */
public class WrapperConfigurationStore extends AbstractServiceWrapper<ConfigurationStore> implements ConfigurationStore {
    
    public WrapperConfigurationStore(@ParamSpecial(type = SpecialAttributeType.bundle) final Bundle bundle) {
        super(bundle, ConfigurationStore.class);
    }

    public boolean containsConfiguration(Artifact configId) {
        return get().containsConfiguration(configId);
    }

    public File createNewConfigurationDir(Artifact configId) throws ConfigurationAlreadyExistsException {
        return get().createNewConfigurationDir(configId);
    }

    public void exportConfiguration(Artifact configId, OutputStream output) throws IOException, NoSuchConfigException {
        get().exportConfiguration(configId, output);
    }

    public AbstractName getAbstractName() {
        return get().getAbstractName();
    }

    public String getObjectName() {
        return get().getObjectName();
    }

    public void install(ConfigurationData configurationData) throws IOException, InvalidConfigException {
        get().install(configurationData);
    }

    public boolean isInPlaceConfiguration(Artifact configId) throws NoSuchConfigException, IOException {
        return get().isInPlaceConfiguration(configId);
    }

    public List<ConfigurationInfo> listConfigurations() {
        return get().listConfigurations();
    }

    public ConfigurationData loadConfiguration(Artifact configId) throws NoSuchConfigException, IOException, InvalidConfigException {
        return get().loadConfiguration(configId);
    }

    public Set<URL> resolve(Artifact configId, String moduleName, String path) throws NoSuchConfigException, MalformedURLException {
        return get().resolve(configId, moduleName, path);
    }

    public void uninstall(Artifact configId) throws NoSuchConfigException, IOException {
        get().uninstall(configId);
    }
}
