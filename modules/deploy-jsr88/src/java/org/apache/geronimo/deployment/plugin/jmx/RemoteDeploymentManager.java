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

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Connects to a Kernel in a remote VM (may or many not be on the same machine).
 *
 * @version $Rev$ $Date$
 */
public class RemoteDeploymentManager extends JMXDeploymentManager {
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
}
