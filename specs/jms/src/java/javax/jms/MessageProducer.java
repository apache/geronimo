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

/** A client uses a <CODE>MessageProducer</CODE> object to send messages to a
 * destination. A <CODE>MessageProducer</CODE> object is created by passing a
 * <CODE>Destination</CODE> object to a message-producer creation method
 * supplied by a session.
 *
 * <P><CODE>MessageProducer</CODE> is the parent interface for all message
 * producers.
 *
 * <P>A client also has the option of creating a message producer without
 * supplying a destination. In this case, a destination must be provided with
 * every send operation. A typical use for this kind of message producer is
 * to send replies to requests using the request's <CODE>JMSReplyTo</CODE>
 * destination.
 *
 * <P>A client can specify a default delivery mode, priority, and time to live
 * for messages sent by a message producer. It can also specify the delivery
 * mode, priority, and time to live for an individual message.
 *
 * <P>A client can specify a time-to-live value in milliseconds for each
 * message it sends. This value defines a message expiration time that
 * is the sum of the message's time-to-live and the GMT when it is sent (for
 * transacted sends, this is the time the client sends the message, not
 * the time the transaction is committed).
 *
 * <P>A JMS provider should do its best to expire messages accurately;
 * however, the JMS API does not define the accuracy provided.
 *
 * @version     $Revision: 1.1 $ $Date: 2003/08/16 02:29:57 $
 * @author      Mark Hapner
 * @author      Rich Burridge
 * @author      Kate Stout
 *
 * @see         javax.jms.TopicPublisher
 * @see         javax.jms.QueueSender
 * @see         javax.jms.Session#createProducer
 */

public interface MessageProducer {

    /** Sets whether message IDs are disabled.
     *
     * <P>Since message IDs take some effort to create and increase a
     * message's size, some JMS providers may be able to optimize message
     * overhead if they are given a hint that the message ID is not used by
     * an application. By calling the <CODE>setDisableMessageID</CODE>
     * method on this message producer, a JMS client enables this potential
     * optimization for all messages sent by this message producer. If the JMS
     * provider accepts this hint,
     * these messages must have the message ID set to null; if the provider
     * ignores the hint, the message ID must be set to its normal unique value.
     *
     * <P>Message IDs are enabled by default.
     *
     * @param value indicates if message IDs are disabled
     *
     * @exception JMSException if the JMS provider fails to set message ID to
     *                         disabled due to some internal error.
     */

    void
            setDisableMessageID(boolean value) throws JMSException;


    /** Gets an indication of whether message IDs are disabled.
     *
     * @return an indication of whether message IDs are disabled
     *
     * @exception JMSException if the JMS provider fails to determine if
     *                         message IDs are disabled due to some internal
     *                         error.
     */

    boolean
            getDisableMessageID() throws JMSException;


    /** Sets whether message timestamps are disabled.
     *
     * <P>Since timestamps take some effort to create and increase a
     * message's size, some JMS providers may be able to optimize message
     * overhead if they are given a hint that the timestamp is not used by an
     * application. By calling the <CODE>setDisableMessageTimestamp</CODE>
     * method on this message producer, a JMS client enables this potential
     * optimization for all messages sent by this message producer.  If the
     * JMS provider accepts this hint,
     * these messages must have the timestamp set to zero; if the provider
     * ignores the hint, the timestamp must be set to its normal value.
     *
     * <P>Message timestamps are enabled by default.
     *
     * @param value indicates if message timestamps are disabled
     *
     * @exception JMSException if the JMS provider fails to set timestamps to
     *                         disabled due to some internal error.
     */

    void
            setDisableMessageTimestamp(boolean value) throws JMSException;


    /** Gets an indication of whether message timestamps are disabled.
     *
     * @return an indication of whether message timestamps are disabled
     *
     * @exception JMSException if the JMS provider fails to determine if
     *                         timestamps are disabled due to some internal
     *                         error.
     */

