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
import java.util.Iterator;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.transaction.DoubleKeyedHashMap;
import org.apache.geronimo.transaction.InstanceContext;
import org.tranql.cache.InTxCache;


/**
 * @version $Rev$ $Date$
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
    private InTxCache inTxCache;

    public abstract boolean getRollbackOnly() throws SystemException;

    public abstract void setRollbackOnly() throws SystemException;

    public abstract void suspend() throws SystemException;

    public abstract void resume() throws SystemException, InvalidTransactionException;

    public abstract boolean commit() throws HeuristicMixedException, HeuristicRollbackException, RollbackException, SystemException;

    public abstract void rollback() throws SystemException;

    public final void associate(InstanceContext context) throws Throwable {
        if (associatedContexts.put(context.getContainerId(), context.getId(), context) == null) {
            context.associate();
        }
    }

    public final void unassociate(InstanceContext context) throws Throwable {
        associatedContexts.remove(context.getContainerId(), context.getId());
        context.unassociate();
    }

    public final void unassociate(Object containerId, Object id) throws Throwable {
        InstanceContext context = (InstanceContext) associatedContexts.remove(containerId, id);
        if (context != null) {
            context.unassociate();
        }
    }

    public final InstanceContext getContext(Object containerId, Object id) {
        return (InstanceContext) associatedContexts.get(containerId, id);
    }

    protected final ArrayList getAssociatedContexts() {
        return new ArrayList(associatedContexts.values());
    }

    protected final void unassociateAll() {
        ArrayList toFlush = getAssociatedContexts();
        for (Iterator i = toFlush.iterator(); i.hasNext();) {
            InstanceContext context = (InstanceContext) i.next();
            try {
                context.unassociate();
            } catch (Throwable throwable) {
                log.warn("Error while unassociating instance from transaction context: " + context, throwable);
            }
        }
    }

    public final InstanceContext beginInvocation(InstanceContext context) throws Throwable {
        if (context.getId() != null) {
            associate(context);
            dirtyContexts.put(context.getContainerId(), context.getId(), context);
        }
        context.enter();
        InstanceContext caller = currentContext;
        currentContext = context;
        return caller;
    }

    public final void endInvocation(InstanceContext caller) {
        currentContext.exit();
        currentContext = caller;
    }

    public final void flushState() throws Throwable {
        while (dirtyContexts.isEmpty() == false) {
            ArrayList toFlush = new ArrayList(dirtyContexts.values());
            dirtyContexts.clear();
            for (Iterator i = toFlush.iterator(); i.hasNext();) {
                InstanceContext context = (InstanceContext) i.next();
                if (!context.isDead()) {
                    context.flush();
                }
            }
        }
        if (currentContext != null && currentContext.getId() != null) {
            dirtyContexts.put(currentContext.getContainerId(), currentContext.getId(), currentContext);
        }
        if(inTxCache != null) {
            inTxCache.flush();
        }
    }

    public final void setInTxCache(InTxCache inTxCache) {
        this.inTxCache = inTxCache;
    }
    
    public final InTxCache getInTxCache() {
        return inTxCache;
    }
}
