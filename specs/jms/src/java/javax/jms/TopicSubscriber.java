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

/** A client uses a <CODE>TopicSubscriber</CODE> object to receive messages that
 * have been published to a topic. A <CODE>TopicSubscriber</CODE> object is the
 * publish/subscribe form of a message consumer. A <CODE>MessageConsumer</CODE>
 * can be created by using <CODE>Session.createConsumer</CODE>.
 *
 * <P>A <CODE>TopicSession</CODE> allows the creation of multiple
 * <CODE>TopicSubscriber</CODE> objects per topic.  It will deliver each
 * message for a topic to each
 * subscriber eligible to receive it. Each copy of the message
 * is treated as a completely separate message. Work done on one copy has
 * no effect on the others; acknowledging one does not acknowledge the
 * others; one message may be delivered immediately, while another waits
 * for its subscriber to process messages ahead of it.
 *
 * <P>Regular <CODE>TopicSubscriber</CODE> objects are not durable. They
 * receive only messages that are published while they are active.
 *
 * <P>Messages filtered out by a subscriber's message selector will never
 * be delivered to the subscriber. From the subscriber's perspective, they
 * do not exist.
 *
 * <P>In some cases, a connection may both publish and subscribe to a topic.
 * The subscriber <CODE>NoLocal</CODE> attribute allows a subscriber to inhibit
 * the
 * delivery of messages published by its own connection.
 *
 * <P>If a client needs to receive all the messages published on a topic,
 * including the ones published while the subscriber is inactive, it uses
 * a durable <CODE>TopicSubscriber</CODE>. The JMS provider retains a record of
 * this durable
 * subscription and insures that all messages from the topic's publishers
 * are retained until they are acknowledged by this durable
 * subscriber or they have expired.
 *
 * <P>Sessions with durable subscribers must always provide the same client
 * identifier. In addition, each client must specify a name that uniquely
 * identifies (within client identifier) each durable subscription it creates.
 * Only one session at a time can have a <CODE>TopicSubscriber</CODE> for a
 * particular durable subscription.
 *
 * <P>A client can change an existing durable subscription by creating a
 * durable <CODE>TopicSubscriber</CODE> with the same name and a new topic
 * and/or message
 * selector. Changing a durable subscription is equivalent to unsubscribing
 * (deleting) the old one and creating a new one.
 *
 * <P>The <CODE>unsubscribe</CODE> method is used to delete a durable
 * subscription. The <CODE>unsubscribe</CODE> method can be used at the
 * <CODE>Session</CODE> or <CODE>TopicSession</CODE> level.
 * This method deletes the state being
 * maintained on behalf of the subscriber by its provider.
 *
 * <P>Creating a <CODE>MessageConsumer</CODE> provides the same features as
 * creating a <CODE>TopicSubscriber</CODE>. To create a durable subscriber,
 * use of <CODE>Session.CreateDurableSubscriber</CODE> is recommended. The
 * <CODE>TopicSubscriber</CODE> is provided to support existing code.
 *
 *
 * @version     $Revision: 1.1 $ $Date: 2003/08/16 02:29:58 $
 * @author      Mark Hapner
 * @author      Rich Burridge
 * @author      Kate Stout
 *
 * @see         javax.jms.Session#createConsumer
 * @see         javax.jms.Session#createDurableSubscriber
 * @see         javax.jms.TopicSession
 * @see         javax.jms.TopicSession#createSubscriber
 * @see         javax.jms.MessageConsumer
 */

public interface TopicSubscriber extends MessageConsumer {

    /** Gets the <CODE>Topic</CODE> associated with this subscriber.
     *
     * @return this subscriber's <CODE>Topic</CODE>
     *
     * @exception JMSException if the JMS provider fails to get the topic for
     *                         this topic subscriber
     *                         due to some internal error.
     */

    Topic getTopic() throws JMSException;


    /** Gets the <CODE>NoLocal</CODE> attribute for this subscriber.
     * The default value for this attribute is false.
     *
     * @return true if locally published messages are being inhibited
     *
     * @exception JMSException if the JMS provider fails to get the
     *                         <CODE>NoLocal</CODE> attribute for
     *                         this topic subscriber
     *                         due to some internal error.
     */

    boolean getNoLocal() throws JMSException;
}
