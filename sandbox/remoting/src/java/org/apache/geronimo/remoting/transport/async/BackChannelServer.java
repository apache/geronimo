/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.remoting.transport.async;

import java.net.URI;

import org.apache.geronimo.remoting.router.Router;

/**
 * @version $Rev$ $Date$
 */
public class BackChannelServer extends AbstractServer {

    private Router dispatcher;

    /**
     * @see org.apache.geronimo.remoting.transport.TransportServer#bind(java.net.URI, org.apache.geronimo.remoting.transport.Router)
     */
    public void bind(URI bindURI, Router dispatcher) throws Exception {
        this.dispatcher = dispatcher;
    }

    /**
     * @see org.apache.geronimo.remoting.transport.TransportServer#getClientConnectURI()
     */
    public URI getClientConnectURI() {
        return null;
    }

    /**
     * @see org.apache.geronimo.remoting.transport.async.AbstractServer#getNextRouter()
     */
    public Router getNextRouter() {
        // TODO Auto-generated method stub
        return null;
    }

}
