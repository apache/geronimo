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
package org.apache.geronimo.ejb;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.ejb.EJBException;
import javax.naming.InitialContext;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.Container;
import org.apache.geronimo.ejb.container.EJBPlugins;
import org.apache.geronimo.ejb.context.ExecutionContext;
import org.apache.geronimo.ejb.context.TxExecutionContext;
import org.apache.geronimo.ejb.metadata.CommitOption;
import org.apache.geronimo.ejb.metadata.EJBMetadata;

/**
 *
 * @version $Revision: 1.2 $ $Date: 2003/08/11 10:41:20 $
 */
public class SynchronizationRegistry {
    private static final String EJB_REGISTRY_KEY = "EJB_REGISTRY_KEY";

    private TransactionManager transactionManager;

    /**
     * Holds the entities registry for each thread.
     * This registry holds information for all entities in the
     * current thread, but not associated with a transaction.
     */
    private ThreadLocal registryThreadLocal;

    private final Log log = LogFactory.getLog(getClass());

    /**
     * @jmx:managed-operation
     */
    public void create() {
    }

    /**
     * @jmx:managed-operation
     */
    public void start() throws Exception {
        // Get the transaction manager
        InitialContext context = new InitialContext();
        transactionManager = (TransactionManager) context.lookup("java:/TransactionManager");

        // Create the thread local to hold the entity registry
        registryThreadLocal = new ThreadLocal() {
            protected Object initialValue() {
                return new Registry();
            }
        };
    }

    /**
     * @jmx:managed-operation
     */
    public void stop() {
        transactionManager = null;
    }

    /**
     * @jmx:managed-operation
     */
    public void destroy() {
    }

    public void beginInvocation(EnterpriseContext ctx) throws Exception {
        Registry registry = getRegistry();
        Container container = ctx.getContainer();

        // load if not valid
        if (!ctx.isValid()) {
            // Not valid... tell the persistence manager to load the state
            // Note: make sure to not chnage anything befoe calling load
            // you can get an exception from load
            EJBPlugins.getPersistenceManager(container).load(ctx);

            // Now the state is valid
            ctx.setValid(true);
        }

        // before we add the new context to the invocation stack we need to
        // mark the current head of the stack as dirty because it could have
        // been modified
        LinkedList invocationStack = registry.getInvocationStack();
        if (!invocationStack.isEmpty()) {
            EnterpriseContext head = (EnterpriseContext) invocationStack.getFirst();
            ContextKey headKey = new ContextKey(head.getContainer(), head.getId());
            registry.getDirtyMap().put(headKey, head);
        }

        // Add the context to the stack of contexts being currently invoked
        invocationStack.addFirst(ctx);

        // if this is a previously unseen context log it
        ContextKey key = new ContextKey(container, ctx.getId());
        if (registry.getAssociatedMap().put(key, ctx) == null) {
            if (log.isTraceEnabled()) {
                log.trace("Associated new entity: " +
                        "ejb=" + EJBPlugins.getEJBMetadata(container).getName() +
                        ", id=" + key.getId());
            }
        }
    }

    public void endInvocation(boolean threwException, Object id, EnterpriseContext ctx) throws Exception {
        ContextKey key = new ContextKey(ctx.getContainer(), id);
        Registry registry = getRegistry();

        // If the head context on the invocation stack is not the requested ctx
        // we have a serious problem.
        LinkedList invocationStack = registry.getInvocationStack();
        if (invocationStack.isEmpty()) {
            throw new IllegalStateException("The invocation stack in inconsistent.  " +
                    "Expected context=" + ctx + ", but stack was empty");
        }

        EnterpriseContext head = (EnterpriseContext) invocationStack.removeFirst();
        if (ctx != head) {
            throw new IllegalStateException("The invocation stack in inconsistent.  " +
                    "Expected context=" + ctx + ", but got context=" + head);
        }

        ExecutionContext executionContext = ExecutionContext.getContext();
        if (executionContext instanceof TxExecutionContext == false) {
            // only store the entity if an exception was not thrown
            if (!executionContext.isReadOnly() && !threwException) {
                synchronizeEntity(registry, ctx);
            }

            // always disassociate the entity
            disassociateEntity(threwException, id, ctx);
            registry.getAssociatedMap().remove(key);

            // just to be safe clear the regisry if the invocation is done
            if (registry.getInvocationStack().isEmpty()) {
                registry.clear();
            }
        } else {
            // If this is a read/write invocation, assume the context is dirty,
            // when an entity in a tx leave an invocation
            if (!executionContext.isReadOnly()) {
                registry.getDirtyMap().put(key, ctx);
            }
        }
    }

