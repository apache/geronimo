/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


package org.apache.geronimo.jetty7;

import java.io.IOException;

import javax.security.auth.Subject;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectorInstanceContext;
import org.apache.geronimo.connector.outbound.connectiontracking.SharedConnectorInstanceContext;
import org.apache.geronimo.jetty7.handler.IntegrationContext;
import org.apache.geronimo.security.Callers;
import org.apache.geronimo.security.ContextManager;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoServletHolder extends ServletHolder {

    private final IntegrationContext integrationContext;
    private final Subject runAsSubject;
    private final JettyServletRegistration servletRegistration;

    public GeronimoServletHolder(IntegrationContext integrationContext, Subject runAsSubject, JettyServletRegistration servletRegistration) {
        this.integrationContext = integrationContext;
        this.runAsSubject = runAsSubject;
        this.servletRegistration = servletRegistration;
    }

    //TODO probably need to override init and destroy (?) to handle runAsSubject since we are not setting it in the superclass any more.

    /**
     * Service a request with this servlet.  Set the ThreadLocal to hold the
     * current JettyServletHolder.
     */
    public void handle(Request baseRequest, ServletRequest request, ServletResponse response)
            throws ServletException, IOException {
        if (runAsSubject == null) {
            super.handle(baseRequest, request, response);
        } else {
            Callers oldCallers = ContextManager.pushNextCaller(runAsSubject);
            try {
                super.handle(baseRequest, request, response);
            } finally {
                ContextManager.popCallers(oldCallers);
            }
        }
    }


    public synchronized Object newInstance() throws InstantiationException, IllegalAccessException {
        return servletRegistration.newInstance(_className);
    }

    public void destroyInstance(Object o) throws Exception {
        super.destroyInstance(o);
        servletRegistration.destroyInstance(o);
    }

    @Override
    public void doStart() throws Exception {
        javax.naming.Context context = integrationContext.setContext();
        boolean txActive = integrationContext.isTxActive();
        SharedConnectorInstanceContext newContext = integrationContext.newConnectorInstanceContext(null);
        ConnectorInstanceContext connectorContext = integrationContext.setConnectorInstance(null, newContext);
        try {
            try {
                super.doStart();
            } finally {
                integrationContext.restoreConnectorContext(connectorContext, null, newContext);
            }
        } finally {
            integrationContext.restoreContext(context);
            integrationContext.completeTx(txActive, null);
        }
    }

    @Override
    public void doStop() throws Exception {
        javax.naming.Context context = integrationContext.setContext();
        boolean txActive = integrationContext.isTxActive();
        SharedConnectorInstanceContext newContext = integrationContext.newConnectorInstanceContext(null);
        ConnectorInstanceContext connectorContext = integrationContext.setConnectorInstance(null, newContext);
        try {
            try {
                super.doStop();
            } finally {
                integrationContext.restoreConnectorContext(connectorContext, null, newContext);
            }
        } finally {
            integrationContext.restoreContext(context);
            integrationContext.completeTx(txActive, null);
        }
    }

}
