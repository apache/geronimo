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
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.NoSuchStoreException;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.jmx.KernelDelegate;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.connector.deployment.RARConfigurer;
import org.apache.geronimo.web.deployment.WARConfigurer;


/**
 * @version $Rev$ $Date$
 */
public class JMXDeploymentManager implements DeploymentManager {
    private JMXConnector jmxConnector;
    private MBeanServerConnection mbServerConnection;
    private Kernel kernel;
    private ConfigurationManager configurationManager;
    private CommandContext commandContext;

    public JMXDeploymentManager(JMXConnector jmxConnector) throws IOException {
        this.jmxConnector = jmxConnector;
        mbServerConnection = jmxConnector.getMBeanServerConnection();
        kernel = new KernelDelegate(mbServerConnection);
        configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
        commandContext = new CommandContext();
        commandContext.setLogErrors(true);
        commandContext.setVerbose(true);
    }

    public void release() {
        try {
            ConfigurationUtil.releaseConfigurationManager(kernel, configurationManager);
            jmxConnector.close();
        } catch (IOException e) {
            throw (IllegalStateException) new IllegalStateException("Unable to close connection").initCause(e);
        } finally {
            mbServerConnection = null;
            configurationManager = null;
            kernel = null;
        }
    }

    public Target[] getTargets() {
        if (kernel == null) {
            throw new IllegalStateException("Disconnected");
        }
        List stores = configurationManager.listStores();
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
                List infos = configurationManager.listConfigurations(storeName);
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
        command.setCommandContext(commandContext);
        new Thread(command).start();
        return command;
    }

    public ProgressObject distribute(Target[] targetList, InputStream moduleArchive, InputStream deploymentPlan) {
        if (kernel == null) {
            throw new IllegalStateException("Disconnected");
        }
        DistributeCommand command = new DistributeCommand(kernel, targetList, moduleArchive, deploymentPlan);
        command.setCommandContext(commandContext);
        new Thread(command).start();
        return command;
    }

    public ProgressObject start(TargetModuleID[] moduleIDList) {
        if (kernel == null) {
            throw new IllegalStateException("Disconnected");
        }
        StartCommand command = new StartCommand(kernel, moduleIDList);
        command.setCommandContext(commandContext);
        new Thread(command).start();
        return command;
    }

    public ProgressObject stop(TargetModuleID[] moduleIDList) {
        if (kernel == null) {
            throw new IllegalStateException("Disconnected");
        }
        StopCommand command = new StopCommand(kernel, moduleIDList);
        command.setCommandContext(commandContext);
        new Thread(command).start();
        return command;
    }

    public ProgressObject undeploy(TargetModuleID[] moduleIDList) {
        if (kernel == null) {
            throw new IllegalStateException("Disconnected");
        }
        UndeployCommand command = new UndeployCommand(kernel, moduleIDList);
        command.setCommandContext(commandContext);
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
        command.setCommandContext(commandContext);
        new Thread(command).start();
        return command;
    }

    public ProgressObject redeploy(TargetModuleID[] moduleIDList, InputStream moduleArchive, InputStream deploymentPlan) {
        if (kernel == null) {
            throw new IllegalStateException("Disconnected");
        }
        RedeployCommand command = new RedeployCommand(kernel, moduleIDList, moduleArchive, deploymentPlan);
        command.setCommandContext(commandContext);
        new Thread(command).start();
        return command;
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
        if(dObj.getType().equals(ModuleType.CAR)) {
            //todo: need a client configurer
        } else if(dObj.getType().equals(ModuleType.EAR)) {
            //todo: need an EAR configurer
        } else if(dObj.getType().equals(ModuleType.EJB)) {
            try {
                Class cls = Class.forName("org.openejb.deployment.EJBConfigurer");
                return (DeploymentConfiguration)cls.getMethod("createConfiguration", new Class[]{DeployableObject.class}).invoke(cls.newInstance(), new Object[]{dObj});
            } catch (Exception e) {
                System.err.println("Unable to invoke EJB deployer");
                e.printStackTrace();
            }
        } else if(dObj.getType().equals(ModuleType.RAR)) {
            return new RARConfigurer().createConfiguration(dObj);
        } else if(dObj.getType().equals(ModuleType.WAR)) {
            return new WARConfigurer().createConfiguration(dObj); // this is jetty
            // todo: Tomcat WARConfigurer
        }
        throw new InvalidModuleException("Not supported");
    }

    public void setCommandContext(CommandContext commandContext) {
        this.commandContext = commandContext;
    }

    public static class CommandContext {
        private boolean logErrors;
        private boolean verbose;

        public CommandContext() {
        }

        public boolean isLogErrors() {
            return logErrors;
        }

        public void setLogErrors(boolean logErrors) {
            this.logErrors = logErrors;
        }

        public boolean isVerbose() {
            return verbose;
        }

        public void setVerbose(boolean verbose) {
            this.verbose = verbose;
        }
    }
}
