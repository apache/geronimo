/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.transaction.InstanceContext;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

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

    public final static GBeanInfo GBEAN_INFO;

    private final ThreadLocal currentInstanceContexts = new ThreadLocal();

    public InstanceContext enter(InstanceContext newInstanceContext)
            throws ResourceException {
        InstanceContext oldInstanceContext = (InstanceContext) currentInstanceContexts.get();
        currentInstanceContexts.set(newInstanceContext);
        notifyConnections(newInstanceContext);
        return oldInstanceContext;
    }

    private void notifyConnections(InstanceContext oldInstanceContext) throws ResourceException {
        Map connectionManagerToManagedConnectionInfoMap = oldInstanceContext.getConnectionManagerMap();
        for (Iterator i = connectionManagerToManagedConnectionInfoMap.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            ConnectionTrackingInterceptor mcci =
                    (ConnectionTrackingInterceptor) entry.getKey();
            Set connections = (Set) entry.getValue();
            mcci.enter(connections);
        }
    }

    public void newTransaction() throws ResourceException {
        InstanceContext oldInstanceContext = (InstanceContext) currentInstanceContexts.get();
        notifyConnections(oldInstanceContext);
    }

    public void exit(InstanceContext reenteringInstanceContext)
            throws ResourceException {
        InstanceContext oldInstanceContext = (InstanceContext) currentInstanceContexts.get();
        Map resources = oldInstanceContext.getConnectionManagerMap();
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
        currentInstanceContexts.set(reenteringInstanceContext);
    }


    public void handleObtained(
            ConnectionTrackingInterceptor connectionTrackingInterceptor,
            ConnectionInfo connectionInfo) {
        InstanceContext instanceContext = (InstanceContext) currentInstanceContexts.get();
        if (instanceContext == null) {
            return;
        }
        Map resources = instanceContext.getConnectionManagerMap();
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
        InstanceContext instanceContext = (InstanceContext) currentInstanceContexts.get();
        if (instanceContext == null) {
            return;
        }
        Map resources = instanceContext.getConnectionManagerMap();
        Set infos = (Set) resources.get(connectionTrackingInterceptor);
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
        InstanceContext currentInstanceContext = (InstanceContext) currentInstanceContexts.get();
        if (currentInstanceContext != null) {
            Set unshareableResources = currentInstanceContext.getUnshareableResources();
            boolean unshareable = unshareableResources.contains(key);
            connectionInfo.setUnshareable(unshareable);
            Set applicationManagedSecurityResources = currentInstanceContext.getApplicationManagedSecurityResources();
            boolean applicationManagedSecurity = applicationManagedSecurityResources.contains(key);
            connectionInfo.setApplicationManagedSecurity(applicationManagedSecurity);
        }
    }

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(ConnectionTrackingCoordinator.class, NameFactory.JCA_RESOURCE);
        infoFactory.addInterface(TrackedConnectionAssociator.class);
        infoFactory.addInterface(ConnectionTracker.class);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
