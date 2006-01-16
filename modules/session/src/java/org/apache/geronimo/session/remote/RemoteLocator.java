/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.session.remote;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
import edu.emory.mathcs.backport.java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.session.Locator;
import org.apache.geronimo.session.NoSuchSessionException;
import org.apache.geronimo.session.Server;
import org.apache.geronimo.session.SessionNotLocalException;
import org.apache.geronimo.session.WriteLockTimedOutException;
import org.apache.geronimo.session.local.LocalLocator;
import org.apache.geronimo.session.local.LocalSessionLocation;
import org.apache.geronimo.session.local.SessionImpl;

import java.util.Map;
import java.util.Set;

/**
 * A {@link Locator} which supports remote servers and remote sessions.
 * 
 * @version $Revision: $
 */
public class RemoteLocator extends LocalLocator implements RemoteClient, SessionListener {
    private static final Log log = LogFactory.getLog(RemoteLocator.class);

    private SessionListener sessionListener;
    private Set servers = new CopyOnWriteArraySet();
    private Map serverMap = new ConcurrentHashMap();

    public RemoteLocator(String localServerName) {
        super(localServerName);
    }

    public synchronized void addSession(LocalSessionLocation location) {
        super.addSession(location);
        if (sessionListener != null && location.isLocal()) {
            String localServerName = getLocalServerName();
            String sessionId = location.getSessionId();
            sessionListener.onSessionCreate(localServerName, sessionId);
        }
    }

    public void removeMovedSession(SessionImpl session) {
        super.removeMovedSession(session);
        if (sessionListener != null) {
            sessionListener.onSessionMove(getLocalServerName(), session.getSessionId());
        }
    }

    // Properties
    // -------------------------------------------------------------------------
    public SessionListener getSessionListener() {
        return sessionListener;
    }

    public void setSessionListener(SessionListener sessionListener) {
        this.sessionListener = sessionListener;
    }

    // RemoteControl interface
    // -------------------------------------------------------------------------
    public Map moveState(String serverName, String sessionId) throws NoSuchSessionException, SessionNotLocalException,
            WriteLockTimedOutException {
        RemotableSessionLocation location = (RemotableSessionLocation) getSessionLocation(sessionId);
        if (location == null) {
            throw new NoSuchSessionException(sessionId);
        }
        return location.lockForWrite(serverName);
    }

    public void unlock(String serverName, String sessionId) throws NoSuchSessionException, WriteLockTimedOutException {
        RemotableSessionLocation location = (RemotableSessionLocation) getSessionLocation(sessionId);
        location.unlock(serverName);
    }

    // SessionListener interface
    // -------------------------------------------------------------------------
    public void onServerCreate(Server server, RemoteClient remoteLocator) {
        server = new RemoteServer(server, remoteLocator);
        servers.add(server);
        serverMap.put(server.getName(), server);
    }

    public void onServerRemove(String serverName) {
        Server server = (Server) serverMap.remove(serverName);
        if (server != null) {
            servers.remove(server);
        }
    }

    public void onSessionCreate(String serverName, String sessionId) {
        RemoteServer server = getRemoteServer(serverName);
        if (server == null) {
            log.warn("Unknown server: " + serverName + " for session: " + sessionId);
        }
        else {
            RemotableSessionLocation location = new RemotableSessionLocation(this, server, sessionId);
            addSession(location);
        }
    }

    public void onSessionMove(String serverName, String sessionId) {
    }

    public void onSessionDestroy(String serverName, String sessionId) {
    }

    // Helper methods
    // -------------------------------------------------------------------------
    public RemoteClient getRemoteClient() {
        return this;
    }

    public RemoteServer[] getRemoteServers() {
        synchronized (servers) {
            RemoteServer[] answer = new RemoteServer[servers.size()];
            servers.toArray(answer);
            return answer;
        }
    }

    public RemoteServer getRemoteServer(String serverName) {
        return (RemoteServer) serverMap.get(serverName);
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    public synchronized void sessionDestroyed(LocalSessionLocation location) {
        super.sessionDestroyed(location);
        if (sessionListener != null) {
            sessionListener.onSessionDestroy(getLocalServerName(), location.getSessionId());
        }
    }

    protected synchronized SessionImpl getSession(String sessionId) throws NoSuchSessionException {
        LocalSessionLocation location = (LocalSessionLocation) getSessionLocation(sessionId);
        if (location != null) {
            return location.getLocalSession();
        }
        throw new NoSuchSessionException(sessionId);
    }

    protected LocalSessionLocation createSessionLocation(String sessionId) {
        SessionImpl session = new SessionImpl(sessionId);
        RemotableSessionLocation answer = new RemotableSessionLocation(this, session);
        return answer;
    }
}
