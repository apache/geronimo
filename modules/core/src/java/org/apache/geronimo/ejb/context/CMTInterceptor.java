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
package org.apache.geronimo.ejb.context;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import javax.ejb.EJBException;
import javax.ejb.TransactionRequiredLocalException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionRequiredException;

import org.apache.geronimo.ejb.EJBInvocationUtil;
import org.apache.geronimo.common.Invocation;
import org.apache.geronimo.common.InvocationResult;
import org.apache.geronimo.common.InvocationType;
import org.apache.geronimo.ejb.container.EJBPlugins;
import org.apache.geronimo.ejb.metadata.EJBMetadata;
import org.apache.geronimo.ejb.metadata.TransactionAttribute;
import org.apache.geronimo.ejb.metadata.MethodMetadata;
import org.apache.geronimo.transaction.GeronimoRollbackException;
import org.apache.geronimo.transaction.GeronimoTransactionRolledbackException;
import org.apache.geronimo.transaction.GeronimoTransactionRolledbackLocalException;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/10 20:51:54 $
 */
public final class CMTInterceptor extends ExecutionContextInterceptor {
    private EJBMetadata ejbMetadata;

    public void start() throws Exception {
        super.start();
        ejbMetadata = EJBPlugins.getEJBMetadata(getContainer());
    }

    public void stop() {
        ejbMetadata = null;
        super.stop();
    }

    public InvocationResult invoke(Invocation invocation) throws Exception {
        Method m = EJBInvocationUtil.getMethod(invocation);
        if (m == null) {
            // we are not invoking a method (e.g. its a CMR message) so pass straight through
            return getNext().invoke(invocation);
        }
        MethodMetadata methodMetadata = ejbMetadata.getMethodMetadata(m);
        TransactionAttribute transactionAttribute = null;
        if(methodMetadata != null) {
            transactionAttribute = methodMetadata.getTransactionAttribute();
        }

        if (transactionAttribute == null) {
            return getNext().invoke(invocation);
        }

        Transaction oldTransaction = getTransaction();
        boolean inTransaction = (oldTransaction != null);
        InvocationType type = InvocationType.getType(invocation);

        if (transactionAttribute == TransactionAttribute.NOT_SUPPORTED) {
            // suspend any current transaction and invoke without one
            return noTxContext(invocation, oldTransaction);
        } else if (transactionAttribute == TransactionAttribute.UNSPECIFIED ||
                transactionAttribute == TransactionAttribute.REQUIRED) {
            // if we have a transaction use it otherwise start one
            if (inTransaction) {
                return sameTxContext(invocation, oldTransaction);
            } else {
                return newTxContext(invocation, oldTransaction);
            }
        } else if (transactionAttribute == TransactionAttribute.SUPPORTS) {
            // use the existing context
            if (inTransaction) {
                return sameTxContext(invocation, oldTransaction);
            } else {
                return noTxContext(invocation, oldTransaction);
            }
        } else if (transactionAttribute == TransactionAttribute.REQUIRES_NEW) {
            // always start a new transaction
            return newTxContext(invocation, oldTransaction);
        } else if (transactionAttribute == TransactionAttribute.MANDATORY) {
            // we must have a transaction, otherwise error
            if (inTransaction) {
                return sameTxContext(invocation, oldTransaction);
            } else {
                if (type.isLocalInvocation()) {
                    throw new TransactionRequiredLocalException("Transaction is mandatory");
                } else {
                    throw new TransactionRequiredException("Transaction is mandatory");
                }
            }
        } else if (transactionAttribute == TransactionAttribute.NEVER) {
            // we must not have a transaction, if we do error
            if (inTransaction) {
                if (type.isLocalInvocation()) {
                    throw new EJBException("Transaction present and method is marked NEVER");
                } else {
                    throw new RemoteException("Transaction present and method is marked NEVER");
                }
            } else {
                return noTxContext(invocation, oldTransaction);
            }
        } else {
            // we should never get here because the above is a type safe enumeration, but someone may
            // hack in another value, so be safe...
            throw new AssertionError();
        }
    }

    /**
     * Invoke the next interceptor with a new TxExecutionContext and a new Transaction.
     * If the current Thread is associated with a Transaction, it will be suspended and
     * a new transactional context created.
     * @param invocation the invocation to pass down
     * @return the result from the interceptor
     * @throws Exception from the next interceptor
     * @throws EJBTransactionException if there was a problem interacting with the TransactionManager
     */
    private InvocationResult newTxContext(Invocation invocation, Transaction oldTransaction) throws EJBTransactionException, Exception {
        if (oldTransaction == null) {
            // we have no transaction, start the new one
            return invokeWithNewTx(invocation);
        } else {
            // we have a transaction, so suspend it first and then start a new one
            suspend();
            try {
                return invokeWithNewTx(invocation);
            } finally {
                resume(oldTransaction);
            }
        }
    }

