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

import org.apache.geronimo.cache.InstancePool;
import org.apache.geronimo.common.AbstractInterceptor;
import org.apache.geronimo.common.Container;
import org.apache.geronimo.common.Invocation;
import org.apache.geronimo.common.InvocationResult;
import org.apache.geronimo.common.InvocationType;
import org.apache.geronimo.ejb.EJBInvocationUtil;
import org.apache.geronimo.ejb.EnterpriseContext;
import org.apache.geronimo.ejb.container.EJBPlugins;

/**
 * This interceptor acquires an instance from the pool before invocation,
 * and returns it to the pools after invocation.
 *
 *
 * @version $Revision: 1.3 $ $Date: 2003/08/11 17:59:11 $
 */
public final class StatelessInstanceInterceptor extends AbstractInterceptor {
    private InstancePool pool;

    public void start() throws Exception {
        super.start();
        Container container = getContainer();
        pool = EJBPlugins.getInstancePool(container);
    }

    public void stop() {
        pool = null;
        super.stop();
    }

    public InvocationResult invoke(final Invocation invocation) throws Exception {
        if (InvocationType.getType(invocation).isHomeInvocation()) {
            // Stateless home invocations don't call on an instance
            return getNext().invoke(invocation);
        }

        // get the context
        EnterpriseContext ctx = (EnterpriseContext) pool.acquire();
        assert ctx.getInstance() != null: "Got a context with no instance assigned";

        // initialize the context and set it into the invocation
        EJBInvocationUtil.putEnterpriseContext(invocation, ctx);

        // @todo I don't think we need set principal at all
        //ctx.setPrincipal(invocation.getPrincipal());

        // invoke next, but remember if it threw a system exception
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
            if (threwSystemException) {
                // invocation threw a system exception so the pool needs to dispose of the context
                pool.remove(ctx);
            } else {
                // return the context to the pool
                pool.release(ctx);
            }
        }
    }
}
