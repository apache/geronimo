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
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.ObjectNameUtil;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.management.geronimo.J2EEDomain;
import org.apache.geronimo.management.geronimo.J2EEServer;

/**
 * @version $Rev$ $Date$
 */
public class J2EEDomainImpl implements J2EEDomain {
    private final String objectName;
    private final Collection servers;

    public J2EEDomainImpl(String objectName, Collection servers) {
        this.objectName = objectName;
        ObjectName myObjectName = ObjectNameUtil.getObjectName(this.objectName);
        verifyObjectName(myObjectName);
        this.servers = servers;
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
        return Util.getObjectNames(getServerInstances());
    }

    public J2EEServer[] getServerInstances() {
        if (servers == null) return new J2EEServer[0];
        return (J2EEServer[]) servers.toArray(new J2EEServer[servers.size()]);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(J2EEDomainImpl.class, NameFactory.J2EE_DOMAIN);

        infoFactory.addReference("Servers", J2EEServer.class, NameFactory.J2EE_SERVER);
        infoFactory.setConstructor(new String[]{"objectName", "Servers"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
