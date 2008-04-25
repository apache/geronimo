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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.security.auth.login.FailedLoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.deployment.ModuleConfigurer;
import org.apache.geronimo.deployment.plugin.GeronimoDeploymentManager;
import org.apache.geronimo.deployment.plugin.local.AbstractDeployCommand;
import org.apache.geronimo.deployment.plugin.local.DistributeCommand;
import org.apache.geronimo.deployment.plugin.local.RedeployCommand;
import org.apache.geronimo.deployment.plugin.remote.RemoteDeployUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.config.NoSuchStoreException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.InvalidGBeanException;
import org.apache.geronimo.system.jmx.KernelDelegate;
import org.apache.geronimo.system.plugin.DownloadPoller;
import org.apache.geronimo.system.plugin.DownloadResults;
import org.apache.geronimo.system.plugin.PluginInstaller;
import org.apache.geronimo.system.plugin.PluginRepositoryList;
import org.apache.geronimo.system.plugin.ServerArchiver;
import org.apache.geronimo.system.plugin.model.PluginListType;
import org.apache.geronimo.system.plugin.model.PluginType;
import org.apache.geronimo.system.plugin.model.AttributesType;
import org.codehaus.plexus.archiver.ArchiverException;

/**
 * Connects to a Kernel in a remote VM (may or many not be on the same machine).
 *
 * @version $Rev$ $Date$
 */
