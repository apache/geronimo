/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.transaction;

import javax.transaction.Transaction;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Synchronization;
import javax.transaction.xa.XAResource;

/**
 * Our version of a JTA Transaction which also carries metadata such as isolation level
 * and write-intent. Delegates all Transaction methods to a real Transaction obtained from
 * the vendor's TransactionManager.
 *
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/10 09:59:36 $
 */
public class TransactionProxy implements Transaction {
    private final Transaction delegate;

    public TransactionProxy(Transaction delegate) {
        this.delegate = delegate;
    }

    public void commit() throws HeuristicMixedException, HeuristicRollbackException, RollbackException, SecurityException, SystemException {
        delegate.commit();
    }

    public boolean delistResource(XAResource xaResource, int i) throws IllegalStateException, SystemException {
        return delegate.delistResource(xaResource, i);
    }

    public boolean enlistResource(XAResource xaResource) throws IllegalStateException, RollbackException, SystemException {
        return delegate.enlistResource(xaResource);
    }

    public int getStatus() throws SystemException {
        return delegate.getStatus();
    }

    public void registerSynchronization(Synchronization synchronization) throws IllegalStateException, RollbackException, SystemException {
        delegate.registerSynchronization(synchronization);
    }

    public void rollback() throws IllegalStateException, SystemException {
        delegate.rollback();
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        delegate.setRollbackOnly();
    }

    Transaction getDelegate() {
        return delegate;
    }
}
