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

import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.management.geronimo.JMSBroker;
import org.apache.geronimo.management.geronimo.JMSManager;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Common methods for JMS portlets
 *
 * @version $Rev$ $Date$
 */
public class BaseJMSPortlet extends BasePortlet {
    /**
     * Gets a Map relating broker name to JMSBroker instance
     */
    protected static Map getBrokerMap(RenderRequest renderRequest, JMSManager manager) throws PortletException {

        JMSBroker[] brokers = (JMSBroker[]) manager.getContainers();
        Map map = new LinkedHashMap();
        try {
            for (int i = 0; i < brokers.length; i++) {
                AbstractName name = PortletManager.getNameFor(renderRequest, brokers[i]);
                map.put(name.getName().get("name"), brokers[i]);
            }
        } catch (Exception e) {
            throw new PortletException(e);
        }
        return map;
    }
}
