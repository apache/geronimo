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
package org.apache.geronimo.connector.deployment;

import java.util.Hashtable;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.j2ee.management.J2EEApplication;
import org.apache.geronimo.j2ee.management.J2EEServer;
import org.apache.geronimo.j2ee.management.impl.InvalidObjectNameException;
import org.apache.geronimo.j2ee.management.impl.Util;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;

/**
 * @version $Revision: 1.2 $ $Date: 2004/08/04 07:18:20 $
 */
public class ResouceAdapterModuleImpl {
    private final Kernel kernel;
    private final String baseName;
    private final J2EEServer server;
    private final J2EEApplication application;
    private final String deploymentDescriptor;

    public ResouceAdapterModuleImpl(Kernel kernel, String objectName, J2EEServer server, J2EEApplication application, String deploymentDescriptor) {
        ObjectName myObjectName = JMXUtil.getObjectName(objectName);
        verifyObjectName(myObjectName);

        // build the base name used to query the server for child modules
        Hashtable keyPropertyList = myObjectName.getKeyPropertyList();
        String name = (String) keyPropertyList.get("name");
        String j2eeServerName = (String) keyPropertyList.get("J2EEServer");
        String j2eeApplicationName = (String) keyPropertyList.get("J2EEApplication");
        baseName = myObjectName.getDomain() + ":J2EEServer=" + j2eeServerName + ",J2EEApplication=" + j2eeApplicationName + ",ResouceAdapterModule=" + name + ",";

        this.kernel = kernel;
        this.server = server;
        this.application = application;
        this.deploymentDescriptor = deploymentDescriptor;
    }

    public String getDeploymentDescriptor() {
        return deploymentDescriptor;
    }

    public String getServer() {
        return server.getobjectName();
    }

    public String getApplication() {
        if (application == null) {
            return null;
        }
        return application.getobjectName();
    }

    public String[] getJavaVMs() {
        return server.getjavaVMs();
    }

    public String[] getResourceAdapters() throws MalformedObjectNameException {
        return Util.getObjectNames(kernel, baseName, new String[]{"ResourceAdapter"});
    }

    /**
     * ObjectName must match this pattern:
     * <p/>
     * domain:j2eeType=ResourceAdapterModule,name=MyName,J2EEServer=MyServer,J2EEApplication=MyApplication
     */
    private void verifyObjectName(ObjectName objectName) {
        if (objectName.isPattern()) {
            throw new InvalidObjectNameException("ObjectName can not be a pattern", objectName);
        }
        Hashtable keyPropertyList = objectName.getKeyPropertyList();
        if (!"ResourceAdapterModule".equals(keyPropertyList.get("j2eeType"))) {
            throw new InvalidObjectNameException("ResourceAdapterModule object name j2eeType property must be 'ResourceAdapterModule'", objectName);
        }
        if (!keyPropertyList.containsKey("name")) {
            throw new InvalidObjectNameException("ResourceAdapterModule object must contain a name property", objectName);
        }
        if (!keyPropertyList.containsKey("J2EEServer")) {
            throw new InvalidObjectNameException("ResourceAdapterModule object name must contain a J2EEServer property", objectName);
        }
        if (!keyPropertyList.containsKey("J2EEApplication")) {
            throw new InvalidObjectNameException("ResourceAdapterModule object name must contain a J2EEApplication property", objectName);
        }
        if (keyPropertyList.size() != 4) {
            throw new InvalidObjectNameException("ResourceAdapterModule object name can only have j2eeType, name, J2EEApplication, and J2EEServer properties", objectName);
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(ResouceAdapterModuleImpl.class);
        infoFactory.addReference("J2EEServer", J2EEServer.class);
        infoFactory.addReference("J2EEApplication", J2EEApplication.class);

        infoFactory.addAttribute("deploymentDescriptor", String.class, true);

        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addAttribute("server", String.class, false);
        infoFactory.addAttribute("application", String.class, false);
        infoFactory.addAttribute("javaVMs", String[].class, false);
        infoFactory.addAttribute("resourceAdapters", String[].class, false);

        infoFactory.setConstructor(new String[]{
            "kernel",
            "objectName",
            "J2EEServer",
            "J2EEApplication",
            "deploymentDescriptor"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
