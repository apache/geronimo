/**
 *
 * Copyright 2006 The Apache Software Foundation
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
package org.apache.geronimo.spring.transaction;

import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.NestedTransactionNotSupportedException;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.HeuristicCompletionException;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.context.TransactionContext;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.proxy.ProxyManager;

import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.Synchronization;
import javax.transaction.Status;
import java.util.List;
import java.util.Iterator;

/**
 * Wrap our TransactionContextManager in a spring idea of a transaction manager.
 *
 * @version $Rev:$ $Date:$
 */
public class SpringTransactionManager extends AbstractPlatformTransactionManager {

    private final TransactionContextManager transactionContextManager;


    /**
     * Construct the spring transaction manager wrapper around our transaction context manager.
     * @param kernelName normally "geronimo"
     * @param transactionContextManagerName normally "geronimo.server:J2EEApplication=null,J2EEModule=geronimo/j2ee-server/1.1-SNAPSHOT/car,J2EEServer=geronimo,j2eeType=TransactionContextManager,name=TransactionContextManager"
     * although the version may change.
     * @throws MalformedObjectNameException
     */
    public SpringTransactionManager(String kernelName, String transactionContextManagerName) throws MalformedObjectNameException {

        ObjectName transactionContextManagerObjectName = ObjectName.getInstance(transactionContextManagerName);
        Kernel kernel = KernelRegistry.getKernel(kernelName);
        ProxyManager proxyManager = kernel.getProxyManager();
        transactionContextManager = (TransactionContextManager) proxyManager.createProxy(transactionContextManagerObjectName, TransactionContextManager.class);

    }

    protected Object doGetTransaction() throws TransactionException {
        return new TransactionContextWrapper(transactionContextManager.getContext());
    }

    protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
        TransactionContextWrapper transactionContextWrapper = (TransactionContextWrapper) transaction;
        int timeoutSeconds = definition.getTimeout();
        TransactionContext newTransactionContext;
        try {
            newTransactionContext = transactionContextManager.newBeanTransactionContext(timeoutSeconds * 1000);
        } catch (NotSupportedException e) {
            throw new NestedTransactionNotSupportedException("Nested Transaction not supported", e);
        } catch (SystemException e) {
            throw new TransactionSystemException("System exception", e);
        }
        transactionContextWrapper.setTransactionContext(newTransactionContext);
    }

    protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
        TransactionContextWrapper transactionContextWrapper = (TransactionContextWrapper) status.getTransaction();
        if (transactionContextWrapper == null) {
            throw new IllegalTransactionStateException("No transaction present in statns");
        }
        TransactionContext transactionContext = transactionContextWrapper.getTransactionContext();
        try {
            transactionContext.commit();
        } catch (HeuristicMixedException e) {
             throw new HeuristicCompletionException(HeuristicCompletionException.STATE_MIXED, e);
        } catch (HeuristicRollbackException e) {
            throw new HeuristicCompletionException(HeuristicCompletionException.STATE_ROLLED_BACK, e);
        } catch (RollbackException e) {
            throw new UnexpectedRollbackException("Rollback exception", e);
        } catch (SystemException e) {
            throw new TransactionSystemException("System exception", e);
        }
        transactionContextWrapper.setTransactionContext(transactionContextManager.getContext());
    }

    protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
        TransactionContextWrapper transactionContextWrapper = (TransactionContextWrapper) status.getTransaction();
        if (transactionContextWrapper == null) {
            throw new IllegalTransactionStateException("No transaction present in statns");
        }
        TransactionContext transactionContext = transactionContextWrapper.getTransactionContext();
        try {
            transactionContext.rollback();
        } catch (SystemException e) {
            throw new TransactionSystemException("System exception", e);
        }
        transactionContextWrapper.setTransactionContext(transactionContextManager.getContext());
    }

    //stuff we actually need to overide the default implementations of

    protected boolean isExistingTransaction(Object transaction) throws TransactionException {
        TransactionContextWrapper transactionContextWrapper = (TransactionContextWrapper) transaction;
        TransactionContext transactionContext = transactionContextWrapper.getTransactionContext();
        return transactionContext.isActive();
    }

    protected boolean useSavepointForNestedTransaction() {
        return false;
    }

    protected Object doSuspend(Object transaction) throws TransactionException {
        TransactionContextWrapper transactionContextWrapper = (TransactionContextWrapper) transaction;
        TransactionContext transactionContext = transactionContextWrapper.getTransactionContext();
        try {
            transactionContext.suspend();
        } catch (SystemException e) {
            throw new TransactionSystemException("System exception", e);
        }
        return transactionContext;
    }

    protected void doResume(Object transaction, Object suspendedResources) throws TransactionException {
        TransactionContext transactionContext = (TransactionContext) suspendedResources;
        try {
            transactionContext.resume();
        } catch (SystemException e) {
            throw new TransactionSystemException("System exception", e);
        } catch (InvalidTransactionException e) {
            throw new IllegalTransactionStateException("Invalid transaction", e);
        }
        TransactionContextWrapper transactionContextWrapper = (TransactionContextWrapper) transaction;
        transactionContextWrapper.setTransactionContext(transactionContext);
     }

    protected void doSetRollbackOnly(DefaultTransactionStatus status) throws TransactionException {
        TransactionContextWrapper transactionContextWrapper = (TransactionContextWrapper) status.getTransaction();
        TransactionContext transactionContext = transactionContextWrapper.getTransactionContext();
        try {
            transactionContext.setRollbackOnly();
        } catch (SystemException e) {
            throw new TransactionSystemException("System exception", e);
        }
    }

    protected void registerAfterCompletionWithExistingTransaction(Object transaction, List synchronizations)
            throws TransactionException {
        TransactionContextWrapper transactionContextWrapper = (TransactionContextWrapper) transaction;
        TransactionContext transactionContext = transactionContextWrapper.getTransactionContext();
        Synchronization synchronization = new AfterCompletionSynchronization(synchronizations);
        try {
            transactionContext.registerSynchronization(synchronization);
        } catch (RollbackException e) {
            throw new UnexpectedRollbackException("Rollback exception", e);
        } catch (SystemException e) {
            throw new TransactionSystemException("System exception", e);
        }
    }

    private static class AfterCompletionSynchronization implements Synchronization {

        private final List synchronizations;

        public AfterCompletionSynchronization(List synchronizations) {
            this.synchronizations = synchronizations;
        }

        public void beforeCompletion() {
        }

        public void afterCompletion(int status) {
            if (status == Status.STATUS_COMMITTED) {
                springAfterCompletion(TransactionSynchronization.STATUS_COMMITTED);
            } else if (status == Status.STATUS_ROLLEDBACK) {
                springAfterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
            } else {
                throw new IllegalArgumentException("Unexpected status in afterCompletion: " + status);
            }
        }

        private void springAfterCompletion(int statusCommitted) {
            for (Iterator iterator = synchronizations.iterator(); iterator.hasNext();) {
                TransactionSynchronization transactionSynchronization = (TransactionSynchronization) iterator.next();
                try {
                    transactionSynchronization.afterCompletion(statusCommitted);
                } catch(Throwable t) {
                    //ignore??
                }
            }
        }
    }

    /**
     * The spring javadoc does not make clear to me if this class is needed (spring transaction object is expected to
     * change which transaction it represents) or superfluous (a new spring transaction object is created whenever
     * a new transaction begins).
     */
    private static class TransactionContextWrapper {
        private TransactionContext transactionContext;

        protected TransactionContextWrapper(TransactionContext transactionContext) {
            this.transactionContext = transactionContext;
        }

        protected TransactionContext getTransactionContext() {
            return transactionContext;
        }

        protected void setTransactionContext(TransactionContext transactionContext) {
            this.transactionContext = transactionContext;
        }
    }
}
