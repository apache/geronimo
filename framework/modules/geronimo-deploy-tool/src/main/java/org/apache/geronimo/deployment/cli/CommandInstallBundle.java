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
package org.apache.geronimo.deployment.cli;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

import org.apache.geronimo.cli.deployer.CommandArgs;
import org.apache.geronimo.cli.deployer.InstallBundleCommandArgs;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.plugin.GeronimoDeploymentManager;
import org.apache.geronimo.deployment.plugin.jmx.RemoteDeploymentManager;
import org.osgi.jmx.framework.FrameworkMBean;

public class CommandInstallBundle extends AbstractCommand {
    public void execute(ConsoleReader consoleReader, ServerConnection connection, CommandArgs commandArgs) throws DeploymentException {
        
        if (!isOffline(connection)) {
            if (!(commandArgs instanceof InstallBundleCommandArgs)) {
                throw new DeploymentSyntaxException("CommandArgs has the type [" + commandArgs.getClass() + "]; expected [" + InstallBundleCommandArgs.class + "]");
            }
            InstallBundleCommandArgs recordBundleCommandArgs = (InstallBundleCommandArgs) commandArgs;
            
            File bundleFile = new File(recordBundleCommandArgs.getArgs()[0]);
            if(!bundleFile.exists() || !bundleFile.isFile() || !bundleFile.canRead()) {
                throw new DeploymentException("File does not exist or not a normal file or not readable. "+bundleFile);
            }
            
            DeploymentManager dmgr = connection.getDeploymentManager();
            if(dmgr instanceof GeronimoDeploymentManager) {
                GeronimoDeploymentManager mgr = (GeronimoDeploymentManager) dmgr;
                
                boolean start = recordBundleCommandArgs.isStart();
                int startLevel = recordBundleCommandArgs.getStartLevel();
                String groupId = recordBundleCommandArgs.getGroupId();
                try {
                    long bundleId = mgr.recordInstall(bundleFile, groupId, startLevel);
                    if ( bundleId > 0 ){
                        consoleReader.printString(DeployUtils.reformat("Installed and recorded bundle: " + bundleId, 4, 72));
                        
                        if (start){
                            if(mgr instanceof RemoteDeploymentManager) {
                                MBeanServerConnection mbsc = ((RemoteDeploymentManager) mgr).getJMXConnector().getMBeanServerConnection();
                                try {
                                    FrameworkMBean frameworkMBean = getFrameworkMBean(mbsc);
                                    frameworkMBean.startBundle(bundleId);
                                    consoleReader.printString(DeployUtils.reformat("Started bundle: " + bundleId, 4, 72));
                                } catch (Exception e) {
                                    throw new DeploymentException("Unable to start bundle.", e);
                                }
                                
                            }else {
                                consoleReader.printString(DeployUtils.reformat("Currently the start option only support JMX connection.", 4, 72));
                            }
                        }
                    }else {
                        consoleReader.printString(DeployUtils.reformat("Bundle installation failed, so did not record bundle.", 4, 72));
                    }
                } catch (Exception e) {
                    throw new DeploymentException("Unable to record bundle "+recordBundleCommandArgs.getArgs()[0], e);
                }
            } else {
                throw new DeploymentException("Unable to record bundle using " + dmgr.getClass().getName() + " deployment manager");
            }
            
        } else { //offline not supported
            try {
                consoleReader.printString(DeployUtils.reformat("Install bundle offline is not supported!", 4, 72));
            } catch (IOException e) {
                throw new DeploymentException("Install bundle offline is not supported!");
            }
        }
    }
    
    private FrameworkMBean getFrameworkMBean(MBeanServerConnection mbsc) throws Exception{
        Set<ObjectName> objectNameSet = mbsc.queryNames(new ObjectName("osgi.core:type=framework,*"), null);
        if (objectNameSet.isEmpty()) {
            throw new Exception("Framework mbean not found");
            
        } else if (objectNameSet.size() == 1) {
            return (FrameworkMBean) MBeanServerInvocationHandler.newProxyInstance(mbsc, objectNameSet.iterator().next(), FrameworkMBean.class, false);   
            
        } else {
            throw new Exception("Found multiple framework mbeans");
        }
    }
    
}
