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
package org.apache.geronimo.tomcat.valve;

import org.apache.catalina.valves.ValveBase;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.context.TransactionContext;
import org.apache.geronimo.transaction.context.InheritableTransactionContext;

import javax.servlet.ServletException;
import javax.transaction.SystemException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import java.io.IOException;

/**
 * @version $Rev: $ $Date: $
 */
public class TransactionContextValve extends ValveBase {

    private final TransactionContextManager transactionContextManager;

    public TransactionContextValve(TransactionContextManager transactionContextManager){
        this.transactionContextManager = transactionContextManager;
    }

    public void invoke(Request request, Response response) throws IOException, ServletException {

        TransactionContext oldTransactionContext = transactionContextManager.getContext();
        TransactionContext newTransactionContext = null;

        if (oldTransactionContext == null || !(oldTransactionContext instanceof InheritableTransactionContext)) {
            newTransactionContext = transactionContextManager.newUnspecifiedTransactionContext();
        }

        // Pass this request on to the next valve in our pipeline
        getNext().invoke(request, response);

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
