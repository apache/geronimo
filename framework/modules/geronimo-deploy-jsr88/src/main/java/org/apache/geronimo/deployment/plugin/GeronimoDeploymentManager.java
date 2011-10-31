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
package org.apache.geronimo.deployment.plugin;

import java.net.URL;

import javax.enterprise.deploy.spi.DeploymentManager;

import org.apache.geronimo.deployment.plugin.eba.EBADeploymentManager;
import org.apache.geronimo.system.bundle.BundleRecorder;
import org.apache.geronimo.system.plugin.PluginInstaller;
import org.apache.geronimo.system.plugin.ServerArchiver;

/**
 * Enhanced features for Geronimo deployment manager
 *
 * @version $Rev$ $Date$
 */
public interface GeronimoDeploymentManager extends DeploymentManager, PluginInstaller, ServerArchiver, EBADeploymentManager, BundleRecorder {

    public <T> T getImplementation(Class<T> clazz);
    public URL[] getRepositories();
    public boolean isRedefineClassesSupported();
    
}
