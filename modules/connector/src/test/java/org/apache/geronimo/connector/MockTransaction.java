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

package org.apache.geronimo.connector;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

/**
 * Dummy implementation of Transaction interface for use in
 * {@link TxUtilsTest}
 * @version $Rev$ $Date$
 */
public class MockTransaction implements Transaction {

    private int status = -1;

    /** Creates a new instance of MockWorkManager */
    public MockTransaction() {
    }

    public void commit() throws HeuristicMixedException,
            HeuristicRollbackException,
            RollbackException,
            SecurityException,
            SystemException {
    }

    public boolean delistResource(XAResource xaRes, int flag)
            throws IllegalStateException, SystemException {
        return false;
    }

    public boolean enlistResource(XAResource xaRes)
            throws IllegalStateException,
            RollbackException,
            SystemException {
        return false;
    }

    public int getStatus() throws SystemException {
        return status;
    }

    public void registerSynchronization(Synchronization synch)
            throws IllegalStateException,
            RollbackException,
            SystemException {
    }

    public void rollback() throws IllegalStateException, SystemException {
    }

    public void setRollbackOnly()
            throws IllegalStateException,
            SystemException {
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