public class RemoteDeploymentManager extends JMXDeploymentManager implements GeronimoDeploymentManager, ServerArchiver {
    private final Logger log = LoggerFactory.getLogger(getClass());

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
            Enumeration en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface iface = (NetworkInterface) en.nextElement();
                Enumeration ine = iface.getInetAddresses();
                while (ine.hasMoreElements()) {
                    InetAddress address = (InetAddress) ine.nextElement();
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

    protected DistributeCommand createDistributeCommand(Target[] targetList, File moduleArchive, File deploymentPlan) {
        if (isSameMachine) {
            return super.createDistributeCommand(targetList, moduleArchive, deploymentPlan);
        } else {
            return new org.apache.geronimo.deployment.plugin.remote.DistributeCommand(kernel, targetList, moduleArchive,
                    deploymentPlan);
        }
    }

    protected DistributeCommand createDistributeCommand(Target[] targetList, ModuleType moduleType, InputStream moduleArchive, InputStream deploymentPlan) {
        if (isSameMachine) {
            return super.createDistributeCommand(targetList, moduleType, moduleArchive, deploymentPlan);
        } else {
            return new org.apache.geronimo.deployment.plugin.remote.DistributeCommand(kernel, targetList, moduleType,
                    moduleArchive, deploymentPlan);
        }
    }

    protected RedeployCommand createRedeployCommand(TargetModuleID[] moduleIDList, File moduleArchive, File deploymentPlan) {
        if (isSameMachine) {
            return super.createRedeployCommand(moduleIDList, moduleArchive, deploymentPlan);
        } else {
            return new org.apache.geronimo.deployment.plugin.remote.RedeployCommand(kernel, moduleIDList, moduleArchive,
                    deploymentPlan);
        }
    }

    protected RedeployCommand createRedeployCommand(TargetModuleID[] moduleIDList, InputStream moduleArchive, InputStream deploymentPlan) {
        if (isSameMachine) {
            return super.createRedeployCommand(moduleIDList, moduleArchive, deploymentPlan);
        } else {
            return new org.apache.geronimo.deployment.plugin.remote.RedeployCommand(kernel, moduleIDList, moduleArchive,
                    deploymentPlan);
        }
    }

    public PluginListType listPlugins(URL mavenRepository, String username, String password) throws FailedLoginException, IOException {
        PluginInstaller installer = getPluginInstaller();
        try {
            return installer.listPlugins(mavenRepository, username, password);
        } finally {
            kernel.getProxyManager().destroyProxy(installer);
        }
    }
    
    public void validatePlugin(PluginType plugin) throws MissingDependencyException {
        PluginInstaller installer = getPluginInstaller();
        try {
            installer.validatePlugin(plugin);
        } finally {
            kernel.getProxyManager().destroyProxy(installer);
        }
    }

    public Dependency[] checkPrerequisites(PluginType plugin) {
        PluginInstaller installer = getPluginInstaller();
        try {
            return installer.checkPrerequisites(plugin);
        } finally {
            kernel.getProxyManager().destroyProxy(installer);
        }
    }


    public DownloadResults install(PluginListType configsToInstall, String defaultRepository, boolean restrictToDefaultRepository, String username, String password) {
        PluginInstaller installer = getPluginInstaller();
        try {
            return installer.install(configsToInstall, defaultRepository, restrictToDefaultRepository, username, password);
        } finally {
            kernel.getProxyManager().destroyProxy(installer);
        }
    }

    public void install(PluginListType configsToInstall, String defaultRepository, boolean restrictToDefaultRepository, String username, String password, DownloadPoller poller) {
        PluginInstaller installer = getPluginInstaller();
        try {
            installer.install(configsToInstall, defaultRepository, restrictToDefaultRepository, username, password, poller);
        } finally {
            kernel.getProxyManager().destroyProxy(installer);
        }
    }

    public Object startInstall(PluginListType configsToInstall, String defaultRepository, boolean restrictToDefaultRepository, String username, String password) {
        PluginInstaller installer = getPluginInstaller();
        try {
            return installer.startInstall(configsToInstall, defaultRepository, restrictToDefaultRepository, username, password);
        } finally {
            kernel.getProxyManager().destroyProxy(installer);
        }
    }

    public Object startInstall(File carFile, String defaultRepository, boolean restrictToDefaultRepository, String username, String password) {
        File[] args = new File[]{carFile};
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
        PluginInstaller installer = getPluginInstaller();
        try {
            return installer.startInstall(carFile, defaultRepository, restrictToDefaultRepository, username, password);
        } finally {
            kernel.getProxyManager().destroyProxy(installer);
        }
    }

    public DownloadResults checkOnInstall(Object key) {
        PluginInstaller installer = getPluginInstaller();
        try {
            return installer.checkOnInstall(key);
        } finally {
            kernel.getProxyManager().destroyProxy(installer);
        }
    }

    private PluginInstaller getPluginInstaller() {
        Set<AbstractName> set = kernel.listGBeans(new AbstractNameQuery(PluginInstaller.class.getName()));
        for (AbstractName name : set) {
            return (PluginInstaller) kernel.getProxyManager().createProxy(name, PluginInstaller.class);
        }
        throw new IllegalStateException("No plugin installer found");
    }
    private ServerArchiver getServerArchiver() {
        Set<AbstractName> set = kernel.listGBeans(new AbstractNameQuery(ServerArchiver.class.getName()));
        for (AbstractName name : set) {
            return (ServerArchiver) kernel.getProxyManager().createProxy(name, ServerArchiver.class);
        }
        throw new IllegalStateException("No plugin installer found");
    }

    //not likely to be useful remotely
    public PluginListType createPluginListForRepositories(String repo) throws NoSuchStoreException {
        PluginInstaller installer = getPluginInstaller();
        try {
            return installer.createPluginListForRepositories(repo);
        } finally {
            kernel.getProxyManager().destroyProxy(installer);
        }
    }

    public Map getInstalledPlugins() {
        PluginInstaller installer = getPluginInstaller();
        try {
            return installer.getInstalledPlugins();
        } finally {
            kernel.getProxyManager().destroyProxy(installer);
        }
    }

    public PluginType getPluginMetadata(Artifact configId) {
        PluginInstaller installer = getPluginInstaller();
        try {
            return installer.getPluginMetadata(configId);
        } finally {
            kernel.getProxyManager().destroyProxy(installer);
        }
    }

    public void updatePluginMetadata(PluginType metadata) {
        PluginInstaller installer = getPluginInstaller();
        try {
            installer.updatePluginMetadata(metadata);
        } finally {
            kernel.getProxyManager().destroyProxy(installer);
        }
    }

    public URL[] getRepositories() {
        List<URL> list = new ArrayList<URL>();
        Set<AbstractName> set = kernel.listGBeans(new AbstractNameQuery(PluginRepositoryList.class.getName()));
        for (AbstractName name : set) {
            PluginRepositoryList repo = (PluginRepositoryList) kernel.getProxyManager().createProxy(name,
                    PluginRepositoryList.class);
            list.addAll(repo.getRepositories());
            kernel.getProxyManager().destroyProxy(repo);
        }
        return list.toArray(new URL[list.size()]);
    }

    public Artifact installLibrary(File libFile, String groupId) throws IOException {
        File[] args = new File[]{libFile};
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
        Set<AbstractName> set = kernel.listGBeans(new AbstractNameQuery(PluginInstaller.class.getName()));
        for (AbstractName name : set) {
            PluginInstaller installer = (PluginInstaller) kernel.getProxyManager().createProxy(name, PluginInstaller.class);
            Artifact artifact = installer.installLibrary(libFile, groupId);
            kernel.getProxyManager().destroyProxy(installer);
            return artifact;
        }
        return null;
    }

    public DownloadResults installPluginList(String targetRepositoryPath, String relativeTargetServerPath, PluginListType pluginList) throws Exception {
        PluginInstaller installer = getPluginInstaller();
        try {
            return installer.installPluginList(targetRepositoryPath, relativeTargetServerPath, pluginList);
        } finally {
            kernel.getProxyManager().destroyProxy(installer);
        }
    }

    public void mergeOverrides(String server, AttributesType overrides) throws InvalidGBeanException, IOException {
        PluginInstaller installer = getPluginInstaller();
        try {
            installer.mergeOverrides(server, overrides);
        } finally {
            kernel.getProxyManager().destroyProxy(installer);
        }
    }

    public File archive(String sourcePath, String destPath, Artifact artifact) throws ArchiverException, IOException {
        ServerArchiver archiver = getServerArchiver();
        try {
            return archiver.archive(sourcePath, destPath, artifact);
        } finally {
            kernel.getProxyManager().destroyProxy(archiver);
        }
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
