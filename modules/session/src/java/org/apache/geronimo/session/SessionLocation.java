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
package org.apache.geronimo.session;

/**
 * Represents the location of a session which could be local or remote.
 * 
 * @version $Revision: $
 */
public interface SessionLocation {

    /**
     * Is the session local to this JVM so that it can be used directly or
     * must the user redirect, proxy or move.
     */
    boolean isLocal();

    /**
     * Is this session movable.
     */
    boolean isMovable();

    /**
     * Returns the session ID.
     */
    String getSessionId();

    /**
     * Returns the server (JVM) where this session is currently located so that
     * you can redirector proxy to the actual server where the session is hosted
     * or maybe use some server metadata to make a decision if its worth moving
     * it.
     */
    Server getServer();

    /**
     * Acquires the current session handle; you must release it when you have
     * done.
     * 
     * NOTE this session object is not thread safe
     * 
     * @see Session#release()
     */
    Session getSession() throws SessionNotLocalException;

    /**
     * Moves the remote session to this JVM and acquires the current session;
     * you must release it when you have done.
     * 
     * NOTE this session object is not thread safe
     * 
     * @see Session#release()
     */
    Session moveLocally() throws NoSuchSessionException, WriteLockTimedOutException, SessionNotLocalException,
            SessionNotMovableException;

    /**
     * Destroys the session from the system
     */
    void destroy() throws SessionNotLocalException;

}
