/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.jms;

/** A <CODE>TopicSession</CODE> object provides methods for creating
 * <CODE>TopicPublisher</CODE>, <CODE>TopicSubscriber</CODE>, and
 * <CODE>TemporaryTopic</CODE> objects. It also provides a method for
 * deleting its client's durable subscribers.
 *
 *<P>A <CODE>TopicSession</CODE> is used for creating Pub/Sub specific
 * objects. In general, use the  <CODE>Session</CODE> object, and
 *  use <CODE>TopicSession</CODE>  only to support
 * existing code. Using the <CODE>Session</CODE> object simplifies the
 * programming model, and allows transactions to be used across the two
 * messaging domains.
 *
 * <P>A <CODE>TopicSession</CODE> cannot be used to create objects specific to the
 * point-to-point domain. The following methods inherit from
 * <CODE>Session</CODE>, but must throw an
 * <CODE>IllegalStateException</CODE>
 * if used from <CODE>TopicSession</CODE>:
 *<UL>
 *   <LI><CODE>createBrowser</CODE>
 *   <LI><CODE>createQueue</CODE>
 *   <LI><CODE>createTemporaryQueue</CODE>
 *</UL>
 *
 * @version     $Revision: 1.1 $ $Date: 2003/08/16 02:29:58 $
 * @author      Mark Hapner
 * @author      Rich Burridge
 * @author       Kate Stout
 *
 * @see         javax.jms.Session
 * @see	 javax.jms.Connection#createSession(boolean, int)
 * @see	 javax.jms.TopicConnection#createTopicSession(boolean, int)
 * @see         javax.jms.XATopicSession#getTopicSession()
 */

public interface TopicSession extends Session {

    /** Creates a topic identity given a <CODE>Topic</CODE> name.
     *
     * <P>This facility is provided for the rare cases where clients need to
     * dynamically manipulate topic identity. This allows the creation of a
     * topic identity with a provider-specific name. Clients that depend
     * on this ability are not portable.
     *
     * <P>Note that this method is not for creating the physical topic.
     * The physical creation of topics is an administrative task and is not
     * to be initiated by the JMS API. The one exception is the
     * creation of temporary topics, which is accomplished with the
     * <CODE>createTemporaryTopic</CODE> method.
     *
     * @param topicName the name of this <CODE>Topic</CODE>
     *
     * @return a <CODE>Topic</CODE> with the given name
     *
     * @exception JMSException if the session fails to create a topic
     *                         due to some internal error.
     */

    Topic createTopic(String topicName) throws JMSException;


    /** Creates a nondurable subscriber to the specified topic.
     *
     * <P>A client uses a <CODE>TopicSubscriber</CODE> object to receive
     * messages that have been published to a topic.
     *
     * <P>Regular <CODE>TopicSubscriber</CODE> objects are not durable.
     * They receive only messages that are published while they are active.
     *
     * <P>In some cases, a connection may both publish and subscribe to a
     * topic. The subscriber <CODE>NoLocal</CODE> attribute allows a subscriber
     * to inhibit the delivery of messages published by its own connection.
     * The default value for this attribute is false.
     *
     * @param topic the <CODE>Topic</CODE> to subscribe to
     *
     * @exception JMSException if the session fails to create a subscriber
     *                         due to some internal error.
     * @exception InvalidDestinationException if an invalid topic is specified.
     */

    TopicSubscriber createSubscriber(Topic topic) throws JMSException;


    /** Creates a nondurable subscriber to the specified topic, using a
     * message selector or specifying whether messages published by its
     * own connection should be delivered to it.
     *
     * <P>A client uses a <CODE>TopicSubscriber</CODE> object to receive
     * messages that have been published to a topic.
     *
     * <P>Regular <CODE>TopicSubscriber</CODE> objects are not durable.
     * They receive only messages that are published while they are active.
     *
     * <P>Messages filtered out by a subscriber's message selector will
     * never be delivered to the subscriber. From the subscriber's
     * perspective, they do not exist.
     *
     * <P>In some cases, a connection may both publish and subscribe to a
     * topic. The subscriber <CODE>NoLocal</CODE> attribute allows a subscriber
     * to inhibit the delivery of messages published by its own connection.
     * The default value for this attribute is false.
     *
     * @param topic the <CODE>Topic</CODE> to subscribe to
     * @param messageSelector only messages with properties matching the
     * message selector expression are delivered. A value of null or
     * an empty string indicates that there is no message selector
     * for the message consumer.
     * @param noLocal if set, inhibits the delivery of messages published
     * by its own connection
     *
     * @exception JMSException if the session fails to create a subscriber
     *                         due to some internal error.
     * @exception InvalidDestinationException if an invalid topic is specified.
     * @exception InvalidSelectorException if the message selector is invalid.
     */

    TopicSubscriber createSubscriber(Topic topic, String messageSelector, boolean noLocal) throws JMSException;


