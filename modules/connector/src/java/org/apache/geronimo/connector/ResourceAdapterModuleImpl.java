/**
 *
 * Copyright 2005 The Apache Software Foundation
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

import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.management.J2EEApplication;
import org.apache.geronimo.management.J2EEServer;
import org.apache.geronimo.management.ResourceAdapterModule;
import org.apache.geronimo.j2ee.management.impl.InvalidObjectNameException;
import org.apache.geronimo.kernel.jmx.JMXUtil;

/**
 * @version $Rev$ $Date$
 */
public class ResourceAdapterModuleImpl implements ResourceAdapterModule {
    private final J2EEServer server;
    private final J2EEApplication application;
    private final String deploymentDescriptor;
    private final String[] resourceAdapters;

    private final GBeanData resourceAdapterGBeanData;
    private final Map activationSpecInfoMap;
    private final Map adminObjectInfoMap;
    private final Map managedConnectionFactoryInfoMap;
    private final String objectName;

    public ResourceAdapterModuleImpl(String resourceAdapter,
                                     String objectName, 
                                     J2EEServer server, 
                                     J2EEApplication application, 
                                     String deploymentDescriptor,
                                     GBeanData resourceAdapterGBeanData,
                                     Map activationSpecInfoMap,
                                     Map adminObjectInfoMap,
                                     Map managedConnectionFactoryInfoMap) {
        this.objectName = objectName;
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

    public String[] getResourceAdapters() {
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
}
