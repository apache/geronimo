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
package org.apache.geronimo.corba.channel.nio;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

import EDU.oswego.cs.dl.util.concurrent.Executor;

import org.apache.geronimo.corba.channel.InputHandler;
import org.apache.geronimo.corba.channel.Transport;
import org.apache.geronimo.corba.channel.TransportManager;


public class SyncNIOTransportManager implements TransportManager {

    private final SelectorProvider provider;

    private final Executor executor;

    SyncNIOTransportManager(Executor executor) throws IOException {
        this(executor, SelectorProvider.provider());
    }

    SyncNIOTransportManager(Executor executor, SelectorProvider provider)
            throws IOException
    {
        this.executor = executor;
        this.provider = provider;
    }

    public Transport createTransport(SocketAddress addr, InputHandler handler) throws IOException {
        SocketChannel ch = provider.openSocketChannel();
        ch.configureBlocking(true);
        ch.connect(addr);
        SyncNIOTransport t = new SyncNIOTransport(this, ch, handler);

        // executor.execute(inputListener);

        return t;
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
