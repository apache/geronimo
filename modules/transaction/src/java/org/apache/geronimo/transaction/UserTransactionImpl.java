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

import java.io.Serializable;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.resource.ResourceException;

import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.apache.geronimo.transaction.TransactionContext;
import org.apache.geronimo.transaction.UnspecifiedTransactionContext;
import org.apache.geronimo.transaction.BeanTransactionContext;

/**
 * Implementation of UserTransaction for use in an EJB.
 * This adds the ability to enable or disable the operations depending on
 * the lifecycle of the EJB instance.
 *
 * @version $Revision: 1.4 $ $Date: 2004/04/08 05:22:15 $
 */
public class UserTransactionImpl implements UserTransaction, Serializable {
    private transient TransactionManager txnManager;
    private transient TrackedConnectionAssociator trackedConnectionAssociator;

    private final ThreadLocal state = new StateThreadLocal();
    private static class StateThreadLocal extends ThreadLocal implements Serializable {
        protected Object initialValue() {
            return OFFLINE;
        }
    };

    public UserTransactionImpl() {
        state.set(OFFLINE);
    }

    public void setUp(TransactionManager txnManager, TrackedConnectionAssociator trackedConnectionAssociator) {
        assert !isOnline() : "Only set the tx manager when UserTransaction is offline";
        this.txnManager = txnManager;
        this.trackedConnectionAssociator = trackedConnectionAssociator;
    }

    public boolean isOnline() {
        return state.get() == ONLINE;
    }

    public void setOnline(boolean online) {
        //too bad there's no implies operation
        // online implies transactionManager != null
        assert !online || txnManager != null : "online requires a tx manager";
        state.set(online ? ONLINE : OFFLINE);
    }

    private UserTransaction getUserTransaction() {
        return (UserTransaction) state.get();
    }

    public void begin() throws NotSupportedException, SystemException {
        getUserTransaction().begin();
    }

    public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
        getUserTransaction().commit();
    }

    public int getStatus() throws SystemException {
        return getUserTransaction().getStatus();
    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        getUserTransaction().rollback();
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        getUserTransaction().setRollbackOnly();
    }

    public void setTransactionTimeout(int timeout) throws SystemException {
        getUserTransaction().setTransactionTimeout(timeout);
    }

    private final UserTransaction ONLINE = new OnlineUserTransaction();
    private final class OnlineUserTransaction implements UserTransaction, Serializable {
        public int getStatus() throws SystemException {
            return txnManager.getStatus();
        }

        public void setRollbackOnly() throws IllegalStateException, SystemException {
            txnManager.setRollbackOnly();
        }

        public void setTransactionTimeout(int seconds) throws SystemException {
            txnManager.setTransactionTimeout(seconds);
        }

        public void begin() throws NotSupportedException, SystemException {
            TransactionContext ctx = TransactionContext.getContext();
            if (ctx instanceof UnspecifiedTransactionContext == false) {
                throw new NotSupportedException("Previous Transaction has not been committed");
            }
            UnspecifiedTransactionContext oldContext = (UnspecifiedTransactionContext) ctx;
            BeanTransactionContext newContext = new BeanTransactionContext(txnManager, oldContext);
            oldContext.suspend();
            try {
                newContext.begin();
            } catch (SystemException e) {
                oldContext.resume();
                throw e;
            } catch (NotSupportedException e) {
                oldContext.resume();
                throw e;
            }
            TransactionContext.setContext(newContext);

            if(trackedConnectionAssociator != null) {
                try {
                    trackedConnectionAssociator.newTransaction();
                } catch (ResourceException e) {
                    throw (SystemException)new SystemException().initCause(e);
                }
            }
        }

        public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
            TransactionContext ctx = TransactionContext.getContext();
            if (ctx instanceof BeanTransactionContext == false) {
                throw new IllegalStateException("Transaction has not been started");
            }
            BeanTransactionContext beanContext = (BeanTransactionContext) ctx;
            try {
                beanContext.commit();
            } finally {
                UnspecifiedTransactionContext oldContext = beanContext.getOldContext();
                TransactionContext.setContext(oldContext);
                oldContext.resume();
            }
        }

        public void rollback() throws IllegalStateException, SecurityException, SystemException {
            TransactionContext ctx = TransactionContext.getContext();
            if (ctx instanceof BeanTransactionContext == false) {
                throw new IllegalStateException("Transaction has not been started");
            }
            BeanTransactionContext beanContext = (BeanTransactionContext) ctx;
            try {
                beanContext.rollback();
            } finally {
                UnspecifiedTransactionContext oldContext = beanContext.getOldContext();
                TransactionContext.setContext(oldContext);
                oldContext.resume();
            }
        }
    };

    private static final UserTransaction OFFLINE = new OfflineUserTransaction();
    private static final class OfflineUserTransaction implements UserTransaction, Serializable {
        public void begin() throws NotSupportedException, SystemException {
            throw new IllegalStateException("Cannot use UserTransaction methods in this state");
        }

        public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
            throw new IllegalStateException("Cannot use UserTransaction methods in this state");
        }

        public int getStatus() throws SystemException {
            throw new IllegalStateException("Cannot use UserTransaction methods in this state");
        }

        public void rollback() throws IllegalStateException, SecurityException, SystemException {
            throw new IllegalStateException("Cannot use UserTransaction methods in this state");
        }

        public void setRollbackOnly() throws IllegalStateException, SystemException {
            throw new IllegalStateException("Cannot use UserTransaction methods in this state");
        }

        public void setTransactionTimeout(int seconds) throws SystemException {
            throw new IllegalStateException("Cannot use UserTransaction methods in this state");
        }
    };
}
