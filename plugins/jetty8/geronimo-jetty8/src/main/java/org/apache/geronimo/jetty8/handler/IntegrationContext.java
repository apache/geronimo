/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.jetty8.handler;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.resource.ResourceException;
import javax.servlet.DispatcherType;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectorInstanceContext;
import org.apache.geronimo.connector.outbound.connectiontracking.SharedConnectorInstanceContext;
import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.j2ee.annotation.Holder;
import org.apache.geronimo.naming.java.RootContext;
import org.eclipse.jetty.server.Request;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class IntegrationContext {

    private final Context componentContext;
    private final Set<String> unshareableResources;
    private final Set<String> applicationManagedSecurityResources;
    private final TrackedConnectionAssociator trackedConnectionAssociator;
    private final UserTransaction userTransaction;
    private final Bundle bundle;
    private final Holder holder;
    private final Map<ServletContainerInitializer, Set<Class<?>>> servletContainerInitializerMap;
    private final String webApplicationIdentity;

    public IntegrationContext(Context componentContext, Set<String> unshareableResources, Set<String> applicationManagedSecurityResources, TrackedConnectionAssociator trackedConnectionAssociator, UserTransaction userTransaction, Bundle bundle, Holder holder, Map<ServletContainerInitializer, Set<Class<?>>> servletContainerInitializerMap, String webApplicationIdentity) {
        this.componentContext = componentContext;
        this.unshareableResources = unshareableResources;
        this.applicationManagedSecurityResources = applicationManagedSecurityResources;
        this.trackedConnectionAssociator = trackedConnectionAssociator;
        this.userTransaction = userTransaction;
        this.bundle = bundle;
        this.holder = holder;
        this.servletContainerInitializerMap = servletContainerInitializerMap == null? Collections.<ServletContainerInitializer, Set<Class<?>>>emptyMap(): servletContainerInitializerMap;
        this.webApplicationIdentity = webApplicationIdentity;
    }

    public Context getComponentContext() {
        return componentContext;
    }

    public Set<String> getUnshareableResources() {
        return unshareableResources;
    }

    public Set<String> getApplicationManagedSecurityResources() {
        return applicationManagedSecurityResources;
    }

    public TrackedConnectionAssociator getTrackedConnectionAssociator() {
        return trackedConnectionAssociator;
    }

    public UserTransaction getUserTransaction() {
        return userTransaction;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public Holder getHolder() {
        return holder;
    }

    public String getWebApplicationIdentity(){
        return webApplicationIdentity;
    }

    public SharedConnectorInstanceContext newConnectorInstanceContext(Request baseRequest) {
        return new SharedConnectorInstanceContext(getUnshareableResources(),
                getApplicationManagedSecurityResources(),
                !isDispatch(baseRequest));
    }

    private boolean isDispatch(Request baseRequest) {
        if (baseRequest == null) return true;
        return DispatcherType.REQUEST.equals(baseRequest.getDispatcherType());
    }

    public ConnectorInstanceContext setConnectorInstance(Request baseRequest, SharedConnectorInstanceContext newContext) throws ServletException {
        try {
            SharedConnectorInstanceContext oldContext = (SharedConnectorInstanceContext) getTrackedConnectionAssociator().enter(newContext);
            if (oldContext != null && !isDispatch(baseRequest)) {
                newContext.share(oldContext);
            }
            return oldContext;
        } catch (ResourceException e) {
            throw new ServletException(e);
        }
    }

    public void restoreConnectorContext(ConnectorInstanceContext oldConnectorContext, Request baseRequest, SharedConnectorInstanceContext newContext) throws ServletException {
        try {
            if (isDispatch(baseRequest)) {
                getTrackedConnectionAssociator().exit(oldConnectorContext);
            } else {
                newContext.hide();
                getTrackedConnectionAssociator().exit(oldConnectorContext);
            }
        } catch (ResourceException e) {
            throw new ServletException(e);
        }
    }


    public javax.naming.Context setContext() {
        javax.naming.Context oldContext = RootContext.getComponentContext();
        RootContext.setComponentContext(getComponentContext());
        return oldContext;
    }

    public void restoreContext(javax.naming.Context context) {
        RootContext.setComponentContext(context);
    }

    public boolean isTxActive() throws ServletException {
        try {
            return !(getUserTransaction().getStatus() == Status.STATUS_NO_TRANSACTION
                    || getUserTransaction().getStatus() == Status.STATUS_COMMITTED);
        } catch (SystemException e) {
            throw new ServletException("Could not determine transaction status", e);
        }
    }

    private boolean isMarkedRollback() throws ServletException {
        try {
            return getUserTransaction().getStatus() == Status.STATUS_MARKED_ROLLBACK;
        } catch (SystemException e) {
            throw new ServletException("Could not determine transaction status", e);
        }
    }


    public void completeTx(boolean txActive, Request baseRequest) throws ServletException {
        if ((!txActive && isMarkedRollback()) || (isDispatch(baseRequest) && isTxActive())) {
            try {
                getUserTransaction().rollback();
            } catch (SystemException e) {
                throw new ServletException("Error rolling back transaction left open by user program", e);
            }
        }
    }

    public Map<ServletContainerInitializer, Set<Class<?>>> getServletContainerInitializerMap() {
        return servletContainerInitializerMap;
    }
}
