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

/** The <CODE>XAConnectionFactory</CODE> interface is a base interface for the
 * <CODE>XAQueueConnectionFactory</CODE> and
 * <CODE>XATopicConnectionFactory</CODE> interfaces.
 *
 * <P>Some application servers provide support for grouping JTS capable
 * resource use into a distributed transaction (optional). To include JMS API transactions
 * in a JTS transaction, an application server requires a JTS aware JMS
 * provider. A JMS provider exposes its JTS support using an
 * <CODE>XAConnectionFactory</CODE> object, which an application server uses
 * to create <CODE>XAConnection</CODE> objects.
 *
 * <P><CODE>XAConnectionFactory</CODE> objects are JMS administered objects,
 * just like <CODE>ConnectionFactory</CODE> objects. It is expected that
 * application servers will find them using the Java Naming and Directory
 * Interface (JNDI) API.
 *
 *<P>The <CODE>XAConnectionFactory</CODE> interface is optional. JMS providers
 * are not required to support this interface. This interface is for
 * use by JMS providers to support transactional environments.
 * Client programs are strongly encouraged to use the transactional support
 * available in their environment, rather than use these XA
 * interfaces directly.
 *
 * @version     $Revision: 1.1 $ $Date: 2003/08/16 02:29:58 $
 * @author      Mark Hapner
 * @author      Rich Burridge
 * @author      Kate Stout
 *
 * @see         javax.jms.XAQueueConnectionFactory
 * @see         javax.jms.XATopicConnectionFactory
 */

public interface XAConnectionFactory {

    /** Creates an <CODE>XAConnection</CODE> with the default user identity.
     * The connection is created in stopped mode. No messages
     * will be delivered until the <code>Connection.start</code> method
     * is explicitly called.
     *
     * @return a newly created <CODE>XAConnection</CODE>
     *
     * @exception JMSException if the JMS provider fails to create an XA
     *                         connection due to some internal error.
     * @exception JMSSecurityException  if client authentication fails due to
     *                         an invalid user name or password.
     *
     * @since 1.1
     */

    XAConnection createXAConnection() throws JMSException;


    /** Creates an XA  connection with the specified user identity.
     * The connection is created in stopped mode. No messages
     * will be delivered until the <code>Connection.start</code> method
     * is explicitly called.
     *
     * @param userName the caller's user name
     * @param password the caller's password
     *
     * @return a newly created XA connection
     *
     * @exception JMSException if the JMS provider fails to create an XA
     *                         connection due to some internal error.
     * @exception JMSSecurityException  if client authentication fails due to
     *                         an invalid user name or password.
     *
     * @since 1.1
     */

    XAConnection createXAConnection(String userName, String password) throws JMSException;
}
