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
package org.apache.geronimo.deployment.hot;

import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanQuery;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.deployment.plugin.factories.DeploymentFactoryImpl;
import org.apache.geronimo.deployment.plugin.jmx.JMXDeploymentManager;
import org.apache.geronimo.deployment.cli.DeployUtils;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.kernel.config.PersistentConfigurationList;
import org.apache.geronimo.kernel.Kernel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.management.ObjectName;
import java.io.File;
import java.util.Set;
import java.util.Iterator;

/**
 * A directory-scanning hot deployer
 *
 * @version $Rev: 53762 $ $Date: 2004-10-04 18:54:53 -0400 (Mon, 04 Oct 2004) $
 */
public class DirectoryHotDeployer implements HotDeployer, GBeanLifecycle { //todo: write unit tests
    private static final Log log = LogFactory.getLog("org.apache.geronimo.deployment.hot.Hot Deployer");
    // Try to make this stand out as the user is likely to get a ton of errors if this comes up
    private static final String BAD_LAYOUT_MESSAGE = "CANNOT DEPLOY: It looks like you unpacked an application or module "+
                   "directly into the hot deployment directory.  THIS DOES NOT WORK.  You need to unpack into a "+
                   "subdirectory directly under the hot deploy directory.  For example, if the hot deploy directory "+
                   "is 'deploy/' and your file is 'webapp.war' then you could unpack it into a directory 'deploy/webapp.war/'";
    private DirectoryMonitor monitor;
    private String path;
    private ServerInfo serverInfo;
    private int pollIntervalMillis;
    private String deploymentURI = "deployer:geronimo:inVM";
    private String deploymentUser;
    private String deploymentPassword;
    private transient Kernel kernel;
    private transient DeploymentFactory factory;
    private transient TargetModuleID[] startupModules = null;

    public DirectoryHotDeployer(String path, int pollIntervalMillis, ServerInfo serverInfo, Kernel kernel) {
        this.path = path;
        this.serverInfo = serverInfo;
        this.pollIntervalMillis = pollIntervalMillis;
        this.kernel = kernel;
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
        return deploymentURI;
    }

    public void setDeploymentURI(String deploymentURI) {
        if(deploymentURI != null && !deploymentURI.trim().equals("")) {
            this.deploymentURI = deploymentURI.trim();
        }
    }

    public String getDeploymentUser() {
        return deploymentUser;
    }

    public void setDeploymentUser(String deploymentUser) {
        this.deploymentUser = deploymentUser;
    }

    public String getDeploymentPassword() {
        return deploymentPassword;
    }

    public void setDeploymentPassword(String deploymentPassword) {
        this.deploymentPassword = deploymentPassword;
    }

    public void doStart() throws Exception {
        if(factory == null) {
            factory = new DeploymentFactoryImpl();
        }
        File dir = serverInfo.resolve(path);
        if(!dir.exists()) {
            if(!dir.mkdirs()) {
                throw new IllegalStateException("Hot deploy directory "+dir.getAbsolutePath()+" does not exist and cannot be created!");
            }
        } else if(!dir.canRead() || !dir.isDirectory()) {
            throw new IllegalStateException("Hot deploy directory "+dir.getAbsolutePath()+" is not a readable directory!");
        }
        DeploymentManager mgr = null;
        try {
            mgr = getDeploymentManager();
            Target[] targets = mgr.getTargets();
            startupModules = mgr.getAvailableModules(null, targets);
            mgr.release();
            mgr = null;
            monitor = new DirectoryMonitor(dir, this, pollIntervalMillis);
            log.debug("Hot deploy scanner intialized; starting main loop.");
            Thread t = new Thread(monitor, "Geronimo hot deploy scanner");
            t.setDaemon(true);
            t.start();
        } finally {
            if(mgr != null) mgr.release();
        }
    }

    public void doStop() throws Exception {
        monitor.close();
    }

    public void doFail() {
        if(monitor != null) {
            monitor.close();
        }
    }

