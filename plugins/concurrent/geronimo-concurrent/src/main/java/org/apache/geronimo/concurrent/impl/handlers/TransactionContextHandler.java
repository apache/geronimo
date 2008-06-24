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
package org.apache.geronimo.concurrent.impl.handlers;

import java.util.Map;
import java.util.Set;

import javax.transaction.TransactionManager;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;

/*
 * Geronimo-specific managed context handler that for handling transactions. 
 */
public class TransactionContextHandler extends org.apache.geronimo.concurrent.handlers.TransactionContextHandler {

    private static TransactionManager transactionManager;
        
    public static TransactionManager getTransactionManager() {
        if (transactionManager == null) {
            try {
                Kernel kernel = KernelRegistry.getSingleKernel();
                AbstractNameQuery query = new AbstractNameQuery(TransactionManager.class.getName());
                Set gbeans = kernel.listGBeans(query);
                if (gbeans != null && !gbeans.isEmpty()) {
                    AbstractName name = (AbstractName)gbeans.iterator().next();
                    transactionManager = (TransactionManager)kernel.getGBean(name); 
                    LOG.debug("TransactionManager: " + transactionManager);
                } else {
                    LOG.debug("TransactionManager not found");
                }
            } catch (Exception e) {
                LOG.warn("Failed to find TransactionManager", e);
            }
        }
        return transactionManager;
    }
   
    public void saveContext(Map<String, Object> context) {
        context.put(TRANSACTION_MANAGER, getTransactionManager());
    }
    
}
