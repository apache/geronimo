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
package org.apache.geronimo.console.jmsmanager.server;

import java.util.Map;
import java.util.LinkedHashMap;
import javax.portlet.RenderRequest;
import javax.portlet.PortletException;
import javax.management.ObjectName;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.management.geronimo.JMSBroker;
import org.apache.geronimo.kernel.proxy.GeronimoManagedBean;

/**
 * Common methods for JMS portlets
 *
 * @version $Rev$ $Date$
 */
public class BaseJMSPortlet extends BasePortlet {
    protected static Map getBrokerMap(RenderRequest renderRequest, String managerObjectName) throws PortletException {
        JMSBroker[] brokers;
        Map map = new LinkedHashMap();
        try {
            String[] names = PortletManager.getJMSBrokerNames(renderRequest, managerObjectName);
            brokers = new JMSBroker[names.length];
            for (int i = 0; i < names.length; i++) {
                String name = names[i];
                JMSBroker broker = PortletManager.getJMSBroker(renderRequest, name);
                brokers[i] = broker;
                ObjectName objectName = ObjectName.getInstance(name);
                map.put(objectName.getKeyProperty("name"), broker);
            }
        } catch (Exception e) {
            throw new PortletException(e);
        }
        return map;
    }
}
