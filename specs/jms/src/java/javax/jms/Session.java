/**
 *
 * Copyright 2004 The Apache Software Foundation
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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.jms;

import java.io.Serializable;

/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:58:45 $
 */
public interface Session extends Runnable {
    static final int AUTO_ACKNOWLEDGE = 1;

    static final int CLIENT_ACKNOWLEDGE = 2;

    static final int DUPS_OK_ACKNOWLEDGE = 3;

    static final int SESSION_TRANSACTED = 0;

    BytesMessage createBytesMessage() throws JMSException;

    MapMessage createMapMessage() throws JMSException;

    Message createMessage() throws JMSException;

    ObjectMessage createObjectMessage() throws JMSException;

    ObjectMessage createObjectMessage(Serializable object) throws JMSException;

    StreamMessage createStreamMessage() throws JMSException;

    TextMessage createTextMessage() throws JMSException;

    TextMessage createTextMessage(String text) throws JMSException;

    boolean getTransacted() throws JMSException;

    int getAcknowledgeMode() throws JMSException;

    void commit() throws JMSException;

    void rollback() throws JMSException;

    void close() throws JMSException;

    void recover() throws JMSException;

    MessageListener getMessageListener() throws JMSException;

    void setMessageListener(MessageListener listener) throws JMSException;

    public void run();

    MessageProducer createProducer(Destination destination)
        throws JMSException;

    MessageConsumer createConsumer(Destination destination)
        throws JMSException;

    MessageConsumer createConsumer(
        Destination destination,
        java.lang.String messageSelector)
        throws JMSException;

    MessageConsumer createConsumer(
        Destination destination,
        java.lang.String messageSelector,
        boolean NoLocal)
        throws JMSException;

    Queue createQueue(String queueName) throws JMSException;

    Topic createTopic(String topicName) throws JMSException;

    TopicSubscriber createDurableSubscriber(Topic topic, String name)
        throws JMSException;

    TopicSubscriber createDurableSubscriber(
        Topic topic,
        String name,
        String messageSelector,
        boolean noLocal)
        throws JMSException;

    QueueBrowser createBrowser(Queue queue) throws JMSException;

    QueueBrowser createBrowser(Queue queue, String messageSelector)
        throws JMSException;

    TemporaryQueue createTemporaryQueue() throws JMSException;

    TemporaryTopic createTemporaryTopic() throws JMSException;

    void unsubscribe(String name) throws JMSException;
}
