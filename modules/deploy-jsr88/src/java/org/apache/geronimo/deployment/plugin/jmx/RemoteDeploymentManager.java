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
package org.apache.geronimo.deployment.plugin.jmx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import javax.enterprise.deploy.shared.CommandType;
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
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.system.configuration.ConfigurationList;
import org.apache.geronimo.system.configuration.DownloadPoller;
import org.apache.geronimo.system.configuration.DownloadResults;
import org.apache.geronimo.system.jmx.KernelDelegate;

/**
 * Connects to a Kernel in a remote VM (may or many not be on the same machine).
 *
 * @version $Rev$ $Date$
 */
public class RemoteDeploymentManager extends JMXDeploymentManager implements GeronimoDeploymentManager {
    private JMXConnector jmxConnector;
    private boolean isSameMachine;

    public RemoteDeploymentManager(JMXConnector jmxConnector, String hostname) throws IOException {
        this.jmxConnector = jmxConnector;
        MBeanServerConnection mbServerConnection = jmxConnector.getMBeanServerConnection();
        initialize(new KernelDelegate(mbServerConnection));
        checkSameMachine(hostname);
    }

    public boolean isSameMachine() {
        return isSameMachine;
    }

    private void checkSameMachine(String hostname) {
        isSameMachine = false;
        if(hostname.equals("localhost") || hostname.equals("127.0.0.1")) {
            isSameMachine = true;
            return;
        }
        try {
            InetAddress dest = InetAddress.getByName(hostname);
            Enumeration en = NetworkInterface.getNetworkInterfaces();
            while(en.hasMoreElements()) {
                NetworkInterface iface = (NetworkInterface) en.nextElement();
                Enumeration ine = iface.getInetAddresses();
                while (ine.hasMoreElements()) {
                    InetAddress address = (InetAddress) ine.nextElement();
                    if(address.equals(dest)) {
                        isSameMachine = true;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Unable to look up host name '"+hostname+"'; assuming it is a different machine, but this may not get very far.  ("+e.getMessage()+")");
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
        if(isSameMachine) {
            return super.createDistributeCommand(targetList, moduleArchive, deploymentPlan);
        } else {
            return new org.apache.geronimo.deployment.plugin.remote.DistributeCommand(kernel, targetList, moduleArchive, deploymentPlan);
        }
    }

    protected DistributeCommand createDistributeCommand(Target[] targetList, InputStream moduleArchive, InputStream deploymentPlan) {
        if(isSameMachine) {
            return super.createDistributeCommand(targetList, moduleArchive, deploymentPlan);
        } else {
            return new org.apache.geronimo.deployment.plugin.remote.DistributeCommand(kernel, targetList, moduleArchive, deploymentPlan);
        }
    }

    protected RedeployCommand createRedeployCommand(TargetModuleID[] moduleIDList, File moduleArchive, File deploymentPlan) {
        if(isSameMachine) {
            return super.createRedeployCommand(moduleIDList, moduleArchive, deploymentPlan);
        } else {
            return new org.apache.geronimo.deployment.plugin.remote.RedeployCommand(kernel, moduleIDList, moduleArchive, deploymentPlan);
        }
    }

    protected RedeployCommand createRedeployCommand(TargetModuleID[] moduleIDList, InputStream moduleArchive, InputStream deploymentPlan) {
        if(isSameMachine) {
            return super.createRedeployCommand(moduleIDList, moduleArchive, deploymentPlan);
        } else {
            return new org.apache.geronimo.deployment.plugin.remote.RedeployCommand(kernel, moduleIDList, moduleArchive, deploymentPlan);
        }
    }

    public ConfigurationList listConfigurations(URL mavenRepository, String username, String password) {
        Set set = kernel.listGBeans(new AbstractNameQuery("org.apache.geronimo.system.configuration.ConfigurationInstaller"));
        for (Iterator it = set.iterator(); it.hasNext();) {
            AbstractName name = (AbstractName) it.next();
            try {
                return (ConfigurationList) kernel.invoke(name, "listConfigurations", new Object[]{mavenRepository, username, password}, new String[]{URL.class.getName(), String.class.getName(), String.class.getName()});
            } catch (Exception e) {
                System.err.println("Unable to install configurations: "+e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }

    public DownloadResults install(ConfigurationList installList, String username, String password) {
        Set set = kernel.listGBeans(new AbstractNameQuery("org.apache.geronimo.system.configuration.ConfigurationInstaller"));
        for (Iterator it = set.iterator(); it.hasNext();) {
            AbstractName name = (AbstractName) it.next();
            try {
                return (DownloadResults) kernel.invoke(name, "install", new Object[]{installList, username, password}, new String[]{ConfigurationList.class.getName(), String.class.getName(), String.class.getName()});
            } catch (Exception e) {
                System.err.println("Unable to install configurations: "+e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }

    public void install(ConfigurationList configsToInstall, String username, String password, DownloadPoller poller) {
        Set set = kernel.listGBeans(new AbstractNameQuery("org.apache.geronimo.system.configuration.ConfigurationInstaller"));
        for (Iterator it = set.iterator(); it.hasNext();) {
            AbstractName name = (AbstractName) it.next();
            try {
                kernel.invoke(name, "install", new Object[]{configsToInstall, username, password, poller}, new String[]{ConfigurationList.class.getName(), String.class.getName(), String.class.getName(), DownloadPoller.class.getName()});
            } catch (Exception e) {
                System.err.println("Unable to install configurations: "+e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public Object startInstall(ConfigurationList configsToInstall, String username, String password) {
        Set set = kernel.listGBeans(new AbstractNameQuery("org.apache.geronimo.system.configuration.ConfigurationInstaller"));
        for (Iterator it = set.iterator(); it.hasNext();) {
            AbstractName name = (AbstractName) it.next();
            try {
                return kernel.invoke(name, "startInstall", new Object[]{configsToInstall, username, password}, new String[]{ConfigurationList.class.getName(), String.class.getName(), String.class.getName()});
            } catch (Exception e) {
                System.err.println("Unable to start installing configurations: "+e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }

    public Object startInstall(File carFile, String username, String password) {
        File[] args = new File[]{carFile};
        if(!isSameMachine) {
            AbstractDeployCommand progress = new AbstractDeployCommand(CommandType.DISTRIBUTE, kernel, null, null, null, null, false) {
                public void run() {
                }
            };
            progress.addProgressListener(new ProgressListener() {
                public void handleProgressEvent(ProgressEvent event) {
                    System.out.println(event.getDeploymentStatus().getMessage());
                }
            });
            RemoteDeployUtil.uploadFilesToServer(args, progress);
        }
        Set set = kernel.listGBeans(new AbstractNameQuery("org.apache.geronimo.system.configuration.ConfigurationInstaller"));
        for (Iterator it = set.iterator(); it.hasNext();) {
            AbstractName name = (AbstractName) it.next();
            try {
                return kernel.invoke(name, "startInstall", new Object[]{args[0], username, password}, new String[]{File.class.getName(), String.class.getName(), String.class.getName()});
            } catch (Exception e) {
                System.err.println("Unable to start installing configurations: "+e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }

    public DownloadResults checkOnInstall(Object key) {
        Set set = kernel.listGBeans(new AbstractNameQuery("org.apache.geronimo.system.configuration.ConfigurationInstaller"));
        for (Iterator it = set.iterator(); it.hasNext();) {
            AbstractName name = (AbstractName) it.next();
            try {
                return (DownloadResults)kernel.invoke(name, "checkOnInstall", new Object[]{key}, new String[]{Object.class.getName()});
            } catch (Exception e) {
                System.err.println("Unable to check on configuration install: "+e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }
}
