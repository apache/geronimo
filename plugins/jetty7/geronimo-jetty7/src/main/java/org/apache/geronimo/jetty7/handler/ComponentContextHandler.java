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


package org.apache.geronimo.jetty7.handler;

import java.io.IOException;

import javax.naming.Context;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.geronimo.naming.java.RootContext;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;

/**
 * @version $Rev$ $Date$
 */
public class ComponentContextHandler extends AbstractImmutableHandler {

    private final Context componentContext;

    public ComponentContextHandler(Handler next, Context componentContext) {
        super(next);
        this.componentContext = componentContext;
    }

    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Context oldContext = RootContext.getComponentContext();
        try {
            RootContext.setComponentContext(componentContext);
            next.handle(target, baseRequest, request, response);
        } finally {
            RootContext.setComponentContext(oldContext);
        }
    }

    public void lifecycleCommand(LifecycleCommand lifecycleCommand) throws Exception {
        Context oldContext = RootContext.getComponentContext();
        try {
            RootContext.setComponentContext(componentContext);
            super.lifecycleCommand(lifecycleCommand);
        } finally {
            RootContext.setComponentContext(oldContext);
        }
    }

}
