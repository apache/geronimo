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

import java.util.Hashtable;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.management.J2EEServer;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;

/**
 * @version $Rev$ $Date$
 */
public class J2EEApplicationImpl {
    private final String deploymentDescriptor;
    private final String baseName;
    private final Kernel kernel;
    private final J2EEServer server;

    public J2EEApplicationImpl(Kernel kernel, String objectName, J2EEServer server, String deploymentDescriptor) {
        ObjectName myObjectName = JMXUtil.getObjectName(objectName);
        verifyObjectName(myObjectName);

        // build the base name used to query the server for child modules
        Hashtable keyPropertyList = myObjectName.getKeyPropertyList();
        String name = (String) keyPropertyList.get("name");
        String j2eeServerName = (String) keyPropertyList.get("J2EEServer");
        baseName = myObjectName.getDomain() + ":J2EEServer=" + j2eeServerName + ",J2EEApplication=" + name + ",";

        this.kernel = kernel;
        this.server = server;
        this.deploymentDescriptor = deploymentDescriptor;
    }

    /**
     * ObjectName must match this pattern:
     * <p/>
     * domain:j2eeType=J2EEApplication,name=MyName,J2EEServer=MyServer
     */
    private void verifyObjectName(ObjectName objectName) {
        if (objectName.isPattern()) {
            throw new InvalidObjectNameException("ObjectName can not be a pattern", objectName);
        }
        Hashtable keyPropertyList = objectName.getKeyPropertyList();
        if (!"J2EEApplication".equals(keyPropertyList.get("j2eeType"))) {
            throw new InvalidObjectNameException("J2EEApplication object name j2eeType property must be 'J2EEApplication'", objectName);
        }
        if (!keyPropertyList.containsKey("name")) {
            throw new InvalidObjectNameException("J2EEApplication object must contain a J2EEServer property", objectName);
        }
        if (!keyPropertyList.containsKey("J2EEServer")) {
            throw new InvalidObjectNameException("J2EEApplication object name must contain a J2EEServer property", objectName);
        }
        if (keyPropertyList.size() != 3) {
            throw new InvalidObjectNameException("J2EEApplication object name can only have j2eeType, name, and J2EEServer properties", objectName);
        }
    }

    public String[] getmodules() throws MalformedObjectNameException {
        return Util.getObjectNames(kernel,
                baseName,
                new String[]{"AppClientModule", "EJBModule", "WebModule", "ResourceAdapterModule"});
    }

    public String getDeploymentDescriptor() {
        return deploymentDescriptor;
    }

    public String getServer() {
        return server.getObjectName();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(J2EEApplicationImpl.class);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addReference("j2eeServer", J2EEServer.class);
        infoFactory.addAttribute("deploymentDescriptor", String.class, true);
        infoFactory.addAttribute("modules", String[].class, false);

        infoFactory.setConstructor(new String[]{
            "kernel",
            "objectName",
            "j2eeServer",
            "deploymentDescriptor"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
