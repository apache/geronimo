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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.resource.ResourceException;

import org.apache.geronimo.connector.outbound.ConnectionInfo;
import org.apache.geronimo.connector.outbound.ConnectionTrackingInterceptor;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.transaction.TransactionContext;
import org.apache.geronimo.transaction.InstanceContext;

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
 * @version $Revision: 1.5 $ $Date: 2004/03/10 09:58:33 $
 */
public class ConnectionTrackingCoordinator implements TrackedConnectionAssociator, ConnectionTracker {

    public final static GBeanInfo GBEAN_INFO;

    private final ThreadLocal currentInstanceContexts = new ThreadLocal();
    private final ThreadLocal currentConnectorTransactionContexts = new ThreadLocal();
    private final ThreadLocal currentUnshareableResources = new ThreadLocal();

    public InstanceContext enter(InstanceContext newInstanceContext)
            throws ResourceException {
        InstanceContext oldInstanceContext = (InstanceContext) currentInstanceContexts.get();
        currentInstanceContexts.set(newInstanceContext);
        return oldInstanceContext;
    }

    public void exit(InstanceContext reenteringInstanceContext,
            Set unshareableResources)
            throws ResourceException {
        InstanceContext oldInstanceContext = (InstanceContext) currentInstanceContexts.get();
        Map resources = oldInstanceContext.getConnectionManagerMap();
        for (Iterator i = resources.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            ConnectionTrackingInterceptor mcci =
                    (ConnectionTrackingInterceptor) entry.getKey();
            Set connections = (Set) entry.getValue();
            mcci.exit(connections, unshareableResources);
            if (connections.isEmpty()) {
                i.remove();
            }
        }
        currentInstanceContexts.set(reenteringInstanceContext);
    }

    public Set setUnshareableResources(Set unshareableResources) {
        Set oldUnshareableResources = (Set) currentUnshareableResources.get();
        currentUnshareableResources.set(unshareableResources);
        return oldUnshareableResources;
    }

    public TransactionContext setTransactionContext(TransactionContext newTransactionContext) throws ResourceException {
        TransactionContext oldConnectorTransactionContext = (TransactionContext) currentConnectorTransactionContexts.get();
        currentConnectorTransactionContexts.set(newTransactionContext);
        InstanceContext instanceContext = (InstanceContext) currentInstanceContexts.get();
        Set unshareableResources = (Set) currentUnshareableResources.get();
        Map connectionManagerToManagedConnectionInfoMap = instanceContext.getConnectionManagerMap();
        for (Iterator i = connectionManagerToManagedConnectionInfoMap.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            ConnectionTrackingInterceptor mcci =
                    (ConnectionTrackingInterceptor) entry.getKey();
            Set connections = (Set) entry.getValue();
            mcci.enter(connections, unshareableResources);
        }
        return oldConnectorTransactionContext;
    }

    public void resetTransactionContext(TransactionContext transactionContext) {
        currentConnectorTransactionContexts.set(transactionContext);
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
        //It's not at all clear that an equal ci will be supplied here
        infos.remove(connectionInfo);
    }

    public TransactionContext getTransactionContext() {
        return (TransactionContext) currentConnectorTransactionContexts.get();
    }

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(ConnectionTrackingCoordinator.class.getName());
        infoFactory.addOperation(new GOperationInfo("enter", new String[]{InstanceContext.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("exit", new String[]{InstanceContext.class.getName(), Set.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("setTransactionContext", new String[]{TransactionContext.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("setUnshareableResources", new String[]{Set.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("resetTransactionContext", new String[]{TransactionContext.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("handleObtained", new String[]{ConnectionTrackingInterceptor.class.getName(), ConnectionInfo.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("handleReleased", new String[]{ConnectionTrackingInterceptor.class.getName(), ConnectionInfo.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("getTransactionContext"));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
