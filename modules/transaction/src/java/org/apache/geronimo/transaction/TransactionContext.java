/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
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
 * @version $Revision: 1.1 $ $Date: 2004/01/31 19:27:17 $
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
