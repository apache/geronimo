/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.j2ee.management.impl;

import java.util.Collection;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.j2ee.management.J2EEDeployedObject;
import org.apache.geronimo.j2ee.management.J2EEResource;
import org.apache.geronimo.j2ee.management.JVM;
import org.apache.geronimo.system.serverinfo.ServerInfo;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:41 $
 */
public class ServerImpl {
    private static final String SERVER_VENDOR = "The Apache Software Foundation";
    private final ServerInfo serverInfo;
    private final Collection deployedObjects;
    private final Collection resources;
    private final Collection jvms;

    public ServerImpl(ServerInfo serverInfo, Collection deployedObjects, Collection resources, Collection jvms) {
        this.serverInfo = serverInfo;
        this.deployedObjects = deployedObjects;
        this.resources = resources;
        this.jvms = jvms;
    }

    public String[] getdeployedObjects() {
        return Util.getObjectNames(deployedObjects);
    }

    public String[] getresources() {
        return Util.getObjectNames(resources);
    }

    public String[] getjavaVMs() {
        return Util.getObjectNames(jvms);
    }

    public String getserverVendor() {
        return SERVER_VENDOR;
    }

    public String getserverVersion() {
        return serverInfo.getVersion();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(ServerImpl.class);
        infoFactory.addAttribute("deployedObjects", false);
        infoFactory.addAttribute("resources", false);
        infoFactory.addAttribute("javaVMs", false);
        infoFactory.addAttribute("serverVendor", false);
        infoFactory.addAttribute("serverVersion", false);
        infoFactory.addReference("ServerInfo", ServerInfo.class);
        infoFactory.addReference("DeployedObjects", J2EEDeployedObject.class);
        infoFactory.addReference("Resources", J2EEResource.class);
        infoFactory.addReference("JVMs", JVM.class);
        infoFactory.setConstructor(
                new String[]{"ServerInfo", "DeployedObjects", "Resources", "JVMs"},
                new Class[]{ServerInfo.class, Collection.class, Collection.class, Collection.class}
        );
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
