/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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
 * @version $Rev$ $Date$
 */
public interface Connection {
    Session createSession(boolean transacted, int acknowledgeMode)
        throws JMSException;

    String getClientID() throws JMSException;

    void setClientID(String clientID) throws JMSException;

    ConnectionMetaData getMetaData() throws JMSException;

    ExceptionListener getExceptionListener() throws JMSException;

    void setExceptionListener(ExceptionListener listener) throws JMSException;

    void start() throws JMSException;

    void stop() throws JMSException;

    void close() throws JMSException;

    ConnectionConsumer createConnectionConsumer(
        Destination destination,
        String messageSelector,
        ServerSessionPool sessionPool,
        int maxMessages)
        throws JMSException;

    ConnectionConsumer createDurableConnectionConsumer(
        Topic topic,
        String subscriptionName,
        String messageSelector,
        ServerSessionPool sessionPool,
        int maxMessages)
        throws JMSException;
}
