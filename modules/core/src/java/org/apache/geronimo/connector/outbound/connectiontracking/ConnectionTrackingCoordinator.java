/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */

package org.apache.geronimo.connector.outbound.connectiontracking;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.resource.ResourceException;

import org.apache.geronimo.connector.outbound.ConnectorComponentContext;
import org.apache.geronimo.connector.outbound.ConnectionTrackingInterceptor;
import org.apache.geronimo.connector.outbound.ConnectionInfo;
import org.apache.geronimo.connector.outbound.ConnectorTransactionContext;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;

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
 * @version $Revision: 1.3 $ $Date: 2004/01/01 09:55:08 $
 */
public class ConnectionTrackingCoordinator implements TrackedConnectionAssociator, ConnectionTracker {

    private final ThreadLocal currentConnectorComponentContexts = new ThreadLocal();
    private final ThreadLocal currentConnectorTransactionContexts = new ThreadLocal();
    private final ThreadLocal currentUnshareableResources = new ThreadLocal();

    public ConnectorComponentContext enter(ConnectorComponentContext newConnectorComponentContext)
            throws ResourceException {
        ConnectorComponentContext oldConnectorComponentContext = (ConnectorComponentContext) currentConnectorComponentContexts.get();
        currentConnectorComponentContexts.set(newConnectorComponentContext);
        return oldConnectorComponentContext;
    }

    public void exit(ConnectorComponentContext reenteringConnectorComponentContext,
                     Set unshareableResources)
            throws ResourceException {
        ConnectorComponentContext oldConnectorComponentContext = (ConnectorComponentContext) currentConnectorComponentContexts.get();
        Map resources = oldConnectorComponentContext.getConnectionManagerMap();
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
        currentConnectorComponentContexts.set(reenteringConnectorComponentContext);
    }

    public Set setUnshareableResources(Set unshareableResources) {
        Set oldUnshareableResources = (Set) currentUnshareableResources.get();
        currentUnshareableResources.set(unshareableResources);
        return oldUnshareableResources;
    }

    public ConnectorTransactionContext setConnectorTransactionContext(ConnectorTransactionContext newConnectorTransactionContext) throws ResourceException {
        ConnectorTransactionContext oldConnectorTransactionContext = (ConnectorTransactionContext) currentConnectorTransactionContexts.get();
        currentConnectorTransactionContexts.set(newConnectorTransactionContext);
        ConnectorComponentContext connectorComponentContext = (ConnectorComponentContext) currentConnectorComponentContexts.get();
        Set unshareableResources = (Set) currentUnshareableResources.get();
        Map connectionManagerToManagedConnectionInfoMap = connectorComponentContext.getConnectionManagerMap();
        for (Iterator i = connectionManagerToManagedConnectionInfoMap.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            ConnectionTrackingInterceptor mcci =
                    (ConnectionTrackingInterceptor) entry.getKey();
            Set connections = (Set) entry.getValue();
            mcci.enter(connections, unshareableResources);
        }
        return oldConnectorTransactionContext;
    }

    public void resetConnectorTransactionContext(ConnectorTransactionContext connectorTransactionContext) {
        currentConnectorTransactionContexts.set(connectorTransactionContext);
    }

    public void handleObtained(
            ConnectionTrackingInterceptor connectionTrackingInterceptor,
            ConnectionInfo connectionInfo) {
        ConnectorComponentContext connectorComponentContext = (ConnectorComponentContext) currentConnectorComponentContexts.get();
        if (connectorComponentContext == null) {
            return;
        }
        Map resources = connectorComponentContext.getConnectionManagerMap();
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
        ConnectorComponentContext connectorComponentContext = (ConnectorComponentContext) currentConnectorComponentContexts.get();
        if (connectorComponentContext == null) {
            return;
        }
        Map resources = connectorComponentContext.getConnectionManagerMap();
        Set infos = (Set) resources.get(connectionTrackingInterceptor);
        //It's not at all clear that an equal ci will be supplied here
        infos.remove(connectionInfo);
    }

    public ConnectorTransactionContext getConnectorTransactionContext() {
        return (ConnectorTransactionContext) currentConnectorTransactionContexts.get();
    }

    public static GeronimoMBeanInfo getGeronimoMBeanInfo() {
        GeronimoMBeanInfo mbeanInfo = new GeronimoMBeanInfo();
        mbeanInfo.setTargetClass(ConnectionTrackingCoordinator.class.getName());
        mbeanInfo.addOperationsDeclaredIn(TrackedConnectionAssociator.class );
        mbeanInfo.addOperationsDeclaredIn(ConnectionTracker.class);
        return mbeanInfo;
    }
}
