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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.transaction.ConnectionReleaser;
import org.apache.geronimo.transaction.DoubleKeyedHashMap;
import org.apache.geronimo.transaction.InstanceContext;
import org.tranql.cache.InTxCache;


/**
 * @version $Rev: 155376 $ $Date: 2005-02-25 15:10:24 -0800 (Fri, 25 Feb 2005) $
 */
abstract class AbstractTransactionContext implements TransactionContext {
    protected static final Log log = LogFactory.getLog(AbstractTransactionContext.class);
    protected Map managedConnections;

    private InstanceContext currentContext;
    private final DoubleKeyedHashMap associatedContexts = new DoubleKeyedHashMap();
    private final DoubleKeyedHashMap dirtyContexts = new DoubleKeyedHashMap();
    private InTxCache inTxCache;

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
        if (currentContext != null) {
            currentContext.exit();
        }
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
}
