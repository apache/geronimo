/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xalan" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, Lotus
 * Development Corporation., http://www.lotus.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.geronimo.remoting.transport.async.bio;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.remoting.router.Router;
import org.apache.geronimo.remoting.transport.TransportException;
import org.apache.geronimo.remoting.transport.URISupport;
import org.apache.geronimo.remoting.transport.async.AbstractServer;
import org.apache.geronimo.remoting.transport.async.ChannelPool;
/**
 * Provides a Blocking implemenation of the AsynchChannelServer interface.
 * 
 * Sets up a blocking ServerSocket to accept blocking client connections.
 * 
 * @version $Revision: 1.3 $ $Date: 2003/08/30 14:49:25 $
 */
public final class BlockingServer extends AbstractServer implements Runnable {
    final static private Log log = LogFactory.getLog(BlockingServer.class);

    /**
     * The default timeout for the server socket. This is
     * set so the socket will periodically return to check
     * the running flag.
     */
    private final static int SO_TIMEOUT = 5000;
    private ServerSocketChannel serverSocketChannel;
    private URI uri;
    private URI connectURI;
    private Thread worker;
    private boolean running;
    private int compression = -1;
    private boolean enableTcpNoDelay;
    private Router nextRouter;

    public void bind(URI localURI, Router dispatcher) throws IOException, URISyntaxException {
        this.uri = localURI;
        this.nextRouter = dispatcher;

        String serverBindAddress = uri.getHost();
        String clientConnectAddress = null;
        int serverBindPort = uri.getPort();
        int clientConnectPort = serverBindPort;
        int connectBackLog = 50;
        enableTcpNoDelay = true;

        Properties params = URISupport.parseQueryParameters(uri);
        enableTcpNoDelay = params.getProperty("tcp.nodelay", "true").equals("true");
        connectBackLog = Integer.parseInt(params.getProperty("tcp.backlog", "50"));
        clientConnectAddress = params.getProperty("client.host");
        clientConnectPort = Integer.parseInt(params.getProperty("client.port", "0"));
        clientConnectPort = Integer.parseInt(params.getProperty("compression", "-1"));

        compression = Math.min(compression, 9);
        compression = Math.max(compression, -1);

        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(InetAddress.getByName(serverBindAddress), serverBindPort),connectBackLog);
        serverSocketChannel.socket().setSoTimeout(SO_TIMEOUT);

        // Lookup the local host name if needed.
        clientConnectAddress =
            (clientConnectAddress == null || clientConnectAddress.length() == 0)
                ? InetAddress.getLocalHost().getHostName()
                : clientConnectAddress;
        clientConnectPort = (clientConnectPort <= 0) ? serverSocketChannel.socket().getLocalPort() : clientConnectPort;

        // Create the client URI:
        Properties clientParms = new Properties();
        clientParms.put("tcp.nodelay", enableTcpNoDelay ? "true" : "false");
        clientParms.put("compression", "" + compression);
        this.connectURI =
            new URI(
                "async",
                null,
                clientConnectAddress,
                clientConnectPort,
                "",
                URISupport.toQueryString(clientParms),
                null);
        log.info(
            "Remoting 'async' protocol available at: "
                + serverSocketChannel.socket().getInetAddress()
                + ":"
                + serverSocketChannel.socket().getLocalPort());
        log.info("Remoting 'async' protocol clients will connect to: " + connectURI);
    }

    synchronized public void start() throws Exception {
        if (running)
            return;
        running = true;
        worker = new Thread(this, "Acceptor " + getClientConnectURI());
        worker.setDaemon(true);
        worker.start();
        super.start();
    }

    public void stop() throws Exception {
        if (!running)
            return;
        running = false;
        try {
            worker.interrupt();
            worker.join();
        } catch (InterruptedException e) {
        }
        super.stop();
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            while (running) {
                SocketChannel socket = null;
                try {
                    socket = serverSocketChannel.accept();
                    if (log.isTraceEnabled())
                        log.trace("Accepted connection: " + socket);
                } catch (java.io.InterruptedIOException e) {
                    // It's ok, this is due to the SO_TIME_OUT
                    continue;
                }
                try {
                    socket.socket().setTcpNoDelay(enableTcpNoDelay);
                    BlockingChannel channel = new BlockingChannel();
                    channel.init(connectURI, socket);
                    ChannelPool pool = getChannelPool(channel.getRemoteURI());
                    pool.associate(channel);
                } catch (TransportException ie) {
                    log.debug("Client connection could not be accepted: ", ie);
                } catch (IOException ie) {
                    log.debug("Client connection could not be accepted: ", ie);
                } catch (URISyntaxException e) {
                    log.debug("Client connection could not be accepted: ", e);
                }
            }
        } catch (SocketException e) {
            // There is no easy way (other than string comparison) to
            // determine if the socket exception is caused by connection
            // reset by peer. In this case, it's okay to ignore both
            // SocketException and IOException.

            if (running) // We may have been stopped.
                log.warn(
                    "SocketException occured (Connection reset by peer?). Shutting down remoting 'async' protocol.");
        } catch (IOException e) {
            if (running) // We may have been stopped.
                log.warn("IOException occured. Shutting down remoting 'async' protocol.");
        }
    }

    /**
     * @see org.apache.j2ee.remoting.transport.TransportServer#getClientConnectURI()
     */
    public URI getClientConnectURI() {
        return connectURI;
    }

    /**
     * @see org.apache.j2ee.remoting.transport.TransportServer#dispose()
     */
    public void dispose() throws Exception {

        if (running) {
            // we were disposed before a stop!  Shutdown QUICK.
            running = false;
            worker.interrupt();
        }
        serverSocketChannel.close();

        super.dispose();
    }

    /**
     * @see org.apache.geronimo.remoting.transport.async.AbstractServer#getNextRouter()
     */
    public Router getNextRouter() {
        return nextRouter;
    }
}