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

package org.apache.geronimo.transaction.context;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.xa.XAResource;

import org.apache.geronimo.transaction.ConnectionReleaser;
import org.apache.geronimo.transaction.InstanceContext;

/**
 * @version $Rev$ $Date$
 */
public interface TransactionContext {
    boolean isInheritable();

    boolean isActive();

    boolean enlistResource(XAResource xaResource) throws RollbackException, SystemException;

    boolean delistResource(XAResource xaResource, int flag) throws SystemException;

    void registerSynchronization(Synchronization synchronization) throws RollbackException, SystemException;

    boolean getRollbackOnly() throws SystemException;

    void setRollbackOnly() throws SystemException;

    void suspend() throws SystemException;

    void resume() throws SystemException, InvalidTransactionException;

    boolean commit() throws HeuristicMixedException, HeuristicRollbackException, RollbackException, SystemException;

    void rollback() throws SystemException;

    void associate(InstanceContext context) throws Throwable;

    void unassociate(InstanceContext context) throws Throwable;

    void unassociate(Object containerId, Object id) throws Throwable;

    InstanceContext getContext(Object containerId, Object id);

    InstanceContext beginInvocation(InstanceContext context) throws Throwable;

    void endInvocation(InstanceContext caller);

    void flushState() throws Throwable;

    void setInTxCache(Flushable flushable);

    Flushable getInTxCache();

    void setManagedConnectionInfo(ConnectionReleaser key, Object info);

    Object getManagedConnectionInfo(ConnectionReleaser key);
}