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

import javax.transaction.xa.XAResource;

/** The <CODE>XASession</CODE> interface extends the capability of
 * <CODE>Session</CODE> by adding access to a JMS provider's support for the
 * Java Transaction API (JTA) (optional). This support takes the form of a
 * <CODE>javax.transaction.xa.XAResource</CODE> object. The functionality of
 * this object closely resembles that defined by the standard X/Open XA
 * Resource interface.
 *
 * <P>An application server controls the transactional assignment of an
 * <CODE>XASession</CODE> by obtaining its <CODE>XAResource</CODE>. It uses
 * the <CODE>XAResource</CODE> to assign the session to a transaction, prepare
 * and commit work on the transaction, and so on.
 *
 * <P>An <CODE>XAResource</CODE> provides some fairly sophisticated facilities
 * for interleaving work on multiple transactions, recovering a list of
 * transactions in progress, and so on. A JTA aware JMS provider must fully
 * implement this functionality. This could be done by using the services
 * of a database that supports XA, or a JMS provider may choose to implement
 * this functionality from scratch.
 *
 * <P>A client of the application server is given what it thinks is a
 * regular JMS <CODE>Session</CODE>. Behind the scenes, the application server
 * controls the transaction management of the underlying
 * <CODE>XASession</CODE>.
 *
 * <P>The <CODE>XASession</CODE> interface is optional.  JMS providers
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
 * @see         javax.jms.Session
 */

public interface XASession extends Session {

    /** Gets the session associated with this <CODE>XASession</CODE>.
     *
     * @return the  session object
     *
     * @exception JMSException if an internal error occurs.
     *
     * @since 1.1
     */

    Session getSession() throws JMSException;

    /** Returns an XA resource to the caller.
     *
     * @return an XA resource to the caller
     */

    XAResource getXAResource();

    /** Indicates whether the session is in transacted mode.
     *
     * @return true
     *
     * @exception JMSException if the JMS provider fails to return the
     *                         transaction mode due to some internal error.
     */

    boolean getTransacted() throws JMSException;


    /** Throws a <CODE>TransactionInProgressException</CODE>, since it should
     * not be called for an <CODE>XASession</CODE> object.
     *
     * @exception TransactionInProgressException if the method is called on
     *                         an <CODE>XASession</CODE>.
     *
     */

    void commit() throws JMSException;


    /** Throws a <CODE>TransactionInProgressException</CODE>, since it should
     * not be called for an <CODE>XASession</CODE> object.
     *
     * @exception TransactionInProgressException if the method is called on
     *                         an <CODE>XASession</CODE>.
     *
     */

    void rollback() throws JMSException;
}
