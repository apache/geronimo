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

package org.apache.geronimo.console.jmsmanager.renderers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Topic;
import javax.management.ObjectName;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.geronimo.console.jmsmanager.AbstractJMSManager;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViewMessagesRenderer extends AbstractJMSManager implements
        PortletRenderer {

    private static final Logger log = LoggerFactory.getLogger(ViewMessagesRenderer.class);

    private static final TopicListener topicListener = new TopicListener();

    public String render(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        List messageList = getMessageList(request, response);
        request.setAttribute("messages", messageList);
        return "/WEB-INF/view/jmsmanager/viewmessages.jsp";
    }

    public List getMessageList(RenderRequest request, RenderResponse response)
            throws PortletException {
        String destinationApplicationName = request
                .getParameter("destinationApplicationName");
        String destinationModuleName = request
                .getParameter("destinationModuleName");
        String destinationName = request.getParameter("destinationName");

        List ret = new ArrayList();
        try {
            //TODO configid disabled
            AbstractName adminObjectName = null;//NameFactory.getComponentName(null,
//                    null, destinationApplicationName, NameFactory.JCA_RESOURCE,
//                    destinationModuleName, destinationName, null, baseContext);
            Destination dest = (Destination) kernel.invoke(adminObjectName,
                    "$getResource");
            if (dest instanceof Queue) {
                Queue queue = (Queue) dest;
                QueueConnectionFactory qConFactory = null;
                QueueConnection qConnection = null;
                QueueSession qSession = null;
                QueueBrowser qBrowser = null;
                try {
                    qConFactory = (QueueConnectionFactory) kernel.invoke(
                            JCA_MANAGED_CONNECTION_FACTORY_NAME,
                            "$getResource");
                    qConnection = qConFactory.createQueueConnection();
                    qSession = qConnection.createQueueSession(false,
                            QueueSession.AUTO_ACKNOWLEDGE);
                    qBrowser = qSession.createBrowser(queue);
                    qConnection.start();
                    for (Enumeration e = qBrowser.getEnumeration(); e
                            .hasMoreElements();) {
                        Object o = e.nextElement();
                        ret.add(o);
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
            } else if (dest instanceof Topic) {
                //TODO configid disabled
                AbstractName tBrowserObjName = null;//NameFactory.getComponentName(null,
//                        null, destinationApplicationName,
//                        NameFactory.JCA_RESOURCE, destinationModuleName,
//                        destinationName, "TopicBrowser", baseContext);
                ret = (List) kernel.invoke(tBrowserObjName, "getMessages");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return ret;
    }

    static class TopicListener implements MessageListener {
        /**
         * Hashtable to hold the messages for each topic. Messages are stored as
         * Message/Topic key/value pairs.
         */
        private Map messagesMap = new Hashtable();

        public final String name = this.toString();

        public synchronized void onMessage(Message message) {
            try {
                Destination dest = message.getJMSDestination();
                List messages = null;
                if (!messagesMap.containsKey(dest)) {
                    register((Topic) dest);
                }
                messages = (List) messagesMap.get(dest);

                if (!messages.contains(message)) {
                    messages.add(message);
                }
                messagesMap.put(dest, messages);
            } catch (JMSException e) {
                log.error(e.getMessage(), e);
            }
        }

        public void register(Topic topic) {
            if (!messagesMap.containsKey(topic)) {
                List messages = new ArrayList();
                messagesMap.put(topic, messages);
            }
        }

        public List getMessages(Topic topic) {
            List ret;
            if (messagesMap.containsKey(topic)) {
                ret = (List) messagesMap.get(topic);
            } else {
                ret = Collections.EMPTY_LIST;
            }
            return Collections.unmodifiableList(ret);
        }

        public boolean isListeningTo(Topic topic) {
            return messagesMap.containsKey(topic);
        }
    }

}
