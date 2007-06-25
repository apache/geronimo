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

package org.apache.geronimo.connector.outbound.connectiontracking;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.resource.ResourceException;
import javax.resource.spi.DissociatableManagedConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.connector.outbound.ConnectionInfo;
import org.apache.geronimo.connector.outbound.ConnectionReturnAction;
import org.apache.geronimo.connector.outbound.ConnectionTrackingInterceptor;
import org.apache.geronimo.connector.outbound.ManagedConnectionInfo;

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

    private final boolean lazyConnect;
    private final ThreadLocal currentInstanceContexts = new ThreadLocal();
    private final ConcurrentMap proxiesByConnectionInfo = new ConcurrentHashMap();

    public ConnectionTrackingCoordinator() {
        this(false);
    }

    public ConnectionTrackingCoordinator(boolean lazyConnect) {
        this.lazyConnect = lazyConnect;
    }

    public boolean isLazyConnect() {
        return lazyConnect;
    }

    public ConnectorInstanceContext enter(ConnectorInstanceContext newContext) throws ResourceException {
        ConnectorInstanceContext oldContext = (ConnectorInstanceContext) currentInstanceContexts.get();
        currentInstanceContexts.set(newContext);
        associateConnections(newContext);
        return oldContext;
    }

    private void associateConnections(ConnectorInstanceContext context) throws ResourceException {
            Map connectionManagerToManagedConnectionInfoMap = context.getConnectionManagerMap();
            for (Iterator i = connectionManagerToManagedConnectionInfoMap.entrySet().iterator(); i.hasNext();) {
                Map.Entry entry = (Map.Entry) i.next();
                ConnectionTrackingInterceptor mcci =
                        (ConnectionTrackingInterceptor) entry.getKey();
                Set connections = (Set) entry.getValue();
                mcci.enter(connections);
            }
    }

    public void newTransaction() throws ResourceException {
        ConnectorInstanceContext currentContext = (ConnectorInstanceContext) currentInstanceContexts.get();
        if (currentContext == null) {
            return;
        }
        associateConnections(currentContext);
    }

    public void exit(ConnectorInstanceContext oldContext) throws ResourceException {
        ConnectorInstanceContext currentContext = (ConnectorInstanceContext) currentInstanceContexts.get();
        try {
            // for each connection type opened in this componet
            Map resources = currentContext.getConnectionManagerMap();
            for (Iterator i = resources.entrySet().iterator(); i.hasNext();) {
                Map.Entry entry = (Map.Entry) i.next();
                ConnectionTrackingInterceptor mcci =
                        (ConnectionTrackingInterceptor) entry.getKey();
                Set connections = (Set) entry.getValue();

                // release proxy connections
                if (lazyConnect) {
                    for (Iterator infoIterator = connections.iterator(); infoIterator.hasNext();) {
                        ConnectionInfo connectionInfo = (ConnectionInfo) infoIterator.next();
                        releaseProxyConnection(connectionInfo);
                    }
                }

                // use connection interceptor to dissociate connections that support disassociation
                mcci.exit(connections);

                // if no connection remain clear context... we could support automatic commit, rollback or exception here
                if (connections.isEmpty()) {
                    i.remove();
                }
            }
        } finally {
            // when lazy we do not need or want to track open connections... they will automatically reconnect
            if (lazyConnect) {
                currentContext.getConnectionManagerMap().clear();
            }
            currentInstanceContexts.set(oldContext);
        }
    }

    /**
     * A new connection (handle) has been obtained.  If we are within a component context, store the connection handle
     * so we can disassociate connections that support disassociation on exit.
     * @param connectionTrackingInterceptor our interceptor in the connection manager which is used to disassociate the connections
     * @param connectionInfo the connection that was obtained
     * @param reassociate
     */
    public void handleObtained(ConnectionTrackingInterceptor connectionTrackingInterceptor,
            ConnectionInfo connectionInfo,
            boolean reassociate) throws ResourceException {

        ConnectorInstanceContext currentContext = (ConnectorInstanceContext) currentInstanceContexts.get();
        if (currentContext == null) {
            return;
        }

        Map resources = currentContext.getConnectionManagerMap();
        Set infos = (Set) resources.get(connectionTrackingInterceptor);
        if (infos == null) {
            infos = new HashSet();
            resources.put(connectionTrackingInterceptor, infos);
        }

        infos.add(connectionInfo);

        // if lazyConnect, we must proxy so we know when to connect the proxy
        if (!reassociate && lazyConnect) {
            proxyConnection(connectionTrackingInterceptor, connectionInfo);
        }
    }

    /**
     * A connection (handle) has been released or destroyed.  If we are within a component context, remove the connection
     * handle from the context.
     * @param connectionTrackingInterceptor our interceptor in the connection manager
     * @param connectionInfo the connection that was released
     * @param connectionReturnAction
     */
    public void handleReleased(ConnectionTrackingInterceptor connectionTrackingInterceptor,
            ConnectionInfo connectionInfo,
            ConnectionReturnAction connectionReturnAction) {

        ConnectorInstanceContext currentContext = (ConnectorInstanceContext) currentInstanceContexts.get();
        if (currentContext == null) {
            return;
        }

        Map resources = currentContext.getConnectionManagerMap();
        Set infos = (Set) resources.get(connectionTrackingInterceptor);
        if (infos != null) {
            if (connectionInfo.getConnectionHandle() == null) {
                //destroy was called as a result of an error
                ManagedConnectionInfo mci = connectionInfo.getManagedConnectionInfo();
                Collection toRemove = mci.getConnectionInfos();
                infos.removeAll(toRemove);
            } else {
                infos.remove(connectionInfo);
            }
        } else {
            if ( log.isTraceEnabled()) {
                 log.trace("No infos found for handle " + connectionInfo.getConnectionHandle() +
                         " for MCI: " + connectionInfo.getManagedConnectionInfo() +
                         " for MC: " + connectionInfo.getManagedConnectionInfo().getManagedConnection() +
                         " for CTI: " + connectionTrackingInterceptor, new Exception("Stack Trace"));
            }
        }

        // NOTE: This method is also called by DissociatableManagedConnection when a connection has been
        // dissociated in addition to the normal connection closed notification, but this is not a problem
        // because DissociatableManagedConnection are not proied so this method will have no effect
        closeProxyConnection(connectionInfo);
    }

    /**
     * If we are within a component context, before a connection is obtained, set the connection unshareable and
     * applicationManagedSecurity properties so the correct connection type is obtained.
     * @param connectionInfo the connection to be obtained
     * @param key the unique id of the connection manager
     */
    public void setEnvironment(ConnectionInfo connectionInfo, String key) {
        ConnectorInstanceContext currentContext = (ConnectorInstanceContext) currentInstanceContexts.get();
        if (currentContext != null) {
            // is this resource unshareable in this component context
            Set unshareableResources = currentContext.getUnshareableResources();
            boolean unshareable = unshareableResources.contains(key);
            connectionInfo.setUnshareable(unshareable);

            // does this resource use application managed security in this component context
            Set applicationManagedSecurityResources = currentContext.getApplicationManagedSecurityResources();
            boolean applicationManagedSecurity = applicationManagedSecurityResources.contains(key);
            connectionInfo.setApplicationManagedSecurity(applicationManagedSecurity);
        }
    }

    private void proxyConnection(ConnectionTrackingInterceptor connectionTrackingInterceptor, ConnectionInfo connectionInfo) throws ResourceException {
        // if this connection already has a proxy no need to create another
        if (connectionInfo.getConnectionProxy() != null) return;

        // DissociatableManagedConnection do not need to be proxied
        if (connectionInfo.getManagedConnectionInfo().getManagedConnection() instanceof DissociatableManagedConnection) {
            return;
        }

        try {
            Object handle = connectionInfo.getConnectionHandle();
            ConnectionInvocationHandler invocationHandler = new ConnectionInvocationHandler(connectionTrackingInterceptor, connectionInfo, handle);
            Object proxy = Proxy.newProxyInstance(getClassLoader(handle), handle.getClass().getInterfaces(), invocationHandler);

            // add it to our map... if the map already has a proxy for this connection, use the existing one
            Object existingProxy = proxiesByConnectionInfo.putIfAbsent(connectionInfo, proxy);
            if (existingProxy != null) proxy = existingProxy;

            connectionInfo.setConnectionProxy(proxy);
        } catch (Throwable e) {
            throw new ResourceException("Unable to construct connection proxy", e);
        }
    }

    private void releaseProxyConnection(ConnectionInfo connectionInfo) {
        ConnectionInvocationHandler invocationHandler = getConnectionInvocationHandler(connectionInfo);
        if (invocationHandler != null) {
            invocationHandler.releaseHandle();
        }
    }

    private void closeProxyConnection(ConnectionInfo connectionInfo) {
        ConnectionInvocationHandler invocationHandler = getConnectionInvocationHandler(connectionInfo);
        if (invocationHandler != null) {
            invocationHandler.close();
            proxiesByConnectionInfo.remove(connectionInfo);
            connectionInfo.setConnectionProxy(null);
        }
    }

    // Favor the thread context class loader for proxy construction
    private ClassLoader getClassLoader(Object handle) {
        ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader();
        if (threadClassLoader != null) {
            return threadClassLoader;
        }
        return handle.getClass().getClassLoader();
    }

    private ConnectionInvocationHandler getConnectionInvocationHandler(ConnectionInfo connectionInfo) {
        Object proxy = connectionInfo.getConnectionProxy();
        if (proxy == null) {
            proxy = proxiesByConnectionInfo.get(connectionInfo);
        }

        // no proxy or proxy already destroyed
        if (proxy == null) return null;

        if (Proxy.isProxyClass(proxy.getClass())) {
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(proxy);
            if (invocationHandler instanceof ConnectionInvocationHandler) {
                return (ConnectionInvocationHandler) invocationHandler;
            }
        }
        return null;
    }

    public static class ConnectionInvocationHandler implements InvocationHandler {
        private ConnectionTrackingInterceptor connectionTrackingInterceptor;
        private ConnectionInfo connectionInfo;
        private final Object handle;
        private boolean released = false;

        public ConnectionInvocationHandler(ConnectionTrackingInterceptor connectionTrackingInterceptor, ConnectionInfo connectionInfo, Object handle) {
            this.connectionTrackingInterceptor = connectionTrackingInterceptor;
            this.connectionInfo = connectionInfo;
            this.handle = handle;
        }

        public Object invoke(Object object, Method method, Object[] args) throws Throwable {
            Object handle;
            if (method.getDeclaringClass() == Object.class) {
                if (method.getName().equals("finalize")) {
                    // ignore the handle will get called if it implemented the method
                    return null;
                }
                if (method.getName().equals("clone")) {
                    throw new CloneNotSupportedException();
                }
                // for equals, hashCode and toString don't activate handle
                synchronized (this) {
                    handle = this.handle;
                }
            } else {
                handle = getHandle();
            }
            
            try {
                Object value = method.invoke(handle, args);
                return value;
            } catch (InvocationTargetException ite) {
                // catch InvocationTargetExceptions and turn them into the target exception (if there is one)
                Throwable t = ite.getTargetException();
                if (t != null) {
                    throw t;
                }
                throw ite;
            }

        }

        public synchronized boolean isReleased() {
            return released;
        }

        public synchronized void releaseHandle() {
            released = true;
        }

        public synchronized void close() {
            connectionTrackingInterceptor = null;
            connectionInfo = null;
            released = true;
        }

        public synchronized Object getHandle() {
            if (connectionTrackingInterceptor == null) {
                // connection has been closed... send invocations directly to the handle
                // which will throw an exception or in some clases like JDBC connection.close()
                // ignore the invocation
                return handle;
            }

            if (released) {
                try {
                    connectionTrackingInterceptor.reassociateConnection(connectionInfo);
                } catch (ResourceException e) {
                    throw (IllegalStateException) new IllegalStateException("Could not obtain a physical connection").initCause(e);
                }
                released = false;
            }
            return handle;
        }
    }
}
