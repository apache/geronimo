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
package org.apache.geronimo.jetty.interceptor;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.apache.geronimo.transaction.context.InheritableTransactionContext;
import org.apache.geronimo.transaction.context.TransactionContext;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;

/**
 * @version $Rev:  $ $Date:  $
 */
public class TransactionContextBeforeAfter implements BeforeAfter {
    
    private final BeforeAfter next;
    private final int oldTxIndex;
    private final int newTxIndex;
    private final TransactionContextManager transactionContextManager;

    public TransactionContextBeforeAfter(BeforeAfter next, int oldTxIndex, int newTxIndex, TransactionContextManager transactionContextManager) {
        this.next = next;
        this.oldTxIndex = oldTxIndex;
        this.newTxIndex = newTxIndex;
        this.transactionContextManager = transactionContextManager;
    }

    public void before(Object[] context, HttpRequest httpRequest, HttpResponse httpResponse) {
        TransactionContext oldTransactionContext = transactionContextManager.getContext();
        TransactionContext newTransactionContext = null;
        if (oldTransactionContext == null || !(oldTransactionContext instanceof InheritableTransactionContext)) {
            newTransactionContext = transactionContextManager.newUnspecifiedTransactionContext();
        }
        context[oldTxIndex] = oldTransactionContext;
        context[newTxIndex] = newTransactionContext;

        if (next != null) {
            next.before(context, httpRequest, httpResponse);
        }
    }

    public void after(Object[] context, HttpRequest httpRequest, HttpResponse httpResponse) {
        if (next != null) {
            next.after(context, httpRequest, httpResponse);
        }
        TransactionContext oldTransactionContext = (TransactionContext) context[oldTxIndex];
        TransactionContext newTransactionContext = (TransactionContext) context[newTxIndex];
        try {
            if (newTransactionContext != null) {
                if (newTransactionContext != transactionContextManager.getContext()) {
                    transactionContextManager.getContext().rollback();
                    newTransactionContext.rollback();
                    throw new RuntimeException("WRONG EXCEPTION! returned from servlet call with wrong tx context");
                }
                newTransactionContext.commit();

            } else {
                if (oldTransactionContext != transactionContextManager.getContext()) {
                    if (transactionContextManager.getContext() != null) {
                        transactionContextManager.getContext().rollback();
                    }
                    throw new RuntimeException("WRONG EXCEPTION! returned from servlet call with wrong tx context");
                }
            }
        } catch (SystemException e) {
            throw new RuntimeException("WRONG EXCEPTION!", e);
        } catch (HeuristicMixedException e) {
            throw new RuntimeException("WRONG EXCEPTION!", e);
        } catch (HeuristicRollbackException e) {
            throw new RuntimeException("WRONG EXCEPTION!", e);
        } catch (RollbackException e) {
            throw new RuntimeException("WRONG EXCEPTION!", e);
        } finally {
            //this is redundant when we enter with an inheritable context and nothing goes wrong.
            transactionContextManager.setContext(oldTransactionContext);
        }
    }

}
