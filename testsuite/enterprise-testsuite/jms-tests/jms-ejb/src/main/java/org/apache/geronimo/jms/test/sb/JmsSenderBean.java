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

package org.apache.geronimo.jms.test.sb;

import java.text.MessageFormat;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.MessageConsumer;

import org.apache.geronimo.jms.test.mdb.to.SimpleTransferObject;
import org.apache.geronimo.jms.test.mdb.MessageReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:tomasz[et]mazan[dot]pl">Tomasz Mazan</a>
 */
@Stateless(name = "JmsSender")
public class JmsSenderBean implements JmsSenderRemote {

    /**
     * Logger
     */
    private static Logger logger = LoggerFactory.getLogger(JmsSenderBean.class);

    /**
     * Injected connection factory
     */
    @Resource(name = "MSConnectionFactory")
    private ConnectionFactory connFactory = null;

    /**
     * Injected jms destination
     */
    @Resource(name = "MRRequests")
    private Queue jmsRequestQueue = null;

    /**
     * Injected jms destination
     */
    @Resource(name = "MRResponses")
    private Queue jmsResponseQueue = null;

    /**
     * {@inheritDoc}
     *
     * @see org.apache.geronimo.jms.test.sb.JmsSenderRemote#sendMessage(java.lang.String, int, int)
     */
    @TransactionAttribute(value = TransactionAttributeType.REQUIRED)
    public String sendMessage(String requestName, int priority, int counter) {

        logger.info(MessageFormat.format("Receive task to send {0} message(s) with basename {1} (p={2})", counter, requestName, priority));

        Connection conn = null;
        Session sess = null;
        MessageProducer prod = null;
        try {
            conn = this.connFactory.createConnection();
            sess = conn.createSession(true, Session.SESSION_TRANSACTED);
            prod = sess.createProducer(jmsRequestQueue);
            conn.start();

            for (int i = 0; i < counter; ++i) {
                SimpleTransferObject to = new SimpleTransferObject(requestName, i);

                ObjectMessage msg = sess.createObjectMessage();
                msg.setJMSReplyTo(jmsResponseQueue);
                msg.setObject(to);

                logger.info("Sending message with name " + to.getName());
                prod.send(msg, Message.DEFAULT_DELIVERY_MODE, priority, Message.DEFAULT_TIME_TO_LIVE);
                logger.info("Message sent.");
                MessageReceiver.browse("After send", sess, jmsRequestQueue, logger);
                MessageReceiver.browse("After send", sess, jmsResponseQueue, logger);
            }
            return "OK";

        } catch (JMSException e) {
            logger.error("Bad news! Sending failed due to exception: " + e.getMessage());
            return "FAIL";
        } finally {
            try {
                if (prod != null) {
                    prod.close();
                }
                if (sess != null) {
                    sess.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (JMSException e) {
                logger.error("Could not finalize jms connections");
            }
        }
    }

    @TransactionAttribute(value = TransactionAttributeType.REQUIRED)
    public Integer receiveMessage() {
        Connection conn = null;
        Session sess = null;
        MessageConsumer consumer = null;
        try {
            conn = this.connFactory.createConnection();
            sess = conn.createSession(true, Session.SESSION_TRANSACTED);
            //to test is messages are output from mdb.
            consumer = sess.createConsumer(jmsResponseQueue);
            //to test if messages are consumed by mdb
//            consumer = sess.createConsumer(jmsRequestQueue);
            conn.start();
            MessageReceiver.browse("Before receive", sess, jmsRequestQueue, logger);
            MessageReceiver.browse("Before receive", sess, jmsResponseQueue, logger);
            Message message = consumer.receive(1000);
            if (message != null) {
                ObjectMessage om = (ObjectMessage) message;
                SimpleTransferObject sto = (SimpleTransferObject) om.getObject();
                logger.info("Received message with name " + sto.getName() + sto.getId());
                return sto.getId();
            }
            return null;
        } catch (JMSException e) {
            logger.error("Bad news! Sending failed due to exception: " + e.getMessage());
            return null;
        } finally {
            try {
                if (consumer != null) {
                    consumer.close();
                }
                if (sess != null) {
                    sess.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (JMSException e) {
                logger.error("Could not finalize jms connections");
            }
        }

    }

}
