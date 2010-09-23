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

package org.apache.geronimo.tomcat.core;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;

import org.apache.catalina.LifecycleState;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.ApplicationContext;
import org.apache.geronimo.tomcat.GeronimoStandardContext;
import org.apache.geronimo.web.security.WebSecurityConstraintStore;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoApplicationContext extends ApplicationContext {

    private GeronimoStandardContext context;

    private WebSecurityConstraintStore webSecurityConstraintStore;
    /**
     * @param context
     */
    public GeronimoApplicationContext(GeronimoStandardContext context) {
        super(context);
        this.context = context;
    }

    @Override
    public Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) throws IllegalStateException {
        Dynamic dynamic = super.addServlet(servletName, servletClass);
        if (!context.getConfigured() || webSecurityConstraintStore == null) {
            return dynamic;
        }
        webSecurityConstraintStore.addContainerCreatedDynamicServletEntry(servletName, servletClass.getName());
        return createGeronimoApplicationServletRegistrationAdapter(dynamic, servletName);
    }

    @Override
    public Dynamic addServlet(String servletName, Servlet servlet) throws IllegalStateException {
        Dynamic dynamic = super.addServlet(servletName, servlet);
        if (!context.getConfigured() || webSecurityConstraintStore == null) {
            return dynamic;
        }
        if (webSecurityConstraintStore.isContainerCreatedDynamicServlet(servlet)) {
            webSecurityConstraintStore.addContainerCreatedDynamicServletEntry(servletName, servlet.getClass().getName());
        }
        return createGeronimoApplicationServletRegistrationAdapter(dynamic, servletName);
    }

    @Override
    public Dynamic addServlet(String servletName, String servletClass) throws IllegalStateException {
        Dynamic dynamic = super.addServlet(servletName, servletClass);
        if (!context.getConfigured() || webSecurityConstraintStore == null) {
            return dynamic;
        }
        webSecurityConstraintStore.addContainerCreatedDynamicServletEntry(servletName, servletClass);
        return createGeronimoApplicationServletRegistrationAdapter(dynamic, servletName);
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> c) throws ServletException  {
        T servlet = super.createServlet(c);
        if (!context.getConfigured() || webSecurityConstraintStore == null) {
            webSecurityConstraintStore.addContainerCreatedDynamicServlet(servlet);
        }
        return servlet;
    }

    @Override
    public void declareRoles(String... roles) {
        if (!context.getConfigured() || webSecurityConstraintStore == null) {
            super.declareRoles(roles);
            return;
        }
        if (!context.getState().equals(LifecycleState.STARTING_PREP)) {
            throw new IllegalStateException("declareRoles is not allowed to invoke after the ServletContext is initialized");
        }
        webSecurityConstraintStore.declareRoles(roles);
    }

    protected Dynamic createGeronimoApplicationServletRegistrationAdapter(Dynamic applicationServletRegistration, String servletName) {
        if (applicationServletRegistration == null) {
            return null;
        }
        return new GeronimoApplicationServletRegistrationAdapter(context, this, (Wrapper) context.findChild(servletName), applicationServletRegistration);
    }

    public WebSecurityConstraintStore getWebSecurityConstraintStore() {
        return webSecurityConstraintStore;
    }

    public void setWebSecurityConstraintStore(WebSecurityConstraintStore webSecurityConstraintStore) {
        this.webSecurityConstraintStore = webSecurityConstraintStore;
    }
}
