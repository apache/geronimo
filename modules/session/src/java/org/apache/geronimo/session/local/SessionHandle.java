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

import org.apache.geronimo.session.Session;

/**
 * Represents a single threads access to a session. NOTE this class is not
 * thread safe.
 * 
 * @version $Revision: $
 */
public class SessionHandle implements Session {

    private SessionImpl session;
    private boolean released;
    private LocalSessionLocation locator;

    public SessionHandle(LocalSessionLocation locator) {
        this.locator = locator;
        this.session = locator.getSessionAndHoldReadLock();
    }

    public void addState(String key, Object value) {
        checkNotReleased();
        session.addState(key, value);
    }

    public String getSessionId() {
        return session.getSessionId();
    }

    public Object getState(String key) {
        checkNotReleased();
        return session.getState(key);
    }

    public Object removeState(String key) {
        checkNotReleased();
        return session.removeState(key);
    }

    public void release() {
        if (!released) {
            session = null;
            released = true;
            locator.release();
        }
    }

    protected void checkNotReleased() {
        if (released) {
            throw new IllegalArgumentException("Cannot use this session handle as it has already been released");
        }
    }

}
