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

/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:58:45 $
 */
public interface MessageProducer {
    void setDisableMessageID(boolean value) throws JMSException;

    boolean getDisableMessageID() throws JMSException;

    void setDisableMessageTimestamp(boolean value) throws JMSException;

    boolean getDisableMessageTimestamp() throws JMSException;

    void setDeliveryMode(int deliveryMode) throws JMSException;

    int getDeliveryMode() throws JMSException;

    void setPriority(int defaultPriority) throws JMSException;

    int getPriority() throws JMSException;

    void setTimeToLive(long timeToLive) throws JMSException;

    long getTimeToLive() throws JMSException;

    Destination getDestination() throws JMSException;

    void close() throws JMSException;

    void send(Message message) throws JMSException;

    void send(Message message, int deliveryMode, int priority, long timeToLive)
        throws JMSException;

    void send(Destination destination, Message message) throws JMSException;

    void send(
        Destination destination,
        Message message,
        int deliveryMode,
        int priority,
        long timeToLive)
        throws JMSException;
}
