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

/** A <CODE>TopicConnection</CODE> object is an active connection to a
 * publish/subscribe JMS provider. A client uses a <CODE>TopicConnection</CODE>
 * object to create one or more <CODE>TopicSession</CODE> objects
 * for producing and consuming messages.
 *
 *<P>A <CODE>TopicConnection</CODE> can be used to create a
 *<CODE>TopicSession</CODE>, from which
 * specialized topic-related objects can be created.
 * A more general, and recommended approach is to use the
 * <CODE>Connection</CODE> object.
 *
 *
 * <P>The <CODE>TopicConnection</CODE> object
 * should be used to support existing code.
 *
 * @version     $Revision: 1.1 $ $Date: 2003/08/16 02:29:58 $
 * @author      Mark Hapner
 * @author      Rich Burridge
 * @author      Kate Stout
 *
 * @see         javax.jms.Connection
 * @see         javax.jms.ConnectionFactory
 * @see	 javax.jms.TopicConnectionFactory
 */

public interface TopicConnection extends Connection {

    /** Creates a <CODE>TopicSession</CODE> object.
     *
     * @param transacted indicates whether the session is transacted
     * @param acknowledgeMode indicates whether the consumer or the
     * client will acknowledge any messages it receives; ignored if the session
     * is transacted. Legal values are <code>Session.AUTO_ACKNOWLEDGE</code>,
     * <code>Session.CLIENT_ACKNOWLEDGE</code>, and
     * <code>Session.DUPS_OK_ACKNOWLEDGE</code>.
     *
     * @return a newly created topic session
     *
     * @exception JMSException if the <CODE>TopicConnection</CODE> object fails
     *                         to create a session due to some internal error or
     *                         lack of support for the specific transaction
     *                         and acknowledgement mode.
     *
     * @see Session#AUTO_ACKNOWLEDGE
     * @see Session#CLIENT_ACKNOWLEDGE
     * @see Session#DUPS_OK_ACKNOWLEDGE
     */

    TopicSession createTopicSession(boolean transacted, int acknowledgeMode) throws JMSException;


    /** Creates a connection consumer for this connection (optional operation).
     * This is an expert facility not used by regular JMS clients.
     *
     * @param topic the topic to access
     * @param messageSelector only messages with properties matching the
     * message selector expression are delivered.  A value of null or
     * an empty string indicates that there is no message selector
     * for the message consumer.
     * @param sessionPool the server session pool to associate with this
     * connection consumer
     * @param maxMessages the maximum number of messages that can be
     * assigned to a server session at one time
     *
     * @return the connection consumer
     *
     * @exception JMSException if the <CODE>TopicConnection</CODE> object fails
     *                         to create a connection consumer due to some
     *                         internal error or invalid arguments for
     *                         <CODE>sessionPool</CODE> and
     *                         <CODE>messageSelector</CODE>.
     * @exception InvalidDestinationException if an invalid topic is specified.
     * @exception InvalidSelectorException if the message selector is invalid.
     * @see javax.jms.ConnectionConsumer
     */

    ConnectionConsumer createConnectionConsumer(Topic topic, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException;


    /** Create a durable connection consumer for this connection (optional operation).
     * This is an expert facility not used by regular JMS clients.
     *
     * @param topic the topic to access
     * @param subscriptionName durable subscription name
     * @param messageSelector only messages with properties matching the
     * message selector expression are delivered.  A value of null or
     * an empty string indicates that there is no message selector
     * for the message consumer.
     * @param sessionPool the server session pool to associate with this
     * durable connection consumer
     * @param maxMessages the maximum number of messages that can be
     * assigned to a server session at one time
     *
     * @return the durable connection consumer
     *
     * @exception JMSException if the <CODE>TopicConnection</CODE> object fails
     *                         to create a connection consumer due to some
     *                         internal error or invalid arguments for
     *                         <CODE>sessionPool</CODE> and
     *                         <CODE>messageSelector</CODE>.
     * @exception InvalidDestinationException if an invalid topic is specified.
     * @exception InvalidSelectorException if the message selector is invalid.
     * @see javax.jms.ConnectionConsumer
     */

    ConnectionConsumer createDurableConnectionConsumer(Topic topic, String subscriptionName, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException;
}
