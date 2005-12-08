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

package org.apache.geronimo.transaction.context;

import java.io.Serializable;
import javax.resource.ResourceException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.geronimo.transaction.TrackedConnectionAssociator;

public final class OnlineUserTransaction implements UserTransaction, Serializable {
    private transient TransactionContextManager transactionContextManager;
    private transient TrackedConnectionAssociator trackedConnectionAssociator;

    boolean isActive() {
        return transactionContextManager != null;
    }

    public void setUp(TransactionContextManager transactionContextManager, TrackedConnectionAssociator trackedConnectionAssociator) {
        this.transactionContextManager = transactionContextManager;
        this.trackedConnectionAssociator = trackedConnectionAssociator;
    }


    public int getStatus() throws SystemException {
        return transactionContextManager.getStatus();
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        transactionContextManager.setRollbackOnly();
    }

    public void setTransactionTimeout(int seconds) throws SystemException {
        if (seconds < 0) {
            throw new SystemException("transaction timeout must be positive or 0, not " + seconds);
        }
        transactionContextManager.setTransactionTimeout(seconds);
    }

    public void begin() throws NotSupportedException, SystemException {
        transactionContextManager.newBeanTransactionContext(0L);

        if(trackedConnectionAssociator != null) {
            try {
                trackedConnectionAssociator.newTransaction();
            } catch (ResourceException e) {
                throw (SystemException)new SystemException().initCause(e);
            }
        }
    }

    public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
        TransactionContext ctx = transactionContextManager.getContext();
        if (ctx instanceof BeanTransactionContext == false) {
            throw new IllegalStateException("Transaction has not been started");
        }
        BeanTransactionContext beanContext = (BeanTransactionContext) ctx;
        try {
            if (!beanContext.commit()) {
                throw new RollbackException();
            }
        } finally {
            TransactionContext oldContext = beanContext.getOldContext();
            transactionContextManager.setContext(oldContext);
            try {
                oldContext.resume();
            } catch (InvalidTransactionException e) {
                throw (SystemException)new SystemException("Unable to resume perexisting transaction context").initCause(e);
            }
        }
    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        TransactionContext ctx = transactionContextManager.getContext();
        if (ctx instanceof BeanTransactionContext == false) {
            throw new IllegalStateException("Transaction has not been started");
        }
        BeanTransactionContext beanContext = (BeanTransactionContext) ctx;
        try {
            beanContext.rollback();
        } finally {
            TransactionContext oldContext = beanContext.getOldContext();
            transactionContextManager.setContext(oldContext);
            try {
                oldContext.resume();
            } catch (InvalidTransactionException e) {
                throw (SystemException)new SystemException("Unable to resume perexisting transaction context").initCause(e);
            }
        }
    }
}
