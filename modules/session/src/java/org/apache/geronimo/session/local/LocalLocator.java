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
package org.apache.geronimo.session.local;

import org.apache.geronimo.session.Locator;
import org.apache.geronimo.session.Server;
import org.apache.geronimo.session.Session;
import org.apache.geronimo.session.SessionAlreadyExistsException;
import org.apache.geronimo.session.SessionLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link Locator} which can only be run on a single server and only supports
 * local sessions.
 * 
 * @version $Revision: $
 */
public class LocalLocator implements Locator {

    private final Server localServer;
    private final Map map = new HashMap();
    private SessionStateListener stateListener;

    public LocalLocator(String localServerName) {
        localServer = new LocalServerImpl(localServerName);
    }

    public synchronized SessionLocation getSessionLocation(String sessionId) {
        return (SessionLocation) map.get(sessionId);
    }

    public synchronized Session createSession(String sessionId) throws SessionAlreadyExistsException {
        if (map.containsKey(sessionId)) {
            throw new SessionAlreadyExistsException(sessionId);
        }
        LocalSessionLocation location = createSessionLocation(sessionId);
        if (stateListener != null) {
            location.setStateListener(stateListener);
        }
        addSession(location);
        return new SessionHandle(location);
    }

    public Server getLocalServer() {
        return localServer;
    }

    public synchronized void addSession(LocalSessionLocation location) {
        String sessionId = location.getSessionId();
        map.put(sessionId, location);
    }

    public synchronized void removeMovedSession(SessionImpl session) {
        String sessionId = session.getSessionId();
        map.remove(sessionId);
    }

    // Properties
    // -------------------------------------------------------------------------
    public String getLocalServerName() {
        return getLocalServer().getName();
    }

    public SessionStateListener getStateListener() {
        return stateListener;
    }

    public void setStateListener(SessionStateListener stateListener) {
        this.stateListener = stateListener;
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    public synchronized void sessionDestroyed(LocalSessionLocation location) {
        String sessionId = location.getSessionId();
        map.remove(sessionId);
    }

    protected LocalSessionLocation createSessionLocation(String sessionId) {
        SessionImpl session = new SessionImpl(sessionId);
        LocalSessionLocation answer = new LocalSessionLocation(this, session);
        return answer;
    }
}
