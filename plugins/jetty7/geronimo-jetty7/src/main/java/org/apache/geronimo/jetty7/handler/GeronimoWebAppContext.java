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


package org.apache.geronimo.jetty7.handler;

import java.io.IOException;
import java.util.Map; 
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectorInstanceContext;
import org.apache.geronimo.connector.outbound.connectiontracking.SharedConnectorInstanceContext;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoWebAppContext extends WebAppContext {

    private Handler handler;
    protected final IntegrationContext integrationContext;
    protected Map<String, String> contextParamMap; 


    public GeronimoWebAppContext(SecurityHandler securityHandler, SessionHandler sessionHandler, ServletHandler servletHandler, ErrorHandler errorHandler, IntegrationContext integrationContext, ClassLoader classLoader) {
        super(sessionHandler, securityHandler, servletHandler, errorHandler);
        this.integrationContext = integrationContext;
        setClassLoader(classLoader);
    }
    
    /**
     * Set any context parameters that need to be set during 
     * the doStart() phase of the initialization.  
     * 
     * @param contextParamMap
     *               The parameter map;
     */
    public void setContextParamMap(Map<String, String> contextParamMap) {
        this.contextParamMap = contextParamMap; 
    }

    public void setTwistyHandler(Handler handler)  
    {  
        this.handler = handler;
    }

    public Handler newTwistyHandler() {
        return new TwistyHandler();
    }

    @Override
    protected void doStart() throws Exception {
        // jetty 7.2.0 forces the setInitParameter() calls to be delayed until 
        // the doStart() method is called.  Set these before allowing the superclass to 
        // complete startup. 
        if (contextParamMap != null && contextParamMap.size() > 0) {
            
            for (Entry<String, String> entry : contextParamMap.entrySet()) {

                getServletContext().setInitParameter(entry.getKey(), entry.getValue());
            }
        }
        
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
    protected void doStop() throws Exception {
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

    @Override
    public void doScope(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        javax.naming.Context context = integrationContext.setContext();
        boolean txActive = integrationContext.isTxActive();
        SharedConnectorInstanceContext newContext = integrationContext.newConnectorInstanceContext(baseRequest);
        ConnectorInstanceContext connectorContext = integrationContext.setConnectorInstance(baseRequest, newContext);
        try {
            try {
                super.doScope(target, baseRequest, request, response);
            } finally {
                integrationContext.restoreConnectorContext(connectorContext, baseRequest, newContext);
            }
        } finally {
            integrationContext.restoreContext(context);
            integrationContext.completeTx(txActive, baseRequest);
        }
    }


//    @Override
//    public void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
//        handler.handle(target, baseRequest, request, response);
//    }

    private class TwistyHandler implements Handler {

        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            GeronimoWebAppContext.super.doHandle(target, baseRequest, request, response);
        }

        public void setServer(Server server) {
             GeronimoWebAppContext.super.setServer(server);
        }

        public Server getServer() {
            return GeronimoWebAppContext.super.getServer();
        }

        public void destroy() {
            GeronimoWebAppContext.super.destroy();
        }

        public void start() throws Exception {
            GeronimoWebAppContext.super.start();
        }

        public void stop() throws Exception {
            GeronimoWebAppContext.super.stop();
        }

        public boolean isRunning() {
            return GeronimoWebAppContext.super.isRunning();
        }

        public boolean isStarted() {
            return GeronimoWebAppContext.super.isStarted();
        }

        public boolean isStarting() {
            return GeronimoWebAppContext.super.isStarting();
        }

        public boolean isStopping() {
            return GeronimoWebAppContext.super.isStopping();
        }

        public boolean isStopped() {
            return GeronimoWebAppContext.super.isStopped();
        }

        public boolean isFailed() {
            return GeronimoWebAppContext.super.isFailed();
        }

        public void addLifeCycleListener(Listener listener) {
        }

        public void removeLifeCycleListener(Listener listener) {
        }
    }

}
