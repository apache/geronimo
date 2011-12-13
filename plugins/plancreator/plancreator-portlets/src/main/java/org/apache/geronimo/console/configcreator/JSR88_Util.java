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
package org.apache.geronimo.console.configcreator;

import java.io.File;

import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;

import org.apache.geronimo.deployment.plugin.jmx.CommandContext;
import org.apache.geronimo.deployment.plugin.jmx.JMXDeploymentManager;
import org.apache.geronimo.deployment.plugin.local.DistributeCommand;
import org.apache.geronimo.j2ee.deployment.ApplicationInfo;
import org.apache.geronimo.j2ee.deployment.EARConfigBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;

/**
 * Util class for JSR-88 related functions
 * 
 * @version $Rev$ $Date$
 */
public class JSR88_Util {

    /*private static List getEjbClassLoaders(PortletRequest request) {
        List deployedEjbs = JSR77_Util.getDeployedEJBs(request);
        List configurations = new ArrayList();
        for (int i = 0; i < deployedEjbs.size(); i++) {
            String ejbPatternName = ((ReferredData) deployedEjbs.get(i)).getPatternName();
            configurations.add(getDependencyString(ejbPatternName));
        }
        return getConfigClassLoaders(configurations);
    }

    private static List getConfigClassLoaders(List configurationNames) {
        List classLoaders = new ArrayList();
        ConfigurationManager configurationManager = PortletManager.getConfigurationManager();
        for (int i = 0; i < configurationNames.size(); i++) {
            Artifact configurationId = Artifact.create((String) configurationNames.get(i));
            classLoaders.add(configurationManager.getConfiguration(configurationId).getConfigurationClassLoader());
        }
        return classLoaders;
    }*/

    public static ApplicationInfo createApplicationInfo(PortletRequest actionRequest, File moduleFile) {
        ApplicationInfo applicationInfo = null;
        EARConfigBuilder.createPlanMode.set(Boolean.TRUE);
        try {
            DeploymentFactoryManager dfm = DeploymentFactoryManager.getInstance();
            DeploymentManager mgr = dfm.getDeploymentManager("deployer:geronimo:inVM", null, null);
            if (mgr instanceof JMXDeploymentManager) {
                ((JMXDeploymentManager) mgr).setLogConfiguration(false, true);
            }
            Target[] targets = mgr.getTargets();
            if (null == targets) {
                throw new IllegalStateException("No target to distribute to");
            }
            targets = new Target[] { targets[0] };
            DistributeCommand command = new DistributeCommand(getKernel(), targets, moduleFile, null);
            CommandContext commandContext = new CommandContext(true, true, null, null, false);
            commandContext.setUsername("system");
            commandContext.setPassword("manager");
            command.setCommandContext(commandContext);
            command.doDeploy(targets[0], true);
        } catch (Exception e) {
            // Any better ideas?
            if(EARConfigBuilder.appInfo.get() == null) throw new RuntimeException(e);
            
        } finally {
            EARConfigBuilder.createPlanMode.set(Boolean.FALSE);
            applicationInfo = EARConfigBuilder.appInfo.get();
            EARConfigBuilder.appInfo.set(null);
        }
        return applicationInfo;
    }

    private static Kernel getKernel() {
        // todo: consider making this configurable; we could easily connect to a remote kernel if we wanted to
        Kernel kernel = null;
        try {
            kernel = (Kernel) new InitialContext().lookup("java:comp/GeronimoKernel");
        } catch (NamingException e) {
            // log.error("Unable to look up kernel in JNDI", e);
        }
        if (kernel == null) {
            // log.debug("Unable to find kernel in JNDI; using KernelRegistry instead");
            kernel = KernelRegistry.getSingleKernel();
        }
        return kernel;
    }

    public static String[] deploy(PortletRequest actionRequest, File moduleFile, File planFile)
            throws PortletException {
        // TODO this is a duplicate of the code from
        // org.apache.geronimo.console.configmanager.DeploymentPortlet.processAction()
        // TODO need to eliminate this duplicate code
        DeploymentFactoryManager dfm = DeploymentFactoryManager.getInstance();
        String[] statusMsgs = new String[2];
        try {
            DeploymentManager mgr = dfm.getDeploymentManager("deployer:geronimo:inVM", null, null);
            try {
                if (mgr instanceof JMXDeploymentManager) {
                    ((JMXDeploymentManager) mgr).setLogConfiguration(false, true);
                }

                Target[] targets = mgr.getTargets();
                if (null == targets) {
                    throw new IllegalStateException("No target to distribute to");
                }
                targets = new Target[] { targets[0] };

                ProgressObject progress = mgr.distribute(targets, moduleFile, planFile);
                while (progress.getDeploymentStatus().isRunning()) {
                    Thread.sleep(100);
                }

                if (progress.getDeploymentStatus().isCompleted()) {
                    progress = mgr.start(progress.getResultTargetModuleIDs());
                    while (progress.getDeploymentStatus().isRunning()) {
                        Thread.sleep(100);
                    }
                    statusMsgs[0] = "infoMsg01";
                } else {
                    statusMsgs[0] = "errorMsg02";
                    statusMsgs[1] = progress.getDeploymentStatus().getMessage();
                }
            } finally {
                mgr.release();
            }
        } catch (Exception e) {
            throw new PortletException(e);
        }
        return statusMsgs;
    }
}
