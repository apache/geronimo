/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.deployment.plugin;

import java.io.InputStream;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressObject;

import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.xmlbeans.XmlObject;

/**
 *
 *
 * @version $Rev$ $Date$
 */
public class DisconnectedServer implements DeploymentServer {
    public boolean isLocal() {
        throw new IllegalStateException("Disconnected");
    }

    public Target[] getTargets() throws IllegalStateException {
        throw new IllegalStateException("Disconnected");
    }

    public TargetModuleID[] getAvailableModules(ModuleType moduleType, Target[] targetList) throws TargetException, IllegalStateException {
        throw new IllegalStateException("Disconnected");
    }

    public TargetModuleID[] getRunningModules(ModuleType moduleType, Target[] targetList) throws TargetException, IllegalStateException {
        throw new IllegalStateException("Disconnected");
    }

    public TargetModuleID[] getNonRunningModules(ModuleType moduleType, Target[] targetList) throws TargetException, IllegalStateException {
        throw new IllegalStateException("Disconnected");
    }

    public ProgressObject distribute(Target[] targetList, ConfigurationBuilder builder, InputStream in, XmlObject plan) throws IllegalStateException {
        throw new IllegalStateException("Disconnected");
    }

    public ProgressObject start(TargetModuleID[] moduleIDList) throws IllegalStateException {
        throw new IllegalStateException("Disconnected");
    }

    public ProgressObject stop(TargetModuleID[] moduleIDList) throws IllegalStateException {
        throw new IllegalStateException("Disconnected");
    }

    public boolean isRedeploySupported() {
        return false;
    }

    public ProgressObject redeploy(TargetModuleID[] moduleIDList, InputStream moduleArchive, InputStream deploymentPlan) throws UnsupportedOperationException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public ProgressObject undeploy(TargetModuleID[] moduleIDList) throws IllegalStateException {
        throw new IllegalStateException("Disconnected");
    }

    public void release() {
    }

    public static final GBeanInfo GBEAN_INFO;
    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory("JSR88 Disconnected Server", DisconnectedServer.class);
        infoFactory.addInterface(DeploymentServer.class);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }
}
