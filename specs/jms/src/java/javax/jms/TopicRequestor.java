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
 * @version $Revision: 1.4 $ $Date: 2004/03/10 10:00:03 $
 */
public class TopicRequestor {
    private TopicSession session;
    private Topic topic;
    private TemporaryTopic temporaryTopic;
    private TopicPublisher publisher;
    private TopicSubscriber subscriber;

    public TopicRequestor(TopicSession session, Topic topic)
        throws JMSException {
        super();
        setSession(session);
        setTopic(topic);
        setTemporaryTopic(session.createTemporaryTopic());
        setPublisher(session.createPublisher(topic));
        setSubscriber(session.createSubscriber(getTemporaryTopic()));
    }

    public Message request(Message message) throws JMSException {
        message.setJMSReplyTo(getTemporaryTopic());
        getPublisher().publish(message);
        return (getSubscriber().receive());
    }

    public void close() throws JMSException {
        getSession().close();
        getTemporaryTopic().delete();
    }

    private void setPublisher(TopicPublisher publisher) {
        this.publisher = publisher;
    }

    private TopicPublisher getPublisher() {
        return publisher;
    }

    private void setSession(TopicSession session) {
        this.session = session;
    }

    private TopicSession getSession() {
        return session;
    }

    private void setSubscriber(TopicSubscriber subscriber) {
        this.subscriber = subscriber;
    }

    private TopicSubscriber getSubscriber() {
        return subscriber;
    }

    private void setTemporaryTopic(TemporaryTopic temporaryTopic) {
        this.temporaryTopic = temporaryTopic;
    }

    private TemporaryTopic getTemporaryTopic() {
        return temporaryTopic;
    }

    private void setTopic(Topic topic) {
        this.topic = topic;
    }

    private Topic getTopic() {
        return topic;
    }
}
