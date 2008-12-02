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
package org.apache.geronimo.jetty6.cluster;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.geronimo.clustering.ClusteredInvocation;
import org.apache.geronimo.clustering.ClusteredInvocationException;
import org.apache.geronimo.jetty6.AbstractPreHandler;
import org.mortbay.component.LifeCycle;
import org.mortbay.jetty.HttpException;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractClusteredPreHandler extends AbstractPreHandler {

    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
            throws IOException, ServletException {
        ClusteredInvocation invocation = newClusteredInvocation(target, request, response, dispatch);
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

    public void addLifeCycleListener(LifeCycle.Listener listener) {
    }

    public void removeLifeCycleListener(LifeCycle.Listener listener) {
    }

    protected abstract ClusteredInvocation newClusteredInvocation(String target,
            HttpServletRequest request, HttpServletResponse response, int dispatch);


    protected abstract class WebClusteredInvocation implements ClusteredInvocation {
        protected final String target;
        protected final HttpServletRequest request;
        protected final HttpServletResponse response;
        protected final int dispatch;

        protected WebClusteredInvocation(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) {
            this.target = target;
            this.request = request;
            this.response = response;
            this.dispatch = dispatch;
        }

        protected void invokeLocally() throws ClusteredInvocationException {
            try {
                next.handle(target, request, response, dispatch);
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
