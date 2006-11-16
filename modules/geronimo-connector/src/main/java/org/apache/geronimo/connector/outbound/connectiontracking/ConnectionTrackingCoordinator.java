/**
 *
 * Copyright 2003-2005 The Apache Software Foundation
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

package org.apache.geronimo.connector.outbound.connectiontracking;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.resource.ResourceException;

import org.apache.geronimo.connector.outbound.ConnectionInfo;
import org.apache.geronimo.connector.outbound.ConnectionTrackingInterceptor;
import org.apache.geronimo.connector.outbound.ManagedConnectionInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ConnectionTrackingCoordinator tracks connections that are in use by
 * components such as EJB's.  The component must notify the ccm
 * when a method enters and exits.  On entrance, the ccm will
 * notify ConnectionManager stacks so the stack can make sure all
 * connection handles left open from previous method calls are
 * attached to ManagedConnections of the correct security context, and
 * the ManagedConnections are enrolled in any current transaction.
 * On exit, the ccm will notify ConnectionManager stacks of the handles
 * left open, so they may be disassociated if appropriate.
 * In addition, when a UserTransaction is started the ccm will notify
 * ConnectionManager stacks so the existing ManagedConnections can be
 * enrolled properly.
 *
 * @version $Rev$ $Date$
 */
public class ConnectionTrackingCoordinator implements TrackedConnectionAssociator, ConnectionTracker {
    private static final Log log = LogFactory.getLog(ConnectionTrackingCoordinator.class.getName());

    private final ThreadLocal currentInstanceContexts = new ThreadLocal();

    public ConnectorInstanceContext enter(ConnectorInstanceContext newConnectorInstanceContext)
            throws ResourceException {
        ConnectorInstanceContext oldConnectorInstanceContext = (ConnectorInstanceContext) currentInstanceContexts.get();
        currentInstanceContexts.set(newConnectorInstanceContext);
        notifyConnections(newConnectorInstanceContext);
        return oldConnectorInstanceContext;
    }

    private void notifyConnections(ConnectorInstanceContext oldConnectorInstanceContext) throws ResourceException {
        Map connectionManagerToManagedConnectionInfoMap = oldConnectorInstanceContext.getConnectionManagerMap();
        for (Iterator i = connectionManagerToManagedConnectionInfoMap.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            ConnectionTrackingInterceptor mcci =
                    (ConnectionTrackingInterceptor) entry.getKey();
            Set connections = (Set) entry.getValue();
            mcci.enter(connections);
        }
    }

    public void newTransaction() throws ResourceException {
        ConnectorInstanceContext oldConnectorInstanceContext = (ConnectorInstanceContext) currentInstanceContexts.get();
        if (oldConnectorInstanceContext == null) {
            return;
        }
        notifyConnections(oldConnectorInstanceContext);
    }

    public void exit(ConnectorInstanceContext reenteringConnectorInstanceContext)
            throws ResourceException {
        ConnectorInstanceContext oldConnectorInstanceContext = (ConnectorInstanceContext) currentInstanceContexts.get();
        Map resources = oldConnectorInstanceContext.getConnectionManagerMap();
        for (Iterator i = resources.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            ConnectionTrackingInterceptor mcci =
                    (ConnectionTrackingInterceptor) entry.getKey();
            Set connections = (Set) entry.getValue();
            mcci.exit(connections);
            if (connections.isEmpty()) {
                i.remove();
            }
        }
        currentInstanceContexts.set(reenteringConnectorInstanceContext);
    }


    public void handleObtained(
            ConnectionTrackingInterceptor connectionTrackingInterceptor,
            ConnectionInfo connectionInfo) {
        ConnectorInstanceContext connectorInstanceContext = (ConnectorInstanceContext) currentInstanceContexts.get();
        if (connectorInstanceContext == null) {
            return;
        }
        Map resources = connectorInstanceContext.getConnectionManagerMap();
        Set infos = (Set) resources.get(connectionTrackingInterceptor);
        if (infos == null) {
            infos = new HashSet();
            resources.put(connectionTrackingInterceptor, infos);
        }
        infos.add(connectionInfo);
    }

    public void handleReleased(
            ConnectionTrackingInterceptor connectionTrackingInterceptor,
            ConnectionInfo connectionInfo) {
        ConnectorInstanceContext connectorInstanceContext = (ConnectorInstanceContext) currentInstanceContexts.get();
        if (connectorInstanceContext == null) {
            return;
        }
        Map resources = connectorInstanceContext.getConnectionManagerMap();
        Set infos = (Set) resources.get(connectionTrackingInterceptor);
        if (infos == null) {
            if (log.isTraceEnabled()) {
                log.trace("No infos found for handle " + connectionInfo.getConnectionHandle() + " for MCI: " + connectionInfo.getManagedConnectionInfo() + " for MC: " + connectionInfo.getManagedConnectionInfo().getManagedConnection() + " for CTI: " + connectionTrackingInterceptor, new Exception("Stack Trace"));
            }
        }
        if (connectionInfo.getConnectionHandle() == null) {
            //destroy was called as a result of an error
            ManagedConnectionInfo mci = connectionInfo.getManagedConnectionInfo();
            Collection toRemove = mci.getConnectionInfos();
            infos.removeAll(toRemove);
        } else {
            infos.remove(connectionInfo);
        }
    }

    public void setEnvironment(ConnectionInfo connectionInfo, String key) {
        ConnectorInstanceContext currentConnectorInstanceContext = (ConnectorInstanceContext) currentInstanceContexts.get();
        if (currentConnectorInstanceContext != null) {
            Set unshareableResources = currentConnectorInstanceContext.getUnshareableResources();
            boolean unshareable = unshareableResources.contains(key);
            connectionInfo.setUnshareable(unshareable);
            Set applicationManagedSecurityResources = currentConnectorInstanceContext.getApplicationManagedSecurityResources();
            boolean applicationManagedSecurity = applicationManagedSecurityResources.contains(key);
            connectionInfo.setApplicationManagedSecurity(applicationManagedSecurity);
        }
    }

}
