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
package org.apache.geronimo.web.jetty;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.naming.Context;
import javax.resource.ResourceException;
import javax.security.jacc.PolicyContext;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.connector.outbound.ConnectorComponentContext;
import org.apache.geronimo.connector.outbound.ConnectorTransactionContext;
import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.connector.outbound.connectiontracking.defaultimpl.DefaultComponentContext;
import org.apache.geronimo.connector.outbound.connectiontracking.defaultimpl.DefaultTransactionContext;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.naming.java.RootContext;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.jetty.servlet.WebApplicationContext;

/**
 *
 *
 * @version $Revision: 1.6 $ $Date: 2004/01/19 06:38:23 $
 */
public class JettyWebApplicationContext extends WebApplicationContext {

    private static Log log = LogFactory.getLog(JettyWebApplicationContext.class);


    private Context componentContext;
    private String contextID;
    private TransactionManager transactionManager;
    private TrackedConnectionAssociator trackedConnectionAssociator;

    //we don't examine the dd for these yet.
    private final Set unshareableResources = Collections.EMPTY_SET;

    //this should be replaced by global tx context handling.
    private final Map transactionToTransactionContextMap = Collections.synchronizedMap(new WeakHashMap());

    public JettyWebApplicationContext() {
    }

    public JettyWebApplicationContext(String webApp, String contextID, Context componentContext, TransactionManager transactionManager, TrackedConnectionAssociator trackedConnectionAssociator) {
        super(webApp);
        this.contextID = contextID;
        this.componentContext = componentContext;
        this.transactionManager = transactionManager;
        this.trackedConnectionAssociator = trackedConnectionAssociator;
    }

    public Object enterContextScope(HttpRequest httpRequest, HttpResponse httpResponse) {
        log.info("Entering context " + httpRequest.getRequestURL());
        ReadOnlyContext oldContext = RootContext.getComponentContext();
        RootContext.setComponentContext((ReadOnlyContext) componentContext);
        String oldContextID = null;
        try {
            oldContextID = PolicyContext.getContextID();
        } catch (Throwable e) {
            log.info(e);
        }
        try {
            PolicyContext.setContextID(contextID);
        } catch (Throwable e) {
            log.info(e);
        }
        //tx handling. Definitely a hack until we get a system wide tx context system.
        try {
            Transaction transaction = transactionManager == null? null: transactionManager.getTransaction();
            ConnectorTransactionContext newConnectorTransactionContext;
            if (transaction == null || transaction.getStatus() == Status.STATUS_COMMITTED || transaction.getStatus() == Status.STATUS_ROLLEDBACK) {
                newConnectorTransactionContext = new DefaultTransactionContext(null);
            } else {
                newConnectorTransactionContext = (ConnectorTransactionContext) transactionToTransactionContextMap.get(transaction);
                if (newConnectorTransactionContext == null) {
                    newConnectorTransactionContext = new DefaultTransactionContext(transaction);
                    transactionToTransactionContextMap.put(transaction, newConnectorTransactionContext);
                }
            }
            Set oldUnshareableResources = trackedConnectionAssociator.setUnshareableResources(unshareableResources);
            ConnectorComponentContext oldConnectorComponentContext = trackedConnectionAssociator.enter(new DefaultComponentContext());
            ConnectorTransactionContext oldConnectorTransactionContext = trackedConnectionAssociator.setConnectorTransactionContext(newConnectorTransactionContext);
            Object scope = super.enterContextScope(httpRequest, httpResponse);
            ThreadContext threadContext = new ThreadContext(oldUnshareableResources, oldConnectorComponentContext, oldConnectorTransactionContext, oldContext, oldContextID, scope);
            return threadContext;
        } catch (SystemException e) {
            throw new RuntimeException(e);
        } catch (RollbackException e) {
            throw new RuntimeException(e);
        } catch (ResourceException e) {
            throw new RuntimeException(e);
        }
    }

    public void leaveContextScope(HttpRequest httpRequest, HttpResponse httpResponse, Object o) {
        ThreadContext threadContext = (ThreadContext) o;
        super.leaveContextScope(httpRequest, httpResponse, threadContext == null? null: threadContext.scope);
        if (threadContext == null) {
            return;
        }
        try {
            trackedConnectionAssociator.exit(threadContext.connectorComponentContext, unshareableResources);
        } catch (ResourceException e) {
            throw new RuntimeException(e);
        }
        trackedConnectionAssociator.resetConnectorTransactionContext(threadContext.connectorTransactionContext);
        trackedConnectionAssociator.setUnshareableResources(threadContext.unshareableResources);
        RootContext.setComponentContext(threadContext.context);
        PolicyContext.setContextID(threadContext.contextID);
        log.info("Leaving context " + httpRequest.getRequestURL());
    }

    public Context getComponentContext() {
        return componentContext;
    }

    public void setComponentContext(Context componentContext) {
        this.componentContext = componentContext;
    }

    public String getContextID() {
        return contextID;
    }

    public void setContextID(String contextID) {
        this.contextID = contextID;
    }

    private static class ThreadContext {
        final Set unshareableResources;
        final ConnectorComponentContext connectorComponentContext;
        final ConnectorTransactionContext connectorTransactionContext;
        final ReadOnlyContext context;
        final String contextID;
        final Object scope;

        ThreadContext(Set unshareableResources, ConnectorComponentContext connectorComponentContext, ConnectorTransactionContext connectorTransactionContext, ReadOnlyContext context, String contextID, Object scope) {
            this.unshareableResources = unshareableResources;
            this.connectorComponentContext = connectorComponentContext;
            this.connectorTransactionContext = connectorTransactionContext;
            this.context = context;
            this.contextID = contextID;
            this.scope = scope;
        }
    }
}
