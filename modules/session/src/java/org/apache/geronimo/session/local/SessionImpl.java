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

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;

import org.apache.geronimo.session.Session;
import org.apache.geronimo.session.SessionDestroyedException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @version $Revision: $
 */
public class SessionImpl implements Session {

    private final String sessionId;
    private final Map map;
    private boolean destroyed;
    private SessionStateTracker stateTracker;

    public SessionImpl(String sessionId) {
        this.sessionId = sessionId;
        this.map = new ConcurrentHashMap();
    }

    /**
     * Must be a concurrent map
     */
    public SessionImpl(String sessionId, Map map) {
        this.sessionId = sessionId;
        this.map = map;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void release() {
        checkDestroyed();
    }

    public void addState(String key, Object value) {
        checkDestroyed();
        map.put(key, value);
        if (stateTracker != null) {
            stateTracker.onDeltaChange(key);
        }
    }

    public Object getState(String key) {
        checkDestroyed();
        Object answer = map.get(key);
        if (stateTracker != null) {
            stateTracker.onDeltaChange(key);
        }
        return answer;
    }

    public Object removeState(String key) {
        checkDestroyed();
        Object answer = map.remove(key);
        if (answer != null) {
            if (stateTracker != null) {
                stateTracker.onDeltaChange(key);
            }
        }
        return answer;
    }

    protected void checkDestroyed() throws SessionDestroyedException {
        if (destroyed) {
            throw new SessionDestroyedException(sessionId);
        }
    }

    public Map getStateMap() {
        return map;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    public Map createDeltas(Set deltaKeys) {
        Map deltas = new HashMap();
        for (Iterator iter = deltaKeys.iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            Object value = map.get(key);
            deltas.put(key, value);
        }
        return deltas;
    }

    protected SessionStateTracker getStateTracker() {
        return stateTracker;
    }

    protected void setStateTracker(SessionStateTracker stateTracker) {
        this.stateTracker = stateTracker;
    }

}
