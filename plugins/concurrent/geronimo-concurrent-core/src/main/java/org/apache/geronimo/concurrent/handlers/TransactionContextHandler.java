/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.concurrent.handlers;

import java.util.Map;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.util.concurrent.ContextService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.concurrent.ManagedContextHandler;

/*
 * Managed context handler that for handling transactions. Follows 
 * Bean-Managed Transaction Demarcation rules as described in EJB 3.0 specification
 * 13.6.1 section.
 */
public abstract class TransactionContextHandler implements ManagedContextHandler {

    protected final static Log LOG = LogFactory.getLog(TransactionContextHandler.class);
    
    protected final static String TRANSACTION_MANAGER = 
        TransactionContextHandler.class.getName() + ".transactionManager";
    
    protected final static String CLIENT_TRANSACTION = 
        TransactionContextHandler.class.getName() + ".clientTransaction";
               
    private boolean isUseParentTransaction(Map<String, Object> threadContext) {
        Object useParentTransaction = 
            threadContext.get(ContextService.USE_PARENT_TRANSACTION);
        if (useParentTransaction instanceof String) {
            return Boolean.valueOf((String)useParentTransaction).booleanValue();
        } else if (useParentTransaction instanceof Boolean) {
            return Boolean.TRUE.equals(useParentTransaction);
        } else {
            return false;
        }
    }
    
    public void setContext(Map<String, Object> threadContext) {
        LOG.debug("setContext");
        
        if (!isUseParentTransaction(threadContext)) {                  
            suspendTransaction(threadContext);
        }
    }
    
    public void unsetContext(Map<String, Object> threadContext) {
        LOG.debug("unsetContext");
        
        if (!isUseParentTransaction(threadContext)) {                  
            resumeTransaction(threadContext);
        }
    }
    
    protected TransactionManager getTransactionManager(Map<String, Object> threadContext) {
        return (TransactionManager)threadContext.get(TRANSACTION_MANAGER);
    }
    
    protected void suspendTransaction(Map<String, Object> threadContext) {
        LOG.debug("suspendTransaction");
        
        TransactionManager manager = getTransactionManager(threadContext);
        
        try {
            Transaction clientTransaction = manager.suspend();
            if (clientTransaction != null) {
                threadContext.put(CLIENT_TRANSACTION, clientTransaction);
            }
        } catch (SystemException e) {
            LOG.warn("Failed to suspend transaction", e);
        }        
    }

    public void resumeTransaction(Map<String, Object> threadContext) {
        LOG.debug("resumeTransaction");
        
        TransactionManager manager = getTransactionManager(threadContext);
                
        try {
            /*
            * The Container must detect the case in which a transaction was started, but
            * not completed, in the business method, and handle it as follows:
            */
            Transaction transaction = manager.getTransaction();

            if (transaction == null) {
                return;
            }

            if (transaction.getStatus() != Status.STATUS_ROLLEDBACK && 
                transaction.getStatus() != Status.STATUS_COMMITTED) {
                String message = "The task started a transaction but did not complete it.";
                
                LOG.error(message);

                try {
                    manager.rollback();
                } catch (Throwable t) {
                    // ignore
                }
            }

        } catch (SystemException e) {
            LOG.warn("Error handling transaction", e);
        } finally {
            Transaction clientTransaction = 
                (Transaction)threadContext.get(CLIENT_TRANSACTION);
            if (clientTransaction != null) {
                try {
                    manager.resume(clientTransaction);
                } catch (Exception  e) {
                    LOG.warn("Failed to resume transaction", e);
                }
            }
        }
    }    
  
}
