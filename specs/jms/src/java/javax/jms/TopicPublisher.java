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

/** A client uses a <CODE>TopicPublisher</CODE> object to publish messages on a
 * topic. A <CODE>TopicPublisher</CODE> object is the publish-subscribe form
 * of a message producer.
 *
 * <P>Normally, the <CODE>Topic</CODE> is specified when a
 * <CODE>TopicPublisher</CODE> is created.  In this case, an attempt to use
 * the <CODE>publish</CODE> methods for an unidentified
 * <CODE>TopicPublisher</CODE> will throw a
 * <CODE>java.lang.UnsupportedOperationException</CODE>.
 *
 * <P>If the <CODE>TopicPublisher</CODE> is created with an unidentified
 * <CODE>Topic</CODE>, an attempt to use the <CODE>publish</CODE> methods that
 * assume that the <CODE>Topic</CODE> has been identified will throw a
 * <CODE>java.lang.UnsupportedOperationException</CODE>.
 *
 * <P>During the execution of its <CODE>publish</CODE> method,
 * a message must not be changed by other threads within the client.
 * If the message is modified, the result of the <CODE>publish</CODE> is
 * undefined.
 *
 * <P>After publishing a message, a client may retain and modify it
 * without affecting the message that has been published. The same message
 * object may be published multiple times.
 *
 * <P>The following message headers are set as part of publishing a
 * message: <code>JMSDestination</code>, <code>JMSDeliveryMode</code>,
 * <code>JMSExpiration</code>, <code>JMSPriority</code>,
 * <code>JMSMessageID</code> and <code>JMSTimeStamp</code>.
 * When the message is published, the values of these headers are ignored.
 * After completion of the <CODE>publish</CODE>, the headers hold the values
 * specified by the method publishing the message. It is possible for the
 * <CODE>publish</CODE> method not to set <code>JMSMessageID</code> and
 * <code>JMSTimeStamp</code> if the
 * setting of these headers is explicitly disabled by the
 * <code>MessageProducer.setDisableMessageID</code> or
 * <code>MessageProducer.setDisableMessageTimestamp</code> method.
 *
 *<P>Creating a <CODE>MessageProducer</CODE> provides the same features as
 * creating a <CODE>TopicPublisher</CODE>. A <CODE>MessageProducer</CODE> object is
 * recommended when creating new code. The  <CODE>TopicPublisher</CODE> is
 * provided to support existing code.

 *
 *<P>Because <CODE>TopicPublisher</CODE> inherits from
 * <CODE>MessageProducer</CODE>, it inherits the
 * <CODE>send</CODE> methods that are a part of the <CODE>MessageProducer</CODE>
 * interface. Using the <CODE>send</CODE> methods will have the same
 * effect as using the
 * <CODE>publish</CODE> methods: they are functionally the same.
 *
 *
 * @version     $Revision: 1.1 $ $Date: 2003/08/16 02:29:58 $
 * @author      Mark Hapner
 * @author      Rich Burridge
 * @author      Kate Stout
 *
 * @see Session#createProducer(Destination)
 * @see TopicSession#createPublisher(Topic)
 */

public interface TopicPublisher extends MessageProducer {

    /** Gets the topic associated with this <CODE>TopicPublisher</CODE>.
     *
     * @return this publisher's topic
     *
     * @exception JMSException if the JMS provider fails to get the topic for
     *                         this <CODE>TopicPublisher</CODE>
     *                         due to some internal error.
     */

    Topic getTopic() throws JMSException;


    /** Publishes a message to the topic.
     * Uses the <CODE>TopicPublisher</CODE>'s default delivery mode, priority,
     * and time to live.
     *
     * @param message the message to publish
     *
     * @exception JMSException if the JMS provider fails to publish the message
     *                         due to some internal error.
     * @exception MessageFormatException if an invalid message is specified.
     * @exception InvalidDestinationException if a client uses this method
     *                         with a <CODE>TopicPublisher</CODE> with
     *                         an invalid topic.
     * @exception java.lang.UnsupportedOperationException if a client uses this
     *                         method with a <CODE>TopicPublisher</CODE> that
     *                         did not specify a topic at creation time.
     *
     * @see javax.jms.MessageProducer#getDeliveryMode()
     * @see javax.jms.MessageProducer#getTimeToLive()
     * @see javax.jms.MessageProducer#getPriority()
     */

    void publish(Message message) throws JMSException;


    /** Publishes a message to the topic, specifying delivery mode,
     * priority, and time to live.
     *
     * @param message the message to publish
     * @param deliveryMode the delivery mode to use
     * @param priority the priority for this message
     * @param timeToLive the message's lifetime (in milliseconds)
     *
     * @exception JMSException if the JMS provider fails to publish the message
     *                         due to some internal error.
     * @exception MessageFormatException if an invalid message is specified.
     * @exception InvalidDestinationException if a client uses this method
     *                         with a <CODE>TopicPublisher</CODE> with
     *                         an invalid topic.
     * @exception java.lang.UnsupportedOperationException if a client uses this
     *                         method with a <CODE>TopicPublisher</CODE> that
     *                         did not specify a topic at creation time.
     */

    void publish(Message message, int deliveryMode, int priority, long timeToLive) throws JMSException;


    /** Publishes a message to a topic for an unidentified message producer.
     * Uses the <CODE>TopicPublisher</CODE>'s default delivery mode,
     * priority, and time to live.
     *
     * <P>Typically, a message producer is assigned a topic at creation
     * time; however, the JMS API also supports unidentified message producers,
     * which require that the topic be supplied every time a message is
     * published.
     *
     * @param topic the topic to publish this message to
     * @param message the message to publish
     *
     * @exception JMSException if the JMS provider fails to publish the message
     *                         due to some internal error.
     * @exception MessageFormatException if an invalid message is specified.
     * @exception InvalidDestinationException if a client uses
     *                         this method with an invalid topic.
     *
     * @see javax.jms.MessageProducer#getDeliveryMode()
     * @see javax.jms.MessageProducer#getTimeToLive()
     * @see javax.jms.MessageProducer#getPriority()
     */

    void publish(Topic topic, Message message) throws JMSException;


    /** Publishes a message to a topic for an unidentified message
     * producer, specifying delivery mode, priority and time to live.
     *
     * <P>Typically, a message producer is assigned a topic at creation
     * time; however, the JMS API also supports unidentified message producers,
     * which require that the topic be supplied every time a message is
     * published.
     *
     * @param topic the topic to publish this message to
     * @param message the message to publish
     * @param deliveryMode the delivery mode to use
     * @param priority the priority for this message
     * @param timeToLive the message's lifetime (in milliseconds)
     *
     * @exception JMSException if the JMS provider fails to publish the message
     *                         due to some internal error.
     * @exception MessageFormatException if an invalid message is specified.
     * @exception InvalidDestinationException if a client uses
     *                         this method with an invalid topic.
     */

    void publish(Topic topic, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException;
}
