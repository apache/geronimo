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

import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;
import edu.emory.mathcs.backport.java.util.concurrent.locks.Lock;

import org.apache.geronimo.session.NoSuchSessionException;
import org.apache.geronimo.session.Server;
import org.apache.geronimo.session.Session;
import org.apache.geronimo.session.SessionLocation;
import org.apache.geronimo.session.SessionNotLocalException;
import org.apache.geronimo.session.SessionNotMovableException;
import org.apache.geronimo.session.WriteLockTimedOutException;
import org.apache.geronimo.session.local.LocalLocator;
import org.apache.geronimo.session.local.LocalSessionLocation;
import org.apache.geronimo.session.local.SessionHandle;
import org.apache.geronimo.session.local.SessionImpl;

import java.util.Map;

/**
 * An implementation of {@link SessionLocation} which can be either local or
 * remote.
 * 
 * @version $Revision: $
 */
public class RemotableSessionLocation extends LocalSessionLocation {
    private final String sessionId;
    private Server server;
    private String lastServerToLockMe;
    private long lockTimeout = 10000L;
    private boolean movable = true;

    public RemotableSessionLocation(LocalLocator locator, RemoteServer server, String sessionId) {
        super(locator, null);
        this.server = server;
        this.sessionId = sessionId;
    }

    public RemotableSessionLocation(LocalLocator locator, SessionImpl session) {
        super(locator, session);
        this.server = locator.getLocalServer();
        this.sessionId = session.getSessionId();
    }

    public String getSessionId() {
        return sessionId;
    }

    public boolean isLocal() {
        Lock readLock = lock.readLock();
        readLock.lock();
        try {
            return session != null;
        }
        finally {
            readLock.unlock();
        }
    }

    public Session moveLocally() throws NoSuchSessionException, WriteLockTimedOutException, SessionNotLocalException,
            SessionNotMovableException {
        if (!isMovable()) {
            throw new SessionNotMovableException(sessionId);
        }
        Lock writeLock = lock.writeLock();
        writeLock.lock();
        Server remoteServer = getServer();

        try {
            if (session != null) {
                return new SessionHandle(this);
            }
            Server localServer = getLocator().getLocalServer();
            String localServerName = localServer.getName();

            RemoteClient remoteControl = ((RemoteServer) remoteServer).getRemoteClient();

            Map state = remoteControl.moveState(localServerName, sessionId);
            try {
                session = new SessionImpl(sessionId, state);
                server = localServer;
                return new SessionHandle(this);
            }
            finally {
                remoteControl.unlock(localServerName, sessionId);
            }
        }
        catch (WriteLockTimedOutException e) {
            session = null;
            server = remoteServer;
            throw e;
        }
        finally {
            writeLock.unlock();
        }
    }

    public Map lockForWrite(String serverName) throws SessionNotLocalException, WriteLockTimedOutException {
        Lock writeLock = lock.writeLock();
        try {
            if (writeLock.tryLock(lockTimeout, TimeUnit.MILLISECONDS)) {
                if (session == null) {
                    writeLock.unlock();
                    throw new SessionNotLocalException(sessionId);
                }
                lastServerToLockMe = serverName;
                return session.getStateMap();
            }
            else {
                throw new WriteLockTimedOutException(sessionId);
            }
        }
        catch (InterruptedException e) {
            throw new WriteLockTimedOutException(sessionId);
        }
    }

    public void unlock(String serverName) throws WriteLockTimedOutException {
        //
        // FIXME
        //
        // TODO: the locks don't actually work as they are typically called
        // from different threads; we need a better way to grab the write lock
        // for a timeout until the unlock method comes in
        // 
        Lock writeLock = lock.writeLock();
        if (lastServerToLockMe != null && serverName.equals(lastServerToLockMe)) {
            lastServerToLockMe = null;
            writeLock.unlock();
        }
        else {
            throw new WriteLockTimedOutException(sessionId);
        }
    }

    public boolean isMovable() {
        return movable;
    }

    public void setMovable(boolean movable) {
        this.movable = movable;
    }

    public Server getServer() {
        Lock readLock = lock.readLock();
        readLock.lock();
        try {
            return server;
        }
        finally {
            readLock.unlock();
        }
    }

}
