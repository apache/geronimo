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

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.EJBException;
import javax.transaction.Transaction;

import org.apache.geronimo.ejb.EJBInvocationUtil;
import org.apache.geronimo.common.Invocation;
import org.apache.geronimo.common.InvocationResult;

/**
 * Interceptor for Bean-Managed Transactions for Stateful Session Beans.
 * Allows a transaction that is still active when a method completes to be
 * re-associated with the invocation the next time the bean is invoked.
 *
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/10 20:51:54 $
 */
public final class StatefulBMTInterceptor extends ExecutionContextInterceptor {
    private final static Map savedTransactions = new HashMap();

    public InvocationResult invoke(Invocation invocation) throws Exception {
        InvocationResult result;
        Object id = EJBInvocationUtil.getId(invocation);
        // suspend any transaction supplied by the client
        Transaction oldTransaction = suspend();
        try {
            // push an outer, non-transactional context
            NoTxExecutionContext noTxContext = new NoTxExecutionContext();
            ExecutionContext.push(noTxContext);
            try {
                resumeInstanceTx(id);
                try {
                    result = getNext().invoke(invocation);
                } finally {
                    suspendInstanceTx(id);
                    assert (getTransaction() == null);
                }
            } catch (Error e) {
                // system exception
                ExecutionContext.pop(noTxContext);
                noTxContext.abnormalTermination(e);
                throw e;
            } catch (RuntimeException e) {
                // system exception
                ExecutionContext.pop(noTxContext);
                noTxContext.abnormalTermination(e);
                throw e;
            } catch (RemoteException e) {
                // system exception
                ExecutionContext.pop(noTxContext);
                noTxContext.abnormalTermination(e);
                throw e;
            } catch (Exception e) {
                // application exception
                ExecutionContext.pop(noTxContext);
                noTxContext.normalTermination();
                throw e;
            }
            ExecutionContext.pop(noTxContext);
            noTxContext.normalTermination();
            return result;
        } finally {
            if (oldTransaction != null) {
                // resume original transaction from client
                resume(oldTransaction);
            }
        }
    }

    /**
     * Locate any previous transaction for this bean
     * @param id the id of the bean
     */
    private void resumeInstanceTx(Object id) {
        TxExecutionContext txContext;
        synchronized (savedTransactions) {
            txContext = (TxExecutionContext) savedTransactions.remove(id);
        }
        if (txContext != null) {
            resume(txContext.getTransaction());
            ExecutionContext.push(txContext);
        }
    }

    /**
     * If the call returned with a transaction associated, then save it
     * so that it can be resumed the next time
     * @param id
     */
    private void suspendInstanceTx(Object id) {
        Transaction tx = getTransaction();
        if (tx != null) {
            TxExecutionContext context = TxExecutionContext.getContext(tx);
            if (context == null) {
                // we have not seen this one yet, maybe they started it directly and not through UserTransaction
                // anyway, we roll it back and error out
                try {
                    tx.rollback();
                } catch (Exception e) {
                    log.error("Unable to roll back transaction", e);
                }
                throw new EJBException("Invalid transaction");
            }

            ExecutionContext.pop(context);
            suspend();
            synchronized (savedTransactions) {
                savedTransactions.put(id, context);
            }
        }
    }
}
