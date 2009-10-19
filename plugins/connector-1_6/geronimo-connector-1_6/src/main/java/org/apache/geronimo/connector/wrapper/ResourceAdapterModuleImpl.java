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
package org.apache.geronimo.connector.wrapper;

import java.util.Hashtable;
import java.util.Map;

import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.management.impl.InvalidObjectNameException;
import org.apache.geronimo.kernel.ObjectNameUtil;
import org.apache.geronimo.management.J2EEApplication;
import org.apache.geronimo.management.J2EEServer;
import org.apache.geronimo.management.geronimo.ResourceAdapter;
import org.apache.geronimo.management.geronimo.ResourceAdapterModule;

/**
 * @version $Rev$ $Date$
 */
public class ResourceAdapterModuleImpl implements ResourceAdapterModule {
    private final J2EEServer server;
    private final J2EEApplication application;
    private final String deploymentDescriptor;
    private final ResourceAdapter resourceAdapter;

    private final GBeanData resourceAdapterGBeanData;
    private final Map activationSpecInfoMap;
    private final Map adminObjectInfoMap;
    private final Map managedConnectionFactoryInfoMap;
    private final String objectName;
    private final String displayName;
    private final String description;
    private final String vendorName;
    private final String resourceAdapterVersion;
    private final String eisType;

    public ResourceAdapterModuleImpl(String objectName,
                                     ResourceAdapter resourceAdapter,
                                     J2EEServer server,
                                     J2EEApplication application,
                                     String deploymentDescriptor,
                                     GBeanData resourceAdapterGBeanData,
                                     Map activationSpecInfoMap,
                                     Map adminObjectInfoMap,
                                     Map managedConnectionFactoryInfoMap,
                                     String displayName,
                                     String description,
                                     String vendorName,
                                     String resourceAdapterVersion,
                                     String eisType) {
        this.objectName = objectName;
        ObjectName myObjectName = ObjectNameUtil.getObjectName(objectName);
        verifyObjectName(myObjectName);

        this.resourceAdapter = resourceAdapter;

        this.server = server;
        this.application = application;
        this.deploymentDescriptor = deploymentDescriptor;

        this.resourceAdapterGBeanData = resourceAdapterGBeanData;
        this.activationSpecInfoMap = activationSpecInfoMap;
        this.adminObjectInfoMap = adminObjectInfoMap;
        this.managedConnectionFactoryInfoMap = managedConnectionFactoryInfoMap;
        this.description = description;
        this.displayName = displayName;
        this.vendorName = vendorName;
        this.resourceAdapterVersion = resourceAdapterVersion;
        this.eisType = eisType;
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
        return new String[]{resourceAdapter.getObjectName()};
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

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getVendorName() {
        return vendorName;
    }

    public String getResourceAdapterVersion() {
        return resourceAdapterVersion;
    }

    public String getEISType() {
        return eisType;
    }

    public ResourceAdapter[] getResourceAdapterInstances() {
        return new ResourceAdapter[] {resourceAdapter};
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
