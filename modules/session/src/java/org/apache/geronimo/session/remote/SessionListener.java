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

import org.apache.geronimo.session.Server;

/**
 * Notifications of new sessions arriving, sessions being destroyed or sessions
 * migrating.
 * 
 * @version $Revision: $
 */
public interface SessionListener {
    public void onServerCreate(Server server, RemoteClient remoteServerCallback);

    public void onServerRemove(String serverName);

    public void onSessionCreate(String serverName, String sessionId);

    public void onSessionMove(String serverName, String sessionId);

    public void onSessionDestroy(String serverName, String sessionId);

}
