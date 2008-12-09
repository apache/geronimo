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
package org.apache.geronimo.console.jmsmanager.server;

import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.management.geronimo.JMSBroker;
import org.apache.geronimo.management.geronimo.JMSManager;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Common methods for JMS portlets
 *
 * @version $Rev$ $Date$
 */
public class BaseJMSPortlet extends BasePortlet {
    /**
     * Gets a Map relating broker name to JMSBroker instance
     */
    protected static List<BrokerWrapper> getBrokerList(RenderRequest renderRequest, JMSManager manager) throws PortletException {

        JMSBroker[] brokers = (JMSBroker[]) manager.getContainers();
        List<BrokerWrapper> beans = new ArrayList<BrokerWrapper>();
        try {
            for (JMSBroker broker : brokers) {
                AbstractName abstractName = PortletManager.getNameFor(renderRequest, broker);
                String displayName = abstractName.getName().get("name").toString();
                beans.add(new BrokerWrapper(displayName, abstractName.toString(), broker));
            }
        } catch (Exception e) {
            throw new PortletException(e);
        }
        return beans;
    }
    
    public static class BrokerWrapper {
        private String brokerName;
        private String brokerURI;
        private JMSBroker broker;

        public BrokerWrapper(String brokerName, String brokerURI, JMSBroker broker) {
            this.brokerName = brokerName;
            this.brokerURI = brokerURI;
            this.broker = broker;
        }

        public String getBrokerName() {
            return brokerName;
        }

        public JMSBroker getBroker() {
            return broker;
        }

        public String getBrokerURI() {
            return brokerURI;
        }
    }
}
