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

import org.apache.geronimo.cache.InstanceCache;
import org.apache.geronimo.cache.InstancePool;
import org.apache.geronimo.common.AbstractInterceptor;
import org.apache.geronimo.common.RPCContainer;
import org.apache.geronimo.common.Invocation;
import org.apache.geronimo.common.InvocationResult;
import org.apache.geronimo.common.InvocationType;
import org.apache.geronimo.ejb.EJBInvocationUtil;
import org.apache.geronimo.ejb.EnterpriseContext;
import org.apache.geronimo.ejb.container.EJBPlugins;
import org.apache.geronimo.ejb.context.ExecutionContext;
import org.apache.geronimo.lock.LockContext;
import org.apache.geronimo.lock.LockDomain;
import org.apache.geronimo.lock.LockReentranceException;

/**
 *
 *
 *
 * @version $Revision: 1.7 $ $Date: 2003/08/23 22:09:39 $
 */
public final class StatefulInstanceInterceptor extends AbstractInterceptor {
    private InstancePool pool;
    private InstanceCache cache;
    private LockDomain lockDomain;

    protected void doStart() throws Exception {
        RPCContainer container = getContainer();
        lockDomain = EJBPlugins.getLockDomain(container);
        pool = EJBPlugins.getInstancePool(container);
        cache = EJBPlugins.getInstanceCache(container);
    }

    protected void doStop() throws Exception {
        lockDomain = null;
        cache = null;
        pool = null;
    }

    public InvocationResult invoke(Invocation invocation) throws Exception {
        EnterpriseContext ctx;
        Object id = null;

        InvocationType type = InvocationType.getType(invocation);
        if (type.isHomeInvocation()) {
            ctx = (EnterpriseContext) pool.acquire();
        } else {
            id = EJBInvocationUtil.getId(invocation);
            ctx = (EnterpriseContext) cache.get(id);
        }
        EJBInvocationUtil.putEnterpriseContext(invocation, ctx);

        // @todo I don't think we need to set princpal
        //ctx.setPrincipal(invocation.getPrincipal());

        boolean threwSystemException = false;
        try {
            return getNext().invoke(invocation);
        } catch (RuntimeException e) {
            threwSystemException = true;
            throw e;
        } catch (RemoteException e) {
            threwSystemException = true;
            throw e;
        } catch (Error e) {
            threwSystemException = true;
            throw e;
        } finally {
            // this invocation is done so remove the reference to the context
            EJBInvocationUtil.putEnterpriseContext(invocation, null);
            if (type.isHomeInvocation()) {
                if (threwSystemException) {
                    // invocation threw a system exception so the pool needs to dispose of the context
                    pool.remove(ctx);
                } else if (ctx.getId() != null) {
                    // we were created
                    LockContext lockContext = ExecutionContext.getContext().getLockContext();
                    try {
                        lockContext.exclusiveLock(lockDomain, ctx.getId());
                    } catch (LockReentranceException e) {
                        // this should never happen as we have a new id
                        throw new AssertionError();
                    } catch (InterruptedException e) {
                        // this should never happen as we have a new id
                        throw new AssertionError();
                    }
                    // todo we should get from the instance factory for a create
                    // move this context from the pool to the cache
                    pool.remove(ctx);
                    cache.putActive(ctx.getId(), ctx);
                } else {
                    // return the context to the pool
                    pool.release(ctx);
                }
            } else {
                if (threwSystemException) {
                    // invocation threw a system exception so the cache needs to dispose of the context
                    cache.remove(id);
                } else if (ctx.getId() == null) {
                    // the instance was removed
                    cache.remove(id);
                } else {
                    assert ctx.getId().equals(id);
                }
            }
        }
    }
}
