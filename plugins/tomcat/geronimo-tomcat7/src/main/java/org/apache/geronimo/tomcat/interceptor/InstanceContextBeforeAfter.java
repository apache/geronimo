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
package org.apache.geronimo.tomcat.interceptor;

import java.util.Set;

import javax.resource.ResourceException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectorInstanceContext;
import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.connector.outbound.connectiontracking.SharedConnectorInstanceContext;

public class InstanceContextBeforeAfter implements BeforeAfter{

    private final BeforeAfter next;
    private final int oldIndex;
    private final int newIndex;
    private final Set unshareableResources;
    private final Set applicationManagedSecurityResources;
    private final TrackedConnectionAssociator trackedConnectionAssociator;

    public InstanceContextBeforeAfter(BeforeAfter next, int oldIndex, int newIndex, Set unshareableResources, Set applicationManagedSecurityResources, TrackedConnectionAssociator trackedConnectionAssociator) {
        this.next = next;
        this.oldIndex = oldIndex;
        this.newIndex = newIndex;
        this.unshareableResources = unshareableResources;
        this.applicationManagedSecurityResources = applicationManagedSecurityResources;
        this.trackedConnectionAssociator = trackedConnectionAssociator;
    }

    public void before(BeforeAfterContext beforeAfterContext, ServletRequest httpRequest, ServletResponse httpResponse, int dispatch) {
        try {
            SharedConnectorInstanceContext newConnectorInstanceContext = new SharedConnectorInstanceContext(unshareableResources, applicationManagedSecurityResources, false);
            ConnectorInstanceContext oldContext = trackedConnectionAssociator.enter(newConnectorInstanceContext);
            if (oldContext != null) {
                newConnectorInstanceContext.share(oldContext);
            }
            beforeAfterContext.contexts[oldIndex] = oldContext;
            beforeAfterContext.clearRequiredFlags[oldIndex] = true;

            beforeAfterContext.contexts[newIndex] = newConnectorInstanceContext;
            beforeAfterContext.clearRequiredFlags[newIndex] = true;
        } catch (ResourceException e) {
            throw new RuntimeException(e);
        }
        try {
            if (next != null) {
                next.before(beforeAfterContext, httpRequest, httpResponse, dispatch);
            }
        } catch (RuntimeException e) {
            cleanUp(beforeAfterContext);
            throw e;
        }
    }

    public void after(BeforeAfterContext beforeAfterContext, ServletRequest httpRequest, ServletResponse httpResponse, int dispatch) {
        try {
            if (next != null) {
                next.after(beforeAfterContext, httpRequest, httpResponse, dispatch);
            }
        } finally {
            cleanUp(beforeAfterContext);
        }
    }

    private void cleanUp(BeforeAfterContext beforeAfterContext) {
        if (beforeAfterContext.clearRequiredFlags[oldIndex]) {
            try {
                ConnectorInstanceContext oldConnectorInstanceContext = (ConnectorInstanceContext) beforeAfterContext.contexts[oldIndex];
                if (beforeAfterContext.clearRequiredFlags[newIndex]) {
                    SharedConnectorInstanceContext newConnectorInstanceContext = (SharedConnectorInstanceContext) beforeAfterContext.contexts[newIndex];
                    if (oldConnectorInstanceContext != null) {
                        newConnectorInstanceContext.hide();
                    }
                    beforeAfterContext.clearRequiredFlags[newIndex] = false;
                }
                trackedConnectionAssociator.exit(oldConnectorInstanceContext);
                beforeAfterContext.clearRequiredFlags[oldIndex] = false;
            } catch (ResourceException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
