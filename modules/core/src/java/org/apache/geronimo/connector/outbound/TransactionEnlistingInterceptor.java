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

package org.apache.geronimo.connector.outbound;

import javax.resource.ResourceException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.apache.geronimo.connector.TxUtils;

/**
 * TransactionEnlistingInterceptor.java
 *
 *
 * Created: Fri Sep 26 14:52:24 2003
 *
 * @version 1.0
 */
public class TransactionEnlistingInterceptor implements ConnectionInterceptor {

    private final ConnectionInterceptor next;
    private final TransactionManager tm;

    public TransactionEnlistingInterceptor(
            ConnectionInterceptor next,
            TransactionManager tm) {
        this.next = next;
        this.tm = tm;
    } // TransactionEnlistingInterceptor constructor

    public void getConnection(ConnectionInfo ci) throws ResourceException {
        next.getConnection(ci);
        try {
            Transaction tx = tm.getTransaction();
            if (TxUtils.isActive(tx)) {
                ManagedConnectionInfo mci = ci.getManagedConnectionInfo();
                XAResource xares = mci.getXAResource();
                tx.enlistResource(xares);
                mci.setTransaction(tx);
            } // end of if ()

        } catch (SystemException e) {
            throw new ResourceException("Could not get transaction", e);
        } // end of try-catch
        catch (RollbackException e) {
            throw new ResourceException(
                    "Could not enlist resource in rolled back transaction",
                    e);
        } // end of catch

    }

    /**
     * The <code>returnConnection</code> method
     *
     * @todo Probably the logic needs improvement if a connection
     * error occurred and we are destroying the handle.
     * @param ci a <code>ConnectionInfo</code> value
     * @param cra a <code>ConnectionReturnAction</code> value
     * @exception ResourceException if an error occurs
     */
    public void returnConnection(
            ConnectionInfo ci,
            ConnectionReturnAction cra) {
        try {
            Transaction tx = tm.getTransaction();
            if (TxUtils.isActive(tx)) {
                ManagedConnectionInfo mci = ci.getManagedConnectionInfo();
                XAResource xares = mci.getXAResource();
                tx.delistResource(xares, XAResource.TMSUSPEND);
                mci.setTransaction(null);
            } // end of if ()

        } catch (SystemException e) {
            //throw new ResourceException("Could not get transaction", e);
        } // end of try-catch

        next.returnConnection(ci, cra);
    }

} // TransactionEnlistingInterceptor
