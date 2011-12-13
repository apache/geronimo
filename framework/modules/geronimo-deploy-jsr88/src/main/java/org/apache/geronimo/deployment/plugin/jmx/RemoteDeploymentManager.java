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
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collection;
import java.util.Enumeration;

import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;

import org.apache.geronimo.deployment.plugin.GeronimoDeploymentManager;
import org.apache.geronimo.deployment.plugin.local.AbstractDeployCommand;
import org.apache.geronimo.deployment.plugin.local.DistributeCommand;
import org.apache.geronimo.deployment.plugin.local.RedeployCommand;
import org.apache.geronimo.deployment.plugin.remote.RemoteDeployUtil;
import org.apache.geronimo.deployment.spi.ModuleConfigurer;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.NoSuchOperationException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.jmx.KernelDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connects to a Kernel in a remote VM (may or many not be on the same machine).
 *
 * @version $Rev$ $Date$
 */
public class RemoteDeploymentManager extends ExtendedDeploymentManager {
    private static final Logger log = LoggerFactory.getLogger(RemoteDeploymentManager.class);

    private JMXConnector jmxConnector;
    private boolean isSameMachine;

    public RemoteDeploymentManager(Collection<ModuleConfigurer> moduleConfigurers) {
        super(moduleConfigurers);
    }

    public void init(JMXConnector jmxConnector, String hostname) throws IOException {
        this.jmxConnector = jmxConnector;
        MBeanServerConnection mbServerConnection = jmxConnector.getMBeanServerConnection();
        initialize(new KernelDelegate(mbServerConnection));
        checkSameMachine(hostname);
    }

    public JMXConnector getJMXConnector() {
        return this.jmxConnector;
    }

    public boolean isSameMachine() {
        return isSameMachine;
    }

