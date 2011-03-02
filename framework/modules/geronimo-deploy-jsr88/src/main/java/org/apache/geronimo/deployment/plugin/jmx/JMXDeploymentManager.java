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
package org.apache.geronimo.deployment.plugin.jmx;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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

import org.apache.geronimo.deployment.spi.ModuleConfigurer;
import org.apache.geronimo.deployment.plugin.TargetImpl;
import org.apache.geronimo.deployment.plugin.TargetModuleIDImpl;
import org.apache.geronimo.deployment.plugin.local.CommandSupport;
import org.apache.geronimo.deployment.plugin.local.DistributeCommand;
import org.apache.geronimo.deployment.plugin.local.RedeployCommand;
import org.apache.geronimo.deployment.plugin.local.StartCommand;
import org.apache.geronimo.deployment.plugin.local.StopCommand;
import org.apache.geronimo.deployment.plugin.local.UndeployCommand;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.management.State;


/**
 * @version $Rev$ $Date$
 */
public abstract class JMXDeploymentManager implements DeploymentManager {

    protected Kernel kernel;
    protected ConfigurationManager configurationManager;
    protected CommandContext commandContext;
    private final Collection<ModuleConfigurer> moduleConfigurers;

    public JMXDeploymentManager(Collection<ModuleConfigurer> moduleConfigurers) {
        if (null == moduleConfigurers) {
            throw new IllegalArgumentException("moduleConfigurers is required");
        }
        this.moduleConfigurers = moduleConfigurers;
    }
    
    protected void initialize(Kernel kernel) throws IOException {
        this.kernel = kernel;
        try {
            configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
        } catch (GBeanNotFoundException e) {
            //TODO consider wrapping exception
            throw new IOException(e.getMessage());
        }
        commandContext = new CommandContext(true, true, null, null, false);
    }

    public void setAuthentication(String username, String password) {
        commandContext.setUsername(username);
        commandContext.setPassword(password);
    }

    public void release() {
        if(kernel != null && configurationManager != null) {
            try {
                ConfigurationUtil.releaseConfigurationManager(kernel, configurationManager);
            } finally {
                configurationManager = null;
                kernel = null;
            }
        }
    }

    public Target[] getTargets() {
        if (kernel == null) {
            throw new IllegalStateException("Disconnected");
        }
        List stores = configurationManager.listStores();
        if (stores.isEmpty()) {
            return null;
        }

        Target[] targets = new Target[stores.size()];
        for (int i = 0; i < stores.size(); i++) {
            AbstractName storeName = (AbstractName) stores.get(i);
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

        if (targetList == null) {
            return null;
        }

        try {
            ArrayList<TargetModuleIDImpl> result = new ArrayList<TargetModuleIDImpl>();
            for (Target aTargetList : targetList) {
                TargetImpl target = (TargetImpl) aTargetList;
                AbstractName storeName = target.getAbstractName();
                List infos = configurationManager.listConfigurations(storeName);
                for (Object info1 : infos) {
                    ConfigurationInfo info = (ConfigurationInfo) info1;
                    if (filter.accept(info)) {
                        String name = info.getConfigID().toString();
                        List list = CommandSupport.loadChildren(kernel, name);
                        TargetModuleIDImpl moduleID = new TargetModuleIDImpl(target, name, (String[]) list.toArray(new String[list.size()]));
                        moduleID.setType(CommandSupport.convertModuleType(info.getType()));
                        if (moduleID.getChildTargetModuleID() != null) {
                            for (int k = 0; k < moduleID.getChildTargetModuleID().length; k++) {
                                TargetModuleIDImpl child = (TargetModuleIDImpl) moduleID.getChildTargetModuleID()[k];
                                if (CommandSupport.isWebApp(kernel, child.getModuleID())) {
                                    child.setType(ModuleType.WAR);
                                }
                            }
                        }
                        result.add(moduleID);
                    }
                }
            }
            CommandSupport.addWebContextPaths(kernel, result);
            return result.size() == 0 ? null : result.toArray(new TargetModuleID[result.size()]);
        } catch (Exception e) {
            throw (TargetException) new TargetException(e.getMessage()).initCause(e);
        }
    }

    public ProgressObject distribute(Target[] targetList, File moduleArchive, File deploymentPlan) {
        if (kernel == null) {
            throw new IllegalStateException("Disconnected");
        }
        DistributeCommand command = createDistributeCommand(targetList, moduleArchive, deploymentPlan);
        command.setCommandContext(commandContext);
        new Thread(command).start();
        return command;
    }

    /**
     * @deprecated
     */
    public ProgressObject distribute(Target[] targetList, InputStream moduleArchive, InputStream deploymentPlan) {
        return distribute(targetList, null, moduleArchive, deploymentPlan);
    }

    public ProgressObject distribute(Target[] targetList, ModuleType moduleType, InputStream moduleArchive, InputStream deploymentPlan) throws IllegalStateException {
        if (kernel == null) {
            throw new IllegalStateException("Disconnected");
        }
        DistributeCommand command = createDistributeCommand(targetList, moduleType, moduleArchive, deploymentPlan);
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
        RedeployCommand command = createRedeployCommand(moduleIDList, moduleArchive, deploymentPlan);
        command.setCommandContext(commandContext);
        new Thread(command).start();
        return command;
    }

    public ProgressObject redeploy(TargetModuleID[] moduleIDList, InputStream moduleArchive, InputStream deploymentPlan) {
        if (kernel == null) {
            throw new IllegalStateException("Disconnected");
        }
        RedeployCommand command = createRedeployCommand(moduleIDList, moduleArchive, deploymentPlan);
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
        if (dObj == null) {
            throw new NullPointerException("No deployable object supplied to configure");
        }
        ModuleConfigurer configurer = null;
        for (ModuleConfigurer moduleConfigurer : moduleConfigurers) {
            if (moduleConfigurer.getModuleType() == dObj.getType()) {
                configurer = moduleConfigurer;
               break;
            }
        }
        if (configurer == null) {
            throw new InvalidModuleException("No configurer for module type: " + dObj.getType() + " registered");
        }
        return configurer.createConfiguration(dObj);
    }

    protected DistributeCommand createDistributeCommand(Target[] targetList, File moduleArchive, File deploymentPlan) {
        return new DistributeCommand(kernel, targetList, moduleArchive, deploymentPlan);
    }

    protected DistributeCommand createDistributeCommand(Target[] targetList, ModuleType moduleType, InputStream moduleArchive, InputStream deploymentPlan) {
        return new DistributeCommand(kernel, targetList, moduleType, moduleArchive, deploymentPlan);
    }

    protected RedeployCommand createRedeployCommand(TargetModuleID[] moduleIDList, File moduleArchive, File deploymentPlan) {
        return new RedeployCommand(kernel, moduleIDList, moduleArchive, deploymentPlan);
    }

    protected RedeployCommand createRedeployCommand(TargetModuleID[] moduleIDList, InputStream moduleArchive, InputStream deploymentPlan) {
        return new RedeployCommand(kernel, moduleIDList, moduleArchive, deploymentPlan);
    }

    public void setLogConfiguration(boolean shouldLog, boolean verboseStatus) {
        commandContext.setLogErrors(shouldLog);
        commandContext.setVerbose(verboseStatus);
    }

    public void setInPlace(boolean inPlace) {
        commandContext.setInPlace(inPlace);
    }
    public Kernel getKernel() {
        return kernel;
    }
}
