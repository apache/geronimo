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
package org.apache.geronimo.connector;

import java.util.Map;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.management.J2EEServer;
import org.apache.geronimo.j2ee.management.J2EEApplication;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanData;

/**
 * @version $Rev:  $ $Date:  $
 */
public class ResourceAdapterImpl {
    private final String jcaResource;

    public ResourceAdapterImpl(String objectName, String jcaResource) throws MalformedObjectNameException {
        this.jcaResource = jcaResource;
    }

    public String getJCAResource() {
        return jcaResource;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(ResourceAdapterImpl.class, NameFactory.RESOURCE_ADAPTER);
        infoBuilder.addAttribute("objectName", String.class, false);
        infoBuilder.addAttribute("JCAResource", String.class, true);

        infoBuilder.setConstructor(new String[]{
            "objectName",
            "JCAResource"
        });

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
