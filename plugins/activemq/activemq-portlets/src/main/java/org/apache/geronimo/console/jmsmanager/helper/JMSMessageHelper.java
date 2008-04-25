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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.console.core.jms.TopicBrowserGBean;
import org.apache.geronimo.console.jmsmanager.JMSMessageInfo;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.proxy.GeronimoManagedBean;
import org.apache.geronimo.management.geronimo.JCAAdminObject;
import org.apache.geronimo.management.geronimo.JCAManagedConnectionFactory;
import org.apache.geronimo.management.geronimo.ResourceAdapterModule;

/**
 * @version $Rev$ $Date$
 */
public abstract class JMSMessageHelper {
    protected static final Kernel kernel = KernelRegistry.getSingleKernel();
    private final Logger log = LoggerFactory.getLogger(getClass());

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
                session.close();
                connection.close();
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
                session.close();
                connection.close();
            }
        }
    }

    public List getMessagesList(RenderRequest request, String adapterObjectName, String adminObjName,
            String physicalName, String type) throws Exception {
        List ret = new ArrayList();
        Destination dest = getDestination(request, adapterObjectName, physicalName);
        if (dest == null)
            return ret;

        if ("Queue".equals(type)) {
            Queue queue = (Queue) dest;
            QueueConnectionFactory qConFactory = null;
            QueueConnection qConnection = null;
            QueueSession qSession = null;
            QueueBrowser qBrowser = null;
            try {
                qConFactory = (QueueConnectionFactory) getJCAManagedConnectionFactory(request, adapterObjectName, type)
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
                try {
                    if (qBrowser != null) {
                        qBrowser.close();
                    }
                } catch (JMSException ignore) {
                }
                try {
                    if (qSession != null) {
                        qSession.close();
                    }
                } catch (JMSException ignore) {
                }
                try {
                    if (qConnection != null) {
                        qConnection.close();
                    }
                } catch (JMSException ignore) {
                }
            }

        } else {
            ret = getMessagesFromTopic(type, physicalName);

        }
        return ret;
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
                if ("Queue".equals(type)) {
                    factories = PortletManager.getOutboundFactoriesForRA(renderRequest, module,
                            new String[] { "javax.jms.QueueConnectionFactory" });
                } else if ("Topic".equals(type)) {
                    factories = PortletManager.getOutboundFactoriesForRA(renderRequest, module,
                            new String[] { "javax.jms.TopicConnectionFactory" });
                }
                if (factories == null ) {
                    factories = PortletManager.getOutboundFactoriesForRA(renderRequest, module,
                            new String[] { "javax.jms.ConnectionFactory" });
                }
                if (factories != null ) {
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
                        String physicalNameTemp = (String) admins[j].getConfigProperty("PhysicalName");
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

    protected abstract List getMessagesFromTopic(String type, String physicalQName) throws Exception;

    public abstract void purge(PortletRequest renderRequest, String type, String physicalQName);

}
