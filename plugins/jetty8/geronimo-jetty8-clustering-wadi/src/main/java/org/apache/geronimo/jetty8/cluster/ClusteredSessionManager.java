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
package org.apache.geronimo.jetty8.cluster;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.geronimo.clustering.SessionAlreadyExistException;
import org.apache.geronimo.clustering.SessionListener;
import org.apache.geronimo.clustering.SessionManager;
import org.eclipse.jetty.server.session.AbstractSessionManager;
import org.eclipse.jetty.server.session.HashSessionIdManager;


/**
 * @version $Rev$ $Date$
 */
public class ClusteredSessionManager extends AbstractSessionManager {

    private final SessionManager sessionManager;
    private final Map<String, ClusteredSession> idToSession = new HashMap<String, ClusteredSession>();

    public ClusteredSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;

        String workerName = sessionManager.getNode().getName();
        workerName = workerName.replaceAll(" ", "");
        HashSessionIdManager sessionIdManager = new HashSessionIdManager();
        sessionIdManager.setWorkerName(workerName);
        setIdManager(sessionIdManager);

        sessionManager.registerListener(new MigrationListener());
    }

    @Override
    protected Session newSession(HttpServletRequest request) {
        return new ClusteredSession(request);
    }

    @Override
    public void complete(HttpSession session) {
        ClusteredSession clusteredSession = (ClusteredSession) session;
        clusteredSession.session.onEndAccess();
    }

    @Override
    protected void addSession(Session session) {
        ClusteredSession clusteredSession = (ClusteredSession) session;
        synchronized (idToSession) {
            idToSession.put(clusteredSession.getClusterId(), clusteredSession);
        }
    }

    @Override
    protected boolean removeSession(String idInCluster) {
        synchronized (idToSession) {
            idToSession.remove(idInCluster);
            return true; 
        }
    }

    @Override
    public Session getSession(String idInCluster) {
        synchronized (idToSession) {
            return idToSession.get(idInCluster);
        }
    }

    @Override
    public int getSessions() {
        synchronized (idToSession) {
            return idToSession.size();
        }
    }

    @Override
    public Map getSessionMap() {
        throw new AssertionError("getSessionMap is never used.");
    }

    @Override
    protected void invalidateSessions() {
        // We do not need to clear idToSession: when the SessionManager GBean is stopped, all the sessions
        // it defines are migrated to other SessionManagers. These outbound session migrations will remove
        // them from idToSession.
    }

    private class MigrationListener implements SessionListener {

        public void notifyInboundSessionMigration(org.apache.geronimo.clustering.Session session) {
            addSession(new ClusteredSession(session), false);
        }

        public void notifyOutboundSessionMigration(org.apache.geronimo.clustering.Session session) {
            ClusteredSession clusteredSession = getClusteredSession(session);
            removeSession(clusteredSession, false);
        }

        public void notifySessionDestruction(org.apache.geronimo.clustering.Session session) {
            ClusteredSession clusteredSession = getClusteredSession(session);
            removeSession(clusteredSession, true);
        }

        private ClusteredSession getClusteredSession(org.apache.geronimo.clustering.Session session) throws AssertionError {
            ClusteredSession clusteredSession;
            synchronized (idToSession) {
                clusteredSession = idToSession.remove(session.getSessionId());
            }
            if (null == clusteredSession) {
                throw new AssertionError("Session [" + session + "] is undefined");
            }
            return clusteredSession;
        }


    }

    public class ClusteredSession extends Session {
        private final org.apache.geronimo.clustering.Session session;

        protected ClusteredSession(HttpServletRequest request) {
            super(request);
            try {
                this.session = sessionManager.createSession(getClusterId());
            } catch (SessionAlreadyExistException e) {
                throw (IllegalStateException) new IllegalStateException().initCause(e);
            }
        }

        protected ClusteredSession(org.apache.geronimo.clustering.Session session) {
            super(System.currentTimeMillis(), System.currentTimeMillis(), session.getSessionId());
            this.session = session;
        }

        @Override
        protected String getClusterId() {
            return super.getClusterId();
        }

        @Override
        public void invalidate() throws IllegalStateException {
            super.invalidate();
            session.release();
        }
    }

}
