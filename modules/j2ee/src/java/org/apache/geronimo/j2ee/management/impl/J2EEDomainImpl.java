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
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.J2EEDomain;

/**
 * @version $Rev$ $Date$
 */
public class J2EEDomainImpl implements J2EEDomain {
    private final Kernel kernel;
    private final String baseName;
    private final String objectName;

    public J2EEDomainImpl(Kernel kernel, String objectName) {
        this.objectName = objectName;
        ObjectName myObjectName = JMXUtil.getObjectName(this.objectName);
        verifyObjectName(myObjectName);
        baseName = myObjectName.getDomain() + ":";

        this.kernel = kernel;
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
     * domain:j2eeType=J2EEDomain,name=domain
     */
    private void verifyObjectName(ObjectName objectName) {
        if (objectName.isPattern()) {
            throw new InvalidObjectNameException("ObjectName can not be a pattern", objectName);
        }
        Hashtable keyPropertyList = objectName.getKeyPropertyList();
        if (!"J2EEDomain".equals(keyPropertyList.get("j2eeType"))) {
            throw new InvalidObjectNameException("J2EEDomain object name j2eeType property must be 'J2EEDomain'", objectName);
        }
        String name = (String) keyPropertyList.get("name");
        if (!objectName.getDomain().equals(name)) {
            throw new InvalidObjectNameException("Domain part of J2EEDomain object name must match name propert", objectName);
        }
    }


    public String[] getServers() {
        return Util.getObjectNames(kernel, baseName, new String[]{"J2EEServer"});
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(J2EEDomainImpl.class, NameFactory.J2EE_DOMAIN);

        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addAttribute("servers", String[].class, false);

        infoFactory.setConstructor(new String[]{"kernel", "objectName"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
