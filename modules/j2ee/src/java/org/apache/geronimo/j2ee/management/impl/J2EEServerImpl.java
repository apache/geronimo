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

package org.apache.geronimo.j2ee.management.impl;

import java.util.Hashtable;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.J2EEServer;

/**
 * @version $Rev$ $Date$
 */
public class J2EEServerImpl implements J2EEServer {
    private static final String SERVER_VENDOR = "The Apache Software Foundation";
    private final Kernel kernel;
    private final String baseName;
    private final ServerInfo serverInfo;
    private final String objectName;

    public J2EEServerImpl(Kernel kernel, String objectName, ServerInfo serverInfo) {
        this.objectName = objectName;
        ObjectName myObjectName = JMXUtil.getObjectName(this.objectName);
        verifyObjectName(myObjectName);

        // build the base name used to query the server for child modules
        Hashtable keyPropertyList = myObjectName.getKeyPropertyList();
        String name = (String) keyPropertyList.get("name");
        baseName = myObjectName.getDomain() + ":J2EEServer=" + name + ",";

        this.kernel = kernel;
        this.serverInfo = serverInfo;
    }

    public String getObjectName() {
        return objectName;
    }

    public boolean isStateManageable() {
        return true;
    }

    public boolean isStatisticsProvider() {
        return false;
    }

    public boolean isEventProvider() {
        return true;
    }

    /**
     * ObjectName must match this pattern:
     * <p/>
     * domain:j2eeType=J2EEServer,name=MyName
     */
    private void verifyObjectName(ObjectName objectName) {
        if (objectName.isPattern()) {
            throw new InvalidObjectNameException("ObjectName can not be a pattern", objectName);
        }
        Hashtable keyPropertyList = objectName.getKeyPropertyList();
        if (!"J2EEServer".equals(keyPropertyList.get("j2eeType"))) {
            throw new InvalidObjectNameException("J2EEServer object name j2eeType property must be 'J2EEServer'", objectName);
        }
        if (!keyPropertyList.containsKey("name")) {
            throw new InvalidObjectNameException("J2EEServer object must contain a J2EEServer property", objectName);
        }
        if (keyPropertyList.size() != 2) {
            throw new InvalidObjectNameException("J2EEServer object name can only have j2eeType, and name", objectName);
        }
    }


    public String[] getDeployedObjects() {
        return Util.getObjectNames(kernel,
                baseName,
                new String[]{"J2EEApplication", "AppClientModule", "EJBModule", "WebModule", "ResourceAdapterModule"});
    }

    public String[] getResources() {
        return Util.getObjectNames(kernel,
                baseName,
                new String[]{"JavaMailResource", "JCAConnectionFactory", "JDBCResource", "JDBCDriver", "JMSResource", "JNDIResource", "JTAResource", "RMI_IIOPResource", "URLResource"});
    }

    public String[] getJavaVMs() {
        return Util.getObjectNames(kernel, baseName, new String[]{"JVM"});
    }

    public String getServerVendor() {
        return SERVER_VENDOR;
    }

    public String getServerVersion() {
        return serverInfo.getVersion();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(J2EEServerImpl.class, NameFactory.J2EE_SERVER);

        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addAttribute("deployedObjects", String[].class, false);
        infoFactory.addAttribute("resources", String[].class, false);
        infoFactory.addAttribute("javaVMs", String[].class, false);
        infoFactory.addAttribute("serverVendor", String.class, false);
        infoFactory.addAttribute("serverVersion", String.class, false);
        infoFactory.addInterface(J2EEServer.class);

        infoFactory.addReference("ServerInfo", ServerInfo.class, NameFactory.GERONIMO_SERVICE);

        infoFactory.setConstructor(new String[]{"kernel", "objectName", "ServerInfo"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
