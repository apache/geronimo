/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.network.protocol;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.network.SelectionEventListner;
import org.apache.geronimo.network.SelectorManager;
import org.apache.geronimo.network.URISupport;


/**
 * @version $Revision: 1.7 $ $Date: 2004/05/01 17:23:55 $
 */
public class ServerSocketAcceptor implements SelectionEventListner {

    final static private Log log = LogFactory.getLog(SocketProtocol.class);

    private URI uri;
    private URI connectURI;

    private ServerSocketChannel serverSocketChannel;
    private int timeOut;
    private boolean TCPNoDelay;
    private boolean reuseAddress = true;
    private SelectorManager selectorManager;
    private SelectionKey selectionKey;

    private ServerSocketAcceptorListener acceptorListener;

    private static final int STARTED = 0;
    private static final int STOPPED = 1;
    private static final int FAILED = 2;
    private int state = STOPPED;

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        if (state == STARTED || state == FAILED) throw new IllegalStateException("Protocol already started");
        this.uri = uri;
    }

    public URI getConnectURI() {
        return connectURI;
    }

    public ServerSocketChannel getServerSocketChannel() {
        return serverSocketChannel;
    }

    public void setServerSocketChannel(ServerSocketChannel serverSocketChannel) {
        if (state == STARTED || state == FAILED) throw new IllegalStateException("Protocol already started");
        this.serverSocketChannel = serverSocketChannel;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(int timeOut) {
        if (state == STARTED || state == FAILED) throw new IllegalStateException("Protocol already started");
        this.timeOut = timeOut;
    }

    public boolean isTCPNoDelay() {
        return TCPNoDelay;
    }

    public void setTCPNoDelay(boolean TCPNoDelay) {
        if (state == STARTED || state == FAILED) throw new IllegalStateException("Protocol already started");
        this.TCPNoDelay = TCPNoDelay;
    }

    public boolean isReuseAddress() {
        return reuseAddress;
    }

    public void setReuseAddress(boolean reuseAddress) {
        if (state == STARTED || state == FAILED) throw new IllegalStateException("Protocol already started");
        this.reuseAddress = reuseAddress;
    }

    public ServerSocketAcceptorListener getAcceptorListener() {
        return acceptorListener;
    }

    public void setAcceptorListener(ServerSocketAcceptorListener acceptorListener) {
        if (state == STARTED || state == FAILED) throw new IllegalStateException("Protocol already started");
        this.acceptorListener = acceptorListener;
    }

    public SelectorManager getSelectorManager() {
        return selectorManager;
    }

    public void setSelectorManager(SelectorManager selectorManager) {
        if (state == STARTED || state == FAILED) throw new IllegalStateException("Protocol already started");
        this.selectorManager = selectorManager;
    }

    public void startup() throws Exception {
        String serverBindAddress = uri.getHost();
        int serverBindPort = uri.getPort();
        int connectBackLog = 50;
        TCPNoDelay = true;

        Properties params = URISupport.parseQueryParameters(uri);
        TCPNoDelay = params.getProperty("tcp.nodelay", "true").equals("true");
        connectBackLog = Integer.parseInt(params.getProperty("tcp.backlog", "50"));

        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().setReuseAddress(reuseAddress);
        serverSocketChannel.socket().bind(new InetSocketAddress(InetAddress.getByName(serverBindAddress), serverBindPort), connectBackLog);
        serverSocketChannel.socket().setSoTimeout(timeOut);
        serverSocketChannel.configureBlocking(false);
        selectionKey = selectorManager.register(serverSocketChannel, SelectionKey.OP_ACCEPT, this);

        // Create the client URI:
        Properties clientParms = new Properties();
        clientParms.put("tcp.nodelay", TCPNoDelay ? "true" : "false");
        connectURI = new URI("async",
                             null,
                             InetAddress.getByName(serverBindAddress).getHostName(),
                             serverSocketChannel.socket().getLocalPort(),
                             "",
                             URISupport.toQueryString(clientParms),
                             null);
        log.info("Remoting 'async' protocol available at: "
                 + serverSocketChannel.socket().getInetAddress()
                 + ":"
                 + serverSocketChannel.socket().getLocalPort());
        log.info("Remoting 'async' protocol clients will connect to: " + connectURI);

        state = STARTED;
    }

    public void drain() throws Exception {
        selectionKey.cancel();
        serverSocketChannel.close();
        state = STOPPED;
    }

    public void teardown() {
        state = STOPPED;        
    }

    public void selectionEvent(SelectionKey selection) {
        if (selection.isAcceptable()) {
            try {
                ServerSocketChannel server = (ServerSocketChannel) selection.channel();
                SocketChannel channel = server.accept();

                if (channel == null) return;

                channel.socket().setTcpNoDelay(TCPNoDelay);
                acceptorListener.accept(channel);

                selectorManager.addInterestOps(selectionKey, SelectionKey.OP_ACCEPT);
            } catch (IOException e) {
                // this should be ok
            }

        }
    }
}
