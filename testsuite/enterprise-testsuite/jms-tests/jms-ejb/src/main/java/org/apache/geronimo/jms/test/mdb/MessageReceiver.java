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

package org.apache.geronimo.jms.test.mdb;

import java.text.MessageFormat;
import java.util.Enumeration;

import javax.ejb.MessageDrivenContext;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.QueueBrowser;
import javax.jms.Queue;

import org.apache.geronimo.jms.test.mdb.exception.ProcessingException;
import org.apache.geronimo.jms.test.mdb.to.TransferObject;
import org.apache.geronimo.jms.test.mdb.to.SimpleTransferObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:tomasz[et]mazan[dot]pl">Tomasz Mazan</a>
 */
public abstract class MessageReceiver implements MessageListener {

    /**
     * Class'es logger
     */
    private static Logger logger = LoggerFactory.getLogger(org.apache.geronimo.jms.test.mdb.SimpleMessageReceiver.class);

    /**
     * Returns instance of injected connection factory
     *
     * @return ConnectionFactory instance
     */
    protected abstract ConnectionFactory getConnectionFactory();

    /**
     * Returns instance of injected context
     *
     * @return MessagDrivenContext instance
     */
    protected abstract MessageDrivenContext getMessageDrivenContext();

    /**
     * Processes received object
     *
     * @param obj object to process
     * @return processed instance of object
     * @throws ProcessingException if message's processing fails
     */
    protected abstract TransferObject processMessage(TransferObject obj) throws ProcessingException;

    /**
     * Method to process delivered messages
     *
     * @param message instance of message
     */
    public void onMessage(Message message) {
        logger.info("MDB received message to process");

        ObjectMessage msg;
        if (ObjectMessage.class.isInstance(message)) {
            msg = (ObjectMessage) message;
        } else {
            logger.error(MessageFormat.format("Invalid class of message {0}. Only object messages are supported", message.getClass()));
            return;
        }

        Object obj = null;
        Destination replyToDest = null;
        try {
            obj = msg.getObject();
            replyToDest = msg.getJMSReplyTo();
        } catch (JMSException e) {
            logger.error("Could not process message due to exception " + e.getMessage());
            return;
        }

        try {
            if (obj == null) {
                logger.error("Object received in message is null");
            } else if (!TransferObject.class.isInstance(obj)) {
                logger.error(MessageFormat.format("Invalid class of object {0} included in received message", obj.getClass()));
            } else {
                TransferObject to = (TransferObject) obj;
                logger.info("Ready to process and return " + ((SimpleTransferObject)to).getName() + " id: " + ((SimpleTransferObject)to).getId());
                to = processMessage(to);
                try {
                    sendResponse(to, replyToDest, message.getJMSPriority());
                } catch (JMSException e) {
                    logger.error(MessageFormat.format("Response could not be sent and will be processed and delivered later. Cause: {0}",
                            e.getMessage()));
                    getMessageDrivenContext().setRollbackOnly();
                }
            }
        } catch (ProcessingException e) {
            logger.error("Could not process message due to exception: " + e.getMessage());
        }

    }

    /**
     * Method send response
     *
     * @param to       transfer processed object
     * @param replyTo  replyTo destination
     * @param priority message priority
     * @throws JMSException if any occurs while preparing or sending message
     */
    private void sendResponse(TransferObject to, Destination replyTo, int priority) throws JMSException {
        logger.debug("Preparing response to send");

        Connection conn = null;
        Session sess = null;
        MessageProducer producer = null;
        try {
            conn = this.createConnection();
            sess = this.createSession(conn);
            producer = this.createMessageProducer(sess, replyTo);
            conn.start();

            ObjectMessage msg = this.createMessage(sess);
            msg.setObject(to);

//            browse("Before reply", sess, replyTo, logger);
            producer.send(msg, Message.DEFAULT_DELIVERY_MODE, priority, Message.DEFAULT_TIME_TO_LIVE);

            logger.info("Success response - sent.");
        } finally {
            try {
                if (producer != null) {
                    producer.close();
                }
                if (sess != null) {
                    sess.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Creates new jms session
     *
     * @param conn connection
     * @return Session instance
     * @throws JMSException if can't create session
     */
    private Session createSession(Connection conn) throws JMSException {
        logger.debug("Creating new jms session");

        Session sess = conn.createSession(true, Session.SESSION_TRANSACTED);

        return sess;
    }

    /**
     * Creates new jms connection
     *
     * @return Connection instance
     * @throws JMSException if can't create session
     */
    private Connection createConnection() throws JMSException {
        logger.debug("Creating new jms connection");

        Connection conn = null;

        conn = this.getConnectionFactory().createConnection();

        return conn;
    }

    /**
     * Creates message producer for specified destination
     *
     * @param session     the session
     * @param destination the destination
     * @return new instance of MessageProducer
     * @throws JMSException if can't create producer
     */
    private MessageProducer createMessageProducer(Session session, Destination destination) throws JMSException {
        logger.debug("Creating new message producer");

        return session.createProducer(destination);
    }

    /**
     * Creates jms message
     *
     * @param session the session
     * @return new instance of Message
     * @throws JMSException if error occurs while creating message
     */
    private ObjectMessage createMessage(Session session) throws JMSException {
        logger.debug("Creating new jms message");

        ObjectMessage msg = session.createObjectMessage();
        return msg;
    }

    public static void browse(String where, Session session, Destination destination, Logger logger) throws JMSException {
/*
        StringBuilder b = new StringBuilder(where);
        b.append("\nqueue: " + destination).append("\n");
        QueueBrowser browser = session.createBrowser((Queue) destination);
        for (Enumeration e = browser.getEnumeration(); e.hasMoreElements();) {
            Message m = (Message) e.nextElement();
            if (m instanceof ObjectMessage) {
                ObjectMessage om = (ObjectMessage) m;
                if (om.getObject() instanceof SimpleTransferObject) {
                    SimpleTransferObject to = (SimpleTransferObject) om.getObject();
                    b.append("  name: " + to.getName() + " id: " + to.getId());
                } else {
                    b.append("  not a SimpleTransferObject: " + om.getObject());
                }
            } else {
                b.append(" not an ObjectMessage: " + m);
            }
            b.append("\n");
        }
        browser.close();
        logger.info(b.toString());
*/
    }

}