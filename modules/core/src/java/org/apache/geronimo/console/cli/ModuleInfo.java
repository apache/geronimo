/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.console.cli;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;
import javax.enterprise.deploy.spi.DConfigBeanRoot;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

/**
 * Describes a generic J2EE module.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:25 $
 */
public abstract class ModuleInfo {
    public File file;
    public JarFile jarFile;
    public final DeploymentContext context;

    public ModuleInfo(DeploymentContext context) {
        this.context = context;
    }

    public abstract boolean initialize(); //todo: change this up somehow
    public abstract String getFileName();
    public abstract DConfigBeanRoot getConfigRoot();
    public abstract void loadDConfigBean(File source) throws IOException, ConfigurationException;
    public abstract void saveDConfigBean(File dest) throws IOException, ConfigurationException;
}