    /**
     * Gets the EnterpriseContext for the current transaction in the
     * specified container with the specified key.
     */
    public EnterpriseContext getContext(Container container, Object id) {
        Registry registry = getRegistry();
        ContextKey key = new ContextKey(container, id);
        return (EnterpriseContext) registry.getAssociatedMap().get(key);
    }

    public void synchronizeEntities() {
        // First we synchronize the head context in the invocation.  We
        // always synchronize the current context, because there is
        // no way to detect if current context has been modified or not.
        // After that, we synchronize anything in the dirty map.  We
        // loop over that map because an ejbStore call back can modifiy
        // another bean.
        Registry registry = getRegistry();

        LinkedList invocationStack = registry.getInvocationStack();
        if (!invocationStack.isEmpty()) {
            EnterpriseContext ctx = (EnterpriseContext) invocationStack.getFirst();
            try {
                synchronizeEntity(registry, ctx);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new EJBException("Unable to store entity: " +
                        "ejb=" + EJBPlugins.getEJBMetadata(ctx.getContainer()).getName() +
                        ", id=" + ctx.getId(), e);
            }
        }

        // @todo add a limit on the number of loops (say 10)
        while (!registry.getDirtyMap().isEmpty()) {
            // get an iterator over the current dirty map
            Iterator entities = registry.getDirtyMap().values().iterator();

            // reset the dirty map, because when we synchronize an entity
            // it may modify another entity and we need to know the additional
            // entities to synchronize in the next iteration of the outer loop
            registry.setDirtyMap(new HashMap());

            // synchronize the dirty entities
            while (entities.hasNext()) {
                EnterpriseContext ctx = (EnterpriseContext) entities.next();
                try {
                    synchronizeEntity(registry, ctx);
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new EJBException("Unable to store entity: " +
                            "ejb=" + EJBPlugins.getEJBMetadata(ctx.getContainer()).getName() +
                            ", id=" + ctx.getId(), e);
                }
            }
        }
    }

