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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.management.ObjectName;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;

import org.apache.geronimo.console.jmsmanager.DestinationStatistics;
import org.apache.geronimo.console.jmsmanager.JMSMessageInfo;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.proxy.GeronimoManagedBean;
import org.apache.geronimo.management.geronimo.JCAAdminObject;
import org.apache.geronimo.management.geronimo.JCAManagedConnectionFactory;
import org.apache.geronimo.management.geronimo.ResourceAdapterModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public abstract class JMSMessageHelper {
    protected static final Kernel kernel = KernelRegistry.getSingleKernel();
    private static final Logger log = LoggerFactory.getLogger(JMSMessageHelper.class);

    public static final String QUEUE_TYPE = "Queue";
    
    public static final String TOPIC_TYPE = "Topic";
    
    public void sendMessage(RenderRequest request, JMSMessageInfo messageInfo) throws Exception {
        Destination dest = getDestination(request, messageInfo.getAdapterObjectName(), messageInfo.getPhysicalName());
        if (dest == null) {
            log.error("Unable to find the destination....Not sending message");
            return;
        }
        if ("Queue".equals(messageInfo.getAdminObjType())) {
            Queue destination = (Queue) dest;
            QueueConnectionFactory connectionFactory = (QueueConnectionFactory) getJCAManagedConnectionFactory(request,
                    messageInfo.getAdapterObjectName(), messageInfo.getAdminObjType()).getConnectionFactory();
            if (connectionFactory == null) {
                log.error("Unable to find Queue Connection factory...Not sending message");
                return;
            }
            QueueConnection connection = null;
            QueueSession session = null;
            try {
                connection = connectionFactory.createQueueConnection();
                connection.start();
                session = connection.createQueueSession(true, Session.DUPS_OK_ACKNOWLEDGE);

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
                producer.send(msg,deliveryMode,messageInfo.getPriority(),TextMessage.DEFAULT_TIME_TO_LIVE);
                session.commit();
            } finally {
                if (session != null)
                    try {
                        session.close();
                    } catch (Exception e) {
                    }
                if (connection != null)
                    try {
                        connection.close();
                    } catch (Exception e) {
                    }                
            }
        } else {
            Topic destination = (Topic) dest;
            TopicConnectionFactory connectionFactory = (TopicConnectionFactory) getJCAManagedConnectionFactory(request,
                    messageInfo.getAdapterObjectName(), messageInfo.getAdminObjType()).getConnectionFactory();
            if (connectionFactory == null) {
                log.error("Unable to find Topic Connection factory...Not sending message");
                return;
            }
            TopicConnection connection = null;
            TopicSession session = null;
            try {
                connection = connectionFactory.createTopicConnection();
                connection.start();
                session = connection.createTopicSession(true, Session.DUPS_OK_ACKNOWLEDGE);

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
                producer.send(msg,deliveryMode,messageInfo.getPriority(),TextMessage.DEFAULT_TIME_TO_LIVE);
                session.commit();
            } finally {
                if (session != null)
                    try {
                        session.close();
                    } catch (Exception e) {
                    }
                if (connection != null)
                    try {
                        connection.close();
                    } catch (Exception e) {
                    }
            }
        }
    }

    public List<JMSMessageInfo> getMessagesList(RenderRequest request, String adapterObjectName, String adminObjName,
            String physicalName, String type) throws Exception {
        Destination destination = getDestination(request, adapterObjectName, physicalName);
        if (destination == null)
            return Collections.emptyList();
        if ("Queue".equals(type)) {
            return getMessageFromQueue(request, destination, adapterObjectName, adminObjName, physicalName);
        } else {
            return getMessagesFromTopic(request, destination, adapterObjectName, adminObjName, physicalName);
        }
    }

    private JCAManagedConnectionFactory getJCAManagedConnectionFactory(RenderRequest renderRequest, String objectName,
            String type) {
        ResourceAdapterModule[] modules = PortletManager.getOutboundRAModules(renderRequest,
                new String[] { "javax.jms.ConnectionFactory", "javax.jms.QueueConnectionFactory",
                        "javax.jms.TopicConnectionFactory", });
        for (int i = 0; i < modules.length; i++) {
            ResourceAdapterModule module = modules[i];
            String objectNameTemp = module.getObjectName();
            if (objectName != null && objectName.equals(objectNameTemp)) {
                JCAManagedConnectionFactory[] factories = null;
                if (QUEUE_TYPE.equals(type)) {
                    factories = PortletManager.getOutboundFactoriesForRA(renderRequest, module,
                            new String[] { "javax.jms.QueueConnectionFactory" });
                } else if (TOPIC_TYPE.equals(type)) {
                    factories = PortletManager.getOutboundFactoriesForRA(renderRequest, module,
                            new String[] { "javax.jms.TopicConnectionFactory" });
                }
                if (factories == null || factories.length == 0) {
                    factories = PortletManager.getOutboundFactoriesForRA(renderRequest, module,
                            new String[] { "javax.jms.ConnectionFactory" });
                }
                if (factories != null && factories.length != 0) {
                    return factories[0];
                }

            }
        }
        return null;

    }

    private Destination getDestination(RenderRequest renderRequest, String objectName, String physicalName) {
        Destination dest = null;
        try {
            ResourceAdapterModule[] modules = PortletManager.getOutboundRAModules(renderRequest, new String[] {
                    "javax.jms.ConnectionFactory", "javax.jms.QueueConnectionFactory",
                    "javax.jms.TopicConnectionFactory", });
            for (int i = 0; i < modules.length; i++) {
                ResourceAdapterModule module = modules[i];
                String objectNameTemp = module.getObjectName();
                if (objectName != null && objectName.equals(objectNameTemp)) {
                    JCAAdminObject[] admins = PortletManager.getAdminObjectsForRA(renderRequest, module, new String[] {
                            "javax.jms.Queue", "javax.jms.Topic" });
                    for (int j = 0; j < admins.length; j++) {
                        GeronimoManagedBean bean = (GeronimoManagedBean) admins[j];
                        ObjectName name = ObjectName.getInstance(bean.getObjectName());
                        String queueName = name.getKeyProperty(NameFactory.J2EE_NAME);
                        String physicalNameTemp = null;
                        try {
                            physicalNameTemp = (String) admins[j].getConfigProperty("PhysicalName");
                        } catch (Exception e) {
                            log.warn("PhysicalName undefined, using queueName as PhysicalName");
                            physicalNameTemp = queueName;
                        }
                        if (physicalName != null && physicalName.equals(physicalNameTemp)) {
                            AbstractName absName = kernel.getAbstractNameFor(bean);
                            dest = (Destination) kernel.invoke(absName, "$getResource");
                            return dest;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            // ignore exception
            log.error("Failed to get destination", ex);
        }
        return dest;
    }

    protected abstract List<JMSMessageInfo> getMessagesFromTopic(RenderRequest request, Destination destination,
            String adapterObjectName, String adminObjName, String physicalName) throws Exception;

    protected List<JMSMessageInfo> getMessageFromQueue(RenderRequest request, Destination destination,
            String adapterObjectName, String adminObjName, String physicalName) throws Exception {
        List<JMSMessageInfo> ret = new ArrayList<JMSMessageInfo>();
        Queue queue = (Queue) destination;
        QueueConnectionFactory qConFactory = null;
        QueueConnection qConnection = null;
        QueueSession qSession = null;
        QueueBrowser qBrowser = null;
        try {
            qConFactory = (QueueConnectionFactory) getJCAManagedConnectionFactory(request, adapterObjectName, QUEUE_TYPE)
                    .getConnectionFactory();
            if (qConFactory == null)
                return ret;
            qConnection = qConFactory.createQueueConnection();
            qSession = qConnection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
            qBrowser = qSession.createBrowser(queue);
            qConnection.start();
            for (Enumeration e = qBrowser.getEnumeration(); e.hasMoreElements();) {
                Message message = (Message)e.nextElement();
                JMSMessageInfo messageInfo = new JMSMessageInfo();
                messageInfo.setPriority(message.getJMSPriority());
                messageInfo.setMessageId(message.getJMSMessageID());
                messageInfo.setDestination(message.getJMSDestination().toString());
                messageInfo.setTimeStamp(message.getJMSTimestamp());
                messageInfo.setExpiration(message.getJMSExpiration());
                messageInfo.setJmsType(message.getJMSType());
                messageInfo.setReplyTo(message.getJMSReplyTo()==null?"":message.getJMSReplyTo().toString());
                messageInfo.setCorrelationId(message.getJMSCorrelationID());
                if (message instanceof TextMessage) {
                    messageInfo.setMessage(((TextMessage) message).getText());
                } else {
                    messageInfo.setMessage("Only Text Messages will be displayed..");
                }
                ret.add(messageInfo);
            }
            qConnection.stop();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (qBrowser != null) {
                try {
                    qBrowser.close();
                } catch (JMSException ignore) {
                }
            }
            if (qSession != null) {
                try {
                    qSession.close();
                } catch (JMSException ignore) {
                }
            }
            if (qConnection != null) {
                try {
                    qConnection.close();
                } catch (JMSException ignore) {
                }
            }
        }
        return ret;
    }
    
    public abstract void purge(PortletRequest request, String adapterObjectName, String adminObjName,
            String physicalName) throws Exception;
    
    public abstract DestinationStatistics getDestinationStatistics(String brokerName, String destType,String physicalName);

}
