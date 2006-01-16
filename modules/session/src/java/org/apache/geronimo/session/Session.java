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
 * The state for a stateful session client which always exists in the local JVM;
 * it is never remote.
 * 
 * There is only one instance of this object in the system for a given session
 * ID; even when using buddy groups to replicate session state there will only
 * be one Session; the others are just backups of the serialized state (a
 * byte[]) and not an actual usable Session object.
 * 
 * @version $Revision: $
 */
public interface Session {

    /**
     * Returns the Id of the session.
     */
    public String getSessionId();

    /**
     * Releases the session so that its changed state can be replicated to its
     * buddies (once a write lock can be acquired as there may be concurrent
     * access to this session).
     * 
     */
    public void release();

    /**
     * Creates the state for the given key.
     */
    public void addState(String key, Object value);

    /**
     * This method assumes that the client will update the state object and so
     * if you are using a state backup mechanism then this method call marks the
     * entry as modified.
     */
    public Object getState(String key);

    /**
     * Removes the state entry for the given key (such as if an EJB stateful
     * session bean is destroyed)
     */
    public Object removeState(String key);
}
