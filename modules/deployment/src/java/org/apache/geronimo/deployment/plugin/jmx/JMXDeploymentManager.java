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
import java.util.ArrayList;
import java.util.List;
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
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

import org.apache.geronimo.deployment.plugin.TargetImpl;
import org.apache.geronimo.deployment.plugin.TargetModuleIDImpl;
import org.apache.geronimo.deployment.plugin.local.DistributeCommand;
import org.apache.geronimo.deployment.plugin.local.RedeployCommand;
import org.apache.geronimo.deployment.plugin.local.StartCommand;
import org.apache.geronimo.deployment.plugin.local.StopCommand;
import org.apache.geronimo.deployment.plugin.local.UndeployCommand;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelMBean;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.NoSuchStoreException;
import org.apache.geronimo.kernel.jmx.MBeanProxyFactory;
import org.apache.geronimo.kernel.management.State;

/**
 *
 *
 * @version $Rev$ $Date$
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
            throw (IllegalStateException) new IllegalStateException("Unable to close connection").initCause(e);
        } finally {
            mbServerConnection = null;
            kernel = null;
        }
    }

    public Target[] getTargets() {
        if (kernel == null) {
            throw new IllegalStateException("Disconnected");
        }
        List stores = kernel.listConfigurationStores();
        if (stores.size() == 0) {
            return null;
        }

        Target[] targets = new Target[stores.size()];
        for (int i = 0; i < stores.size(); i++) {
            ObjectName storeName = (ObjectName) stores.get(i);
            targets[i] = new TargetImpl(storeName, null);
        }
        return targets;
    }

    public TargetModuleID[] getAvailableModules(final ModuleType moduleType, Target[] targetList) throws TargetException {
        ConfigFilter filter = new ConfigFilter() {
            public boolean accept(ConfigurationInfo info) {
                return moduleType == null || info.getType() == ConfigurationModuleType.getFromValue(moduleType.getValue());
            }
        };
        return getModules(targetList, filter);
    }

    public TargetModuleID[] getNonRunningModules(final ModuleType moduleType, Target[] targetList) throws TargetException {
        ConfigFilter filter = new ConfigFilter() {
            public boolean accept(ConfigurationInfo info) {
                return info.getState() != State.RUNNING && (moduleType == null || info.getType() == ConfigurationModuleType.getFromValue(moduleType.getValue()));
            }
        };
        return getModules(targetList, filter);
    }

    public TargetModuleID[] getRunningModules(final ModuleType moduleType, Target[] targetList) throws TargetException {
        ConfigFilter filter = new ConfigFilter() {
            public boolean accept(ConfigurationInfo info) {
                return info.getState() == State.RUNNING && (moduleType == null || info.getType() == ConfigurationModuleType.getFromValue(moduleType.getValue()));
            }
        };
        return getModules(targetList, filter);
    }

    private static interface ConfigFilter {
        boolean accept(ConfigurationInfo info);
    }

    private TargetModuleID[] getModules(Target[] targetList, ConfigFilter filter) throws TargetException {
        if (kernel == null) {
            throw new IllegalStateException("Disconnected");
        }

        try {
            ArrayList result = new ArrayList();
            for (int i = 0; i < targetList.length; i++) {
                TargetImpl target = (TargetImpl) targetList[i];
                ObjectName storeName = target.getObjectName();
                List infos = kernel.listConfigurations(storeName);
                for (int j = 0; j < infos.size(); j++) {
                    ConfigurationInfo info = (ConfigurationInfo) infos.get(j);
                    if (filter.accept(info)) {
                        TargetModuleID moduleID = new TargetModuleIDImpl(target, info.getConfigID().toString());
                        result.add(moduleID);
                    }
                }
            }
            return result.size() == 0 ? null : (TargetModuleID[]) result.toArray(new TargetModuleID[result.size()]);
        } catch (NoSuchStoreException e) {
            throw (TargetException) new TargetException(e.getMessage()).initCause(e);
        }
    }

    public ProgressObject distribute(Target[] targetList, File moduleArchive, File deploymentPlan) {
        if (kernel == null) {
            throw new IllegalStateException("Disconnected");
        }
        DistributeCommand command = new DistributeCommand(kernel, targetList, moduleArchive, deploymentPlan);
        new Thread(command).start();
        return command;
    }

    public ProgressObject distribute(Target[] targetList, InputStream moduleArchive, InputStream deploymentPlan) {
        if (kernel == null) {
            throw new IllegalStateException("Disconnected");
        }
        DistributeCommand command = new DistributeCommand(kernel, targetList, moduleArchive, deploymentPlan);
        new Thread(command).start();
        return command;
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
        if (kernel == null) {
            throw new IllegalStateException("Disconnected");
        }
        UndeployCommand command = new UndeployCommand(kernel, moduleIDList);
        new Thread(command).start();
        return command;
    }

    public boolean isRedeploySupported() {
        return true;
    }

    public ProgressObject redeploy(TargetModuleID[] moduleIDList, File moduleArchive, File deploymentPlan) {
        if (kernel == null) {
            throw new IllegalStateException("Disconnected");
        }
        RedeployCommand command = new RedeployCommand(kernel, moduleIDList, moduleArchive, deploymentPlan);
        new Thread(command).start();
        return command;
    }

    public ProgressObject redeploy(TargetModuleID[] moduleIDList, InputStream moduleArchive, InputStream deploymentPlan) {
        throw new UnsupportedOperationException();
    }

    public Locale[] getSupportedLocales() {
        return new Locale[]{getDefaultLocale()};
    }

    public Locale getCurrentLocale() {
        return getDefaultLocale();
    }

    public Locale getDefaultLocale() {
        return Locale.getDefault();
    }

    public boolean isLocaleSupported(Locale locale) {
        return getDefaultLocale().equals(locale);
    }

    public void setLocale(Locale locale) {
        throw new UnsupportedOperationException("Cannot set Locale");
    }

    public DConfigBeanVersionType getDConfigBeanVersion() {
        return DConfigBeanVersionType.V1_4;
    }

    public boolean isDConfigBeanVersionSupported(DConfigBeanVersionType version) {
        return DConfigBeanVersionType.V1_4.equals(version);
    }

    public void setDConfigBeanVersion(DConfigBeanVersionType version) throws DConfigBeanVersionUnsupportedException {
        if (!isDConfigBeanVersionSupported(version)) {
            throw new DConfigBeanVersionUnsupportedException("Version not supported " + version);
        }
    }

    public DeploymentConfiguration createConfiguration(DeployableObject dObj) throws InvalidModuleException {
        throw new InvalidModuleException("Not supported");
    }
}