    private void synchronizeEntity(Registry registry, EnterpriseContext ctx) throws Exception {
        Container container = ctx.getContainer();
        EJBMetadata ejbMetadata = EJBPlugins.getEJBMetadata(container);

        // any one can mark the tx rollback at any time so check
        // before continuing to the store
        try {
            // store only happens when the transaction will be committed
            if (transactionManager.getStatus() != Status.STATUS_ACTIVE) {
                return;
            }
        } catch (SystemException e) {
            throw new EJBException("Unable to get transaction status", e);
        }

        // only synchronize if we are not already synchronizing
        // the context or the id is not null.  We keep track of what
        // we are already synchronizing to catch the case where an ejbStore
        // call back calls a finder which would result in a store infinite loop.
        // A null id means that the entity has already been removed.
        if (ctx == registry.getInStoreContext() || ctx.getId() == null) {
            return;
        }

        if (log.isTraceEnabled()) {
            log.trace("Synchronizing entity: " +
                    "ejb=" + ejbMetadata.getName() +
                    ", id=" + ctx.getId());
        }

        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ejbMetadata.getClassLoader());
        try {
            registry.setInStoreContext(ctx);
            boolean rollback = true;
            try {
                EJBPlugins.getPersistenceManager(container).store(ctx);
                rollback = true;
            } finally {
                if (rollback) {
                    try {
                        transactionManager.setRollbackOnly();
                    } catch (Throwable e) {
                        log.error("Could not mark transaction for rollback", e);
                    }
                }
            }
        } finally {
            registry.setInStoreContext(null);
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    /**
     * Disassociate entity with transaction.
     */
    private void disassociateEntity(boolean rollback, Object id, EnterpriseContext ctx) {
        // Get the container associated with this context
        Container container = ctx.getContainer();
        EJBMetadata ejbMetadata = EJBPlugins.getEJBMetadata(container);

        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ejbMetadata.getClassLoader());
        try {
            // If rolled back, invalidate instance
            CommitOption commitOption = ejbMetadata.getCommitOption();
            if (rollback) {
                EJBPlugins.getInstanceCache(container).remove(id);
            } else if (commitOption == CommitOption.A || commitOption == CommitOption.D) {
                ctx.setValid(true);
            } else if (commitOption == CommitOption.B) {
                ctx.setValid(false);
            } else {
                EJBPlugins.getInstanceCache(container).remove(id);
                ctx.discard();
                try {
                    EJBPlugins.getPersistenceManager(container).passivate(ctx);
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new EJBException("Could not passivate commit-option C entity bean", e);
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    private Registry getRegistry() {
        ExecutionContext context = ExecutionContext.getContext();
        if (context instanceof TxExecutionContext == false) {
            return (Registry) registryThreadLocal.get();
        }

        // get the map of container to containerMap
        Registry registry = (Registry) context.get(EJB_REGISTRY_KEY);
        if (registry == null) {
            registry = new Registry();
            context.put(EJB_REGISTRY_KEY, registry);
            context.register(new EJBSynchronization());
        }
        return registry;
    }

    private final class Registry {
        private Map dirtyMap = new HashMap();
        private LinkedList invocationStack = new LinkedList();
        private Map associatedMap = new HashMap();
        private EnterpriseContext inStoreContext;

        public void clear() {
            dirtyMap = new HashMap();
            invocationStack = new LinkedList();
            associatedMap = new HashMap();
        }

        public Map getDirtyMap() {
            return dirtyMap;
        }

        public void setDirtyMap(final Map dirtyMap) {
            this.dirtyMap = dirtyMap;
        }

        public LinkedList getInvocationStack() {
            return invocationStack;
        }

        public void setInvocationStack(final LinkedList invocationStack) {
            this.invocationStack = invocationStack;
        }

        public Map getAssociatedMap() {
            return associatedMap;
        }

        public void setAssociatedMap(final Map associatedMap) {
            this.associatedMap = associatedMap;
        }

        public EnterpriseContext getInStoreContext() {
            return inStoreContext;
        }

        public void setInStoreContext(EnterpriseContext inStoreContext) {
            this.inStoreContext = inStoreContext;
        }
    }

    private final class ContextKey {
        private final Container container;
        private final Object id;

        public ContextKey(final Container container, final Object id) {
            if (container == null) {
                throw new IllegalArgumentException("Container is null");
            }
            if (id == null) {
                throw new IllegalArgumentException("Id is null");
            }
            this.container = container;
            this.id = id;
        }

        public Container getContainer() {
            return container;
        }

        public Object getId() {
            return id;
        }

        public boolean equals(Object object) {
            if (!(object instanceof ContextKey)) {
                return false;
            }

            ContextKey key = (ContextKey) object;
            return container.equals(key.getContainer()) && id.equals(key.getId());
        }

        public int hashCode() {
            int result = 17;
            result = 37 * result + container.hashCode();
            result = 37 * result + id.hashCode();
            return result;
        }
    }

    private final class EJBSynchronization implements Synchronization {
        public void beforeCompletion() {
            if (log.isTraceEnabled()) {
                log.trace("beforeCompletion called");
            }

            // let the runtime exceptions fall out, so the committer can determine
            // the root cause of a rollback
            synchronizeEntities();
        }

        public void afterCompletion(int status) {
            Registry registry = getRegistry();
            Map entityMap = registry.getAssociatedMap();
            registry.clear();
            for (Iterator iter = entityMap.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                disassociateEntity(
                        status == Status.STATUS_ROLLEDBACK,
                        ((ContextKey) entry.getKey()).getId(),
                        (EnterpriseContext) entry.getValue());
            }
        }
    }
}

