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
package org.apache.geronimo.console.jmsmanager.helper;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.portlet.PortletRequest;

import org.apache.geronimo.activemq.BrokerServiceGBeanImpl;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.system.jmx.MBeanServerReference;
import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.apache.activemq.broker.jmx.TopicViewMBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Rev$ $Date$
 */
public class AmqJMSMessageHelper extends JMSMessageHelper {
    private static final Log log = LogFactory.getLog(AmqJMSMessageHelper.class);

    public void purge(PortletRequest renderRequest, String type, String physicalQName) {
        try {
            MBeanServer server = getMBeanServer();
            String brokerName = getBrokerName();
            ObjectName objName = new ObjectName("org.apache.activemq" + ":BrokerName=" + brokerName + ",Type=" + type + ",Destination=" + physicalQName);
            if ("Queue".equals(type)) {
                QueueViewMBean proxy = null;
                if (!server.isRegistered(objName)) {
                    // mbean is not yet registered.Adding the destination to activemq broker.
                    ObjectName brokerObj = new ObjectName("org.apache.activemq" + ":BrokerName=" + brokerName + ",Type=Broker");
                    Set set = server.queryMBeans(brokerObj, null);
                    Iterator it = set.iterator();
                    if (it.hasNext()) {
                        ObjectInstance instance = (ObjectInstance) it.next();
                        brokerObj = instance.getObjectName();
                    }
                    BrokerViewMBean brokerMBean = (BrokerViewMBean) MBeanServerInvocationHandler.newProxyInstance(server, brokerObj, BrokerViewMBean.class, true);
                    brokerMBean.addQueue(physicalQName);
                }
                proxy = (QueueViewMBean) MBeanServerInvocationHandler.newProxyInstance(server, objName, QueueViewMBean.class, true);
                proxy.purge();

            }
        } catch (Exception ex) {
            // ignoring the exception
            log.error(ex);
        }
    }

    protected List getMessagesFromTopic(String type, String physicalQName) throws Exception {
        /*
         * MBeanServer server = getMBeanServer(); ObjectName objName = new
         * ObjectName("org.apache.activemq"+":BrokerName=localhost,Type="+type+",Destination="+physicalQName);
         * if(!server.isRegistered(objName)){ //mbean is not yet registered.Adding the destination to activemq broker.
         * ObjectName brokerObj = new ObjectName("org.apache.activemq"+":BrokerName=localhost,Type=Broker");
         * BrokerViewMBean brokerMBean = (BrokerViewMBean)MBeanServerInvocationHandler.newProxyInstance(server,
         * brokerObj, BrokerViewMBean.class, true); brokerMBean.addTopic(physicalQName); } TopicViewMBean mbean =
         * (TopicViewMBean)MBeanServerInvocationHandler.newProxyInstance(server, objName, TopicViewMBean.class, true);
         * return mbean.browseMessages();
         */
        return null;
    }

    private MBeanServer getMBeanServer() throws Exception {
        MBeanServerReference ref = (MBeanServerReference) kernel.getGBean(MBeanServerReference.class);
        return ref.getMBeanServer();
    }

    private String getBrokerName() {
        // default broker name
        String brokerName = "localhost";
        try {
            BrokerServiceGBeanImpl ref = (BrokerServiceGBeanImpl) kernel.getGBean(BrokerServiceGBeanImpl.class);
            brokerName = ref.getBrokerName();
        } catch (Exception e) {
            log.equals(e);
        }
        return brokerName;
    }
}
