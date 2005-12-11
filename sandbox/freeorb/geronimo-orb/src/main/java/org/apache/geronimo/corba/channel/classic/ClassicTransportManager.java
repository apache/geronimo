/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
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
package org.apache.geronimo.corba.channel.classic;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

import EDU.oswego.cs.dl.util.concurrent.Executor;

import org.apache.geronimo.corba.channel.InputHandler;
import org.apache.geronimo.corba.channel.Transport;
import org.apache.geronimo.corba.channel.TransportManager;


public class ClassicTransportManager implements TransportManager {

    private Executor executor;

    public ClassicTransportManager(Executor executor) {
        this.executor = executor;
    }

    public Transport createTransport(SocketAddress addr, InputHandler handler)
            throws IOException
    {


        Socket sock = new Socket();
        sock.connect(addr);

        return new SyncClassicTransport(this, sock, handler);
    }

    public void start() throws InterruptedException {
        // TODO Auto-generated method stub

    }

    public void shutdown() throws IOException {
        // TODO Auto-generated method stub

    }

    public Executor getExecutor() {
        return executor;
    }

}
