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

package org.apache.geronimo.deployment.mavenplugin;

import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;

import org.apache.geronimo.deployment.plugin.factories.DeploymentFactoryImpl;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public abstract class AbstractModuleCommand {
    private String uri;
    private String username;
    private String password;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public abstract void execute() throws Exception;

    protected DeploymentManager getDeploymentManager() throws IOException, DeploymentManagerCreationException {
        if (getUsername() == null) {
            throw new IllegalStateException("No user specified");
        }
        if (getPassword() == null) {
            throw new IllegalStateException("No password specified");
        }
        if (getUri() == null) {
            throw new IllegalStateException("No uri specified");
        }
        new DeploymentFactoryImpl();

        ClassLoader oldcl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            DeploymentFactoryManager factoryManager = DeploymentFactoryManager.getInstance();
            DeploymentManager manager = factoryManager.getDeploymentManager(getUri(), getUsername(), getPassword());
            return manager;
        } finally {
            Thread.currentThread().setContextClassLoader(oldcl);
        }
    }
}
