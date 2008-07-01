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
package org.apache.geronimo.tomcat.cluster;

import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.catalina.ha.deploy.FarmWarDeployer;


public class FarmWarDeployerGBean extends ClusterDeployerGBean{

    private static final Logger log = LoggerFactory.getLogger(FarmWarDeployerGBean.class);

    private final ServerInfo serverInfo;

    public FarmWarDeployerGBean(){
       serverInfo = null;
    }
    
    public FarmWarDeployerGBean(String tempDir,
                                String deployDir,
                                String watchDir,
                                boolean watchEnabled,
                                int processDeployFrequency,
                                ServerInfo serverInfo) throws Exception {

        super("org.apache.catalina.ha.deploy.FarmWarDeployer", null);

        if (serverInfo == null){
            throw new IllegalArgumentException("serverInfo cannot be null.");
        }

        this.serverInfo = serverInfo;

        FarmWarDeployer farm = (FarmWarDeployer)deployer;

        if (tempDir == null)
            tempDir = "var/catalina/war-temp";
        farm.setTempDir(serverInfo.resolvePath(tempDir));

        if (deployDir == null)
            deployDir = "var/catalina/war-deploy";
        farm.setDeployDir(serverInfo.resolvePath(deployDir));

        if (watchDir == null)
            watchDir = "var/catalina/war-listen";
        farm.setWatchDir(serverInfo.resolvePath(watchDir));

        farm.setWatchEnabled(watchEnabled);

        farm.setProcessDeployFrequency(processDeployFrequency);

    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("ClusterDeployer", FarmWarDeployerGBean.class, ClusterDeployerGBean.GBEAN_INFO);

        infoFactory.addAttribute("tempDir", String.class, true);
        infoFactory.addAttribute("deployDir", String.class, true);
        infoFactory.addAttribute("watchDir", String.class, true);
        infoFactory.addAttribute("watchEnabled", boolean.class, true);
        infoFactory.addAttribute("processDeployFrequency", int.class, true);
        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");
        infoFactory.addOperation("getInternalObject", "Object");
        infoFactory.setConstructor(new String[] {
                "tempDir",
                "deployDir",
                "watchDir",
                "watchEnabled",
                "processDeployFrequency",
                "ServerInfo"
        });
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}