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
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.management.J2EEServer;
import org.apache.geronimo.j2ee.management.impl.InvalidObjectNameException;
import org.apache.geronimo.kernel.jmx.JMXUtil;

/**
 * @version $Rev$ $Date$
 */
public class JCAConnectionFactoryImpl {
    private final J2EEServer server;
    private final String managedConnectionFactory;

    public JCAConnectionFactoryImpl(String objectName, J2EEServer server, String managedConnectionFactory) {
        ObjectName myObjectName = JMXUtil.getObjectName(objectName);
        verifyObjectName(myObjectName);

        this.server = server;
        this.managedConnectionFactory = managedConnectionFactory;
    }

    public String getServer() {
        return server.getObjectName();
    }

    public String getManagedConnectionFactory() {
        return managedConnectionFactory;
    }

    /**
     * ObjectName must match this pattern:
     * <p/>
     * domain:j2eeType=JCAConnectionFactory,name=MyName,J2EEServer=MyServer,JCAResource=MyJCAResource
     */
    private void verifyObjectName(ObjectName objectName) {
        if (objectName.isPattern()) {
            throw new InvalidObjectNameException("ObjectName can not be a pattern", objectName);
        }
        Hashtable keyPropertyList = objectName.getKeyPropertyList();
        if (!"JCAConnectionFactory".equals(keyPropertyList.get("j2eeType"))) {
            throw new InvalidObjectNameException("JCAConnectionFactory object name j2eeType property must be 'JCAConnectionFactory'", objectName);
        }
        if (!keyPropertyList.containsKey("name")) {
            throw new InvalidObjectNameException("JCAConnectionFactory object must contain a name property", objectName);
        }
        if (!keyPropertyList.containsKey("J2EEServer")) {
            throw new InvalidObjectNameException("JCAConnectionFactory object name must contain a J2EEServer property", objectName);
        }
        if (!keyPropertyList.containsKey("JCAResource")) {
            throw new InvalidObjectNameException("JCAResource object name must contain a JCAResource property", objectName);
        }
//        if (keyPropertyList.size() != 4) {
//            throw new InvalidObjectNameException("JCAConnectionFactory object name can only have j2eeType, name, JCAResource, and J2EEServer properties", objectName);
//        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(JCAConnectionFactoryImpl.class);
        infoFactory.addReference("J2EEServer", J2EEServer.class);

        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addAttribute("server", String.class, false);
        infoFactory.addAttribute("managedConnectionFactory", String.class, true);

        infoFactory.setConstructor(new String[]{"objectName", "J2EEServer", "managedConnectionFactory"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
