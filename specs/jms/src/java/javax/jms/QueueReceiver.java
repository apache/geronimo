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


/** A client uses a <CODE>QueueReceiver</CODE> object to receive messages that
 * have been delivered to a queue.
 *
 * <P>Although it is possible to have multiple <CODE>QueueReceiver</CODE>s
 * for the same queue, the JMS API does not define how messages are
 * distributed between the <CODE>QueueReceiver</CODE>s.
 *
 * <P>If a <CODE>QueueReceiver</CODE> specifies a message selector, the
 * messages that are not selected remain on the queue. By definition, a message
 * selector allows a <CODE>QueueReceiver</CODE> to skip messages. This
 * means that when the skipped messages are eventually read, the total ordering
 * of the reads does not retain the partial order defined by each message
 * producer. Only <CODE>QueueReceiver</CODE>s without a message selector
 * will read messages in message producer order.
 *
 * <P>Creating a <CODE>MessageConsumer</CODE> provides the same features as
 * creating a <CODE>QueueReceiver</CODE>. A <CODE>MessageConsumer</CODE> object is
 * recommended for creating new code. The  <CODE>QueueReceiver</CODE> is
 * provided to support existing code.
 *
 * @version     $Revision: 1.1 $ $Date: 2003/08/16 02:29:58 $
 * @author      Mark Hapner
 * @author      Rich Burridge
 * @author      Kate Stout
 *
 * @see         javax.jms.Session#createConsumer(Destination, String)
 * @see         javax.jms.Session#createConsumer(Destination)
 * @see         javax.jms.QueueSession#createReceiver(Queue, String)
 * @see         javax.jms.QueueSession#createReceiver(Queue)
 * @see         javax.jms.MessageConsumer
 */

public interface QueueReceiver extends MessageConsumer {

    /** Gets the <CODE>Queue</CODE> associated with this queue receiver.
     *
     * @return this receiver's <CODE>Queue</CODE>
     *
     * @exception JMSException if the JMS provider fails to get the queue for
     *                         this queue receiver
     *                         due to some internal error.
     */

    Queue getQueue() throws JMSException;
}
