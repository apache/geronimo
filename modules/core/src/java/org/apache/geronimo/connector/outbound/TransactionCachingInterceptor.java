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

import java.util.WeakHashMap;
import java.util.Iterator;
import java.util.Collection;
import java.util.LinkedList;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.SystemException;
import javax.resource.ResourceException;

import org.apache.geronimo.connector.TxUtils;

/**
 * TransactionCachingInterceptor.java
 *
 *
 * Created: Mon Sep 29 15:07:07 2003
 *
 * @version 1.0
 */
public class TransactionCachingInterceptor implements ConnectionInterceptor {

    private final ConnectionInterceptor next;
    private final TransactionManager tm;
    private final WeakHashMap txToMCIListMap = new WeakHashMap();

    public TransactionCachingInterceptor(final ConnectionInterceptor next, final TransactionManager tm) {
        this.next = next;
        this.tm = tm;
    } // TransactionCachingInterceptor constructor

    public void getConnection(ConnectionInfo ci) throws ResourceException {
        try {
            Transaction tx = tm.getTransaction();
            if (TxUtils.isActive(tx)) {
                ManagedConnectionInfo mci = ci.getManagedConnectionInfo();
                Collection mcis = null;
                synchronized (txToMCIListMap) {
                    mcis = (Collection) txToMCIListMap.get(tx);
                }
                /*Access to mcis should not need to be synchronized
                 * unless several requests in the same transaction in
                 * different threads are being processed at the same
                 * time.  This cannot occur with transactions imported
                 * through jca.  I don't know about any other possible
                 * ways this could occur.*/
                if (mcis != null) {
                    for (Iterator i = mcis.iterator(); i.hasNext();) {
                        ManagedConnectionInfo oldmci = (ManagedConnectionInfo) i.next();
                        if (mci.securityMatches(oldmci)) {
                            ci.setManagedConnectionInfo(oldmci);
                            return;
                        } // end of if ()

                    } // end of for ()

                } // end of if ()
                else {
                    mcis = new LinkedList();
                    synchronized (txToMCIListMap) {
                        txToMCIListMap.put(tx, mcis);
                    }
                } // end of else
                next.getConnection(ci);
                //put it in the map
                synchronized (mcis) {
                    mcis.add(ci.getManagedConnectionInfo());
                }

            } // end of if ()
            else {
                next.getConnection(ci);
            } // end of else

        } catch (SystemException e) {
            throw new ResourceException("Could not get transaction from transaction manager", e);
        } // end of try-catch

    }

    public void returnConnection(ConnectionInfo ci, ConnectionReturnAction cra) {

        try {
            Transaction tx = tm.getTransaction();
            if (cra == ConnectionReturnAction.DESTROY || !TxUtils.isActive(tx)) {
                next.returnConnection(ci, cra);
            }
            //if tx is active, we keep it cached and do nothing.
        } catch (SystemException e) {
            //throw new ResourceException("Could not get transaction from transaction manager", e);
        } // end of try-catch

    }

} // TransactionCachingInterceptor
