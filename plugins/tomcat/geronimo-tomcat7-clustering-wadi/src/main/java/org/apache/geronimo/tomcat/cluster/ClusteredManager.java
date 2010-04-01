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

package org.apache.geronimo.tomcat.cluster;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Session;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.session.StandardSession;
import org.apache.geronimo.clustering.SessionAlreadyExistException;
import org.apache.geronimo.clustering.SessionListener;
import org.apache.geronimo.clustering.SessionManager;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class ClusteredManager extends StandardManager {
    private final SessionManager sessionManager;
    private final String nodeName;
    private final Router router;

    public ClusteredManager(SessionManager sessionManager) {
        if (null == sessionManager) {
            throw new IllegalArgumentException("sessionManager is required");
        }
        this.sessionManager = sessionManager;

        nodeName = sessionManager.getNode().getName();
        router = newRouter(nodeName);

        sessionManager.registerListener(new MigrationListener());
    }

    protected Router newRouter(String nodeName) {
        return new JkRouter(nodeName);
    }

    @Override
    public String getJvmRoute() {
        return nodeName;
    }

    @Override
    protected void stopInternal() throws LifecycleException {
        // lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        setState(LifecycleState.STOPPING);
    }

    @Override
    public Session createEmptySession() {
        return new ClusteredSession();
    }

    @Override
    public void backgroundProcess() {
    }

    private class MigrationListener implements SessionListener {

        public void notifyInboundSessionMigration(org.apache.geronimo.clustering.Session session) {
            add(new ClusteredSession(session));
        }

        public void notifyOutboundSessionMigration(org.apache.geronimo.clustering.Session session) {
            ClusteredSession clusteredSession = getClusteredSession(session);
            remove(clusteredSession);
        }

        public void notifySessionDestruction(org.apache.geronimo.clustering.Session session) {
            ClusteredSession clusteredSession = getClusteredSession(session);
            if (null == clusteredSession) {
                return;
            }
            remove(clusteredSession);
        }

        protected ClusteredSession getClusteredSession(org.apache.geronimo.clustering.Session session) {
            String sessionId = session.getSessionId();
            sessionId = router.transformGlobalSessionIdToSessionId(sessionId);
            return (ClusteredSession) ClusteredManager.this.sessions.get(sessionId);
        }
    }

    public class ClusteredSession extends StandardSession {
        private org.apache.geronimo.clustering.Session session;

        protected ClusteredSession() {
            super(ClusteredManager.this);
        }

        protected ClusteredSession(org.apache.geronimo.clustering.Session session) {
            super(ClusteredManager.this);
            this.session = session;

            attributes = session.getState();

            String sessionId = router.transformGlobalSessionIdToSessionId(session.getSessionId());
            super.setId(sessionId);
            setValid(true);
            setNew(false);
        }

        @Override
        public void setId(String id) {
            super.setId(id);

            newUnderlyingSession(id);

            attributes = session.getState();
        }

        protected void newUnderlyingSession(String id) {
            String globalSessionId = router.transformSessionIdToGlobalSessionId(id);

            try {
                session = sessionManager.createSession(globalSessionId);
            } catch (SessionAlreadyExistException e) {
                throw (IllegalStateException) new IllegalStateException().initCause(e);
            }
        }

        @Override
        public void invalidate() throws IllegalStateException {
            super.invalidate();
            session.release();
        }

        @Override
        public void endAccess() {
            super.endAccess();
            session.onEndAccess();
        }
    }

}
