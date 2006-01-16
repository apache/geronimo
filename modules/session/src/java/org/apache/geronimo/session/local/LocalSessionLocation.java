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

import edu.emory.mathcs.backport.java.util.concurrent.locks.Lock;
import edu.emory.mathcs.backport.java.util.concurrent.locks.ReadWriteLock;
import edu.emory.mathcs.backport.java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.geronimo.session.NoSuchSessionException;
import org.apache.geronimo.session.Server;
import org.apache.geronimo.session.Session;
import org.apache.geronimo.session.SessionLocation;
import org.apache.geronimo.session.SessionNotLocalException;
import org.apache.geronimo.session.SessionNotMovableException;
import org.apache.geronimo.session.WriteLockTimedOutException;

/**
 * An implementation of {@link SessionLocation} which supports only local
 * sessions.
 * 
 * @version $Revision: $
 */
public class LocalSessionLocation implements SessionLocation {

    private LocalLocator locator;
    protected SessionImpl session;
    protected final ReadWriteLock lock = new ReentrantReadWriteLock();
    private SessionStateTracker stateTracker;

    /**
     * Creates a local session location
     */
    public LocalSessionLocation(LocalLocator locator, SessionImpl session) {
        this.locator = locator;
        this.session = session;
    }

    public boolean isLocal() {
        return true;
    }

    public String getSessionId() {
        return session.getSessionId();
    }

    public Server getServer() {
        return locator.getLocalServer();
    }

    public boolean isMovable() {
        return false;
    }

    public Session getSession() throws SessionNotLocalException {
        Lock readLock = lock.readLock();
        readLock.lock();
        try {
            if (session == null) {
                throw new SessionNotLocalException(getSessionId());
            }
            return new SessionHandle(this);
        }
        finally {
            readLock.unlock();
        }
    }

    public SessionImpl getLocalSession() {
        return session;
    }

    public ReadWriteLock getLock() {
        return lock;
    }

    public Session moveLocally() throws NoSuchSessionException, WriteLockTimedOutException, SessionNotLocalException,
            SessionNotMovableException {
        throw new SessionNotMovableException(getSessionId());
    }

    public void destroy() throws SessionNotLocalException {
        Lock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            if (session == null) {
                throw new SessionNotLocalException(getSessionId());
            }
            locator.sessionDestroyed(this);
            session.setDestroyed(true);
        }
        finally {
            writeLock.unlock();
        }
    }

    public SessionImpl getSessionAndHoldReadLock() {
        lock.readLock().lock();
        return session;
    }

    public void release() {
        lock.readLock().unlock();
        if (stateTracker != null) {
            Lock writeLock = lock.writeLock();
            if (writeLock.tryLock()) {
                try {
                    stateTracker.checkpoint();
                }
                finally {
                    writeLock.unlock();
                }
            }
        }
    }

    protected SessionStateTracker getStateTracker() {
        return stateTracker;
    }

    protected void setStateTracker(SessionStateTracker stateTracker) {
        this.stateTracker = stateTracker;
        if (session != null) {
            session.setStateTracker(stateTracker);
        }
    }

    public void setStateListener(SessionStateListener stateListener) {
        SessionStateTracker stateTracker = new SessionStateTracker(this, session, stateListener);
        setStateTracker(stateTracker);
    }

    public LocalLocator getLocator() {
        return locator;
    }

}
