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
package org.apache.geronimo.jetty8.cluster.wadi;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.geronimo.clustering.ClusteredInvocation;
import org.apache.geronimo.clustering.ClusteredInvocationException;
import org.apache.geronimo.jetty8.cluster.AbstractClusteredPreHandler;
import org.codehaus.wadi.core.contextualiser.InvocationException;
import org.codehaus.wadi.core.manager.Manager;
import org.codehaus.wadi.web.impl.WebInvocation;
import org.eclipse.jetty.server.Request;


/**
 * 
 * @version $Rev$ $Date$
 */
public class WADIClusteredPreHandler extends AbstractClusteredPreHandler {
    private final Manager wadiManager;

    public WADIClusteredPreHandler(Manager wadiManager) {
        this.wadiManager = wadiManager;
    }
    
    protected ClusteredInvocation newClusteredInvocation(String target, Request baseRequest, HttpServletRequest request,
                                                         HttpServletResponse response) {
        return new WADIWebClusteredInvocation(target, baseRequest, request, response);
    }
    
    protected class WADIWebClusteredInvocation extends WebClusteredInvocation {
        
        public WADIWebClusteredInvocation(String target, Request baseRequest, HttpServletRequest request,
                                          HttpServletResponse response) {
            super(target, baseRequest, request, response);
        }

        public void invoke() throws ClusteredInvocationException {
            WebInvocation invocation = new WebInvocation(5000);
            FilterChain chainAdapter = new FilterChain() {
                public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                    try {
                        invokeLocally();
                    } catch (ClusteredInvocationException e) {
                        throw (IOException) new IOException().initCause(e);
                    }
                }
            };
            invocation.init(request, response, chainAdapter);
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
    
}
