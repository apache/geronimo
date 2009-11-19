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

import org.apache.geronimo.jetty8.AbstractPreHandler;
import org.apache.geronimo.jetty8.PreHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.server.Request;

/**
 *
 * @version $Rev$ $Date$
 */
public class ClusteredSessionHandler extends SessionHandler {
    private final PreHandler chainedHandler;
    
    public ClusteredSessionHandler(ClusteredSessionManager sessionManager, PreHandler chainedHandler) {
        if (null == chainedHandler) {
            throw new IllegalArgumentException("chainedHandler is required");
        }
        this.chainedHandler = chainedHandler;
        chainedHandler.setNextHandler(new ActualHandler());

        setSessionManager(sessionManager);
    }
    
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        setRequestedId(baseRequest, request);
        try {
            chainedHandler.handle(target, baseRequest, request, response);
        } catch (ServletException e) {
            throw (IOException) new IOException().initCause(e);
        }
    }
    
    protected void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        super.handle(target, baseRequest, request, response);
    }

    private class ActualHandler extends AbstractPreHandler {

        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                throws IOException, ServletException {
            doHandle(target, baseRequest, request, response);
        }
    }

}
