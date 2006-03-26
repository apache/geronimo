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

import org.apache.geronimo.kernel.jmx.KernelDelegate;
import org.apache.geronimo.deployment.plugin.local.DistributeCommand;
import org.apache.geronimo.deployment.plugin.local.RedeployCommand;
import org.apache.geronimo.deployment.plugin.GeronimoDeploymentManager;
import org.apache.geronimo.system.configuration.ConfigurationMetadata;
import org.apache.geronimo.system.configuration.DownloadResults;
import org.apache.geronimo.gbean.GBeanQuery;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URI;
import java.util.Enumeration;
import java.util.Set;
import java.util.Iterator;

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

    public ConfigurationMetadata[] listConfigurations(URL mavenRepository) throws IOException {
        Set set = kernel.listGBeans(new GBeanQuery(null, "org.apache.geronimo.system.configuration.ConfigurationInstaller"));
        for (Iterator it = set.iterator(); it.hasNext();) {
            ObjectName name = (ObjectName) it.next();
            try {
                return (ConfigurationMetadata[]) kernel.invoke(name, "listConfigurations", new Object[]{mavenRepository}, new String[]{URL.class.getName()});
            } catch (Exception e) {
                e.printStackTrace();
                throw new IOException("Unable to list configurations: "+e.getMessage());
            }
        }
        return null;
    }

    public ConfigurationMetadata loadDependencies(URL mavenRepository, ConfigurationMetadata source) throws IOException {
        Set set = kernel.listGBeans(new GBeanQuery(null, "org.apache.geronimo.system.configuration.ConfigurationInstaller"));
        for (Iterator it = set.iterator(); it.hasNext();) {
            ObjectName name = (ObjectName) it.next();
            try {
                return (ConfigurationMetadata) kernel.invoke(name, "loadDependencies", new Object[]{mavenRepository, source}, new String[]{URL.class.getName(), ConfigurationMetadata.class.getName()});
            } catch (Exception e) {
                e.printStackTrace();
                throw new IOException("Unable to load dependencies: "+e.getMessage());
            }
        }
        return null;
    }

    public DownloadResults install(URL mavenRepository, URI configId) throws IOException {
        Set set = kernel.listGBeans(new GBeanQuery(null, "org.apache.geronimo.system.configuration.ConfigurationInstaller"));
        for (Iterator it = set.iterator(); it.hasNext();) {
            ObjectName name = (ObjectName) it.next();
            try {
                return (DownloadResults) kernel.invoke(name, "install", new Object[]{mavenRepository, configId}, new String[]{URL.class.getName(), URI.class.getName()});
            } catch (Exception e) {
                e.printStackTrace();
                throw new IOException("Unable to install configurations: "+e.getMessage());
            }
        }
        return null;
    }
}
