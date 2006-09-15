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
package org.apache.geronimo.jetty.cluster;

import java.io.IOException;

import org.apache.geronimo.clustering.ClusteredInvocation;
import org.apache.geronimo.clustering.ClusteredInvocationException;
import org.apache.geronimo.jetty.GeronimoServletHttpRequest;
import org.apache.geronimo.jetty.HandleInterceptor;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;

/**
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractClusteredHandleInterceptor implements HandleInterceptor {

    public void handle(String pathInContext, String pathParams, HttpRequest httpRequest, HttpResponse httpResponse,
            HandleInterceptor end) throws HttpException, IOException {
        ClusteredInvocation invocation = 
            newClusteredInvocation(pathInContext, pathParams, httpRequest, httpResponse, end);
        try {
            invocation.invoke();
        } catch (ClusteredInvocationException e) {
            Throwable cause = e.getCause();
            if (cause instanceof HttpException) {
                throw (HttpException) cause;
            } else if (cause instanceof IOException) {
                throw (IOException) cause;
            } else {
                throw (IOException) new IOException().initCause(cause);
            }
        }
    }
    
    protected abstract ClusteredInvocation newClusteredInvocation(String pathInContext, String pathParams,
            HttpRequest request, HttpResponse response, HandleInterceptor end);
    
    protected abstract class WebClusteredInvocation implements ClusteredInvocation {
        protected final String pathInContext; 
        protected final String pathParams;
        protected final HttpRequest request;
        protected final HttpResponse response;
        protected final HandleInterceptor end;
        
        public WebClusteredInvocation(String pathInContext, String pathParams, HttpRequest request,
                HttpResponse response, HandleInterceptor end) {
            this.pathInContext = pathInContext;
            this.pathParams = pathParams;
            this.request = request;
            this.response = response;
            this.end = end;

            GeronimoServletHttpRequest servletHttpRequest = (GeronimoServletHttpRequest) request.getWrapper();
            servletHttpRequest.setRequestedSessionId(pathParams);
        }

        protected void invokeLocally() throws ClusteredInvocationException {
            try {
                end.handle(pathInContext, pathParams, request, response, null);
            } catch (IOException e) {
                throw new ClusteredInvocationException(e);
            }
        }

        public String getRequestedSessionId() {
            GeronimoServletHttpRequest servletHttpRequest = (GeronimoServletHttpRequest) request.getWrapper();
            return servletHttpRequest.getRequestedSessionId();
        }
    }
}
