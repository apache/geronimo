package org.apache.geronimo.transaction;

import java.io.Serializable;
import javax.transaction.UserTransaction;
import javax.transaction.SystemException;
import javax.transaction.NotSupportedException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.resource.ResourceException;

import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.context.TransactionContext;
import org.apache.geronimo.transaction.context.BeanTransactionContext;
import org.apache.geronimo.transaction.context.UnspecifiedTransactionContext;

/**
 */
public final class OnlineUserTransaction implements UserTransaction, Serializable {
    private transient TransactionContextManager transactionContextManager;
    private transient TrackedConnectionAssociator trackedConnectionAssociator;
    private long transactionTimeoutMilliseconds = 0L;

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
        transactionTimeoutMilliseconds = seconds * 1000;
    }

    public void begin() throws NotSupportedException, SystemException {
        transactionContextManager.newBeanTransactionContext(transactionTimeoutMilliseconds);

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
            beanContext.commit();
        } finally {
            UnspecifiedTransactionContext oldContext = beanContext.getOldContext();
            transactionContextManager.setContext(oldContext);
            oldContext.resume();
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
            UnspecifiedTransactionContext oldContext = beanContext.getOldContext();
            transactionContextManager.setContext(oldContext);
            oldContext.resume();
        }
    }
}
