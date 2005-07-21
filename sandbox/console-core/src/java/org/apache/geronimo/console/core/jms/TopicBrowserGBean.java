/**
 *
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.
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

package org.apache.geronimo.console.core.jms;

import java.util.ArrayList;
import java.util.List;

import javax.jms.Message;
import javax.jms.QueueSession;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.connector.AdminObjectWrapper;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.management.State;

public class TopicBrowserGBean implements GBeanLifecycle, Runnable {

    private static Log log = LogFactory.getLog(TopicBrowserGBean.class);

    private static Kernel kernel = KernelRegistry.getSingleKernel();

    static {
        try {
            ACTIVEMQ_CONTAINER_OBJNAME = ObjectName
                    .getInstance("geronimo.server:J2EEApplication=null,J2EEModule=org/apache/geronimo/ActiveMQServer,J2EEServer=geronimo,j2eeType=JMSServer,name=ActiveMQl");
            ACTIVEMQ_CONNECTOR_OBJNAME = ObjectName
                    .getInstance("geronimo.server:J2EEApplication=null,J2EEServer=geronimo,JCAResource=org/apache/geronimo/SystemJMS,j2eeType=JCAManagedConnectionFactory,name=DefaultActiveMQConnectionFactory");
        } catch (MalformedObjectNameException moe) {
            log.warn("Could not initialize ObjectName", moe);
        }
    }

    private static ObjectName ACTIVEMQ_CONTAINER_OBJNAME;

    private static ObjectName ACTIVEMQ_CONNECTOR_OBJNAME;

    String subscriberName;

    TopicConnectionFactory tConFactory;

    TopicConnection tConnection;

    AdminObjectWrapper connectionFactoryWrapper, topicWrapper;

    TopicSession tSession;

    TopicSubscriber tSubscriber;

    Topic topic;

    Thread t;

    boolean stop;

    public void run() {
        try {
            tConFactory = (TopicConnectionFactory) connectionFactoryWrapper
                    .$getResource();
            topic = (Topic) topicWrapper.$getResource();
            tConnection = tConFactory.createTopicConnection();
            tConnection.setClientID(subscriberName);
            tSession = tConnection.createTopicSession(false,
                    QueueSession.AUTO_ACKNOWLEDGE);
            tSubscriber = tSession.createDurableSubscriber(topic,
                    subscriberName);
            tConnection.start();
            while (!stop) {
                Thread.yield();
            }
            if (tSession != null) {
                tSession.close();
            }
            if (tConnection != null) {
                // If the activeMQ connector or container is not running there
                // is no need to close the connection.
                // Closing the connection would fail anyway.
                if (((Integer) kernel.getAttribute(ACTIVEMQ_CONTAINER_OBJNAME,
                        "state")).intValue() == State.RUNNING_INDEX
                        && ((Integer) kernel.getAttribute(
                                ACTIVEMQ_CONNECTOR_OBJNAME, "state"))
                                .intValue() == State.RUNNING_INDEX) {
                    tConnection.close();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        t = null;
        log.debug("Worker thread stopped.");
    }

    public TopicBrowserGBean(String subscriberName,
            AdminObjectWrapper connectionFactoryWrapper,
            AdminObjectWrapper topicWrapper) {
        this.subscriberName = subscriberName + "@" + this.getClass().getName();
        this.connectionFactoryWrapper = connectionFactoryWrapper;
        this.topicWrapper = topicWrapper;
    }

    /**
     * Start the connection on a topic and add a durable subscription.
     *
     * @see org.apache.geronimo.gbean.GBeanLifecycle#doStart()
     */
    public void doStart() throws WaitingException, Exception {
        t = new Thread(this);
        t.start();
        log.info("Subscribed to topic.");
    }

    /**
     * Close the connection and unregister durable subscription.
     *
     * @see org.apache.geronimo.gbean.GBeanLifecycle#doStop()
     */
    public void doStop() throws WaitingException, Exception {
        stop = true;
        log.info("Unsubscribed to topic.");
    }

    public void doFail() {
        stop = true;
        log.warn("GBean failed.");
    }

    /**
     * Get all the messages since the last call to getMessages(). If this is the
     * first call returns all the messages sent to the Topic
     *
     * @return all the messages since the last call to getMessages() or all the
     *         messages sent to the topic if this is there was no previous call.
     * @throws Exception
     */
    public List getMessages() throws Exception {
        List ret = new ArrayList();
        Message m = null;
        do {
            m = tSubscriber.receiveNoWait();
            if (m != null) {
                ret.add(m);
            }
        } while (m != null);
        return ret;
    }

    /**
     * Remove a durable subscription.
     */
    public void unsubscribe() throws Exception {
        if (tSubscriber != null) {
            tSubscriber.close();
            if (tSession != null) {
                tSession.unsubscribe(subscriberName);
                log.info(subscriberName + " unsubscribed from Topic "
                        + topic.getTopicName() + ".");
            }
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(
                "Topic Browser GBean", TopicBrowserGBean.class);
        infoFactory.addAttribute("subscriberName", String.class, true);

        infoFactory.addReference("ConnectionFactoryWrapper",
                AdminObjectWrapper.class);
        infoFactory.addReference("TopicWrapper", AdminObjectWrapper.class);

        infoFactory.addOperation("getMessages");
        infoFactory.addOperation("unsubscribe");

        infoFactory.setConstructor(new String[] { "subscriberName",
                "ConnectionFactoryWrapper", "TopicWrapper" });

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
