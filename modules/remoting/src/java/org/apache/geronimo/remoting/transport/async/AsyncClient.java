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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.remoting.MarshalledObject;
import org.apache.geronimo.remoting.transport.BytesMarshalledObject;
import org.apache.geronimo.remoting.transport.Msg;
import org.apache.geronimo.remoting.transport.TransportClient;
import org.apache.geronimo.remoting.transport.TransportException;

/**
 * AsyncClientInvoker uses sockets to remotely connect to the
 * a remote AsyncServerInvoker.  Requests are sent asynchronously
 * to allow more concurrent requests to be sent to the server
 * while using fewer sockets.  This is also known as the 'async'
 * protocol.
 *
 * TODO:
 * If you are running on Java 1.4, this transport
 * transport will take advantage of the NIO
 * classes to further reduce the resources used on the server.
 *
 * @version $Revision: 1.6 $ $Date: 2004/03/10 09:59:20 $
 */
public class AsyncClient implements TransportClient {

    static final Log log = LogFactory.getLog(AsyncClient.class);

    /**
     * @see org.apache.geronimo.remoting.transport.TransportClient#sendRequest(org.apache.geronimo.remoting.URI, byte[])
     */
    public Msg sendRequest(URI to, Msg request) throws TransportException {
        AbstractServer server = Registry.instance.getServerForClientRequest();
        ChannelPool pool = server.getChannelPool(to);
        return pool.sendRequest(to, request);
    }

    /**
     * @see org.apache.j2ee.remoting.transport.TransportClient#sendDatagram(org.apache.j2ee.remoting.URI, byte[])
     */
    public void sendDatagram(URI to, Msg request) throws TransportException {
        AbstractServer server = Registry.instance.getServerForClientRequest();
        ChannelPool pool = server.getChannelPool(to);
        pool.sendDatagram(to, request);
    }

    /**
     * @see org.apache.geronimo.remoting.transport.TransportClient#createMsg()
     */
    public Msg createMsg() {
        return new AsyncMsg();
    }

    /**
     * @see org.apache.geronimo.remoting.transport.TransportClient#createMarshalledObject()
     */
    public MarshalledObject createMarshalledObject() {
        return new BytesMarshalledObject(Registry.transportContext);
    }

}
