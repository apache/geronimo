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

/** A <CODE>ServerSession</CODE> object is an application server object that
 * is used by a server to associate a thread with a JMS session (optional).
 *
 * <P>A <CODE>ServerSession</CODE> implements two methods:
 *
 * <UL>
 *   <LI><CODE>getSession</CODE> - returns the <CODE>ServerSession</CODE>'s
 *       JMS session.
 *   <LI><CODE>start</CODE> - starts the execution of the
 *       <CODE>ServerSession</CODE>
 *       thread and results in the execution of the JMS session's
 *       <CODE>run</CODE> method.
 * </UL>
 *
 * <P>A <CODE>ConnectionConsumer</CODE> implemented by a JMS provider uses a
 * <CODE>ServerSession</CODE> to process one or more messages that have
 * arrived. It does this by getting a <CODE>ServerSession</CODE> from the
 * <CODE>ConnectionConsumer</CODE>'s <CODE>ServerSessionPool</CODE>; getting
 * the <CODE>ServerSession</CODE>'s JMS session; loading it with the messages;
 * and then starting the <CODE>ServerSession</CODE>.
 *
 * <P>In most cases the <CODE>ServerSession</CODE> will register some object
 * it provides as the <CODE>ServerSession</CODE>'s thread run object. The
 * <CODE>ServerSession</CODE>'s <CODE>start</CODE> method will call the
 * thread's <CODE>start</CODE> method, which will start the new thread, and
 * from it, call the <CODE>run</CODE> method of the
 * <CODE>ServerSession</CODE>'s run object. This object will do some
 * housekeeping and then call the <CODE>Session</CODE>'s <CODE>run</CODE>
 * method. When <CODE>run</CODE> returns, the <CODE>ServerSession</CODE>'s run
 * object can return the <CODE>ServerSession</CODE> to the
 * <CODE>ServerSessionPool</CODE>, and the cycle starts again.
 *
 * <P>Note that the JMS API does not architect how the
 * <CODE>ConnectionConsumer</CODE> loads the <CODE>Session</CODE> with
 * messages. Since both the <CODE>ConnectionConsumer</CODE> and
 * <CODE>Session</CODE> are implemented by the same JMS provider, they can
 * accomplish the load using a private mechanism.
 *
 * @version     $Revision: 1.1 $ $Date: 2003/08/16 02:29:58 $
 * @author      Mark Hapner
 * @author      Rich Burridge
 *
 * @see         javax.jms.ServerSessionPool
 * @see         javax.jms.ConnectionConsumer
 */

public interface ServerSession {

    /** Return the <CODE>ServerSession</CODE>'s <CODE>Session</CODE>. This must
     * be a <CODE>Session</CODE> created by the same <CODE>Connection</CODE>
     * that will be dispatching messages to it. The provider will assign one or
     * more messages to the <CODE>Session</CODE>
     * and then call <CODE>start</CODE> on the <CODE>ServerSession</CODE>.
     *
     * @return the server session's session
     *
     * @exception JMSException if the JMS provider fails to get the associated
     *                         session for this <CODE>ServerSession</CODE> due
     *                         to some internal error.
     **/

    Session getSession() throws JMSException;


    /** Cause the <CODE>Session</CODE>'s <CODE>run</CODE> method to be called
     * to process messages that were just assigned to it.
     *
     * @exception JMSException if the JMS provider fails to start the server
     *                         session to process messages due to some internal
     *                         error.
     */

    void start() throws JMSException;
}
