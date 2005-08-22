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
 * @version $Rev: 46228 $ $Date: 2004-09-16 21:21:04 -0400 (Thu, 16 Sep 2004) $
 */
public class BaseJMSPortlet extends BasePortlet {
    protected static Map getBrokerMap(RenderRequest renderRequest) throws PortletException {
        JMSBroker[] brokers;
        Map map = new LinkedHashMap();
        try {
            brokers = PortletManager.getJMSBrokers(renderRequest);
            for (int i = 0; i < brokers.length; i++) {
                JMSBroker broker = brokers[i];
                String string = ((GeronimoManagedBean)broker).getObjectName();
                ObjectName name = ObjectName.getInstance(string);
                map.put(name.getKeyProperty("name"), broker);
            }
        } catch (Exception e) {
            throw new PortletException(e);
        }
        return map;
    }
}
