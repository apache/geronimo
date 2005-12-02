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
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

import EDU.oswego.cs.dl.util.concurrent.Executor;

import org.apache.geronimo.corba.channel.InputHandler;
import org.apache.geronimo.corba.channel.Transport;
import org.apache.geronimo.corba.channel.TransportManager;


public class AsyncNIOTransportManager implements TransportManager {

    private NIOSelector selector;

    private final SelectorProvider provider;

    private final Executor executor;

    public AsyncNIOTransportManager(Executor executor) throws IOException {
        this(executor, SelectorProvider.provider());
    }

    AsyncNIOTransportManager(Executor executor, SelectorProvider provider)
            throws IOException
    {
        this.executor = executor;
        this.provider = provider;
        this.selector = new NIOSelector(this, provider.openSelector());
    }

    public synchronized void start() throws InterruptedException {
        executor.execute(selector);
    }

    public void shutdown() throws IOException {
        if (selector.isRunning()) {
            selector.shutdown();
        }
    }

    public Transport createTransport(SocketAddress addr, InputHandler handler) throws IOException {

        SocketChannel ch = provider.openSocketChannel();
        ch.configureBlocking(false);
        ch.connect(addr);

        AsyncNIOSocketTransport result = new AsyncNIOSocketTransport(this, ch, handler);

        NIOSelector sel = getSelector();

        sel.register(ch, result);
        sel.addInterest(ch, SelectionKey.OP_CONNECT, "initial");

        return result;
    }

    NIOSelector getSelector() {
        return selector;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void removeInterest(AsyncNIOSocketTransport transport, int interest, String why) {
        selector.removeInterest(transport.channel(), interest, why);
    }

    public void addInterest(AsyncNIOSocketTransport transport, int interest, String why) {
        selector.addInterest(transport.channel(), interest, why);
    }

}
