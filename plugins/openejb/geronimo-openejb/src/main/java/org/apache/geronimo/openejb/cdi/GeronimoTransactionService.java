/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.openejb.cdi;

import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import org.apache.webbeans.ee.event.TransactionalEventNotifier;
import org.apache.webbeans.spi.TransactionService;

/**
 * @version $Rev:$ $Date:$
 */
public class GeronimoTransactionService implements TransactionService {
    @Override
    public TransactionManager getTransactionManager() {
        try {
            return (TransactionManager) new InitialContext().lookup("java:comp/TransactionManager");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Transaction getTransaction() {
        try {
            return getTransactionManager().getTransaction();
        } catch (SystemException e) {
            return null;
        }
    }

    @Override
    public UserTransaction getUserTransaction() {
        try {
            return (UserTransaction) new InitialContext().lookup("java:comp/env/UserTransaction");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void registerTransactionSynchronization(TransactionPhase phase, ObserverMethod<? super Object> observer, Object event) throws Exception {
        TransactionalEventNotifier.registerTransactionSynchronization(phase, observer, event);
    }
}
