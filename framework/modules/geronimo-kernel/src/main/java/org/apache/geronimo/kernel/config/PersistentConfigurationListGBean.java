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

import java.io.IOException;
import java.util.List;

import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.gbean.wrapper.AbstractServiceWrapper;
import org.apache.geronimo.kernel.repository.Artifact;
import org.osgi.framework.Bundle;

/**
 * @version $Rev:$ $Date:$
 */
@GBean
public class PersistentConfigurationListGBean extends AbstractServiceWrapper<PersistentConfigurationList> implements PersistentConfigurationList {
    public PersistentConfigurationListGBean(@ParamSpecial(type = SpecialAttributeType.bundle)final Bundle bundle) {
        super(bundle, PersistentConfigurationList.class);
    }

    @Override
    public boolean isKernelFullyStarted() {
        return get().isKernelFullyStarted();
    }

    @Override
    public void setKernelFullyStarted(boolean kernelFullyStarted) {
        get().setKernelFullyStarted(kernelFullyStarted);
    }

    @Override
    public void save() throws IOException {
        get().save();
    }

    @Override
    public List<Artifact> restore() throws IOException {
        return get().restore();
    }

    @Override
    public void addConfiguration(Artifact configName) {
        get().addConfiguration(configName);
    }

    @Override
    public void startConfiguration(Artifact configName) {
        get().startConfiguration(configName);
    }

    @Override
    public void stopConfiguration(Artifact configName) {
        get().stopConfiguration(configName);
    }

    @Override
    public void removeConfiguration(Artifact configName) {
        get().removeConfiguration(configName);
    }

    @Override
    public Artifact[] getListedConfigurations(Artifact query) {
        return get().getListedConfigurations(query);
    }

    @Override
    public void migrateConfiguration(Artifact oldName, Artifact newName, Configuration configuration) {
        get().migrateConfiguration(oldName, newName, configuration);
    }

    @Override
    public boolean hasGBeanAttributes(Artifact configName) {
        return get().hasGBeanAttributes(configName);
    }
}