    /**
     * Invoke the next interceptor with a new TxExecutionContext and a new Transaction.
     * There must not be a Transaction currently associated with the Thread
     * @param invocation the invocation to pass down
     * @return the result from the interceptor
     * @throws Exception from the next interceptor
     * @throws EJBTransactionException if there was a problem interacting with the TransactionManager
     */
    private InvocationResult invokeWithNewTx(Invocation invocation) throws Exception {
        TxExecutionContext newContext = new TxExecutionContext(tm);
        InvocationResult result;
        try {
            newContext.startTransaction();
        } catch (Exception e) {
            // [EJB2.0 18.3.6 pp 379] failed to start so throw appropriate exception
            throw wrapTransactionException(invocation, e);
        }

        try {
            result = invokeNext(newContext, invocation);
        } catch (Error e) {
            systemException(newContext, e);
            throw e;
        } catch (RuntimeException e) {
            systemException(newContext, e);
            throw e;
        } catch (RemoteException e) {
            systemException(newContext, e);
            throw e;
        } catch (Exception e) {
            // application exception
            endTransaction(invocation, newContext);
            throw e;
        }
        endTransaction(invocation, newContext);
        return result;
    }

    /**
     * Handle a system exception returned by the interceptor chain
     * @param newContext the current execution context
     * @param t the system exception
     */
    private void systemException(TxExecutionContext newContext, Throwable t) {
        try {
            tm.setRollbackOnly();
            tm.rollback();
        } catch (SystemException e) {
            log.error("Unable to roll back after system exception, continuing", e);
        } finally {
            newContext.abnormalTermination(t);
        }
    }

    /**
     * End the current transaction normally. This will rollback if the transaction is
     * marked for rollback, otherwise it tries to commit. Any errors result in
     * an abnormal termination for the context
     * @param invocation the current invocation
     * @param newContext the current execution context
     * @throws Exception if the commit or rollback failed
     */
    private void endTransaction(Invocation invocation, TxExecutionContext newContext) throws Exception {
        Transaction tx = newContext.getTransaction();
        try {
            if (tx.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                tm.rollback();
            } else {
                tm.commit();
            }
            newContext.normalTermination();
        } catch (Exception e) {
            // [EJB2.0 18.3.6 pp 379] failed to commit/rollback so throw appropriate exception
            // This overrides the normal return of the result or app exception defined in [EJB2.0 18.3.1]
            newContext.abnormalTermination(e);
            throw wrapTransactionException(invocation, e);
        }
    }

    /**
     * Invoke the next interceptor with a new NoTxExecutionContext. If the current Thread
     * is associated with a Transaction, it will be suspended.
     * @param invocation the invocation to pass down
     * @return the result from the interceptor
     * @throws Exception from the next interceptor
     * @throws EJBTransactionException if there was a problem interacting with the TransactionManager
     */
    private InvocationResult noTxContext(Invocation invocation, Transaction oldTransaction) throws EJBTransactionException, Exception {
        if (oldTransaction == null) {
            // we have no transaction, so just invoke
            return invokeWithNoTx(invocation);
        } else {
            // we have a transaction, so suspend it and then invoke without one
            suspend();
            try {
                return invokeWithNoTx(invocation);
            } finally {
                resume(oldTransaction);
            }
        }
    }

    /**
     * Create a new non-transactional context and then invoke the next interceptor
     * @param invocation the invocation to pass down
     * @return the result from the interceptor
     * @throws Exception from the next interceptor
     */
    private InvocationResult invokeWithNoTx(Invocation invocation) throws Exception {
        NoTxExecutionContext newContext = new NoTxExecutionContext();
        try {
            return invokeNext(newContext, invocation);
        } catch (Error e) {
            newContext.abnormalTermination(e);
            throw e;
        } catch (RuntimeException e) {
            newContext.abnormalTermination(e);
            throw e;
        } catch (RemoteException e) {
            newContext.abnormalTermination(e);
            throw e;
        } catch (Exception e) {
            newContext.normalTermination();
            throw e;
        } finally {
            newContext.normalTermination();
        }
    }

    /**
     * Invoke the next interceptor in the same context as we were invoked.
     * Will associate the appropriate ExecutionContext with the current Thread
     * if it does not already have one.
     * @param invocation the invocation to pass down
     * @return the result from the interceptor
     * @throws Exception from the next interceptor
     * @throws EJBTransactionException if we could not register a new TxExecutionContext with the TransactionManager
     */
    private InvocationResult sameTxContext(Invocation invocation, Transaction oldTransaction) throws EJBTransactionException, Exception {
        TxExecutionContext context = TxExecutionContext.getContext(oldTransaction);
        if (context == null) {
            assert (oldTransaction != null);
            try {
                context = new TxExecutionContext(tm, oldTransaction);
            } catch (RollbackException e) {
                throw wrapTransactionException(invocation, e);
            } catch (SystemException e) {
                throw new EJBTransactionException("Unable to register with Transaction", e);
            }
        } else {
            assert (context.getTransaction() == oldTransaction);
        }
        return invokeNext(context, invocation);
    }

    /**
     * Wrap an Exception thrown during start or commit in a RemoteException or EJBException.
     * We use the appropriate GeronimoTransactionRolledback sub-class so that the caller can
     * extract the root cause of the rollback if needed.
     * @param invocation the current invocation
     * @param e the exception thrown during start or commit
     * @return a GeronimoTransactionRolledbackException if the invocation is remote,
     *         a GeronimoTransactionRolledbackLocalException if the invocation is local
     */
    private Exception wrapTransactionException(Invocation invocation, Exception e) {
        if (e instanceof GeronimoRollbackException) {
            GeronimoRollbackException jre = (GeronimoRollbackException) e;
            if (jre.getCause() instanceof Exception) {
                e = (Exception) jre.getCause();
            }
        }
        if (InvocationType.getType(invocation).isLocalInvocation()) {
            return new GeronimoTransactionRolledbackLocalException(e);
        } else {
            return new GeronimoTransactionRolledbackException(e);
        }
    }
}
