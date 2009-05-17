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

package org.apache.geronimo.tomcat.cluster;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.geronimo.clustering.ClusteredInvocation;
import org.apache.geronimo.clustering.ClusteredInvocationException;

/**
 *
 * @version $Rev:$ $Date:$
 */
public abstract class AbstractClusteredValve extends ValveBase {
    private final Router router;
    
    public AbstractClusteredValve(String nodeName) {
        if (null == nodeName) {
            throw new IllegalArgumentException("nodeName is required");
        }
        router = newRouter(nodeName);
    }

    protected Router newRouter(String nodeName) {
        return new JkRouter(nodeName);
    }

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        if (null == request) {
            next.invoke(request, response);
            return;
        }
        
        ClusteredInvocation invocation = newClusteredInvocation(request, response);
        try {
            invocation.invoke();
        } catch (ClusteredInvocationException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ServletException) {
                throw (ServletException) cause;
            } else if (cause instanceof IOException) {
                throw (IOException) cause;
            } else {
                throw (IOException) new IOException().initCause(e);
            }
        }
    }

    protected abstract ClusteredInvocation newClusteredInvocation(Request request, Response response);

    protected abstract class WebClusteredInvocation implements ClusteredInvocation {
        protected final Request request;
        protected final Response response;

        protected WebClusteredInvocation(Request request, Response response) {
            this.request = request;
            this.response = response;
        }

        protected void invokeLocally() throws ClusteredInvocationException {
            String oldRequestedSessionId = router.replaceRoutingInfoInRequestedSessionId(request);
            try {
                next.invoke(request, response);
            } catch (IOException e) {
                throw new ClusteredInvocationException(e);
            } catch (ServletException e) {
                throw new ClusteredInvocationException(e);
            } finally {
                request.setRequestedSessionId(oldRequestedSessionId);
            }
            
            router.writeSessionIdWithRoutingInfo(request, response);
        }

        public String getRequestedSessionId() {
            if (null == request) {
                return null;
            }
            return request.getRequestedSessionId();
        }
    }
    
}
