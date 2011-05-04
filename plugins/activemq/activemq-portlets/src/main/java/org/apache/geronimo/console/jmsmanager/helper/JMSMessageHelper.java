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

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.management.ObjectName;
import javax.portlet.PortletRequest;

import org.apache.geronimo.console.jmsmanager.DestinationStatistics;
import org.apache.geronimo.console.jmsmanager.DestinationType;
import org.apache.geronimo.console.jmsmanager.JMSDestinationInfo;
import org.apache.geronimo.console.jmsmanager.JMSMessageInfo;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.proxy.GeronimoManagedBean;
import org.apache.geronimo.management.geronimo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public abstract class JMSMessageHelper {

    protected static final Kernel kernel = KernelRegistry.getSingleKernel();

    private static final Logger logger = LoggerFactory.getLogger(JMSMessageHelper.class);

    public void sendMessage(PortletRequest portletRequest, JMSDestinationInfo destinationInfo, JMSMessageInfo messageInfo) throws JMSException {
        Destination destination = getDestination(portletRequest, destinationInfo);
        if (destination == null) {
            throw new JMSException("Unable to find the destination....Not sending message");
        }
        ConnectionFactory connectionFactory = getConnectionFactory(portletRequest, destinationInfo);
        if (connectionFactory == null) {
            throw new JMSException("Unable to find the Connection Factory....Not sending message");
        }
        Connection connection = null;
        Session session = null;
        try {
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(true, Session.DUPS_OK_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(destination);
            int deliveryMode = 0;
            if (messageInfo.isPersistent()) {
                deliveryMode = DeliveryMode.PERSISTENT;
            } else {
                deliveryMode = DeliveryMode.NON_PERSISTENT;
            }
            producer.setDeliveryMode(deliveryMode);
            TextMessage msg = session.createTextMessage();
            msg.setText(messageInfo.getMessage());
            msg.setJMSCorrelationID(messageInfo.getCorrelationId());
            msg.setJMSPriority(messageInfo.getPriority());
            msg.setJMSType(messageInfo.getJmsType());
            producer.send(msg, deliveryMode, messageInfo.getPriority(), TextMessage.DEFAULT_TIME_TO_LIVE);
            session.commit();
        } finally {
            if (connection != null)
                try {
                    connection.close();
                } catch (Exception e) {
                }
        }
    }

    public JMSMessageInfo[] getMessagesList(PortletRequest portletRequest, JMSDestinationInfo jmsDestinationInfo, String selector) throws JMSException {
        Destination destination = getDestination(portletRequest, jmsDestinationInfo);
        if (destination == null) {
            throw new JMSException("Fail to find the destination " + jmsDestinationInfo.getPhysicalName());
        }
        if (jmsDestinationInfo.getType().equals(DestinationType.Queue)) {
            return getMessageFromQueue(portletRequest, jmsDestinationInfo, selector);
        } else {
            return getMessagesFromTopic(portletRequest, jmsDestinationInfo, selector);
        }
    }

    protected ConnectionFactory getConnectionFactory(PortletRequest portletRequest, JMSDestinationInfo destinationInfo) throws JMSException {
        ResourceAdapterModule module = (ResourceAdapterModule) PortletManager.getManagedBean(portletRequest, destinationInfo.getResourceAdapterModuleAbName());
        for (ResourceAdapter adapter: module.getResourceAdapterInstances()) {
            for (JCAResource resource: adapter.getJCAResourceImplementations()) {
                for (JCAConnectionFactory jcacf: resource.getConnectionFactoryInstances()) {
                    try {
                        return (ConnectionFactory) jcacf.createConnectionFactory();
                    } catch (Exception e) {
                        //try another
                    }
                }
            }
        }
//        JCAManagedConnectionFactory[] jcaManagedConnectionFactories = PortletManager.getOutboundFactoriesForRA(portletRequest, destinationInfo.getResourceAdapterModuleAbName(), destinationInfo
//                .getType().getConnectionFactoryInterfaces());
//        if (jcaManagedConnectionFactories != null && jcaManagedConnectionFactories.length > 0) {
//            try {
//                return (ConnectionFactory) (jcaManagedConnectionFactories[0].getConnectionFactory());
//            } catch (Exception e) {
//            }
//        }
        return null;
    }

    protected Destination getDestination(PortletRequest portletRequest, JMSDestinationInfo destinationInfo) {
        Destination dest = null;
        try {
            ResourceAdapterModule resourceAdapterModule = (ResourceAdapterModule) PortletManager.getManagementHelper(portletRequest).getObject(destinationInfo.getResourceAdapterModuleAbName());
            JCAAdminObject[] jcaAdminObjects = PortletManager.getAdminObjectsForRA(portletRequest, resourceAdapterModule, new String[] { destinationInfo.getType().getDestinationInterface() });
            String targetPhysicalName = destinationInfo.getPhysicalName() == null ? "" : destinationInfo.getPhysicalName();
            for (JCAAdminObject jcaAdminObject : jcaAdminObjects) {
                Kernel kernel = PortletManager.getKernel();
                AbstractName abstractName = kernel.getAbstractNameFor(jcaAdminObject);
                GeronimoManagedBean bean = PortletManager.getManagedBean(portletRequest, abstractName);
                ObjectName name = ObjectName.getInstance(bean.getObjectName());
                String queueName = name.getKeyProperty(NameFactory.J2EE_NAME);
                String currentPhysicalName = null;
                try {
                    currentPhysicalName = (String) jcaAdminObject.getConfigProperty("PhysicalName");
                } catch (Exception e) {
                    logger.warn("PhysicalName undefined, using queueName as PhysicalName");
                    currentPhysicalName = queueName;
                }
                if (targetPhysicalName.equals(currentPhysicalName)) {
                    AbstractName absName = kernel.getAbstractNameFor(bean);
                    dest = (Destination) kernel.invoke(absName, "$getResource");
                    return dest;
                }
            }
        } catch (Exception ex) {
            logger.error("Failed to get destination", ex);
        }
        return dest;
    }

    protected abstract JMSMessageInfo[] getMessagesFromTopic(PortletRequest portletRequest, JMSDestinationInfo destinationInfo, String selector) throws JMSException;

    @SuppressWarnings("unchecked")
    protected JMSMessageInfo[] getMessageFromQueue(PortletRequest portletRequest, JMSDestinationInfo jmsDestinationInfo, String selector) throws JMSException {
        Connection connection = null;
        try {
            ConnectionFactory connectionFactory = getConnectionFactory(portletRequest, jmsDestinationInfo);            
            Queue queue = (Queue) getDestination(portletRequest, jmsDestinationInfo);
            List<JMSMessageInfo> messages = new LinkedList<JMSMessageInfo>();
            connection = connectionFactory.createConnection();
            connection.start();
            Session session = connection.createSession(false, QueueSession.AUTO_ACKNOWLEDGE);
            QueueBrowser qBrowser = session.createBrowser(queue);
            for (Enumeration e = qBrowser.getEnumeration(); e.hasMoreElements();) {
                Message message = (Message) e.nextElement();
                JMSMessageInfo messageInfo = new JMSMessageInfo();
                messageInfo.setPriority(message.getJMSPriority());
                messageInfo.setMessageId(message.getJMSMessageID());
                messageInfo.setDestination(message.getJMSDestination().toString());
                messageInfo.setTimeStamp(message.getJMSTimestamp());
                messageInfo.setExpiration(message.getJMSExpiration());
                messageInfo.setJmsType(message.getJMSType());
                messageInfo.setReplyTo(message.getJMSReplyTo() == null ? "" : message.getJMSReplyTo().toString());
                messageInfo.setCorrelationId(message.getJMSCorrelationID());
                if (message instanceof TextMessage) {
                    messageInfo.setMessage(((TextMessage) message).getText());
                } else {
                    messageInfo.setMessage("Only Text Messages will be displayed..");
                }
                messages.add(messageInfo);
            }
            return messages.toArray(new JMSMessageInfo[0]);
        } catch (JMSException e) {
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public abstract void purge(PortletRequest portletRequest, JMSDestinationInfo destinationInfo) throws JMSException;

    public abstract DestinationStatistics getDestinationStatistics(PortletRequest portletRequest, JMSDestinationInfo destinationInfo) throws JMSException;
}
