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
package org.apache.geronimo.jetty6.cluster.wadi;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.geronimo.clustering.ClusteredInvocation;
import org.apache.geronimo.clustering.ClusteredInvocationException;
import org.apache.geronimo.jetty6.cluster.AbstractClusteredPreHandler;
import org.codehaus.wadi.InvocationException;
import org.codehaus.wadi.impl.ClusteredManager;
import org.codehaus.wadi.web.impl.WebInvocation;


/**
 * 
 * @version $Rev$ $Date$
 */
public class WADIClusteredPreHandler extends AbstractClusteredPreHandler {
    private final ClusteredManager wadiManager;

    public WADIClusteredPreHandler(ClusteredManager wadiManager) {
        this.wadiManager = wadiManager;
    }
    
    protected ClusteredInvocation newClusteredInvocation(String target, HttpServletRequest request,
            HttpServletResponse response, int dispatch) {
        return new WADIWebClusteredInvocation(target, request, response, dispatch);
    }
    
    protected class WADIWebClusteredInvocation extends WebClusteredInvocation {
        
        public WADIWebClusteredInvocation(String target, HttpServletRequest request,
                HttpServletResponse response, int dispatch) {
            super(target, request, response, dispatch);
        }

        public void invoke() throws ClusteredInvocationException {
            WebInvocation invocation = new WebInvocation();
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
