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
package org.apache.geronimo.connector;

import java.util.Hashtable;
import java.util.Map;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.management.J2EEApplication;
import org.apache.geronimo.j2ee.management.J2EEServer;
import org.apache.geronimo.j2ee.management.impl.InvalidObjectNameException;
import org.apache.geronimo.j2ee.management.impl.Util;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;

/**
 * @version $Rev$ $Date$
 */
public class ResourceAdapterModuleImpl {
    private final J2EEServer server;
    private final J2EEApplication application;
    private final String deploymentDescriptor;
    private final String[] resourceAdapters;

    private final GBeanData resourceAdapterGBeanData;
    private final Map activationSpecInfoMap;
    private final Map adminObjectInfoMap;
    private final Map managedConnectionFactoryInfoMap;

    public ResourceAdapterModuleImpl(String resourceAdapter,
                                     String objectName, 
                                     J2EEServer server, 
                                     J2EEApplication application, 
                                     String deploymentDescriptor,
                                     GBeanData resourceAdapterGBeanData,
                                     Map activationSpecInfoMap,
                                     Map adminObjectInfoMap,
                                     Map managedConnectionFactoryInfoMap) {
        ObjectName myObjectName = JMXUtil.getObjectName(objectName);
        verifyObjectName(myObjectName);

        this.resourceAdapters = new String[] {resourceAdapter};

        this.server = server;
        this.application = application;
        this.deploymentDescriptor = deploymentDescriptor;

        this.resourceAdapterGBeanData = resourceAdapterGBeanData;
        this.activationSpecInfoMap = activationSpecInfoMap;
        this.adminObjectInfoMap = adminObjectInfoMap;
        this.managedConnectionFactoryInfoMap = managedConnectionFactoryInfoMap;
    }

    public String getDeploymentDescriptor() {
        return deploymentDescriptor;
    }

    public String getServer() {
        return server.getObjectName();
    }

    public String getApplication() {
        if (application == null) {
            return null;
        }
        return application.getObjectName();
    }

    public String[] getJavaVMs() {
        return server.getJavaVMs();
    }

    public String[] getResourceAdapters() throws MalformedObjectNameException {
        return resourceAdapters;
    }

    public GBeanData getResourceAdapterGBeanData() {
        return resourceAdapterGBeanData;
    }

    public Map getActivationSpecInfoMap() {
        return activationSpecInfoMap;
    }

    public Map getAdminObjectInfoMap() {
        return adminObjectInfoMap;
    }

    public Map getManagedConnectionFactoryInfoMap() {
        return managedConnectionFactoryInfoMap;
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
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(ResourceAdapterModuleImpl.class, NameFactory.RESOURCE_ADAPTER_MODULE);
        infoBuilder.addReference("J2EEServer", J2EEServer.class);
        infoBuilder.addReference("J2EEApplication", J2EEApplication.class);

        infoBuilder.addAttribute("deploymentDescriptor", String.class, true);

        infoBuilder.addAttribute("resourceAdapter", String.class, true);
        infoBuilder.addAttribute("objectName", String.class, false);
        infoBuilder.addAttribute("server", String.class, false);
        infoBuilder.addAttribute("application", String.class, false);
        infoBuilder.addAttribute("javaVMs", String[].class, false);
        infoBuilder.addAttribute("resourceAdapters", String[].class, false);

        infoBuilder.addAttribute("resourceAdapterGBeanData", GBeanData.class, true);
        infoBuilder.addAttribute("activationSpecInfoMap", Map.class, true);
         infoBuilder.addAttribute("adminObjectInfoMap", Map.class, true);
        infoBuilder.addAttribute("managedConnectionFactoryInfoMap", Map.class, true);

        infoBuilder.setConstructor(new String[]{
            "resourceAdapter",
            "objectName",
            "J2EEServer",
            "J2EEApplication",
            "deploymentDescriptor",
            "resourceAdapterGBeanData",
            "activationSpecInfoMap",
            "adminObjectInfoMap",
            "managedConnectionFactoryInfoMap"
        });

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
