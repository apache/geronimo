/**
 *
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.
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

package org.apache.geronimo.console.databasemanager.connectionmanager;

import java.io.IOException;

import javax.management.ObjectName;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;

import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;

public class ConnectionManagerRenderer {
    private final Kernel kernel;

    public ConnectionManagerRenderer(Kernel kernel) {
        this.kernel = kernel;
    }

    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse, ObjectName gbeanName)
            throws PortletException, IOException {
        if (!"detail".equals(actionRequest.getParameter("mode"))) {
            try {
                J2eeContext j2eeContext = J2eeContextImpl.newContext(gbeanName,
                        NameFactory.JCA_RESOURCE);
                ObjectName connectionManagerName = NameFactory
                        .getComponentName(null, null, null, null, null, null,
                                NameFactory.JCA_CONNECTION_MANAGER, j2eeContext);
                Integer newPartitionMaxSize = Integer.decode(actionRequest
                        .getParameter("partitionMaxSize"));
                Integer oldPartitionMaxSize = (Integer) kernel.getAttribute(
                        connectionManagerName, "partitionMaxSize");
                if (!newPartitionMaxSize.equals(oldPartitionMaxSize)) {
                    kernel.setAttribute(connectionManagerName,
                            "partitionMaxSize", newPartitionMaxSize);
                }
                Integer newPartitionMinSize = Integer.decode(actionRequest
                        .getParameter("partitionMinSize"));
                Integer oldPartitionMinSize = (Integer) kernel.getAttribute(
                        connectionManagerName, "partitionMinSize");
                if (!newPartitionMinSize.equals(oldPartitionMinSize)) {
                    kernel.setAttribute(connectionManagerName,
                            "partitionMinSize", newPartitionMinSize);
                }
                Integer newblockingTimeoutMilliseconds = Integer
                        .decode(actionRequest
                                .getParameter("blockingTimeoutMilliseconds"));
                Integer oldblockingTimeoutMilliseconds = (Integer) kernel
                        .getAttribute(connectionManagerName,
                                "blockingTimeoutMilliseconds");
                if (!newblockingTimeoutMilliseconds
                        .equals(oldblockingTimeoutMilliseconds)) {
                    kernel.setAttribute(connectionManagerName,
                            "blockingTimeoutMilliseconds",
                            newblockingTimeoutMilliseconds);
                }
                Integer newidleTimeoutMinutes = Integer.decode(actionRequest
                        .getParameter("idleTimeoutMinutes"));
                Integer oldidleTimeoutMinutes = (Integer) kernel.getAttribute(
                        connectionManagerName, "idleTimeoutMinutes");
                if (!newidleTimeoutMinutes.equals(oldidleTimeoutMinutes)) {
                    kernel.setAttribute(connectionManagerName,
                            "idleTimeoutMinutes", newidleTimeoutMinutes);
                }
            } catch (Exception e) {
                throw new PortletException(e);
            }
        }
    }

    public void addConnectionManagerInfo(RenderRequest request,
            ObjectName gbeanName) throws PortletException, IOException {
        J2eeContext j2eeContext = J2eeContextImpl.newContext(gbeanName,
                NameFactory.JCA_RESOURCE);
        ConnectionManagerInfo info = null;

        try {
            ObjectName connectionManagerName = NameFactory.getComponentName(
                    null, null, null, null, null, null,
                    NameFactory.JCA_CONNECTION_MANAGER, j2eeContext);
            info = new ConnectionManagerInfo(((Integer) kernel.getAttribute(
                    connectionManagerName, "partitionCount")).intValue(),
                    ((Integer) kernel.getAttribute(connectionManagerName,
                            "connectionCount")).intValue(), ((Integer) kernel
                            .getAttribute(connectionManagerName,
                                    "idleConnectionCount")).intValue(),
                    ((Integer) kernel.getAttribute(connectionManagerName,
                            "partitionMaxSize")).intValue(), ((Integer) kernel
                            .getAttribute(connectionManagerName,
                                    "partitionMinSize")).intValue(),
                    ((Integer) kernel.getAttribute(connectionManagerName,
                            "blockingTimeoutMilliseconds")).intValue(),
                    ((Integer) kernel.getAttribute(connectionManagerName,
                            "idleTimeoutMinutes")).intValue());
        } catch (Exception e) {
            throw new PortletException(e);
        }

        request.setAttribute("connectionManagerInfo", info);
    }
}
