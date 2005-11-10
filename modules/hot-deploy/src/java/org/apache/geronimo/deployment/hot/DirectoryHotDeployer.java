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
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.deployment.plugin.factories.DeploymentFactoryImpl;
import org.apache.geronimo.deployment.cli.DeployUtils;
import org.apache.geronimo.common.DeploymentException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import java.io.File;

/**
 * A directory-scanning hot deployer
 *
 * @version $Rev: 53762 $ $Date: 2004-10-04 18:54:53 -0400 (Mon, 04 Oct 2004) $
 */
public class DirectoryHotDeployer implements HotDeployer, GBeanLifecycle {
    private static final Log log = LogFactory.getLog(DirectoryHotDeployer.class);
    private DirectoryMonitor monitor;
    private String path;
    private ServerInfo serverInfo;
    private int pollIntervalMillis;
    private transient DeploymentFactory factory;
    private transient TargetModuleID[] startupModules = null;

    public DirectoryHotDeployer(String path, int pollIntervalMillis, ServerInfo serverInfo) {
        this.path = path;
        this.serverInfo = serverInfo;
        this.pollIntervalMillis = pollIntervalMillis;
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
            mgr = factory.getDeploymentManager("deployer:geronimo:inVM", null, null);
            Target[] targets = mgr.getTargets();
            startupModules = mgr.getAvailableModules(null, targets);
            mgr.release();
            mgr = null;
            monitor = new DirectoryMonitor(dir, this, pollIntervalMillis);
            monitor.initialize();
            startupModules = null;
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
            log.info("Found new file in deploy directory on startup with ID "+configId);
            return false;
        }
    }

    public boolean isServerRunning() {
        return true; //todo: figure out whether it's safe to do deploy operations
    }

    public long getDeploymentTime(File file, String configId) {
        return file.lastModified(); //todo: how can we find out when a module was deployed?
    }

    public boolean fileAdded(File file) {
        DeploymentManager mgr = null;
        try {
            mgr = factory.getDeploymentManager("deployer:geronimo:inVM", null, null);
            Target[] targets = mgr.getTargets();
            ProgressObject po = mgr.distribute(targets, file, null);
            waitForProgress(po);
            if(po.getDeploymentStatus().isCompleted()) {
                TargetModuleID[] modules = po.getResultTargetModuleIDs();
                po = mgr.start(modules);
                waitForProgress(po);
                if(!po.getDeploymentStatus().isCompleted()) {
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
                log.error("Unable to deploy: "+po.getDeploymentStatus().getMessage(), new DeploymentException());
                return false;
            }
        } catch (DeploymentManagerCreationException e) {
            log.error("Unable to open deployer", e);
            return false;
        } finally {
            if(mgr != null) mgr.release();
        }
        return true;
    }

    public boolean fileRemoved(File file, String configId) {
        DeploymentManager mgr = null;
        try {
            mgr = factory.getDeploymentManager("deployer:geronimo:inVM", null, null);
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
        DeploymentManager mgr = null;
        try {
            mgr = factory.getDeploymentManager("deployer:geronimo:inVM", null, null);
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
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(DirectoryHotDeployer.class);

        infoFactory.addAttribute("path", String.class, true);
        infoFactory.addAttribute("pollIntervalMillis", int.class, true);
        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");
        infoFactory.addInterface(HotDeployer.class);

        infoFactory.setConstructor(new String[]{"path", "pollIntervalMillis", "ServerInfo"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