    /** Creates a durable subscriber to the specified topic.
     *
     * <P>If a client needs to receive all the messages published on a
     * topic, including the ones published while the subscriber is inactive,
     * it uses a durable <CODE>TopicSubscriber</CODE>. The JMS provider
     * retains a record of this
     * durable subscription and insures that all messages from the topic's
     * publishers are retained until they are acknowledged by this
     * durable subscriber or they have expired.
     *
     * <P>Sessions with durable subscribers must always provide the same
     * client identifier. In addition, each client must specify a name that
     * uniquely identifies (within client identifier) each durable
     * subscription it creates. Only one session at a time can have a
     * <CODE>TopicSubscriber</CODE> for a particular durable subscription.
     *
     * <P>A client can change an existing durable subscription by creating
     * a durable <CODE>TopicSubscriber</CODE> with the same name and a new
     * topic and/or
     * message selector. Changing a durable subscriber is equivalent to
     * unsubscribing (deleting) the old one and creating a new one.
     *
     * <P>In some cases, a connection may both publish and subscribe to a
     * topic. The subscriber <CODE>NoLocal</CODE> attribute allows a subscriber
     * to inhibit the delivery of messages published by its own connection.
     * The default value for this attribute is false.
     *
     * @param topic the non-temporary <CODE>Topic</CODE> to subscribe to
     * @param name the name used to identify this subscription
     *
     * @exception JMSException if the session fails to create a subscriber
     *                         due to some internal error.
     * @exception InvalidDestinationException if an invalid topic is specified.
     */

    TopicSubscriber createDurableSubscriber(Topic topic, String name) throws JMSException;


    /** Creates a durable subscriber to the specified topic, using a
     * message selector or specifying whether messages published by its
     * own connection should be delivered to it.
     *
     * <P>If a client needs to receive all the messages published on a
     * topic, including the ones published while the subscriber is inactive,
     * it uses a durable <CODE>TopicSubscriber</CODE>. The JMS provider
     * retains a record of this
     * durable subscription and insures that all messages from the topic's
     * publishers are retained until they are acknowledged by this
     * durable subscriber or they have expired.
     *
     * <P>Sessions with durable subscribers must always provide the same
     * client identifier. In addition, each client must specify a name which
     * uniquely identifies (within client identifier) each durable
     * subscription it creates. Only one session at a time can have a
     * <CODE>TopicSubscriber</CODE> for a particular durable subscription.
     * An inactive durable subscriber is one that exists but
     * does not currently have a message consumer associated with it.
     *
     * <P>A client can change an existing durable subscription by creating
     * a durable <CODE>TopicSubscriber</CODE> with the same name and a new
     * topic and/or
     * message selector. Changing a durable subscriber is equivalent to
     * unsubscribing (deleting) the old one and creating a new one.
     *
     * @param topic the non-temporary <CODE>Topic</CODE> to subscribe to
     * @param name the name used to identify this subscription
     * @param messageSelector only messages with properties matching the
     * message selector expression are delivered.  A value of null or
     * an empty string indicates that there is no message selector
     * for the message consumer.
     * @param noLocal if set, inhibits the delivery of messages published
     * by its own connection
     *
     * @exception JMSException if the session fails to create a subscriber
     *                         due to some internal error.
     * @exception InvalidDestinationException if an invalid topic is specified.
     * @exception InvalidSelectorException if the message selector is invalid.
     */

    TopicSubscriber createDurableSubscriber(Topic topic, String name, String messageSelector, boolean noLocal) throws JMSException;


    /** Creates a publisher for the specified topic.
     *
     * <P>A client uses a <CODE>TopicPublisher</CODE> object to publish
     * messages on a topic.
     * Each time a client creates a <CODE>TopicPublisher</CODE> on a topic, it
     * defines a
     * new sequence of messages that have no ordering relationship with the
     * messages it has previously sent.
     *
     * @param topic the <CODE>Topic</CODE> to publish to, or null if this is an
     * unidentified producer
     *
     * @exception JMSException if the session fails to create a publisher
     *                         due to some internal error.
     * @exception InvalidDestinationException if an invalid topic is specified.
     */

    TopicPublisher createPublisher(Topic topic) throws JMSException;


    /** Creates a <CODE>TemporaryTopic</CODE> object. Its lifetime will be that
     * of the <CODE>TopicConnection</CODE> unless it is deleted earlier.
     *
     * @return a temporary topic identity
     *
     * @exception JMSException if the session fails to create a temporary
     *                         topic due to some internal error.
     */

    TemporaryTopic createTemporaryTopic() throws JMSException;


    /** Unsubscribes a durable subscription that has been created by a client.
     *
     * <P>This method deletes the state being maintained on behalf of the
     * subscriber by its provider.
     *
     * <P>It is erroneous for a client to delete a durable subscription
     * while there is an active <CODE>TopicSubscriber</CODE> for the
     * subscription, or while a consumed message is part of a pending
     * transaction or has not been acknowledged in the session.
     *
     * @param name the name used to identify this subscription
     *
     * @exception JMSException if the session fails to unsubscribe to the
     *                         durable subscription due to some internal error.
     * @exception InvalidDestinationException if an invalid subscription name
     *                                        is specified.
     */

    void unsubscribe(String name) throws JMSException;
}
