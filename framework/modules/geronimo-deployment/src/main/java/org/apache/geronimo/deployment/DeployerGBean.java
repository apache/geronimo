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


package org.apache.geronimo.deployment;

import java.io.File;
import java.util.List;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.gbean.wrapper.AbstractServiceWrapper;
import org.osgi.framework.Bundle;

/**
 * @version $Rev:$ $Date:$
 */
@GBean
public class DeployerGBean extends AbstractServiceWrapper<Deployer> implements Deployer {

    public DeployerGBean(@ParamSpecial(type = SpecialAttributeType.bundle)final Bundle bundle) {
        super(bundle, Deployer.class);
    }

    @Override
    public List<String> deploy(boolean inPlace, File moduleFile, File planFile) throws DeploymentException {
        return get().deploy(inPlace, moduleFile, planFile);
    }

    @Override
    public List<String> deploy(boolean inPlace, File moduleFile, File planFile, String targetConfigStore) throws DeploymentException {
        return get().deploy(inPlace,  moduleFile, planFile, targetConfigStore);
    }

    @Override
    public String getRemoteDeployUploadURL() {
        return get().getRemoteDeployUploadURL();
    }

    @Override
    public List<String> deploy(boolean inPlace, File moduleFile, File planFile, File targetFile, boolean install, String mainClass, String mainGBean, String mainMethod, String manifestConfigurations, String classPath, String endorsedDirs, String extensionDirs, String targetConfigurationStore) throws DeploymentException {
        return get().deploy(inPlace, moduleFile, planFile, targetFile, install, mainClass, mainGBean, mainMethod, manifestConfigurations, classPath, endorsedDirs, extensionDirs, targetConfigurationStore);
    }
}
