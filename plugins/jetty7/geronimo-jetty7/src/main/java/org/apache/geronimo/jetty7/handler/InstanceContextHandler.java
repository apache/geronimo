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
import java.util.Set;

import javax.resource.ResourceException;
import javax.servlet.ServletException;
import org.eclipse.jetty.server.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectorInstanceContext;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectorInstanceContextImpl;
import org.apache.geronimo.connector.outbound.connectiontracking.SharedConnectorInstanceContext;
import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;

/**
 * @version $Rev$ $Date$
 */
public class InstanceContextHandler extends AbstractImmutableHandler {

    private final Set unshareableResources;
    private final Set applicationManagedSecurityResources;
    private final TrackedConnectionAssociator trackedConnectionAssociator;

    public InstanceContextHandler(Handler next, Set unshareableResources, Set applicationManagedSecurityResources, TrackedConnectionAssociator trackedConnectionAssociator) {
        super(next);
        this.unshareableResources = unshareableResources;
        this.applicationManagedSecurityResources = applicationManagedSecurityResources;
        this.trackedConnectionAssociator = trackedConnectionAssociator;
    }

    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        DispatcherType dispatch = baseRequest.getDispatcherType();
        try {
            if (DispatcherType.REQUEST.equals(dispatch)) {
                ConnectorInstanceContext oldContext = trackedConnectionAssociator.enter(new SharedConnectorInstanceContext(unshareableResources, applicationManagedSecurityResources, false));

                try {
                    next.handle(target, baseRequest, request, response);
                } finally {
                    trackedConnectionAssociator.exit(oldContext);
                }
            } else {
                SharedConnectorInstanceContext context = new SharedConnectorInstanceContext(unshareableResources, applicationManagedSecurityResources, true);
                SharedConnectorInstanceContext oldContext = (SharedConnectorInstanceContext) trackedConnectionAssociator.enter(context);
                context.share(oldContext);
                try {
                    next.handle(target, baseRequest, request, response);
                } finally {
                    context.hide();
                    trackedConnectionAssociator.exit(oldContext);
                }
            }
        } catch (ResourceException e) {
            throw new ServletException(e);
        }
    }

    public void lifecycleCommand(LifecycleCommand lifecycleCommand) throws Exception {
        ConnectorInstanceContext oldContext = trackedConnectionAssociator.enter(new ConnectorInstanceContextImpl(unshareableResources, applicationManagedSecurityResources));
        try {
            super.lifecycleCommand(lifecycleCommand);
        } finally {
            trackedConnectionAssociator.exit(oldContext);
        }
    }
}