    boolean
            getDisableMessageTimestamp() throws JMSException;


    /** Sets the producer's default delivery mode.
     *
     * <P>Delivery mode is set to <CODE>PERSISTENT</CODE> by default.
     *
     * @param deliveryMode the message delivery mode for this message
     * producer; legal values are <code>DeliveryMode.NON_PERSISTENT</code>
     * and <code>DeliveryMode.PERSISTENT</code>
     *
     * @exception JMSException if the JMS provider fails to set the delivery
     *                         mode due to some internal error.
     *
     * @see javax.jms.MessageProducer#getDeliveryMode
     * @see javax.jms.DeliveryMode#NON_PERSISTENT
     * @see javax.jms.DeliveryMode#PERSISTENT
     * @see javax.jms.Message#DEFAULT_DELIVERY_MODE
     */

    void
            setDeliveryMode(int deliveryMode) throws JMSException;


    /** Gets the producer's default delivery mode.
     *
     * @return the message delivery mode for this message producer
     *
     * @exception JMSException if the JMS provider fails to get the delivery
     *                         mode due to some internal error.
     *
     * @see javax.jms.MessageProducer#setDeliveryMode
     */

    int
            getDeliveryMode() throws JMSException;


    /** Sets the producer's default priority.
     *
     * <P>The JMS API defines ten levels of priority value, with 0 as the
     * lowest priority and 9 as the highest. Clients should consider priorities
     * 0-4 as gradations of normal priority and priorities 5-9 as gradations
     * of expedited priority. Priority is set to 4 by default.
     *
     * @param defaultPriority the message priority for this message producer;
     *                        must be a value between 0 and 9
     *
     *
     * @exception JMSException if the JMS provider fails to set the priority
     *                         due to some internal error.
     *
     * @see javax.jms.MessageProducer#getPriority
     * @see javax.jms.Message#DEFAULT_PRIORITY
     */

    void
            setPriority(int defaultPriority) throws JMSException;


    /** Gets the producer's default priority.
     *
     * @return the message priority for this message producer
     *
     * @exception JMSException if the JMS provider fails to get the priority
     *                         due to some internal error.
     *
     * @see javax.jms.MessageProducer#setPriority
     */

    int
            getPriority() throws JMSException;


    /** Sets the default length of time in milliseconds from its dispatch time
     * that a produced message should be retained by the message system.
     *
     * <P>Time to live is set to zero by default.
     *
     * @param timeToLive the message time to live in milliseconds; zero is
     * unlimited
     *
     * @exception JMSException if the JMS provider fails to set the time to
     *                         live due to some internal error.
     *
     * @see javax.jms.MessageProducer#getTimeToLive
     * @see javax.jms.Message#DEFAULT_TIME_TO_LIVE
     */

    void
            setTimeToLive(long timeToLive) throws JMSException;


    /** Gets the default length of time in milliseconds from its dispatch time
     * that a produced message should be retained by the message system.
     *
     * @return the message time to live in milliseconds; zero is unlimited
     *
     * @exception JMSException if the JMS provider fails to get the time to
     *                         live due to some internal error.
     *
     * @see javax.jms.MessageProducer#setTimeToLive
     */

    long
            getTimeToLive() throws JMSException;

    /** Gets the destination associated with this <CODE>MessageProducer</CODE>.
     *
     * @return this producer's <CODE>Destination/<CODE>
     *
     * @exception JMSException if the JMS provider fails to get the destination for
     *                         this <CODE>MessageProducer</CODE>
     *                         due to some internal error.
     *@since 1.1
     */

    Destination
            getDestination() throws JMSException;

    /** Closes the message producer.
     *
     * <P>Since a provider may allocate some resources on behalf of a
     * <CODE>MessageProducer</CODE> outside the Java virtual machine, clients
     * should close them when they
     * are not needed. Relying on garbage collection to eventually reclaim
     * these resources may not be timely enough.
     *
     * @exception JMSException if the JMS provider fails to close the producer
     *                         due to some internal error.
     */

