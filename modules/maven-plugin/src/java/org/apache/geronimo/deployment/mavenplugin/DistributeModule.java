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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.status.ProgressObject;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/07/25 08:22:07 $
 *
 * */
public class DistributeModule extends AbstractModuleCommand {

    private String module;
    private String plan;
    private String home;

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public void execute() throws Exception {
        DeploymentManager manager = getDeploymentManager();

        Target[] targets = manager.getTargets();
        File moduleFile = (getModule() == null)? null: getFile(getModule());
        File planFile = (getPlan() == null)? null: getFile((getPlan()));
        ProgressObject progress = manager.distribute(targets, moduleFile, planFile);
        DeploymentClient.waitFor(progress);
    }

    private File getFile(String location) throws MalformedURLException {
        File f = new File(location);
        if (f.exists() && f.canRead()) {
            try {
                return f.getCanonicalFile();
            } catch (IOException e) {
                throw (MalformedURLException) new MalformedURLException("Invalid location: " + location).initCause(e);
            }
        }
        try {
            return new File(new File(getHome()).toURI().resolve(location).toURL().getFile());
        } catch (IllegalArgumentException e) {
            // thrown by URI.resolve if the location is not valid
            throw (MalformedURLException) new MalformedURLException("Invalid location: " + location).initCause(e);
        }
    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }
}
