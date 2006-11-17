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
package org.apache.geronimo.j2ee.management.impl;

import java.util.Hashtable;
import java.util.Collection;
import java.util.ArrayList;
import javax.management.ObjectName;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.ObjectNameUtil;
import org.apache.geronimo.management.AppClientModule;
import org.apache.geronimo.management.EJBModule;
import org.apache.geronimo.management.J2EEModule;
import org.apache.geronimo.management.J2EEResource;
import org.apache.geronimo.management.geronimo.J2EEApplication;
import org.apache.geronimo.management.geronimo.J2EEServer;
import org.apache.geronimo.management.geronimo.ResourceAdapterModule;
import org.apache.geronimo.management.geronimo.WebModule;

/**
 * @version $Rev$ $Date$
 */
public class J2EEApplicationImpl implements J2EEApplication {
    private final String objectName;
    private final String deploymentDescriptor;
    private final J2EEServer server;
    private final Collection resources;
    private final Collection appClientModules;
    private final Collection ejbModules;
    private final Collection resourceAdapterModules;
    private final Collection webModules;

    public J2EEApplicationImpl(String objectName,
            String deploymentDescriptor,
            J2EEServer server,
            Collection resources,
            Collection appClientModules,
            Collection ejbModules,
            Collection resourceAdapterModules,
            Collection webModules) {

        this.objectName = objectName;
        ObjectName myObjectName = ObjectNameUtil.getObjectName(this.objectName);
        verifyObjectName(myObjectName);

        this.deploymentDescriptor = deploymentDescriptor;
        this.server = server;
        this.resources = resources;
        this.appClientModules = appClientModules;
        this.ejbModules = ejbModules;
        this.resourceAdapterModules = resourceAdapterModules;
        this.webModules = webModules;
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

    public String[] getModules() {
        return Util.getObjectNames(getModulesInstances());
    }

    public J2EEModule[] getModulesInstances() {
        ArrayList objects = new ArrayList();
        if (appClientModules != null) {
            objects.addAll(appClientModules);
        }
        if (ejbModules != null) {
            objects.addAll(ejbModules);
        }
        if (webModules != null) {
            objects.addAll(webModules);
        }
        if (resourceAdapterModules != null) {
            objects.addAll(resourceAdapterModules);
        }

        return (J2EEModule[]) objects.toArray(new J2EEModule[objects.size()]);
    }

    public J2EEResource[] getResources() {
        if (resources == null) return new J2EEResource[0];
        return (J2EEResource[]) resources.toArray(new J2EEResource[resources.size()]);
    }

    public AppClientModule[] getClientModules() {
        if (appClientModules == null) return new AppClientModule[0];
        return (AppClientModule[]) appClientModules.toArray(new AppClientModule[appClientModules.size()]);
    }

    public EJBModule[] getEJBModules() {
        if (ejbModules == null) return new EJBModule[0];
        return (EJBModule[]) ejbModules.toArray(new EJBModule[ejbModules.size()]);
    }

    public ResourceAdapterModule[] getRAModules() {
        if (resourceAdapterModules == null) return new ResourceAdapterModule[0];
        return (ResourceAdapterModule[]) resourceAdapterModules.toArray(new ResourceAdapterModule[resourceAdapterModules.size()]);
    }

    public WebModule[] getWebModules() {
        if (webModules == null) return new WebModule[0];
        return (WebModule[]) webModules.toArray(new WebModule[webModules.size()]);
    }

    public String getDeploymentDescriptor() {
        return deploymentDescriptor;
    }

    public String getServer() {
        return server.getObjectName();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(J2EEApplicationImpl.class, NameFactory.J2EE_APPLICATION);
        infoFactory.addAttribute("deploymentDescriptor", String.class, true);
        infoFactory.addReference("Server", J2EEServer.class);
        infoFactory.addReference("Resources", J2EEResource.class);
        infoFactory.addReference("AppClientModules", AppClientModule.class);
        infoFactory.addReference("EJBModules", EJBModule.class);
        infoFactory.addReference("ResourceAdapterModules", ResourceAdapterModule.class);
        infoFactory.addReference("WebModules", WebModule.class);

        infoFactory.setConstructor(new String[]{
                "objectName",
                "deploymentDescriptor",
                "Server",
                "Resources",
                "AppClientModules",
                "EJBModules",
                "ResourceAdapterModules",
                "WebModules",
        });

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
