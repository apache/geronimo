/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.connector.outbound.connectiontracking.defaultimpl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.NotSupportedException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;

import org.apache.geronimo.transaction.ConnectionReleaser;
import org.apache.geronimo.transaction.TransactionContext;
import org.apache.geronimo.connector.outbound.ManagedConnectionInfo;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:57:11 $
 *
 * */
public class DefaultTransactionContext extends TransactionContext implements Synchronization {

    private Map managedConnections;

    private final Transaction transaction;

    public DefaultTransactionContext(Transaction transaction) throws SystemException, RollbackException {
        this.transaction = transaction;
        if (transaction != null) {
            assert transaction.getStatus() == Status.STATUS_ACTIVE;
            transaction.registerSynchronization(this);
        }
    }

    public void begin() throws SystemException, NotSupportedException {
    }

    public void suspend() throws SystemException {
    }

    public void resume() throws SystemException, InvalidTransactionException {
    }

    public void commit() throws HeuristicMixedException, HeuristicRollbackException, RollbackException, SystemException {
    }

    public void rollback() throws SystemException {
    }

    /**
     * Don't try to cache connections if there is no transaction, since there is no
     * event that tells us to release the connection.
     * @param key
     * @param info
     */
    public void setManagedConnectionInfo(ConnectionReleaser key, Object info) {
        if (isActive()) {
            if (managedConnections == null) {
                managedConnections = new HashMap();
            }
            managedConnections.put(key, info);
        }
    }

    public Object getManagedConnectionInfo(ConnectionReleaser key) {
        if (managedConnections == null) {
            return null;
        }
        return (ManagedConnectionInfo) managedConnections.get(key);
    }

    /**
     * I'm not sure I got the condition right here.
     * @return
     */
    public boolean isActive() {
        try {
            return transaction != null && (transaction.getStatus() == Status.STATUS_ACTIVE);
        } catch (SystemException e) {
            return false; //this is doubtful
        }
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void beforeCompletion() {
    }

    public void afterCompletion(int status) {
        if (managedConnections != null) {
            for (Iterator entries = managedConnections.entrySet().iterator(); entries.hasNext();) {
                Map.Entry entry = (Map.Entry) entries.next();
                ConnectionReleaser key = (ConnectionReleaser) entry.getKey();
                key.afterCompletion(entry.getValue());
            }
            //should we clear managedConnections?  might be less work for garbage collector.  any other reason?
        }
    }
}
