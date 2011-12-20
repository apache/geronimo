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


package org.apache.geronimo.kernel.config;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.gbean.wrapper.AbstractServiceWrapper;
import org.apache.geronimo.kernel.repository.Artifact;
import org.osgi.framework.Bundle;

/**
 * @version $Rev:$ $Date:$
 */

@GBean(j2eeType = "ConfigurationStore")
public class ConfigurationStoreGBean extends AbstractServiceWrapper<ConfigurationStore> implements ConfigurationStore {
    public ConfigurationStoreGBean(@ParamSpecial(type = SpecialAttributeType.bundle)final Bundle bundle) {
        super(bundle, ConfigurationStore.class);
    }

    @Override
    public boolean isInPlaceConfiguration(Artifact configId) throws NoSuchConfigException, IOException {
        return get().isInPlaceConfiguration(configId);
    }

    @Override
    public void install(ConfigurationData configurationData) throws IOException, InvalidConfigException {
        get().install(configurationData);
    }

    @Override
    public void uninstall(Artifact configId) throws NoSuchConfigException, IOException {
        get().uninstall(configId);
    }

    @Override
    public ConfigurationData loadConfiguration(Artifact configId) throws NoSuchConfigException, IOException, InvalidConfigException {
        return get().loadConfiguration(configId);
    }

    @Override
    public boolean containsConfiguration(Artifact configId) {
        return get().containsConfiguration(configId);
    }

    @Override
    public String getObjectName() {
        return get().getObjectName();
    }

    @Override
    public AbstractName getAbstractName() {
        return get().getAbstractName();
    }

    @Override
    public List<ConfigurationInfo> listConfigurations() {
        return get().listConfigurations();
    }

    @Override
    public File createNewConfigurationDir(Artifact configId) throws ConfigurationAlreadyExistsException {
        return get().createNewConfigurationDir(configId);
    }

    @Override
    public Set<URL> resolve(Artifact configId, String moduleName, String path) throws NoSuchConfigException, MalformedURLException {
        return get().resolve(configId, moduleName, path);
    }

    @Override
    public void exportConfiguration(Artifact configId, OutputStream output) throws IOException, NoSuchConfigException {
        get().exportConfiguration(configId, output);
    }
}
