/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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
import java.net.URL;
import java.util.Collection;
import java.util.jar.JarFile;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.osgi.framework.Bundle;

/**
 * @version $Rev:386276 $ $Date$
 */
public interface ModuleBuilder {

    /**
     * Starting with bundle
     * @param bundle
     * @param naming
     * @param idBuilder
     * @return
     * @throws DeploymentException
     */
    Module createModule(Bundle bundle, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException;

    /**
     * standalone module
     * @param plan
     * @param moduleFile
     * @param naming
     * @param idBuilder
     * @return
     * @throws DeploymentException
     */
    Module createModule(File plan, JarFile moduleFile, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException;

    /**
     * Component of ear
     * @param plan
     * @param moduleFile
     * @param targetPath
     * @param specDDUrl
     * @param environment
     * @param moduleContextInfo
     * @param parentModule
     *@param naming
     * @param idBuilder   @return
     * @throws DeploymentException
     */
    Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment environment, Object moduleContextInfo, Module parentModule, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException;

    void installModule(JarFile earFile, EARContext earContext, Module module, Collection configurationStores, ConfigurationStore targetConfigurationStore, Collection repository) throws DeploymentException;

    void initContext(EARContext earContext, Module module, Bundle bundle) throws DeploymentException;

    void addGBeans(EARContext earContext, Module module, Bundle bundle, Collection repository) throws DeploymentException;

    String getSchemaNamespace();
}
