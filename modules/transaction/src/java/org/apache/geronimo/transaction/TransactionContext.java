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

package org.apache.geronimo.transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:58:19 $
 */
public abstract class TransactionContext {
    protected static final Log log = LogFactory.getLog(TransactionContext.class);
    private static ThreadLocal CONTEXT = new ThreadLocal();
    private Map managedConnections;

    public static TransactionContext getContext() {
        return (TransactionContext) CONTEXT.get();
    }

    public static void setContext(TransactionContext context) {
        CONTEXT.set(context);
    }

    private InstanceContext currentContext;
    private final org.apache.geronimo.transaction.DoubleKeyedHashMap associatedContexts = new org.apache.geronimo.transaction.DoubleKeyedHashMap();
    private final org.apache.geronimo.transaction.DoubleKeyedHashMap dirtyContexts = new org.apache.geronimo.transaction.DoubleKeyedHashMap();
    private final org.apache.geronimo.transaction.DoubleKeyedHashMap instanceDataCache = new org.apache.geronimo.transaction.DoubleKeyedHashMap();

    public abstract void begin() throws SystemException, NotSupportedException;

    public abstract void suspend() throws SystemException;

    public abstract void resume() throws SystemException, InvalidTransactionException;

    public abstract void commit() throws HeuristicMixedException, HeuristicRollbackException, RollbackException, SystemException;

    public abstract void rollback() throws SystemException;

    public final void associate(InstanceContext context) throws Exception {
        if (associatedContexts.put(context.getContainer(), context.getId(), context) == null) {
            context.associate();
        }
    }

    public final InstanceContext beginInvocation(InstanceContext context) {
        if (context.getId() != null) {
            dirtyContexts.put(context.getContainer(), context.getId(), context);
        }
        InstanceContext caller = currentContext;
        currentContext = context;
        return caller;
    }

    public final void endInvocation(InstanceContext caller) {
        currentContext = caller;
    }

    public final void flushState() throws Exception {
        while (dirtyContexts.isEmpty() == false) {
            ArrayList toFlush = new ArrayList(dirtyContexts.values());
            dirtyContexts.clear();
            for (Iterator i = toFlush.iterator(); i.hasNext();) {
                InstanceContext context = (InstanceContext) i.next();
                context.flush();
            }
        }
        if (currentContext != null && currentContext.getId() != null) {
            dirtyContexts.put(currentContext.getContainer(), currentContext.getId(), currentContext);
        }
    }

    protected void beforeCommit() throws Exception {
        // @todo allow for enrollment during pre-commit
        ArrayList toFlush = new ArrayList(associatedContexts.values());
        for (Iterator i = toFlush.iterator(); i.hasNext();) {
            InstanceContext context = (InstanceContext) i.next();
            context.beforeCommit();
        }
    }

    protected void afterCommit(boolean status) throws Exception {
        // @todo allow for enrollment during pre-commit
        ArrayList toFlush = new ArrayList(associatedContexts.values());
        for (Iterator i = toFlush.iterator(); i.hasNext();) {
            InstanceContext context = (InstanceContext) i.next();
            context.afterCommit(status);
        }
    }

    public final InstanceContext getContext(Object container, Object id) {
        return (InstanceContext) associatedContexts.get(container, id);
    }

    public final void putInstanceData(Object container, Object id, Object data) {
        instanceDataCache.put(container, id, data);
    }

    public final Object getInstancedata(Object container, Object id) {
        return instanceDataCache.get(container, id);
    }

    //Geronimo connector framework support
    public void setManagedConnectionInfo(ConnectionReleaser key, Object info) {
        if (managedConnections == null) {
            managedConnections = new HashMap();
        }
        managedConnections.put(key, info);
    }

    public Object getManagedConnectionInfo(ConnectionReleaser key) {
        if (managedConnections == null) {
            return null;
        }
        return managedConnections.get(key);
    }

    public abstract boolean isActive();

    public abstract Transaction getTransaction();

    protected void connectorAfterCommit() {
        if (managedConnections != null) {
            for (Iterator entries = managedConnections.entrySet().iterator(); entries.hasNext();) {
                Map.Entry entry = (Map.Entry) entries.next();
                ConnectionReleaser key = (ConnectionReleaser) entry.getKey();
                key.afterCompletion(entry.getValue());
            }
            //If BeanTransactionContext never reuses the same instance for sequential BMT, this
            //clearing is unnecessary.
            managedConnections.clear();
        }
    }

}
