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
package org.apache.geronimo.ejb.context;

import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.geronimo.common.AbstractInterceptor;
import org.apache.geronimo.common.Invocation;
import org.apache.geronimo.common.InvocationResult;
import org.apache.geronimo.common.RPCContainer;
import org.apache.geronimo.ejb.container.EJBPlugins;

/**
 *
 *
 *
 * @version $Revision: 1.3 $ $Date: 2003/08/15 14:12:19 $
 */
public abstract class ExecutionContextInterceptor extends AbstractInterceptor {
    protected TransactionManager tm;

    public void start() throws Exception {
        super.start();
        tm = EJBPlugins.getTransactionManager((RPCContainer)getContainer());
    }

    public void stop() {
        tm = null;
        super.stop();
    }

    /**
     * Invoke the appropriate method on the next interceptor inside the supplied context.
     * @param context the context to run inside
     * @param invocation the invocation to pass down
     * @return the result of the invocation
     * @throws java.lang.Exception any Exception from the next interceptor
     */
    protected InvocationResult invokeNext(ExecutionContext context, Invocation invocation) throws Exception {
        ExecutionContext.push(context);
        try {
            return getNext().invoke(invocation);
        } finally {
            ExecutionContext.pop(context);
        }
    }

    protected Transaction getTransaction() throws EJBTransactionException {
        try {
            return tm.getTransaction();
        } catch (SystemException e) {
            throw new EJBTransactionException("Unable to get current Transaction", e);
        }
    }

    /**
     * Suspend the current Transaction
     * @throws org.apache.geronimo.ejb.context.EJBTransactionException if the Transaction could not be suspended
     */
    protected Transaction suspend() throws EJBTransactionException {
        try {
            return tm.suspend();
        } catch (SystemException e) {
            throw new EJBTransactionException("Unable to suspend current transaction", e);
        }
    }

    /**
     * Resume a previous Transaction
     * @param tx the Transaction to resume
     * @throws org.apache.geronimo.ejb.context.EJBTransactionException if the Transaction could not be resumed
     */
    protected void resume(Transaction tx) throws EJBTransactionException {
        try {
            tm.resume(tx);
        } catch (Exception e) {
            throw new EJBTransactionException("Unable to resume current transaction", e);
        }
    }
}
