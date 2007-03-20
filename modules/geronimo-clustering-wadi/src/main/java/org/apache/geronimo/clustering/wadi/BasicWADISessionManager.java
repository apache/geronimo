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
package org.apache.geronimo.clustering.wadi;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.clustering.Node;
import org.apache.geronimo.clustering.Session;
import org.apache.geronimo.clustering.SessionAlreadyExistException;
import org.apache.geronimo.clustering.SessionListener;
import org.apache.geronimo.clustering.SessionManager;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.codehaus.wadi.core.assembler.StackContext;
import org.codehaus.wadi.core.manager.Manager;
import org.codehaus.wadi.core.manager.SessionMonitor;
import org.codehaus.wadi.group.Dispatcher;
import org.codehaus.wadi.replication.manager.ReplicationManagerFactory;
import org.codehaus.wadi.replication.storage.ReplicaStorageFactory;
import org.codehaus.wadi.replication.strategy.BackingStrategyFactory;
import org.codehaus.wadi.servicespace.ServiceSpace;
import org.codehaus.wadi.servicespace.ServiceSpaceName;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 
 * @version $Rev$ $Date$
 */
public class BasicWADISessionManager implements GBeanLifecycle, SessionManager, WADISessionManager {
    private static final Log log = LogFactory.getLog(BasicWADISessionManager.class);

    private final WADISessionManagerConfigInfo configInfo;
    private final ReplicationManagerFactory repManagerFactory;
    private final ReplicaStorageFactory repStorageFactory;
    private final BackingStrategyFactory backingStrategyFactory;
    private final DispatcherHolder dispatcherHolder;
    private final CopyOnWriteArrayList listeners;

    private Manager manager;
    private SessionMonitor sessionMonitor;
    private ServiceSpace serviceSpace;

    public BasicWADISessionManager(WADISessionManagerConfigInfo configInfo,
            ReplicationManagerFactory repManagerFactory, 
            ReplicaStorageFactory repStorageFactory,
            BackingStrategyFactory backingStrategyFactory, 
            DispatcherHolder dispatcherHolder) {
        this.configInfo = configInfo;
        this.dispatcherHolder = dispatcherHolder;
        this.repManagerFactory = repManagerFactory;
        this.repStorageFactory = repStorageFactory;
        this.backingStrategyFactory = backingStrategyFactory;

        listeners = new CopyOnWriteArrayList();
    }

    public void doStart() throws Exception {
        Dispatcher underlyingDisp = dispatcherHolder.getDispatcher();
        
        ServiceSpaceName serviceSpaceName = new ServiceSpaceName(configInfo.getServiceSpaceURI());
        StackContext stackContext = new StackContext(serviceSpaceName,
                underlyingDisp,
                configInfo.getSessionTimeoutSeconds(),
                configInfo.getNumPartitions(),
                configInfo.getSweepInterval(),
                repManagerFactory,
                repStorageFactory,
                backingStrategyFactory);
        stackContext.build();

        serviceSpace = stackContext.getServiceSpace();
        manager = stackContext.getManager();

        sessionMonitor = stackContext.getSessionMonitor();
        sessionMonitor.addSessionListener(new SessionListenerAdapter());
        
        serviceSpace.start();
    }

    public void doStop() throws Exception {
        serviceSpace.stop();
    }

    public void doFail() {
        try {
            serviceSpace.stop();
        } catch (Exception e) {
            log.error(e);
        }
    }

    public Session createSession(String sessionId) throws SessionAlreadyExistException {
        org.codehaus.wadi.core.session.Session session;
        try {
            session = manager.createWithName(sessionId);
        } catch (org.codehaus.wadi.core.manager.SessionAlreadyExistException e) {
            throw new SessionAlreadyExistException(sessionId);
        }
        return new WADISessionAdaptor(session);
    }

    public Manager getManager() {
        return manager;
    }

    public Node getNode() {
        return dispatcherHolder.getNode();
    }

    public void registerListener(SessionListener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(SessionListener listener) {
        listeners.remove(listener);
    }

    private void notifyInboundSessionMigration(org.codehaus.wadi.core.session.Session session) {
        for (Iterator iter = listeners.iterator(); iter.hasNext();) {
            SessionListener listener = (SessionListener) iter.next();
            listener.notifyInboundSessionMigration(new WADISessionAdaptor(session));
        }
    }

    private void notifyOutboundSessionMigration(org.codehaus.wadi.core.session.Session session) {
        for (Iterator iter = listeners.iterator(); iter.hasNext();) {
            SessionListener listener = (SessionListener) iter.next();
            listener.notifyOutboundSessionMigration(new WADISessionAdaptor(session));
        }
    }

    private void notifySessionDestruction(org.codehaus.wadi.core.session.Session session) {
        for (Iterator iter = listeners.iterator(); iter.hasNext();) {
            SessionListener listener = (SessionListener) iter.next();
            listener.notifySessionDestruction(new WADISessionAdaptor(session));
        }
    }

    private class SessionListenerAdapter implements org.codehaus.wadi.core.manager.SessionListener {

        public void onSessionCreation(org.codehaus.wadi.core.session.Session session) {
        }

        public void onSessionDestruction(org.codehaus.wadi.core.session.Session session) {
            notifySessionDestruction(session);
        }
        
        public void onInboundSessionMigration(org.codehaus.wadi.core.session.Session session) {
            notifyInboundSessionMigration(session);
        }
        
        public void onOutbountSessionMigration(org.codehaus.wadi.core.session.Session session) {
            notifyOutboundSessionMigration(session);
        }
        
    }

    public static final GBeanInfo GBEAN_INFO;

    public static final String GBEAN_ATTR_WADI_CONFIG_INFO = "wadiConfigInfo";

    public static final String GBEAN_REF_REPLICATION_MANAGER_FACTORY = "ReplicationManagerFactory";
    public static final String GBEAN_REF_REPLICA_STORAGE_FACTORY = "ReplicaStorageFactory";
    public static final String GBEAN_REF_BACKING_STRATEGY_FACTORY = "BackingStrategyFactory";
    public static final String GBEAN_REF_DISPATCHER_HOLDER = "DispatcherHolder";

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic("WADI Session Manager",
                BasicWADISessionManager.class, NameFactory.GERONIMO_SERVICE);

        infoBuilder.addAttribute(GBEAN_ATTR_WADI_CONFIG_INFO, WADISessionManagerConfigInfo.class, true);

        infoBuilder.addReference(GBEAN_REF_REPLICATION_MANAGER_FACTORY, ReplicationManagerFactory.class,
                NameFactory.GERONIMO_SERVICE);
        infoBuilder.addReference(GBEAN_REF_REPLICA_STORAGE_FACTORY, ReplicaStorageFactory.class,
                NameFactory.GERONIMO_SERVICE);
        infoBuilder.addReference(GBEAN_REF_BACKING_STRATEGY_FACTORY, BackingStrategyFactory.class,
                NameFactory.GERONIMO_SERVICE);
        infoBuilder.addReference(GBEAN_REF_DISPATCHER_HOLDER, DispatcherHolder.class, NameFactory.GERONIMO_SERVICE);

        infoBuilder.addInterface(SessionManager.class);
        infoBuilder.addInterface(WADISessionManager.class);

        infoBuilder.setConstructor(new String[] { GBEAN_ATTR_WADI_CONFIG_INFO,
                GBEAN_REF_REPLICATION_MANAGER_FACTORY, 
                GBEAN_REF_REPLICA_STORAGE_FACTORY,
                GBEAN_REF_BACKING_STRATEGY_FACTORY, 
                GBEAN_REF_DISPATCHER_HOLDER });

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
