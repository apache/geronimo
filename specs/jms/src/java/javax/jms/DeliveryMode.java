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

/** The delivery modes supported by the JMS API are <CODE>PERSISTENT</CODE> and
 * <CODE>NON_PERSISTENT</CODE>.
 *
 * <P>A client marks a message as persistent if it feels that the
 * application will have problems if the message is lost in transit.
 * A client marks a message as non-persistent if an occasional
 * lost message is tolerable. Clients use delivery mode to tell a
 * JMS provider how to balance message transport reliability with throughput.
 *
 * <P>Delivery mode covers only the transport of the message to its
 * destination. Retention of a message at the destination until
 * its receipt is acknowledged is not guaranteed by a <CODE>PERSISTENT</CODE>
 * delivery mode. Clients should assume that message retention
 * policies are set administratively. Message retention policy
 * governs the reliability of message delivery from destination
 * to message consumer. For example, if a client's message storage
 * space is exhausted, some messages may be dropped in accordance with
 * a site-specific message retention policy.
 *
 * <P>A message is guaranteed to be delivered once and only once
 * by a JMS provider if the delivery mode of the message is
 * <CODE>PERSISTENT</CODE>
 * and if the destination has a sufficient message retention policy.
 *
 *
 *
 * @version     $Revision: 1.1 $ $Date: 2003/08/16 02:29:57 $
 * @author      Mark Hapner
 * @author      Rich Burridge
 */

public interface DeliveryMode {

    /** This is the lowest-overhead delivery mode because it does not require
     * that the message be logged to stable storage. The level of JMS provider
     * failure that causes a <CODE>NON_PERSISTENT</CODE> message to be lost is
     * not defined.
     *
     * <P>A JMS provider must deliver a <CODE>NON_PERSISTENT</CODE> message
     * with an
     * at-most-once guarantee. This means that it may lose the message, but it
     * must not deliver it twice.
     */

    static final int NON_PERSISTENT = 1;

    /** This delivery mode instructs the JMS provider to log the message to stable
     * storage as part of the client's send operation. Only a hard media
     * failure should cause a <CODE>PERSISTENT</CODE> message to be lost.
     */

    static final int PERSISTENT = 2;
}