    public boolean isFileDeployed(File file, String configId) {
        try {
            DeployUtils.identifyTargetModuleIDs(startupModules, configId).toArray(new TargetModuleID[0]);
            return true;
        } catch (DeploymentException e) {
            log.debug("Found new file in deploy directory on startup with ID "+configId);
            return false;
        }
    }

    public boolean isServerRunning() {
        // a bit of a hack, but the PersistentConfigurationList is the only thing that knows whether the server is full started!
        GBeanQuery query = new GBeanQuery(null, PersistentConfigurationList.class.getName());
        Set configLists = kernel.listGBeans(query);
        for (Iterator i = configLists.iterator(); i.hasNext();) {
            ObjectName configListName = (ObjectName) i.next();
            try {
                Boolean result = (Boolean) kernel.getAttribute(configListName, "kernelFullyStarted");
                if(!result.booleanValue()) {
                    return false;
                }
            } catch (Exception e) {
                log.warn("Hot deployer unable to determine whether kernel is started", e);
            }
        }
        return true;
    }

    public long getDeploymentTime(File file, String configId) {
        return file.lastModified(); //todo: how can we find out when a module was deployed?
    }

    public void started() {
        startupModules = null;
        log.debug("Initialization complete; directory scanner entering normal scan mode");
    }

    public boolean validateFile(File file, String configId) {
        //todo: some more detailed evaluation
        if(file.isDirectory() && (file.getName().equals("WEB-INF") || file.getName().equals("META-INF"))) {
            log.error("("+file.getName()+") "+BAD_LAYOUT_MESSAGE);
            return false;
        }
        return true;
    }

    public String fileAdded(File file) {
        log.info("Deploying "+file.getName());
        DeploymentManager mgr = null;
        TargetModuleID[] modules = null;
        boolean completed = false;
        try {
            mgr = getDeploymentManager();
            Target[] targets = mgr.getTargets();
            ProgressObject po = mgr.distribute(targets, file, null);
            waitForProgress(po);
            if(po.getDeploymentStatus().isCompleted()) {
                modules = po.getResultTargetModuleIDs();
                po = mgr.start(modules);
                waitForProgress(po);
                if(po.getDeploymentStatus().isCompleted()) {
                    completed = true;
                } else {
                    log.warn("Unable to start some modules for "+file.getAbsolutePath());
                }
                modules = po.getResultTargetModuleIDs();
                for (int i = 0; i < modules.length; i++) {
                    TargetModuleID result = modules[i];
                    System.out.println(DeployUtils.reformat("Deployed "+result.getModuleID()+(targets.length > 1 ? " to "+result.getTarget().getName() : "")+(result.getWebURL() == null ? "" : " @ "+result.getWebURL()), 4, 72));
                    if(result.getChildTargetModuleID() != null) {
                        for (int j = 0; j < result.getChildTargetModuleID().length; j++) {
                            TargetModuleID child = result.getChildTargetModuleID()[j];
                            System.out.println(DeployUtils.reformat("  `-> "+child.getModuleID()+(child.getWebURL() == null ? "" : " @ "+child.getWebURL()),4, 72));
                        }
                    }
                }
            } else {
                log.error("Unable to deploy: "+po.getDeploymentStatus().getMessage());
                return null;
            }
        } catch (DeploymentManagerCreationException e) {
            log.error("Unable to open deployer", e);
            return null;
        } finally {
            if(mgr != null) mgr.release();
        }
        if(completed && modules != null) {
            if(modules.length == 1) {
                return modules[0].getModuleID();
            } else {
                return "";
            }
        } else if(modules != null) { //distribute completed but not start or something like that
            return "";
        } else {
            return null;
        }
    }

    private DeploymentManager getDeploymentManager() throws DeploymentManagerCreationException {
        DeploymentManager manager = factory.getDeploymentManager(deploymentURI, deploymentUser, deploymentPassword);
        if(manager instanceof JMXDeploymentManager) {
            ((JMXDeploymentManager)manager).setLogConfiguration(false, true);
        }
        return manager;
    }

