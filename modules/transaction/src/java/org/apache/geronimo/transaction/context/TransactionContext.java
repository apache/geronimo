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
import org.apache.geronimo.transaction.InstanceContext;
import org.apache.geronimo.transaction.DoubleKeyedHashMap;
import org.apache.geronimo.transaction.ConnectionReleaser;
import org.tranql.cache.InTxCache;
import org.tranql.cache.SimpleFlushStrategy;


/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/07/18 22:02:01 $
 */
public abstract class TransactionContext {
    protected static final Log log = LogFactory.getLog(TransactionContext.class);
    private static ThreadLocal CONTEXT = new ThreadLocal();

    public static TransactionContext getContext() {
        return (TransactionContext) CONTEXT.get();
    }

    public static void setContext(TransactionContext context) {
        CONTEXT.set(context);
    }

    private InstanceContext currentContext;
    private final DoubleKeyedHashMap associatedContexts = new DoubleKeyedHashMap();
    private final DoubleKeyedHashMap dirtyContexts = new DoubleKeyedHashMap();
    private Map managedConnections;
    private InTxCache inTxCache;

    public abstract void begin() throws SystemException, NotSupportedException;

    public abstract void suspend() throws SystemException;

    public abstract void resume() throws SystemException, InvalidTransactionException;

    public abstract void commit() throws HeuristicMixedException, HeuristicRollbackException, RollbackException, SystemException;

    public abstract void rollback() throws SystemException;

    public final void associate(InstanceContext context) throws Throwable {
        if (associatedContexts.put(context.getContainerId(), context.getId(), context) == null) {
            context.associate();
        }
    }

    public final void unassociate(Object containerId, Object id) throws Exception {
        associatedContexts.remove(containerId, id);
        dirtyContexts.remove(containerId, id);
    }

    public final InstanceContext beginInvocation(InstanceContext context) {
        if (context.getId() != null) {
            dirtyContexts.put(context.getContainerId(), context.getId(), context);
        }
        InstanceContext caller = currentContext;
        currentContext = context;
        return caller;
    }

    public final void endInvocation(InstanceContext caller) {
        currentContext = caller;
    }

    public final void flushState() throws Throwable {
        while (dirtyContexts.isEmpty() == false) {
            ArrayList toFlush = new ArrayList(dirtyContexts.values());
            dirtyContexts.clear();
            for (Iterator i = toFlush.iterator(); i.hasNext();) {
                InstanceContext context = (InstanceContext) i.next();
                context.flush();
            }
        }
        if (currentContext != null && currentContext.getId() != null) {
            dirtyContexts.put(currentContext.getContainerId(), currentContext.getId(), currentContext);
        }
        if(inTxCache != null) {
            inTxCache.flush();
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

    public final InstanceContext getContext(Object containerId, Object id) {
        return (InstanceContext) associatedContexts.get(containerId, id);
    }

    public final InTxCache getInTxCache() {
        if (inTxCache == null) {
            inTxCache = new InTxCache(new SimpleFlushStrategy());
        }
        return inTxCache;
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
