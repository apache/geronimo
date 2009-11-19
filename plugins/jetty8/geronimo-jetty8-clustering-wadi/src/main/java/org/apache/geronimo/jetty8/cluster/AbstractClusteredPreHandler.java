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
package org.apache.geronimo.jetty8.cluster;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.geronimo.clustering.ClusteredInvocation;
import org.apache.geronimo.clustering.ClusteredInvocationException;
import org.apache.geronimo.jetty8.AbstractPreHandler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.http.HttpException;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractClusteredPreHandler extends AbstractPreHandler {

    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        ClusteredInvocation invocation = newClusteredInvocation(target, baseRequest, request, response);
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

    protected abstract ClusteredInvocation newClusteredInvocation(String target,
                                                                  Request baseRequest, HttpServletRequest request, HttpServletResponse response);


    protected abstract class WebClusteredInvocation implements ClusteredInvocation {
        protected final String target;
        protected final Request baseRequest;
        protected final HttpServletRequest request;
        protected final HttpServletResponse response;

        protected WebClusteredInvocation(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
            this.target = target;
            this.baseRequest = baseRequest;
            this.request = request;
            this.response = response;
        }

        protected void invokeLocally() throws ClusteredInvocationException {
            try {
                next.handle(target, baseRequest, request, response);
            } catch (IOException e) {
                throw new ClusteredInvocationException(e);
            } catch (ServletException e) {
                //WHAT DO I DO HERE???
                throw new ClusteredInvocationException(e);
            }
        }

        public String getRequestedSessionId() {
            return request.getRequestedSessionId();
        }
    }
    
}
