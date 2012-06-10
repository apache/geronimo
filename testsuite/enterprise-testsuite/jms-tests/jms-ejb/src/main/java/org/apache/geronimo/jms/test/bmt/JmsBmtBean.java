/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.jms.test.bmt;

import java.text.MessageFormat;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.jms.Connection;
import javax.jms.Session;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Message;
import javax.jms.JMSException;
import javax.jms.Topic;
import javax.jms.MessageConsumer;
import javax.jms.Destination;
import javax.transaction.UserTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.jms.test.mdb.to.SimpleTransferObject;
import org.apache.geronimo.jms.test.mdb.MessageReceiver;

/**
 * @version $Rev$ $Date$
 */

@Stateless(name = "JmsBmt")
@TransactionManagement(value = TransactionManagementType.BEAN)
public class JmsBmtBean implements JmsBmtRemote {

    /**
     * Logger
     */
    private static Logger logger = LoggerFactory.getLogger(JmsBmtBean.class);

    /**
     * Injected connection factory
     */
    @Resource(name = "MSConnectionFactory")
    private ConnectionFactory connFactory = null;

    @Resource(name="UserTransaction")
    private UserTransaction ut;

    /**
     * Injected jms destination
     */
    @Resource(name = "TxQueue")
    private Queue queue = null;

    /**
     * Injected jms destination
     */
    @Resource(name = "TxTopic")
    private Topic topic = null;

    public String sendMessageQueue(String requestName, int priority, int counter) {

        logger.info(MessageFormat.format("Receive task to send {0} message(s) with basename {1} (p={2})", counter, requestName, priority));

        return sendMessageToDestination(requestName, priority, counter, queue);
    }

    public String sendMessageTopic(String requestName, int priority, int counter) {

        logger.info(MessageFormat.format("Receive task to send {0} message(s) with basename {1} (p={2})", counter, requestName, priority));

        return sendMessageToDestination(requestName, priority, counter, topic);
    }

    private String sendMessageToDestination(String requestName, int priority, int counter, Destination destination) {
        Connection conn = null;
        Connection c2 = null;
        Session sess = null;
        Session s2 = null;
        MessageProducer prod = null;
        MessageConsumer consumer = null;
        Message received = null;
        try {
            conn = this.connFactory.createConnection();
            c2 = connFactory.createConnection();
            sess = conn.createSession(true, Session.SESSION_TRANSACTED);
            s2 = c2.createSession(true, Session.SESSION_TRANSACTED);
            prod = sess.createProducer(destination);
            consumer = sess.createConsumer(destination);
            conn.start();

            for (int i = 0; i < counter; ++i) {
                SimpleTransferObject to = new SimpleTransferObject(requestName, i);

                ObjectMessage msg = sess.createObjectMessage();
                msg.setObject(to);

                logger.info("Sending message with name " + to.getName());
                ut.begin();
                try {
                    prod.send(msg, Message.DEFAULT_DELIVERY_MODE, priority, Message.DEFAULT_TIME_TO_LIVE);
                } finally {
                    ut.commit();
                }
                logger.info("Message sent.");
                MessageReceiver.browse("After send", sess, destination, logger);
                ut.begin();
                try {
                    received = consumer.receive(1000);
                    if (received == null) throw new JMSException("Not received first time");
                    if (!to.equals(((ObjectMessage)received).getObject())) throw new JMSException("Wrong object inside: " + ((ObjectMessage)received).getObject());
                } finally {
                    ut.rollback();
                }

                ut.begin();
                try {
                    received = consumer.receive(2000);
                    if (received == null) throw new JMSException("Not received second time");
                    if (!to.equals(((ObjectMessage)received).getObject())) throw new JMSException("Wrong object inside: " + ((ObjectMessage)received).getObject());
                } finally {
                    ut.commit();
                }

            }
            return "OK";

        } catch (Exception e) {
            logger.error("Bad news! Sending failed due to exception: " + e.getMessage(), e);
            return "FAIL";
        } finally {
            try {
                if (prod != null) {
                    prod.close();
                }
                if (consumer != null) consumer.close();
                if (sess != null) {
                    sess.close();
                }
                if (conn != null) {
                    conn.close();
                }
                if (s2 != null) s2.close();
                if (c2 != null) c2.close();;
            } catch (JMSException e) {
                logger.error("Could not finalize jms connections");
            }
        }
    }


}
