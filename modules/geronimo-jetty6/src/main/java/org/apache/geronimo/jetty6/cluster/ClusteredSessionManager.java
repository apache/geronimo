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
package org.apache.geronimo.jetty6.cluster;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.geronimo.clustering.SessionAlreadyExistException;
import org.apache.geronimo.clustering.SessionListener;
import org.apache.geronimo.clustering.SessionManager;
import org.mortbay.jetty.servlet.AbstractSessionManager;
import org.mortbay.jetty.servlet.HashSessionIdManager;


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
        setMetaManager(sessionIdManager);

        sessionManager.registerListener(new MigrationListener());

        // sessions are not removed by this manager. They are invalidated via a callback mechanism
        setMaxInactiveInterval(-1);
    }

    @Override
    protected Session newSession(HttpServletRequest request) {
        return new ClusteredSession(request);
    }

    @Override
    protected void addSession(Session session) {
        //todo gianni fixme
        synchronized (idToSession) {
            idToSession.put(session.getId(), (ClusteredSession) session);
        }
    }

    @Override
    protected void removeSession(String idInCluster) {
        //todo gianni fixme
        synchronized (idToSession) {
            idToSession.remove(idInCluster);
        }
    }

    @Override
    protected Session getSession(String idInCluster) {
        //todo gianni fixme
        synchronized (idToSession) {
            return idToSession.get(idInCluster);
        }
    }

    @Override
    public int getSessions() {
        //todo gianni fixme
        synchronized (idToSession) {
            return idToSession.size();
        }
    }


    /**
     * @deprecated. Need to review if it is needed.
     */
    @Override
    public Map getSessionMap() {
        //todo gianni fixme
        return idToSession;
    }

    @Override
    protected void invalidateSessions() {
        //todo gianni fixme
    }


    private class MigrationListener implements SessionListener {

        public void notifyInboundSessionMigration(org.apache.geronimo.clustering.Session session) {
            addSession(new ClusteredSession(session), false);
        }

        public void notifyOutboundSessionMigration(org.apache.geronimo.clustering.Session session) {
            ClusteredSession clusteredSession;
            synchronized (idToSession) {
                clusteredSession = (ClusteredSession) idToSession.remove(session.getSessionId());
            }
            if (null == clusteredSession) {
                throw new AssertionError("Session [" + session + "] is undefined");
            }
            removeSession(clusteredSession, false);
        }
    }

    public class ClusteredSession extends Session {
        private final org.apache.geronimo.clustering.Session session;

        protected ClusteredSession(HttpServletRequest request) {
            super(request);
            try {
                this.session = sessionManager.createSession(getId());
            } catch (SessionAlreadyExistException e) {
                throw (IllegalStateException) new IllegalStateException().initCause(e);
            }
            synchronized (idToSession) {
                idToSession.put(getId(), this);
            }
        }

        protected ClusteredSession(org.apache.geronimo.clustering.Session session) {
            super(session.getSessionId());
            this.session = session;
            synchronized (idToSession) {
                idToSession.put(getId(), this);
            }
        }

        @Override
        protected Map newAttributeMap() {
            return session.getState();
        }
    }

}
