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

import org.apache.geronimo.session.NoSuchSessionException;
import org.apache.geronimo.session.SessionNotLocalException;
import org.apache.geronimo.session.WriteLockTimedOutException;

import java.util.Map;

/**
 * The remote interface to a server; this interface will usually be wrapped in a dynamic proxy
 * to do some kind of remoting such as via Lingo, ActiveIO or OpenEJB
 * 
 * @version $Revision: $
 */
public interface RemoteClient extends SessionListener {

    Map moveState(String serverName, String sessionId) throws NoSuchSessionException, SessionNotLocalException,
            WriteLockTimedOutException;

    void unlock(String serverName, String sessionId) throws NoSuchSessionException, WriteLockTimedOutException;

}
