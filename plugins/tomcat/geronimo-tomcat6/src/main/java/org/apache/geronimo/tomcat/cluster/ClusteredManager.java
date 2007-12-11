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

import java.io.IOException;

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

    public ClusteredManager(SessionManager sessionManager) {
        if (null == sessionManager) {
            throw new IllegalArgumentException("sessionManager is required");
        }
        this.sessionManager = sessionManager;

        sessionManager.registerListener(new MigrationListener());
    }

    @Override
    public Session createEmptySession() {
        return new ClusteredSession();
    }

    @Override
    protected void doLoad() throws ClassNotFoundException, IOException {
    }

    @Override
    protected void doUnload() throws IOException {
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
            remove(clusteredSession);
        }
        
        protected ClusteredSession getClusteredSession(org.apache.geronimo.clustering.Session session) {
            return (ClusteredSession) ClusteredManager.this.sessions.get(session.getSessionId());
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

            super.setId(session.getSessionId());
            setValid(true);
            setNew(false);
        }

        @Override
        public void setId(String id) {
            super.setId(id);
            try {
                session = sessionManager.createSession(id);
            } catch (SessionAlreadyExistException e) {
                throw (IllegalStateException) new IllegalStateException().initCause(e);
            }
            
            attributes = session.getState();
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
