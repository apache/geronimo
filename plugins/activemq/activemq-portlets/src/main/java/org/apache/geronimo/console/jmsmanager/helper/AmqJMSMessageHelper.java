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

import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.portlet.PortletRequest;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.broker.jmx.CompositeDataConstants;
import org.apache.activemq.broker.jmx.DestinationViewMBean;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.apache.activemq.broker.jmx.TopicViewMBean;
import org.apache.activemq.command.BrokerInfo;
import org.apache.geronimo.console.jmsmanager.DestinationStatistics;
import org.apache.geronimo.console.jmsmanager.DestinationType;
import org.apache.geronimo.console.jmsmanager.JMSDestinationInfo;
import org.apache.geronimo.console.jmsmanager.JMSMessageInfo;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.management.geronimo.ResourceAdapterModule;
import org.apache.geronimo.system.jmx.MBeanServerReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class AmqJMSMessageHelper extends JMSMessageHelper {

    private static final Logger logger = LoggerFactory.getLogger(AmqJMSMessageHelper.class);

    @SuppressWarnings("unchecked")
    public void purge(PortletRequest portletRequest, JMSDestinationInfo destinationInfo) throws JMSException {
        try {
            if (destinationInfo.getType().equals(DestinationType.Queue)) {
                BrokerInfo brokerInfo = getBrokerInfo(portletRequest, destinationInfo);
                if (brokerInfo == null || !isInLocalMBeanServer(brokerInfo)) {
                    throw new JMSException("Currently, only queue belong to local broker is supported");
                }
                MBeanServer server = getMBeanServer();
                ObjectName destinationObjectName = createDestinationObjectName(brokerInfo.getBrokerName(), destinationInfo.getType().name(), destinationInfo.getPhysicalName());
                QueueViewMBean proxy;
                if (!server.isRegistered(destinationObjectName)) {
                    // mbean is not yet registered.Adding the destination to activemq broker.
                    ObjectName brokerObjectName = createBrokerObjectName(brokerInfo.getBrokerName());
                    Set set = server.queryMBeans(brokerObjectName, null);
                    Iterator it = set.iterator();
                    if (it.hasNext()) {
                        ObjectInstance instance = (ObjectInstance) it.next();
                        brokerObjectName = instance.getObjectName();
                    }
                    BrokerViewMBean brokerMBean = (BrokerViewMBean) MBeanServerInvocationHandler.newProxyInstance(server, brokerObjectName, BrokerViewMBean.class, true);
                    brokerMBean.addQueue(destinationInfo.getPhysicalName());
                }
                proxy = (QueueViewMBean) MBeanServerInvocationHandler.newProxyInstance(server, destinationObjectName, QueueViewMBean.class, true);
                proxy.purge();
            } else {
                throw new JMSException("Purge action on topic is not supported");
            }
        } catch (MalformedObjectNameException e) {
            throw createJMSException("Fail to find the target object name", e);
        } catch (JMSException e) {
            throw e;
        } catch (Exception e) {
            throw createJMSException("Error occured in the purge action", e);
        }
    }

    private JMSException createJMSException(String reason, Exception e) {
        JMSException jmsException = new JMSException(reason);
        jmsException.setLinkedException(e);
        return jmsException;
    }

    protected ObjectName createDestinationObjectName(String brokerName, String destinationType, String destinactionPhysicalName) throws MalformedObjectNameException {
        return new ObjectName("org.apache.activemq" + ":BrokerName=" + brokerName + ",Type=" + destinationType + ",Destination=" + destinactionPhysicalName);
    }

    protected ObjectName createBrokerObjectName(String brokerName) throws MalformedObjectNameException {
        return new ObjectName("org.apache.activemq" + ":BrokerName=" + brokerName + ",Type=Broker");
    }

    @Override
    protected ConnectionFactory getConnectionFactory(PortletRequest portletRequest, JMSDestinationInfo destinationInfo) throws JMSException{
        ConnectionFactory connectionFactory = super.getConnectionFactory(portletRequest, destinationInfo);
        if (connectionFactory == null) {
            connectionFactory = createActiveMQConnectionFactory(portletRequest, destinationInfo);
        }
        return connectionFactory;
    }

    private ActiveMQConnectionFactory createActiveMQConnectionFactory(PortletRequest portletRequest, JMSDestinationInfo destinationInfo) throws JMSException {
        try {
            Kernel kernel = PortletManager.getKernel();
            ResourceAdapterModule resourceAdapterModule = (ResourceAdapterModule) PortletManager.getManagementHelper(portletRequest).getObject(destinationInfo.getResourceAdapterModuleAbName());
            AbstractName resourceAdapterAbstractName = PortletManager.getNameFor(portletRequest, resourceAdapterModule.getResourceAdapterInstances()[0].getJCAResourceImplementations()[0]
                    .getResourceAdapterInstances()[0]);
            if (kernel.isRunning(resourceAdapterAbstractName)) {
                String serverUrl = (String) kernel.getAttribute(resourceAdapterAbstractName, "ServerUrl");
                String userName = (String) kernel.getAttribute(resourceAdapterAbstractName, "UserName");
                String password = (String) kernel.getAttribute(resourceAdapterAbstractName, "Password");
                return new ActiveMQConnectionFactory(userName, password, serverUrl);
            }
            throw new JMSException("Fail to create ActiveMQConnectionFactory for the resource adapter module is not in running status");
        } catch (JMSException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Fail to create ActiveMQConnectionFactory", e);
            throw createJMSException("Fail to create ActiveMQConnectionFactory", e);
        }
    }

    @Override
    public DestinationStatistics getDestinationStatistics(PortletRequest portletRequest, JMSDestinationInfo destinationInfo) throws JMSException {
        DestinationStatistics stat = new DestinationStatistics();
        try {
            BrokerInfo brokerInfo = getBrokerInfo(portletRequest, destinationInfo);
            if (brokerInfo == null || !isInLocalMBeanServer(brokerInfo)) {
                return stat;
            }
            MBeanServer server = getMBeanServer();
            ObjectName objName = createDestinationObjectName(brokerInfo.getBrokerName(), destinationInfo.getType().name(), destinationInfo.getPhysicalName());
            DestinationViewMBean proxy;
            if (destinationInfo.getType().equals(DestinationType.Queue)) {
                if (!server.isRegistered(objName)) {
                    // mbean is not yet registered.Adding the destination to activemq broker.
                    ObjectName brokerObj = createBrokerObjectName(brokerInfo.getBrokerName());
                    Set set = server.queryMBeans(brokerObj, null);
                    Iterator it = set.iterator();
                    if (it.hasNext()) {
                        ObjectInstance instance = (ObjectInstance) it.next();
                        brokerObj = instance.getObjectName();
                    }
                    BrokerViewMBean brokerMBean = (BrokerViewMBean) MBeanServerInvocationHandler.newProxyInstance(server, brokerObj, BrokerViewMBean.class, true);
                    brokerMBean.addQueue(destinationInfo.getPhysicalName());
                }
                proxy = (DestinationViewMBean) MBeanServerInvocationHandler.newProxyInstance(server, objName, QueueViewMBean.class, true);
            } else {
                if (!server.isRegistered(objName)) {
                    // mbean is not yet registered.Adding the destination to activemq broker.
                    ObjectName brokerObj = createBrokerObjectName(brokerInfo.getBrokerName());
                    Set set = server.queryMBeans(brokerObj, null);
                    Iterator it = set.iterator();
                    if (it.hasNext()) {
                        ObjectInstance instance = (ObjectInstance) it.next();
                        brokerObj = instance.getObjectName();
                    }
                    BrokerViewMBean brokerMBean = (BrokerViewMBean) MBeanServerInvocationHandler.newProxyInstance(server, brokerObj, BrokerViewMBean.class, true);
                    brokerMBean.addTopic(destinationInfo.getPhysicalName());
                }
                proxy = (DestinationViewMBean) MBeanServerInvocationHandler.newProxyInstance(server, objName, TopicViewMBean.class, true);
            }
            stat.setConsumerCount(proxy.getConsumerCount());
            stat.setEnqueueCount(proxy.getEnqueueCount());
            stat.setDequeueCount(proxy.getDequeueCount());
            stat.setQueueSize(proxy.getQueueSize());
        } catch (Exception ex) {
            logger.warn("Failed to get ActiveMQ stats", ex);
        }
        return stat;
    }

    protected JMSMessageInfo[] getMessagesFromTopic(PortletRequest portletRequest, JMSDestinationInfo destinationInfo, String selector) throws JMSException {
        BrokerInfo brokerInfo = getBrokerInfo(portletRequest, destinationInfo);
        if (brokerInfo == null || !isInLocalMBeanServer(brokerInfo)) {
            return new JMSMessageInfo[0];
        }
        try {
            ObjectName destinationObjectName = createDestinationObjectName(brokerInfo.getBrokerName(), destinationInfo.getType().name(), destinationInfo.getPhysicalName());
            MBeanServer mBeanServer = getMBeanServer();
            ObjectInstance objectInstance = mBeanServer.getObjectInstance(destinationObjectName);
            CompositeData[] compositeData = (CompositeData[]) mBeanServer.invoke(objectInstance.getObjectName(), "browse", new Object[] { selector }, new String[] { String.class.getName() });
            if (compositeData.length > 0) {
                JMSMessageInfo[] messageInfos = new JMSMessageInfo[compositeData.length];
                for (int i = 0; i < compositeData.length; i++) {
                    JMSMessageInfo jmsMessageInfo = new JMSMessageInfo();
                    CompositeData data = compositeData[i];
                    if (compositeData[0].getCompositeType().getTypeName().equals("org.apache.activemq.command.ActiveMQTextMessage")) {
                        jmsMessageInfo.setMessage((String) data.get(CompositeDataConstants.MESSAGE_TEXT));
                    } else {
                        jmsMessageInfo.setMessage("Only Text Messages will be displayed..");
                    }
                    jmsMessageInfo.setPriority((Integer) data.get("JMSPriority"));
                    jmsMessageInfo.setMessageId((String) data.get("JMSMessageID"));
                    jmsMessageInfo.setDestination((String) data.get("JMSDestination"));
                    jmsMessageInfo.setTimeStamp(((Date) data.get("JMSTimestamp")).getTime());
                    jmsMessageInfo.setExpiration((Long) data.get("JMSExpiration"));
                    jmsMessageInfo.setJmsType((String) data.get("JMSType"));
                    jmsMessageInfo.setReplyTo((String) data.get("JMSReplyTo"));
                    jmsMessageInfo.setCorrelationId((String) data.get("JMSCorrelationID"));
                    messageInfos[i] = jmsMessageInfo;
                }
                return messageInfos;
            }
            return new JMSMessageInfo[0];
        } catch (Exception e) {
            throw createJMSException("Fail to get messages of the topic " + destinationInfo.getPhysicalName(), e); 
        }
    }

    protected MBeanServer getMBeanServer() {
        MBeanServerReference ref;
        try {
            ref = kernel.getGBean(MBeanServerReference.class);
            return ref.getMBeanServer();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isInLocalMBeanServer(BrokerInfo brokerInfo) {
        try {
            ObjectName brokerObjectNameQuery = new ObjectName("org.apache.activemq" + ":*,Type=Broker");
            MBeanServer mBeanServer = getMBeanServer();
            Set<ObjectInstance> brokerObjectInstances = mBeanServer.queryMBeans(brokerObjectNameQuery, null);
            String targetBrokerId = brokerInfo.getBrokerId().getValue();
            for (ObjectInstance objectInstance : brokerObjectInstances) {
                String brokerId = (String) mBeanServer.getAttribute(objectInstance.getObjectName(), "BrokerId");
                if (targetBrokerId.equals(brokerId)) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("Fail to check the broker in local mbeanserver", e);
            return false;
        }
        return false;
    }

    private BrokerInfo getBrokerInfo(PortletRequest portletRequest, JMSDestinationInfo destinationInfo) throws JMSException {
        ActiveMQConnectionFactory connectionFactory = createActiveMQConnectionFactory(portletRequest, destinationInfo);
        ActiveMQConnection connection = null;
        try {
            connection = (ActiveMQConnection) connectionFactory.createConnection();
            connection.start();
            return connection.getBrokerInfo();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                }
            }
        }
    }
}