    void
            close() throws JMSException;


    /** Sends a message using the <CODE>MessageProducer</CODE>'s
     * default delivery mode, priority, and time to live.
     *
     * @param message the message to send
     *
     * @exception JMSException if the JMS provider fails to send the message
     *                         due to some internal error.
     * @exception MessageFormatException if an invalid message is specified.
     * @exception InvalidDestinationException if a client uses
     *                         this method with a <CODE>MessageProducer</CODE> with
     *                         an invalid destination.
     * @exception java.lang.UnsupportedOperationException if a client uses this
     *                         method with a <CODE>MessageProducer</CODE> that did
     *                         not specify a destination at creation time.
     *
     * @see javax.jms.Session#createProducer
     * @see javax.jms.MessageProducer
     *
     * @since 1.1
     */

    void
            send(Message message) throws JMSException;

    /** Sends a message to the destination, specifying delivery mode, priority, and
     * time to live.
     *
     * @param message the message to send
     * @param deliveryMode the delivery mode to use
     * @param priority the priority for this message
     * @param timeToLive the message's lifetime (in milliseconds)
     *
     * @exception JMSException if the JMS provider fails to send the message
     *                         due to some internal error.
     * @exception MessageFormatException if an invalid message is specified.
     * @exception InvalidDestinationException if a client uses
     *                         this method with a <CODE>MessageProducer</CODE> with
     *                         an invalid destination.
     * @exception java.lang.UnsupportedOperationException if a client uses this
     *                         method with a <CODE>MessageProducer</CODE> that did
     *                         not specify a destination at creation time.
     *
     * @see javax.jms.Session#createProducer
     * @since 1.1
     */

    void
            send(Message message,
                 int deliveryMode,
                 int priority,
                 long timeToLive) throws JMSException;


    /**Sends a message to a destination for an unidentified message producer.
     * Uses the <CODE>MessageProducer</CODE>'s default delivery mode, priority,
     * and time to live.
     *
     * <P>Typically, a message producer is assigned a destination at creation
     * time; however, the JMS API also supports unidentified message producers,
     * which require that the destination be supplied every time a message is
     * sent.
     *
     * @param destination the destination to send this message to
     * @param message the message to send
     *
     * @exception JMSException if the JMS provider fails to send the message
     *                         due to some internal error.
     * @exception MessageFormatException if an invalid message is specified.
     * @exception InvalidDestinationException if a client uses
     *                         this method with an invalid destination.
     * @exception java.lang.UnsupportedOperationException if a client uses this
     *                         method with a <CODE>MessageProducer</CODE> that
     *                         specified a destination at creation time.
     *
     * @see javax.jms.Session#createProducer
     * @see javax.jms.MessageProducer
     * @since 1.1
     */

    void
            send(Destination destination, Message message) throws JMSException;


    /** Sends a message to a destination for an unidentified message producer,
     * specifying delivery mode, priority and time to live.
     *
     * <P>Typically, a message producer is assigned a destination at creation
     * time; however, the JMS API also supports unidentified message producers,
     * which require that the destination be supplied every time a message is
     * sent.
     *
     * @param destination the destination to send this message to
     * @param message the message to send
     * @param deliveryMode the delivery mode to use
     * @param priority the priority for this message
     * @param timeToLive the message's lifetime (in milliseconds)
     *
     * @exception JMSException if the JMS provider fails to send the message
     *                         due to some internal error.
     * @exception MessageFormatException if an invalid message is specified.
     * @exception InvalidDestinationException if a client uses
     *                         this method with an invalid destination.
     *
     * @see javax.jms.Session#createProducer
     * @since 1.1
     */

    void
            send(Destination destination,
                 Message message,
                 int deliveryMode,
                 int priority,
                 long timeToLive) throws JMSException;


}