    public boolean fileRemoved(File file, String configId) {
        log.info("Undeploying "+file.getName());
        DeploymentManager mgr = null;
        try {
            mgr = getDeploymentManager();
            Target[] targets = mgr.getTargets();
            TargetModuleID[] ids = mgr.getAvailableModules(null, targets);
            ids = (TargetModuleID[]) DeployUtils.identifyTargetModuleIDs(ids, configId).toArray(new TargetModuleID[0]);
            ProgressObject po = mgr.undeploy(ids);
            waitForProgress(po);
            if(po.getDeploymentStatus().isCompleted()) {
                TargetModuleID[] modules = po.getResultTargetModuleIDs();
                for (int i = 0; i < modules.length; i++) {
                    TargetModuleID result = modules[i];
                    System.out.println(DeployUtils.reformat("Undeployed "+result.getModuleID()+(targets.length > 1 ? " to "+result.getTarget().getName() : ""), 4, 72));
                }
            } else {
                log.error("Unable to undeploy "+file.getAbsolutePath()+"("+configId+")"+po.getDeploymentStatus().getMessage());
                return false;
            }
        } catch (DeploymentManagerCreationException e) {
            log.error("Unable to open deployer", e);
            return false;
        } catch (Exception e) {
            log.error("Unable to undeploy", e);
            return false;
        } finally {
            if(mgr != null) mgr.release();
        }
        return true;
    }

    public void fileUpdated(File file, String configId) {
        log.info("Redeploying "+file.getName());
        DeploymentManager mgr = null;
        try {
            mgr = getDeploymentManager();
            Target[] targets = mgr.getTargets();
            TargetModuleID[] ids = mgr.getAvailableModules(null, targets);
            ids = (TargetModuleID[]) DeployUtils.identifyTargetModuleIDs(ids, configId).toArray(new TargetModuleID[0]);
            ProgressObject po = mgr.redeploy(ids, file, null);
            waitForProgress(po);
            if(po.getDeploymentStatus().isCompleted()) {
                TargetModuleID[] modules = po.getResultTargetModuleIDs();
                for (int i = 0; i < modules.length; i++) {
                    TargetModuleID result = modules[i];
                    System.out.println(DeployUtils.reformat("Redeployed "+result.getModuleID()+(targets.length > 1 ? " to "+result.getTarget().getName() : "")+(result.getWebURL() == null ? "" : " @ "+result.getWebURL()), 4, 72));
                    if(result.getChildTargetModuleID() != null) {
                        for (int j = 0; j < result.getChildTargetModuleID().length; j++) {
                            TargetModuleID child = result.getChildTargetModuleID()[j];
                            System.out.println(DeployUtils.reformat("  `-> "+child.getModuleID()+(child.getWebURL() == null ? "" : " @ "+child.getWebURL()),4, 72));
                        }
                    }
                }
            } else {
                log.error("Unable to undeploy "+file.getAbsolutePath()+"("+configId+")"+po.getDeploymentStatus().getMessage());
            }
        } catch (DeploymentManagerCreationException e) {
            log.error("Unable to open deployer", e);
        } catch (Exception e) {
            log.error("Unable to undeploy", e);
        } finally {
            if(mgr != null) mgr.release();
        }
    }

    private void waitForProgress(ProgressObject po) {
        while(po.getDeploymentStatus().isRunning()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(DirectoryHotDeployer.class);

        infoFactory.addAttribute("path", String.class, true, true);
        infoFactory.addAttribute("pollIntervalMillis", int.class, true, true);

        // The next 3 args can be used to configure the hot deployer for a remote (out of VM) server
        infoFactory.addAttribute("deploymentURI", String.class, true, true);
        infoFactory.addAttribute("deploymentUser", String.class, true, true);
        infoFactory.addAttribute("deploymentPassword", String.class, true, true);

        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");
        infoFactory.addAttribute("kernel", Kernel.class, false, false);
        infoFactory.addInterface(HotDeployer.class);

        infoFactory.setConstructor(new String[]{"path", "pollIntervalMillis", "ServerInfo", "kernel"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
