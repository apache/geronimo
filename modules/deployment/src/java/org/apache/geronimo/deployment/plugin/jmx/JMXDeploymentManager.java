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
package org.apache.geronimo.deployment.plugin.jmx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.DConfigBeanVersionType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.management.remote.JMXConnector;
import javax.management.MBeanServerConnection;

import org.apache.geronimo.kernel.KernelMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.MBeanProxyFactory;
import org.apache.geronimo.deployment.plugin.local.StartCommand;
import org.apache.geronimo.deployment.plugin.local.StopCommand;
import org.apache.geronimo.deployment.plugin.TargetImpl;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/02 07:05:30 $
 */
public class JMXDeploymentManager implements DeploymentManager {
    private JMXConnector jmxConnector;
    private MBeanServerConnection mbServerConnection;
    private KernelMBean kernel;

    public JMXDeploymentManager(JMXConnector jmxConnector) throws IOException {
        this.jmxConnector = jmxConnector;
        mbServerConnection = jmxConnector.getMBeanServerConnection();
        kernel = (KernelMBean) MBeanProxyFactory.getProxy(KernelMBean.class, mbServerConnection, Kernel.KERNEL);
    }

    public void release() {
        try {
            jmxConnector.close();
        } catch (IOException e) {
            // can't do much with this
        } finally {
            mbServerConnection = null;
            kernel = null;
        }
    }

    public Target[] getTargets() {
        if (kernel == null) {
            throw new IllegalStateException("Disconnected");
        }
        Target target = new TargetImpl("default", null);
        return new Target[]{target};
    }

    public TargetModuleID[] getAvailableModules(ModuleType moduleType, Target[] targetList) throws TargetException {
        throw new UnsupportedOperationException();
    }

    public TargetModuleID[] getNonRunningModules(ModuleType moduleType, Target[] targetList) throws TargetException {
        throw new UnsupportedOperationException();
    }

    public TargetModuleID[] getRunningModules(ModuleType moduleType, Target[] targetList) throws TargetException {
        throw new UnsupportedOperationException();
    }

    public ProgressObject distribute(Target[] targetList, File moduleArchive, File deploymentPlan) {
        throw new UnsupportedOperationException();
    }

    public ProgressObject distribute(Target[] targetList, InputStream moduleArchive, InputStream deploymentPlan) {
        throw new UnsupportedOperationException();
    }

    public ProgressObject start(TargetModuleID[] moduleIDList) {
        if (kernel == null) {
            throw new IllegalStateException("Disconnected");
        }
        StartCommand command = new StartCommand(kernel, moduleIDList);
        new Thread(command).start();
        return command;
    }

    public ProgressObject stop(TargetModuleID[] moduleIDList) {
        if (kernel == null) {
            throw new IllegalStateException("Disconnected");
        }
        StopCommand command = new StopCommand(kernel, moduleIDList);
        new Thread(command).start();
        return command;
    }

    public ProgressObject undeploy(TargetModuleID[] moduleIDList) {
        throw new UnsupportedOperationException();
    }

    public boolean isRedeploySupported() {
        return false;
    }

    public ProgressObject redeploy(TargetModuleID[] moduleIDList, File moduleArchive, File deploymentPlan) {
        throw new UnsupportedOperationException();
    }

    public ProgressObject redeploy(TargetModuleID[] moduleIDList, InputStream moduleArchive, InputStream deploymentPlan) {
        throw new UnsupportedOperationException();
    }

    public Locale[] getSupportedLocales() {
        throw new UnsupportedOperationException();
    }

    public Locale getCurrentLocale() {
        throw new UnsupportedOperationException();
    }

    public Locale getDefaultLocale() {
        throw new UnsupportedOperationException();
    }

    public boolean isLocaleSupported(Locale locale) {
        throw new UnsupportedOperationException();
    }

    public void setLocale(Locale locale) {
        throw new UnsupportedOperationException();
    }

    public DConfigBeanVersionType getDConfigBeanVersion() {
        throw new UnsupportedOperationException();
    }

    public boolean isDConfigBeanVersionSupported(DConfigBeanVersionType version) {
        throw new UnsupportedOperationException();
    }

    public void setDConfigBeanVersion(DConfigBeanVersionType version) throws DConfigBeanVersionUnsupportedException {
        throw new UnsupportedOperationException();
    }

    public DeploymentConfiguration createConfiguration(DeployableObject dObj) throws InvalidModuleException {
        throw new UnsupportedOperationException();
    }
}
