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
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.jmx.GBeanMBeanContext;
import org.apache.geronimo.system.serverinfo.ServerInfo;

/**
 * @version $Revision: 1.1 $ $Date: 2004/05/19 20:53:59 $
 */
public class J2EEServerImpl implements GBean {
    private static final String SERVER_VENDOR = "The Apache Software Foundation";
    private final ServerInfo serverInfo;
    private GBeanContext context;
    private String baseName;

    public J2EEServerImpl(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    public void setGBeanContext(GBeanContext context) {
        this.context = context;
        if (context != null) {
            ObjectName objectName = context.getObjectName();
            verifyObjectName(objectName);

            // build the base name used to query the server for child modules
            Hashtable keyPropertyList = objectName.getKeyPropertyList();
            String name = (String) keyPropertyList.get("name");
            baseName = objectName.getDomain() + ":J2EEServer=" + name + ",";
        } else {
            baseName = null;
        }
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


    public void doStart() {
    }

    public void doStop() {
    }

    public void doFail() {
    }

    public String[] getdeployedObjects() throws MalformedObjectNameException {
        return Util.getObjectNames(((GBeanMBeanContext) context).getServer(),
                baseName,
                new String[]{"J2EEApplication", "AppClientModule", "EJBModule", "WebModule", "ResourceAdapterModule"});
    }

    public String[] getresources() throws MalformedObjectNameException {
        return Util.getObjectNames(((GBeanMBeanContext) context).getServer(),
                baseName,
                new String[]{"JavaMailResource", "JCAConnectionFactory", "JDBCResource", "JDBCDriver", "JMSResource", "JNDIResource", "JTAResource", "RMI_IIOPResource", "URLResource"});
    }

    public String[] getjavaVMs() throws MalformedObjectNameException {
        return Util.getObjectNames(((GBeanMBeanContext) context).getServer(),
                baseName,
                new String[]{"JVM"});
    }

    public String getserverVendor() {
        return SERVER_VENDOR;
    }

    public String getserverVersion() {
        return serverInfo.getVersion();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(J2EEServerImpl.class);
        infoFactory.addAttribute("deployedObjects", false);
        infoFactory.addAttribute("resources", false);
        infoFactory.addAttribute("javaVMs", false);
        infoFactory.addAttribute("serverVendor", false);
        infoFactory.addAttribute("serverVersion", false);
        infoFactory.addReference("ServerInfo", ServerInfo.class);
        infoFactory.setConstructor(new String[]{"ServerInfo"},
                new Class[]{ServerInfo.class});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
