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
package org.apache.geronimo.ejb.cache;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.EJBException;
import javax.ejb.SessionSynchronization;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.apache.geronimo.cache.InstanceCache;
import org.apache.geronimo.core.service.AbstractInterceptor;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.core.service.InvocationType;
import org.apache.geronimo.core.service.RPCContainer;
import org.apache.geronimo.ejb.EJBInvocationUtil;
import org.apache.geronimo.ejb.EnterpriseContext;
import org.apache.geronimo.ejb.container.EJBPlugins;
import org.apache.geronimo.ejb.context.ExecutionContext;
import org.apache.geronimo.ejb.context.TxExecutionContext;
import org.apache.geronimo.ejb.metadata.EJBMetadata;

/**
 *
 *
 *
 * @version $Revision: 1.8 $ $Date: 2003/09/08 04:28:26 $
 */
public final class StatefulSessionSynchronizationInterceptor extends AbstractInterceptor {
    protected TransactionManager tm;
    private InstanceCache cache;
    private boolean hasSynchronization;

    protected void doStart() throws Exception {
        RPCContainer container = getContainer();
        tm = EJBPlugins.getTransactionManager(container);
        cache = EJBPlugins.getInstanceCache(container);
        EJBMetadata ejbMetadata = EJBPlugins.getEJBMetadata(container);
        hasSynchronization = SessionSynchronization.class.isAssignableFrom(ejbMetadata.getBeanClass());
    }

    protected void doStop() throws Exception {
        cache = null;
        tm = null;
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        if (InvocationType.getType(invocation).isHomeInvocation()) {
            // Home invocation's don't have state so they don't need to be synchronized
            return getNext().invoke(invocation);
        }

        Object id = EJBInvocationUtil.getId(invocation);
        EnterpriseContext ctx = EJBInvocationUtil.getEnterpriseContext(invocation);
        if (hasSynchronization) {
            register(id, ctx);
        }
        return getNext().invoke(invocation);
    }

    private static final String INSTANCE_SYNC_KEY = "Stateful Session Synchronization";

    private void register(Object id, EnterpriseContext ctx) throws RemoteException {
        ExecutionContext context = ExecutionContext.getContext();
        if (context instanceof TxExecutionContext == false) {
            return;
        }

        // get the map of container to containerMap
        Map syncMap = (Map) context.get(INSTANCE_SYNC_KEY);
        if (syncMap == null) {
            syncMap = new HashMap();
            context.put(INSTANCE_SYNC_KEY, syncMap);
        }
        // get the map from id to InstanceSynchronization for this container
        Map containerMap = (Map) syncMap.get(getContainer());
        if (containerMap == null) {
            containerMap = new HashMap();
            syncMap.put(getContainer(), containerMap);
        }
        // get the instance synchronization for this id
        InstanceSynchronization sync = (InstanceSynchronization) containerMap.get(id);
        if (sync == null) {
            sync = new InstanceSynchronization(ctx);
            sync.begin();
            context.register(sync);
            containerMap.put(id, sync);
        }
    }

    public class InstanceSynchronization implements Synchronization {
        private final EnterpriseContext ctx;
        private boolean discard = false;

        public InstanceSynchronization(EnterpriseContext ctx) {
            this.ctx = ctx;
        }

        private void begin() throws RemoteException {
            ((SessionSynchronization) ctx.getInstance()).afterBegin();
        }

        public void beforeCompletion() {
            try {
                int status = tm.getStatus();
                // beforeCompletion only happens when the transaction will be committed
                if (status != Status.STATUS_ACTIVE) {
                    return;
                }
            } catch (SystemException e) {
                throw new EJBException("Unable to get transaction status", e);
            }

            try {
                ((SessionSynchronization) ctx.getInstance()).beforeCompletion();
            } catch (Error e) {
                discard = true;
                throw e;
            } catch (RuntimeException e) {
                discard = true;
                throw e;
            } catch (RemoteException e) {
                discard = true;
                throw new EJBException(e);
            } finally {
                if (discard) {
                    cache.remove(ctx);
                    try {
                        tm.setRollbackOnly();
                    } catch (SystemException e) {
                        throw new EJBException("Unable to set transaction rollback only", e);
                    }
                }
            }
        }

        public void afterCompletion(int status) {
            // can't do much if the instance was discarded
            if (discard) {
                return;
            }

            try {
                ((SessionSynchronization) ctx.getInstance()).afterCompletion(status == Status.STATUS_COMMITTED);
            } catch (Error e) {
                discard = true;
                // give this to the parent, so don't log
                throw e;
            } catch (RuntimeException e) {
                discard = true;
                // log and eat
                log.warn(e);
            } catch (RemoteException e) {
                discard = true;
                // log and eat
                log.warn(e);
            } finally {
                if (discard) {
                    cache.remove(ctx);
                }
            }
        }
    }
}
