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

/** A client uses a <CODE>MessageConsumer</CODE> object to receive messages
 * from a destination.  A <CODE>MessageConsumer</CODE> object is created by
 * passing a <CODE>Destination</CODE> object to a message-consumer creation
 * method supplied by a session.
 *
 * <P><CODE>MessageConsumer</CODE> is the parent interface for all message
 * consumers.
 *
 * <P>A message consumer can be created with a message selector. A message
 * selector allows
 * the client to restrict the messages delivered to the message consumer to
 * those that match the selector.
 *
 * <P>A client may either synchronously receive a message consumer's messages
 * or have the consumer asynchronously deliver them as they arrive.
 *
 * <P>For synchronous receipt, a client can request the next message from a
 * message consumer using one of its <CODE>receive</CODE> methods. There are
 * several variations of <CODE>receive</CODE> that allow a
 * client to poll or wait for the next message.
 *
 * <P>For asynchronous delivery, a client can register a
 * <CODE>MessageListener</CODE> object with a message consumer.
 * As messages arrive at the message consumer, it delivers them by calling the
 * <CODE>MessageListener</CODE>'s <CODE>onMessage</CODE> method.
 *
 * <P>It is a client programming error for a <CODE>MessageListener</CODE> to
 * throw an exception.
 *
 * @version     $Revision: 1.1 $ $Date: 2003/08/16 02:29:57 $
 * @author      Mark Hapner
 * @author      Rich Burridge
 *
 * @see         javax.jms.QueueReceiver
 * @see         javax.jms.TopicSubscriber
 * @see         javax.jms.Session
 */

public interface MessageConsumer {

    /** Gets this message consumer's message selector expression.
     *
     * @return this message consumer's message selector, or null if no
     *         message selector exists for the message consumer (that is, if
     *         the message selector was not set or was set to null or the
     *         empty string)
     *
     * @exception JMSException if the JMS provider fails to get the message
     *                         selector due to some internal error.
     */

    String
            getMessageSelector() throws JMSException;


    /** Gets the message consumer's <CODE>MessageListener</CODE>.
     *
     * @return the listener for the message consumer, or null if no listener
     * is set
     *
     * @exception JMSException if the JMS provider fails to get the message
     *                         listener due to some internal error.
     * @see javax.jms.MessageConsumer#setMessageListener
     */

    MessageListener
            getMessageListener() throws JMSException;


    /** Sets the message consumer's <CODE>MessageListener</CODE>.
     *
     * <P>Setting the message listener to null is the equivalent of
     * unsetting the message listener for the message consumer.
     *
     * <P>The effect of calling <CODE>MessageConsumer.setMessageListener</CODE>
     * while messages are being consumed by an existing listener
     * or the consumer is being used to consume messages synchronously
     * is undefined.
     *
     * @param listener the listener to which the messages are to be
     *                 delivered
     *
     * @exception JMSException if the JMS provider fails to set the message
     *                         listener due to some internal error.
     * @see javax.jms.MessageConsumer#getMessageListener
     */

    void
            setMessageListener(MessageListener listener) throws JMSException;


    /** Receives the next message produced for this message consumer.
     *
     * <P>This call blocks indefinitely until a message is produced
     * or until this message consumer is closed.
     *
     * <P>If this <CODE>receive</CODE> is done within a transaction, the
     * consumer retains the message until the transaction commits.
     *
     * @return the next message produced for this message consumer, or
     * null if this message consumer is concurrently closed
     *
     * @exception JMSException if the JMS provider fails to receive the next
     *                         message due to some internal error.
     *
     */

    Message
            receive() throws JMSException;


    /** Receives the next message that arrives within the specified
     * timeout interval.
     *
     * <P>This call blocks until a message arrives, the
     * timeout expires, or this message consumer is closed.
     * A <CODE>timeout</CODE> of zero never expires, and the call blocks
     * indefinitely.
     *
     * @param timeout the timeout value (in milliseconds)
     *
     * @return the next message produced for this message consumer, or
     * null if the timeout expires or this message consumer is concurrently
     * closed
     *
     * @exception JMSException if the JMS provider fails to receive the next
     *                         message due to some internal error.
     */

    Message
            receive(long timeout) throws JMSException;


    /** Receives the next message if one is immediately available.
     *
     * @return the next message produced for this message consumer, or
     * null if one is not available
     *
     * @exception JMSException if the JMS provider fails to receive the next
     *                         message due to some internal error.
     */

    Message
            receiveNoWait() throws JMSException;


    /** Closes the message consumer.
     *
     * <P>Since a provider may allocate some resources on behalf of a
     * <CODE>MessageConsumer</CODE> outside the Java virtual machine, clients
     * should close them when they
     * are not needed. Relying on garbage collection to eventually reclaim
     * these resources may not be timely enough.
     *
     * <P>This call blocks until a <CODE>receive</CODE> or message listener in
     * progress has completed. A blocked message consumer <CODE>receive</CODE>
     * call
     * returns null when this message consumer is closed.
     *
     * @exception JMSException if the JMS provider fails to close the consumer
     *                         due to some internal error.
     */

    void
            close() throws JMSException;
}
