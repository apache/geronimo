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

import java.util.Enumeration;

/** A client uses a <CODE>QueueBrowser</CODE> object to look at messages on a
 * queue without removing them.
 *
 * <P>The <CODE>getEnumeration</CODE> method returns a
 * <CODE>java.util.Enumeration</CODE> that is used to scan
 * the queue's messages. It may be an enumeration of the entire content of a
 * queue, or it may contain only the messages matching a message selector.
 *
 * <P>Messages may be arriving and expiring while the scan is done. The JMS API
 * does
 * not require the content of an enumeration to be a static snapshot of queue
 * content. Whether these changes are visible or not depends on the JMS
 * provider.
 *
 *<P>A <CODE>QueueBrowser</CODE> can be created from either a
 * <CODE>Session</CODE> or a <CODE> QueueSession</CODE>.
 *
 * @version     $Revision: 1.1 $ $Date: 2003/08/16 02:29:58 $
 * @author      Mark Hapner
 * @author      Rich Burridge
 * @author      Kate Stout
 *
 *  @see         javax.jms.Session#createBrowser
 * @see         javax.jms.QueueSession#createBrowser
 * @see         javax.jms.QueueReceiver
 */

public interface QueueBrowser {

    /** Gets the queue associated with this queue browser.
     *
     * @return the queue
     *
     * @exception JMSException if the JMS provider fails to get the
     *                         queue associated with this browser
     *                         due to some internal error.
     */

    Queue getQueue() throws JMSException;


    /** Gets this queue browser's message selector expression.
     *
     * @return this queue browser's message selector, or null if no
     *         message selector exists for the message consumer (that is, if
     *         the message selector was not set or was set to null or the
     *         empty string)
     *
     * @exception JMSException if the JMS provider fails to get the
     *                         message selector for this browser
     *                         due to some internal error.
     */

    String getMessageSelector() throws JMSException;


    /** Gets an enumeration for browsing the current queue messages in the
     * order they would be received.
     *
     * @return an enumeration for browsing the messages
     *
     * @exception JMSException if the JMS provider fails to get the
     *                         enumeration for this browser
     *                         due to some internal error.
     */

    Enumeration getEnumeration() throws JMSException;


    /** Closes the <CODE>QueueBrowser</CODE>.
     *
     * <P>Since a provider may allocate some resources on behalf of a
     * QueueBrowser outside the Java virtual machine, clients should close them
     * when they
     * are not needed. Relying on garbage collection to eventually reclaim
     * these resources may not be timely enough.
     *
     * @exception JMSException if the JMS provider fails to close this
     *                         browser due to some internal error.
     */

    void close() throws JMSException;
}
