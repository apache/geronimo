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
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.management.J2EEApplication;
import org.apache.geronimo.management.J2EEServer;
import org.apache.geronimo.management.AppClientModule;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.ObjectNameUtil;

/**
 * @version $Revision$ $Date$
 */
public class J2EEAppClientModuleImpl implements AppClientModule {
    private final String deploymentDescriptor;
    private final J2EEServer server;
    private final J2EEApplication application;
    private final ClassLoader classLoader;
    private final String objectName;

    public J2EEAppClientModuleImpl(String objectName, J2EEServer server, J2EEApplication application, String deploymentDescriptor, ClassLoader classLoader) {
        this.objectName = objectName;
        ObjectName myObjectName = ObjectNameUtil.getObjectName(this.objectName);
        verifyObjectName(myObjectName);

        this.server = server;
        this.application = application;
        this.deploymentDescriptor = deploymentDescriptor;
        this.classLoader = classLoader;
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
     * domain:j2eeType=AppClientModule,name=MyName,J2EEServer=MyServer,J2EEApplication=MyApplication
     */
    private void verifyObjectName(ObjectName objectName) {
        if (objectName.isPattern()) {
            throw new InvalidObjectNameException("ObjectName can not be a pattern", objectName);
        }
        Hashtable keyPropertyList = objectName.getKeyPropertyList();
        if (!"AppClientModule".equals(keyPropertyList.get("j2eeType"))) {
            throw new InvalidObjectNameException("AppClientModule object name j2eeType property must be 'AppClientModule'", objectName);
        }
        if (!keyPropertyList.containsKey("name")) {
            throw new InvalidObjectNameException("AppClientModule object must contain a name property", objectName);
        }
        if (!keyPropertyList.containsKey("J2EEServer")) {
            throw new InvalidObjectNameException("AppClientModule object name must contain a J2EEServer property", objectName);
        }
        if (!keyPropertyList.containsKey("J2EEApplication")) {
            throw new InvalidObjectNameException("AppClientModule object name must contain a J2EEApplication property", objectName);
        }
        if (keyPropertyList.size() != 4) {
            throw new InvalidObjectNameException("AppClientModule object name can only have j2eeType, name, J2EEApplication, and J2EEServer properties", objectName);
        }
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

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(J2EEAppClientModuleImpl.class, NameFactory.APP_CLIENT_MODULE);
        infoFactory.addReference("J2EEServer", J2EEServer.class);
        infoFactory.addReference("J2EEApplication", J2EEApplication.class);

        infoFactory.addAttribute("deploymentDescriptor", String.class, true);

        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addAttribute("server", String.class, false);
        infoFactory.addAttribute("application", String.class, false);
        infoFactory.addAttribute("javaVMs", String[].class, false);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addInterface(AppClientModule.class);


        infoFactory.setConstructor(new String[]{
            "objectName",
            "J2EEServer",
            "J2EEApplication",
            "deploymentDescriptor",
            "classLoader"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