    private void checkSameMachine(String hostname) {
        isSameMachine = false;
        if (hostname.equals("localhost") || hostname.equals("127.0.0.1")) {
            isSameMachine = true;
            return;
        }
        try {
            InetAddress dest = InetAddress.getByName(hostname);
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface iface = en.nextElement();
                Enumeration<InetAddress> ine = iface.getInetAddresses();
                while (ine.hasMoreElements()) {
                    InetAddress address = ine.nextElement();
                    if (address.equals(dest)) {
                        isSameMachine = true;
                    }
                }
            }
        } catch (Exception e) {
            log.error(
                    "Unable to look up host name '" + hostname + "'; assuming it is a different machine, but this may not get very far.",
                    e);
        }
    }

    public void release() {
        super.release();
        try {
            jmxConnector.close();
            jmxConnector = null;
        } catch (IOException e) {
            throw (IllegalStateException) new IllegalStateException("Unable to close connection").initCause(e);
        }
    }

    @Override
    protected <T> T getImplementation(AbstractName name, Class<T> clazz) {
        return kernel.getProxyManager().createProxy(name, clazz);
    }

    @Override
    protected DistributeCommand createDistributeCommand(Target[] targetList, File moduleArchive, File deploymentPlan) {
        if (isSameMachine) {
            return super.createDistributeCommand(targetList, moduleArchive, deploymentPlan);
        } else {
            return new org.apache.geronimo.deployment.plugin.remote.DistributeCommand(kernel, targetList, moduleArchive,
                    deploymentPlan);
        }
    }

    @Override
    protected DistributeCommand createDistributeCommand(Target[] targetList, ModuleType moduleType, InputStream moduleArchive, InputStream deploymentPlan) {
        if (isSameMachine) {
            return super.createDistributeCommand(targetList, moduleType, moduleArchive, deploymentPlan);
        } else {
            return new org.apache.geronimo.deployment.plugin.remote.DistributeCommand(kernel, targetList, moduleType,
                    moduleArchive, deploymentPlan);
        }
    }

    @Override
    protected RedeployCommand createRedeployCommand(TargetModuleID[] moduleIDList, File moduleArchive, File deploymentPlan) {
        if (isSameMachine) {
            return super.createRedeployCommand(moduleIDList, moduleArchive, deploymentPlan);
        } else {
            return new org.apache.geronimo.deployment.plugin.remote.RedeployCommand(kernel, moduleIDList, moduleArchive,
                    deploymentPlan);
        }
    }

    @Override
    protected RedeployCommand createRedeployCommand(TargetModuleID[] moduleIDList, InputStream moduleArchive, InputStream deploymentPlan) {
        if (isSameMachine) {
            return super.createRedeployCommand(moduleIDList, moduleArchive, deploymentPlan);
        } else {
            return new org.apache.geronimo.deployment.plugin.remote.RedeployCommand(kernel, moduleIDList, moduleArchive,
                    deploymentPlan);
        }
    }

    @Override
    public Object startInstall(File carFile, String defaultRepository, boolean restrictToDefaultRepository, String username, String password) {
        File[] args = new File[] {carFile};
        if (!isSameMachine) {
            AbstractDeployCommand progress = new AbstractDeployCommand(CommandType.DISTRIBUTE, kernel, null, null, null,
                    null, null, false) {
                public void run() {
                }
            };
            progress.addProgressListener(new ProgressListener() {
                public void handleProgressEvent(ProgressEvent event) {
                    log.info(event.getDeploymentStatus().getMessage());
                }
            });
            progress.setCommandContext(commandContext);
            RemoteDeployUtil.uploadFilesToServer(args, progress);
        }
        // make sure to pass args[0] as RemoteDeployUtil.uploadFilesToServer will update
        // the args argument with the filenames returned from the server
        return super.startInstall(args[0], defaultRepository, restrictToDefaultRepository, username, password);
    }

    @Override
    public Artifact installLibrary(File libFile, String groupId) throws IOException {
        File[] args = new File[] {libFile};
        if(!isSameMachine) {
            AbstractDeployCommand progress = new AbstractDeployCommand(CommandType.DISTRIBUTE, kernel, null, null, null, null, null, false) {
                public void run() {
                }
            };
            progress.addProgressListener(new ProgressListener() {
                public void handleProgressEvent(ProgressEvent event) {
                    log.info(event.getDeploymentStatus().getMessage());
                }
            });
            progress.setCommandContext(commandContext);
            RemoteDeployUtil.uploadFilesToServer(args, progress);
        }
        // make sure to pass args[0] as RemoteDeployUtil.uploadFilesToServer will update
        // the args argument with the filenames returned from the server
        return super.installLibrary(args[0], groupId);
    }

    @Override
    public void updateEBAContent(AbstractName applicationGBeanName, long bundleId, File bundleFile) throws GBeanNotFoundException, NoSuchOperationException, Exception{
        if(!isSameMachine) {
            throw new UnsupportedOperationException("Update EBA content operation is not supportted from a remote JMX connection");
        }
        
        super.updateEBAContent(applicationGBeanName, bundleId, bundleFile);
    }
    
    @Override
    public boolean hotSwapEBAContent(AbstractName applicationGBeanName, long bundleId, File changesFile, boolean updateArchive) throws GBeanNotFoundException, NoSuchOperationException, Exception{
        if(!isSameMachine) {
            throw new UnsupportedOperationException("Update EBA content operation is not supportted from a remote JMX connection");
        }
        
        return super.hotSwapEBAContent(applicationGBeanName, bundleId, changesFile, updateArchive);
    }
    
    @Override
    public long recordInstall(File bundleFile, String gourpId, int startLevel) throws IOException {
        if(!isSameMachine) {
            throw new UnsupportedOperationException("recordBundle operation is not supportted from a remote JMX connection.");
        }
        
        return super.recordInstall(bundleFile, gourpId, startLevel);
    }
    
    public static final GBeanInfo GBEAN_INFO;
    public static final String GBEAN_REF_MODULE_CONFIGURERS = "ModuleConfigurers";

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(RemoteDeploymentManager.class,
                "RemoteDeploymentManager");
        infoFactory.addInterface(GeronimoDeploymentManager.class);
        infoFactory.addReference(GBEAN_REF_MODULE_CONFIGURERS, ModuleConfigurer.class);

        infoFactory.setConstructor(new String[]{GBEAN_REF_MODULE_CONFIGURERS});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
