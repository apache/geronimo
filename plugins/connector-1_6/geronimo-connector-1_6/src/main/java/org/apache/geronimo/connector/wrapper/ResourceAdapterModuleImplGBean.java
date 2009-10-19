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

import java.util.Map;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.management.J2EEApplication;
import org.apache.geronimo.management.J2EEServer;
import org.apache.geronimo.management.geronimo.ResourceAdapter;
import org.apache.geronimo.management.geronimo.ResourceAdapterModule;

/**
 * @version $Revision$
 */
public class ResourceAdapterModuleImplGBean {

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(ResourceAdapterModuleImplGBean.class, ResourceAdapterModuleImpl.class, NameFactory.RESOURCE_ADAPTER_MODULE);
        infoBuilder.addReference("J2EEServer", J2EEServer.class, null);
        infoBuilder.addReference("J2EEApplication", J2EEApplication.class, null);

        infoBuilder.addAttribute("deploymentDescriptor", String.class, true);

        infoBuilder.addReference("ResourceAdapter", ResourceAdapter.class, NameFactory.RESOURCE_ADAPTER);
        infoBuilder.addAttribute("objectName", String.class, false);
        infoBuilder.addAttribute("server", String.class, false);
        infoBuilder.addAttribute("application", String.class, false);
        infoBuilder.addAttribute("javaVMs", String[].class, false);
        infoBuilder.addAttribute("resourceAdapters", String[].class, false);

        infoBuilder.addAttribute("resourceAdapterGBeanData", GBeanData.class, true);
        infoBuilder.addAttribute("activationSpecInfoMap", Map.class, true);
        infoBuilder.addAttribute("adminObjectInfoMap", Map.class, true);
        infoBuilder.addAttribute("managedConnectionFactoryInfoMap", Map.class, true);

        infoBuilder.addAttribute("displayName", String.class, true, false);
        infoBuilder.addAttribute("description", String.class, true, false);
        infoBuilder.addAttribute("vendorName", String.class, true, false);
        infoBuilder.addAttribute("EISType", String.class, true, false);
        infoBuilder.addAttribute("resourceAdapterVersion", String.class, true, false);


        infoBuilder.addInterface(ResourceAdapterModule.class);

        infoBuilder.setConstructor(new String[]{
                "objectName",
                "ResourceAdapter",
                "J2EEServer",
                "J2EEApplication",
                "deploymentDescriptor",
                "resourceAdapterGBeanData",
                "activationSpecInfoMap",
                "adminObjectInfoMap",
                "managedConnectionFactoryInfoMap",
                "displayName",
                "description",
                "vendorName",
                "resourceAdapterVersion",
                "EISType"
        });

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
