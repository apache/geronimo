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

package org.apache.geronimo.transaction.manager;

import java.io.Serializable;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

//import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;

/**
 * TODO this needs to migrate to using TransactionContext.
 * TODO this needs to notify the TrackedConnectionAssociator when a tx starts.
 *
 *
 * @version $Revision: 1.5 $ $Date: 2004/03/10 09:59:37 $
 */
public class UserTransactionImpl implements UserTransaction, Serializable {
    private transient TransactionManager transactionManager;
//    private transient TrackedConnectionAssociator trackedConnectionAssociator;

    public UserTransactionImpl() {
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

//    public TrackedConnectionAssociator getTrackedConnectionAssociator() {
//        return trackedConnectionAssociator;
//    }
//
//    public void setTrackedConnectionAssociator(TrackedConnectionAssociator trackedConnectionAssociator) {
//        this.trackedConnectionAssociator = trackedConnectionAssociator;
//    }

    public void begin() throws NotSupportedException, SystemException {
        checkState();
        transactionManager.begin();
    }

    public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
        checkState();
        transactionManager.commit();
    }

    public int getStatus() throws SystemException {
        checkState();
        return transactionManager.getStatus();
    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        checkState();
        transactionManager.rollback();
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        checkState();
        transactionManager.setRollbackOnly();
    }

    public void setTransactionTimeout(int timeout) throws SystemException {
        checkState();
        transactionManager.setTransactionTimeout(timeout);
    }

    private void checkState() {
//        if (transactionManager == null || trackedConnectionAssociator == null) {
//            throw new IllegalStateException("UserTransaction is disabled");
//        }
    }
}
