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
public interface TopicSession extends Session {
    Topic createTopic(String topicName) throws JMSException;

    TopicSubscriber createSubscriber(Topic topic) throws JMSException;

    TopicSubscriber createSubscriber(
        Topic topic,
        String messageSelector,
        boolean noLocal)
        throws JMSException;

    TopicSubscriber createDurableSubscriber(Topic topic, String name)
        throws JMSException;

    TopicSubscriber createDurableSubscriber(
        Topic topic,
        String name,
        String messageSelector,
        boolean noLocal)
        throws JMSException;

    TopicPublisher createPublisher(Topic topic) throws JMSException;

    TemporaryTopic createTemporaryTopic() throws JMSException;

    void unsubscribe(String name) throws JMSException;
}
