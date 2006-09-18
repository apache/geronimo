/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.clustering.wadi;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.geronimo.clustering.ClusteredInvocation;
import org.apache.geronimo.clustering.ClusteredInvocationException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.jetty.HandleInterceptor;
import org.apache.geronimo.jetty.cluster.AbstractClusteredHandleInterceptor;
import org.codehaus.wadi.InvocationException;
import org.codehaus.wadi.impl.ClusteredManager;
import org.codehaus.wadi.web.impl.WebInvocation;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.jetty.servlet.ServletHttpRequest;
import org.mortbay.jetty.servlet.ServletHttpResponse;


/**
 * 
 * @version $Rev$ $Date$
 */
public class WADIClusteredHandleInterceptor extends AbstractClusteredHandleInterceptor implements GBeanLifecycle {
    private final WADISessionManager sessionManager;
    
    private ClusteredManager wadiManager;

    public WADIClusteredHandleInterceptor(WADISessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }
    
    public void doStart() throws Exception {
        wadiManager = sessionManager.getManager();
    }
    
    public void doStop() throws Exception {
        wadiManager = null;
    }
    
    public void doFail() {
        wadiManager = null;
    }
    
    protected ClusteredInvocation newClusteredInvocation(String pathInContext, String pathParams, HttpRequest request,
            HttpResponse response, HandleInterceptor end) {
        return new WADIWebClusteredInvocation(pathInContext, pathParams, request, response, end);
    }
    
    protected class WADIWebClusteredInvocation extends WebClusteredInvocation {
        
        public WADIWebClusteredInvocation(String pathInContext, String pathParams, HttpRequest request,
                HttpResponse response, HandleInterceptor end) {
            super(pathInContext, pathParams, request, response, end);
        }

        public void invoke() throws ClusteredInvocationException {
            ServletHttpRequest servletHttpRequest = (ServletHttpRequest) request.getWrapper();
            ServletHttpResponse servletHttpResponse = (ServletHttpResponse) response.getWrapper();

            WebInvocation invocation = WebInvocation.getThreadLocalInstance();
            FilterChain chainAdapter = new FilterChain() {
                public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                    try {
                        invokeLocally();
                    } catch (ClusteredInvocationException e) {
                        throw (IOException) new IOException().initCause(e);
                    }
                }
            };
            invocation.init(servletHttpRequest, servletHttpResponse, chainAdapter);
            try {
                wadiManager.contextualise(invocation);
            } catch (InvocationException e) {
                Throwable throwable = e.getCause();
                if (throwable instanceof IOException) {
                    throw new ClusteredInvocationException(throwable);
                } else if (throwable instanceof ServletException) {
                    throw new ClusteredInvocationException(throwable);
                } else {
                    throw new ClusteredInvocationException(e);
                }
            }
        }
    }
    
    public static final GBeanInfo GBEAN_INFO;

    public static final String GBEAN_REF_WADI_SESSION_MANAGER = "WADISessionManager";

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic("WADI Clustered Handle Interceptor",
                WADIClusteredHandleInterceptor.class, NameFactory.GERONIMO_SERVICE);
        
        infoBuilder.addReference(GBEAN_REF_WADI_SESSION_MANAGER, WADISessionManager.class, 
                NameFactory.GERONIMO_SERVICE);
        
        infoBuilder.setConstructor(new String[]{GBEAN_REF_WADI_SESSION_MANAGER});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }
    
    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
