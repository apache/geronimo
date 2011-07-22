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
package org.apache.geronimo.deployment.hot;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.status.ProgressObject;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.cli.DeployUtils;
import org.apache.geronimo.deployment.plugin.factories.DeploymentFactoryWithKernel;
import org.apache.geronimo.deployment.plugin.jmx.JMXDeploymentManager;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.DeploymentWatcher;
import org.apache.geronimo.kernel.config.PersistentConfigurationList;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.geronimo.kernel.util.JarUtils;
import org.apache.geronimo.system.main.ServerStatus;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A directory-scanning hot deployer
 *
 * @version $Rev$ $Date$
 */
public class DirectoryHotDeployer implements HotDeployer, DeploymentWatcher, ServerStatus, GBeanLifecycle { 
    private static final Logger log = LoggerFactory.getLogger(DirectoryHotDeployer.class);

    private DirectoryMonitor monitor;
    private String path;
    private String monitorFileName;
    private ServerInfo serverInfo;
    private int pollIntervalMillis;
    private transient boolean serverRunning = false;
    private ModuleHandler moduleHandler;

    public DirectoryHotDeployer(String path, String monitorFileName, int pollIntervalMillis, 
                                ServerInfo serverInfo, ConfigurationManager configManager, Kernel kernel) {
        this.path = path;
        this.monitorFileName = monitorFileName;
        this.serverInfo = serverInfo;
        this.pollIntervalMillis = pollIntervalMillis;
        this.moduleHandler = new ModuleHandler(kernel, configManager);
    }

    public void deployed(Artifact id) {
        // no action when something is deployed
    }

    // BTW, now we support EBA deployment. And if an EBA includes a WAB, which also has a configuration id during EBA deployment, 
    // this method also can be called with the WAB id. And the WAB's id will also go into the "toRemove" list.
    public void undeployed(Artifact id) {
        if (monitor != null) {
            String moduleId = id.toString();
            if (moduleId.equals(moduleHandler.getWorkingOnConfigId())) {
                // don't react to undelpoy events we generated ourselves
                // because the file update action(i.e. redeploy) will cause the old module to undeploy first
                // we must handle this so that its config id won't go "toRemove" list to avoid the deletion of the new file.
                return; 
            }
            monitor.removeFile(id.toString());
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    public int getPollIntervalMillis() {
        return pollIntervalMillis;
    }

    public void setPollIntervalMillis(int pollIntervalMillis) {
        this.pollIntervalMillis = pollIntervalMillis;
    }

    public String getDeploymentURI() {
        return moduleHandler.getDeploymentURI();
    }

    public void setDeploymentURI(String deploymentURI) {
        moduleHandler.setDeploymentURI(deploymentURI);
    }

    public String getDeploymentUser() {
        return moduleHandler.getDeploymentUser();
    }

    public void setDeploymentUser(String deploymentUser) {
       moduleHandler.setDeploymentUser(deploymentUser);
    }

    public String getDeploymentPassword() {
        return moduleHandler.getDeploymentPassword();
    }

    public void setDeploymentPassword(String deploymentPassword) {
        moduleHandler.setDeploymentPassword(deploymentPassword);
    }

    public void doStart() throws Exception {
        File dir = serverInfo.resolveServer(path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IllegalStateException("Hot deploy directory " + dir.getAbsolutePath() + " does not exist and cannot be created!");
            }
        } else if (!dir.canRead() || !dir.isDirectory()) {
            throw new IllegalStateException("Hot deploy directory " + dir.getAbsolutePath() + " is not a readable directory!");
        }
        
        File monitorFile = null;
        if (monitorFileName != null && !monitorFileName.isEmpty()) {
            monitorFile = serverInfo.resolveServer(dir + File.separator + monitorFileName);
            if (!monitorFile.createNewFile()) {
                if (!monitorFile.canWrite()) {
                    throw new IllegalStateException("Hot deploy persist state file " + monitorFile.getAbsolutePath() + " could not be created or is not writable");
                }
            }
        }
                
        monitor = new DirectoryMonitor(dir, monitorFile, moduleHandler, pollIntervalMillis);
        log.debug("Hot deploy scanner intialized");
    }

    public void doStop() throws Exception {
        monitor.close();
    }

    public void doFail() {
        if (monitor != null) {
            monitor.close();
        }
    }

    public boolean isServerStarted() {
        return serverRunning;
    }
    
    public void setServerStarted(boolean started) {
        serverRunning = started;
        if (started) {
            Thread t = new Thread(monitor, "Geronimo hot deploy scanner");
            t.setDaemon(true);
            t.start();
            log.debug("Hot deploy scanner started");
        }
    }
    
    public void started() {
        log.debug("Initialization complete; directory scanner entering normal scan mode");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(DirectoryHotDeployer.class);

        infoFactory.addAttribute("path", String.class, true, true);
        infoFactory.addAttribute("monitorFileName", String.class, true, true);
        infoFactory.addAttribute("pollIntervalMillis", int.class, true, true);

        // The next 3 args can be used to configure the hot deployer for a remote (out of VM) server
        infoFactory.addAttribute("deploymentURI", String.class, true, true);
        infoFactory.addAttribute("deploymentUser", String.class, true, true);
        infoFactory.addAttribute("deploymentPassword", String.class, true, true);

        infoFactory.addReference("ConfigManager", ConfigurationManager.class, "ConfigurationManager");
        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");
        infoFactory.addAttribute("kernel", Kernel.class, false, false);
        infoFactory.addInterface(HotDeployer.class);

        infoFactory.setConstructor(new String[]{"path", "monitorFileName", "pollIntervalMillis", "ServerInfo", "ConfigManager", "kernel"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
 
}
