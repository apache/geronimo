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
import javax.ejb.EJBException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.apache.geronimo.common.Invocation;
import org.apache.geronimo.common.InvocationResult;
import org.apache.geronimo.common.InvocationType;
import org.apache.geronimo.ejb.container.EJBPlugins;

/**
 *
 *
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/10 20:51:54 $
 */
public final class StatelessBMTInterceptor extends ExecutionContextInterceptor {
    private String ejbName;

    public void start() throws Exception {
        super.start();
        ejbName = EJBPlugins.getEJBMetadata(getContainer()).getName();
    }

    public InvocationResult invoke(Invocation invocation) throws Exception {
        // suspend any transaction supplied by the client
        Transaction oldTransaction = suspend();
        InvocationResult result;
        try {
            NoTxExecutionContext noTxContext = new NoTxExecutionContext();
            ExecutionContext.push(noTxContext);
            try {
                try {
                    result = getNext().invoke(invocation);
                } finally {
                    checkStatelessCompletion(noTxContext, invocation);
                }
            } catch (Error e) {
                ExecutionContext.pop(noTxContext);
                noTxContext.abnormalTermination(e);
                throw e;
            } catch (RuntimeException e) {
                ExecutionContext.pop(noTxContext);
                noTxContext.abnormalTermination(e);
                throw e;
            } catch (RemoteException e) {
                ExecutionContext.pop(noTxContext);
                noTxContext.abnormalTermination(e);
                throw e;
            } catch (Exception e) {
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
     * Check that there was no transaction associated when the bean returned.
     * If one was, roll it back and throw the appropriate Exception
     * @param noTxContext the outer context (used for assertion)
     * @param invocation the invocation (used to determine which interface was called)
     * @throws Exception a RemoteException or EJBException reporting the error
     */
    private void checkStatelessCompletion(ExecutionContext noTxContext, Invocation invocation) throws Exception {
        int status;
        try {
            status = tm.getStatus();
        } catch (SystemException e) {
            throw new EJBTransactionException("Unable to determine transaction status", e);
        }

        if (status == Status.STATUS_NO_TRANSACTION) {
            return;
        }

        try {
            tm.rollback();
        } catch (Exception e) {
            log.error("Unable to roll back transaction", e);
        }

        Exception e;
        String msg = "Stateless EJB " + ejbName + " exited with associated transaction - rolling back";
        if (InvocationType.getType(invocation).isLocalInvocation()) {
            e = new EJBException(msg);
        } else {
            e = new RemoteException(msg);
        }

        // pop the TxExecutionContext added when they started the transaction
        ExecutionContext context = ExecutionContext.getContext();
        if (context instanceof TxExecutionContext) {
            ExecutionContext.pop(context);
            assert (ExecutionContext.getContext() == noTxContext);
            context.abnormalTermination(e);
        }

        throw e;
    }
}
