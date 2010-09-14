/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.openejb.cluster.stateful.container;

import java.rmi.dgc.VMID;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.geronimo.clustering.Session;
import org.apache.geronimo.clustering.SessionAlreadyExistException;
import org.apache.geronimo.clustering.SessionListener;
import org.apache.geronimo.clustering.SessionManager;
import org.apache.geronimo.openejb.cluster.infra.SessionManagerTracker;
import org.apache.openejb.core.stateful.Cache;
import org.apache.openejb.core.stateful.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class WadiCache implements Cache<Object, Instance>, SessionManagerTracker {
    private final ConcurrentMap<Object, SessionManager> sessionManagersById = new ConcurrentHashMap<Object, SessionManager>();

    private final ConcurrentMap<Object, WadiInstance> localInstances = new ConcurrentHashMap<Object, WadiInstance>();

    private CacheListener<Instance> listener;

    public WadiCache() {
    }

    public WadiCache(CacheListener<Instance> listener) {
        this.setListener(listener);
    }

    public void init() {
    }

    public void destroy() {
    }

    public synchronized CacheListener<Instance> getListener() {
        return listener;
    }

    public synchronized void setListener(CacheListener<Instance> listener) {
        this.listener = listener;
    }

    public SessionManager getSessionManager(Object deploymentId) {
        SessionManager sessionManager = sessionManagersById.get(deploymentId);
        if (sessionManager == null) {
            throw new IllegalStateException("No SessionManager registered for deployment [" + deploymentId + "]");
        }
        return sessionManager;
    }

    public void addSessionManager(Object deploymentId, SessionManager sessionManager) {
        sessionManagersById.put(deploymentId, sessionManager);
        sessionManager.registerListener(new MigrationListener());
    }

    public void removeSessionManager(Object deploymentId, SessionManager sessionManager) {
        sessionManagersById.remove(deploymentId);
    }

    public void add(Object primaryKey, Instance instance) {
        if (!primaryKey.equals(instance.primaryKey)) throw new IllegalArgumentException("primaryKey does not equal instance.primaryKey");

        // Check if we already have this primary key cached locally
        WadiInstance wadiInstance = localInstances.get(primaryKey);
        if (wadiInstance != null) {
            wadiInstance.lock.lock();
            try {
                if (wadiInstance.getState() != WadiInstanceState.REMOVED) {
                    throw new IllegalStateException("An entry for the key " + primaryKey + " already exists");
                }
                // Entry has been removed between get and lock, simply remove the garbage entry
                localInstances.remove(primaryKey);
            } finally {
                wadiInstance.lock.unlock();
            }
        }

        if (!(primaryKey instanceof VMID)) {
            // primaryKey.toString() must be an unique String representation for an unique identifier. Here, we
            // check that primaryKey is a VMID as its Object.toString implementation returns an unique String
            // representation. Other types may not implement Object.toString() "correctly".
            throw new AssertionError("primaryKey MUST be a " + VMID.class.getName());
        }

        try {
            Object deploymentId = instance.beanContext.getDeploymentID();
            Session session = getSessionManager(deploymentId).createSession(primaryKey.toString());
            localInstances.put(primaryKey, new WadiInstance(instance, session));
        } catch (SessionAlreadyExistException e) {
            throw (IllegalStateException) new IllegalStateException().initCause(e);
        }
    }

    public Instance checkOut(Object primaryKey) throws Exception {
        // attempt (up to 10 times) to obtain the entry from the cache
        for (int i = 0; i < 10; i++) {
            // find the entry
            WadiInstance wadiInstance = localInstances.get(primaryKey);
            if (wadiInstance == null) {
                return null;
            }

            wadiInstance.lock.lock();
            try {
                // verfiy state
                switch (wadiInstance.getState()) {
                    case AVAILABLE:
                        break;
                    case CHECKED_OUT:
                        throw new IllegalStateException("The entry " + primaryKey + " is already checked-out");
                    case PASSIVATED:
                        // Entry was passivated between get and lock, we need to load the Entry again
                        // If the cache somehow got corrupted by an entry containing in state PASSIVATED, this remove
                        // call will remove the corruption
                        localInstances.remove(primaryKey, wadiInstance);
                        continue;
                    case REMOVED:
                        // Entry has been removed between get and lock (most likely by undeploying the EJB), simply drop the instance
                        return null;
                }

                // mark entry as in-use
                wadiInstance.setState(WadiInstanceState.CHECKED_OUT);

                return wadiInstance.instance;
            } finally {
                wadiInstance.lock.unlock();
            }
        }

        // something is really messed up with this entry, try to cleanup before throwing an exception
        localInstances.remove(primaryKey);
        throw new RuntimeException("Cache is corrupted: the entry " + primaryKey + " in the Map 'cache' is in state PASSIVATED");    }

    public void checkIn(Object primaryKey) {
        // find the entry
        WadiInstance wadiInstance = localInstances.get(primaryKey);
        if (wadiInstance == null) {
            return;
        }

        wadiInstance.lock.lock();
        try {
            // verfiy state
            switch (wadiInstance.getState()) {
                case AVAILABLE:
                    throw new IllegalStateException("The entry " + primaryKey + " is not checked-out");
                case PASSIVATED:
                    // An entry in-use should not be passivated so we can only assume
                    // that the caller never checked out the bean in the first place
                    throw new IllegalStateException("The entry " + primaryKey + " is not checked-out");
                case REMOVED:
                    // Entry has been removed between get and lock (most likely by undeploying the EJB), simply drop the instance
                    return;
            }

            // mark entry as available
            wadiInstance.setState(WadiInstanceState.AVAILABLE);
        } finally {
            wadiInstance.lock.unlock();
        }

        // todo should this be instide of the lock?
        wadiInstance.endAccess();
    }

    public Instance remove(Object primaryKey) {
        // find the entry
        WadiInstance wadiInstance = localInstances.get(primaryKey);
        if (wadiInstance == null) {
            return null;
        }

        wadiInstance.lock.lock();
        try {
            // remove the entry from the cache
            localInstances.remove(primaryKey);

            // There is no need to check the state because users of the cache
            // are responsible for maintaining references to beans in use

            // mark the entry as removed
            wadiInstance.setState(WadiInstanceState.REMOVED);

        } finally {
            wadiInstance.lock.unlock();
        }

        wadiInstance.release();
        return wadiInstance.instance;
    }

    public void removeAll(CacheFilter<Instance> filter) {
        for (Iterator<WadiInstance> iterator = localInstances.values().iterator(); iterator.hasNext();) {
            WadiInstance wadiInstance = iterator.next();

            wadiInstance.lock.lock();
            try {
                if (filter.matches(wadiInstance.instance)) {
                    // remove the entry from the cache and lru
                    iterator.remove();

                    // There is no need to check the state because users of the cache
                    // are responsible for maintaining references to beans in use

                    // mark the entry as removed
                    wadiInstance.setState(WadiInstanceState.REMOVED);
                }
            } finally {
                wadiInstance.lock.unlock();
            }
        }
    }

    protected class MigrationListener implements SessionListener {
        private final Logger log = LoggerFactory.getLogger(MigrationListener.class);

        public void notifyInboundSessionMigration(Session session) {
            WadiInstance instance = getWadiInstance(session);
            if (instance == null) {
                return;
            }

            try {
                CacheListener<Instance> listener = getListener();
                if (listener != null) {
                    listener.afterLoad(instance.instance);
                }
                localInstances.put(instance.instance.primaryKey, instance);
            } catch (Exception e) {
                log.warn("Cannot activate migrated bean entry.", e);
            }
        }

        public void notifyOutboundSessionMigration(Session session) {
            WadiInstance instance = getWadiInstance(session);
            if (instance == null) {
                return;
            }

            try {
                CacheListener<Instance> listener = getListener();
                if (listener != null) {
                    listener.beforeStore(instance.instance);
                }
                localInstances.remove(instance.instance.primaryKey);
            } catch (Exception e) {
                log.warn("Cannot passivate EJB for migration.", e);
            }
        }

        public void notifySessionDestruction(Session session) {
            WadiInstance instance = getWadiInstance(session);
            if (instance == null) {
                return;
            }

            localInstances.remove(instance.instance.primaryKey);
        }
    }

    public static final String SESSION_KEY_ENTRY = "entry";

    protected WadiInstance getWadiInstance(Session session) {
        if (session == null) {
            throw new IllegalArgumentException("session is required");
        }
        Instance instance = (Instance) session.getState(SESSION_KEY_ENTRY);
        if (instance == null) {
            return null;
        }
        return new WadiInstance(instance, session);
    }

    private static class WadiInstance {
        private final Instance instance;
        private final Session session;
        private final ReentrantLock lock = new ReentrantLock();
        private WadiInstanceState state;

        private WadiInstance(Instance instance, Session session) {
            this.instance = instance;
            this.session = session;
            session.addState(SESSION_KEY_ENTRY, instance);
        }

        public void release() {
            session.release();
        }

        public void endAccess() {
            session.addState(SESSION_KEY_ENTRY, this);
            session.onEndAccess();
        }

        private WadiInstanceState getState() {
            assertLockHeld();
            return state;
        }

        private void setState(WadiInstanceState state) {
            assertLockHeld();
            this.state = state;
        }

        private void assertLockHeld() {
            if (!lock.isHeldByCurrentThread()) {
                throw new IllegalStateException("Entry must be locked");
            }
        }
    }

    private enum WadiInstanceState {
        AVAILABLE, CHECKED_OUT, PASSIVATED, REMOVED
    }

}