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
import java.util.Map;
import java.util.HashMap;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.SystemException;
import javax.transaction.Synchronization;
import javax.transaction.RollbackException;
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
    private final Map txToConnectionList = new HashMap();

    public TransactionCachingInterceptor(final ConnectionInterceptor next, final TransactionManager tm) {
        this.next = next;
        this.tm = tm;
    }

    public void getConnection(ConnectionInfo ci) throws ResourceException {
        try {
            Transaction tx = tm.getTransaction();
            if (TxUtils.isActive(tx)) {
                ManagedConnectionInfo mci = ci.getManagedConnectionInfo();
                Collection mcis;
                synchronized (txToConnectionList) {
                    mcis = (Collection)txToConnectionList.get(tx);
                    if (mcis == null) {
                        mcis = new LinkedList();
                        txToConnectionList.put(tx, mcis);
                        tx.registerSynchronization(new Synch(tx, this, mcis));
                    }
                }

                /*Access to mcis should not need to be synchronized
                 * unless several requests in the same transaction in
                 * different threads are being processed at the same
                 * time.  This cannot occur with transactions imported
                 * through jca.  I don't know about any other possible
                 * ways this could occur.*/
                for (Iterator i = mcis.iterator(); i.hasNext();) {
                    ManagedConnectionInfo oldmci = (ManagedConnectionInfo) i.next();
                    if (mci.securityMatches(oldmci)) {
                        ci.setManagedConnectionInfo(oldmci);
                        return;
                    }

                }

                next.getConnection(ci);
                //put it in the map
                mcis.add(ci.getManagedConnectionInfo());

            } else {
                next.getConnection(ci);
            }
        } catch (SystemException e) {
            throw new ResourceException("Could not get transaction from transaction manager", e);
        } catch (RollbackException e) {
            throw new ResourceException("Transaction is rolled back, can't enlist synchronization", e);
        }
    }

    public void returnConnection(ConnectionInfo ci, ConnectionReturnAction cra) {

        try {
            if (cra == ConnectionReturnAction.DESTROY) {
                next.returnConnection(ci, cra);
            }

            Transaction tx = tm.getTransaction();
            if (TxUtils.isActive(tx)) {
                return;
            }
            if (ci.getManagedConnectionInfo().hasConnectionHandles()) {
                return;
            }
            //No transaction, no handles, we return it.
            next.returnConnection(ci, cra);
        } catch (SystemException e) {
            //throw new ResourceException("Could not get transaction from transaction manager", e);
        }

    }


    public void afterCompletion(Transaction tx) {
        Collection connections = (Collection) txToConnectionList.get(tx);
        if (connections != null) {
            for (Iterator iterator = connections.iterator(); iterator.hasNext();) {
                ManagedConnectionInfo managedConnectionInfo = (ManagedConnectionInfo) iterator.next();
                ConnectionInfo connectionInfo = new ConnectionInfo();
                connectionInfo.setManagedConnectionInfo(managedConnectionInfo);
                returnConnection(connectionInfo, ConnectionReturnAction.RETURN_HANDLE);
            }
        }

    }

    private static class Synch implements Synchronization {

        private final Transaction transaction;
        private final TransactionCachingInterceptor returnStack;
        private final Collection connections;

        public Synch(Transaction transaction, TransactionCachingInterceptor returnStack, Collection connections) {
            this.transaction = transaction;
            this.returnStack = returnStack;
            this.connections = connections;
        }

        public void beforeCompletion() {
        }

        public void afterCompletion(int status) {
            for (Iterator iterator = connections.iterator(); iterator.hasNext();) {
                ManagedConnectionInfo managedConnectionInfo = (ManagedConnectionInfo) iterator.next();
                iterator.remove();
                if (!managedConnectionInfo.hasConnectionHandles()) {
                    returnStack.returnConnection(new ConnectionInfo(managedConnectionInfo), ConnectionReturnAction.RETURN_HANDLE);
                }
            }
        }

    }

}
