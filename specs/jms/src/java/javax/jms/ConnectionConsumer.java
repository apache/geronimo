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

/** For application servers, <CODE>Connection</CODE> objects provide a special
 * facility
 * for creating a <CODE>ConnectionConsumer</CODE> (optional). The messages it
 * is to consume are
 * specified by a <CODE>Destination</CODE> and a message selector. In addition,
 * a <CODE>ConnectionConsumer</CODE> must be given a
 * <CODE>ServerSessionPool</CODE> to use for
 * processing its messages.
 *
 * <P>Normally, when traffic is light, a <CODE>ConnectionConsumer</CODE> gets a
 * <CODE>ServerSession</CODE> from its pool, loads it with a single message, and
 * starts it. As traffic picks up, messages can back up. If this happens,
 * a <CODE>ConnectionConsumer</CODE> can load each <CODE>ServerSession</CODE>
 * with more than one
 * message. This reduces the thread context switches and minimizes resource
 * use at the expense of some serialization of message processing.
 *
 * @version     $Revision: 1.1 $ $Date: 2003/08/16 02:29:57 $
 * @author      Mark Hapner
 * @author      Rich Burridge
 *
 * @see javax.jms.Connection#createConnectionConsumer
 * @see javax.jms.Connection#createDurableConnectionConsumer
 * @see javax.jms.QueueConnection#createConnectionConsumer
 * @see javax.jms.TopicConnection#createConnectionConsumer
 * @see javax.jms.TopicConnection#createDurableConnectionConsumer
 */

public interface ConnectionConsumer {

    /** Gets the server session pool associated with this connection consumer.
     *
     * @return the server session pool used by this connection consumer
     *
     * @exception JMSException if the JMS provider fails to get the server
     *                         session pool associated with this consumer due
     *                         to some internal error.
     */

    ServerSessionPool
            getServerSessionPool() throws JMSException;


    /** Closes the connection consumer.
     *
     * <P>Since a provider may allocate some resources on behalf of a
     * connection consumer outside the Java virtual machine, clients should
     * close these resources when
     * they are not needed. Relying on garbage collection to eventually
     * reclaim these resources may not be timely enough.
     *
     * @exception JMSException if the JMS provider fails to release resources
     *                         on behalf of the connection consumer or fails
     *                         to close the connection consumer.
     */

    void
            close() throws JMSException;
}
