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
 * A remote server reference which uses a remote proxy for the
 * {@link RemoteClient}
 * 
 * @version $Revision: $
 */
public class RemoteServer implements Server {

    private final Server server;
    private final RemoteClient remoteClient;

    public RemoteServer(Server server, RemoteClient control) {
        this.server = server;
        this.remoteClient = control;
    }

    public boolean isLocalServer() {
        return false;
    }

    public String[] getAddresses(String protocol) {
        return server.getAddresses(protocol);
    }

    public String getName() {
        return server.getName();
    }

    public void setAddresses(String string, String[] strings) {
        server.setAddresses(string, strings);
    }

    public RemoteClient getRemoteClient() {
        return remoteClient;
    }
}
