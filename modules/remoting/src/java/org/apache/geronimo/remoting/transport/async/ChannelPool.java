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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.remoting.router.Router;
import org.apache.geronimo.remoting.transport.Msg;
import org.apache.geronimo.remoting.transport.TransportException;
import org.apache.geronimo.remoting.transport.async.Correlator.FutureResult;

import EDU.oswego.cs.dl.util.concurrent.Executor;
import EDU.oswego.cs.dl.util.concurrent.Semaphore;
/**
 * a ChannelPool represents a logical connection to a remote uri.
 * - It handles decomposing synchronous requests into async requests.
 * - It pools AsychChannel connections to be able concurrently do multiple
 * asyc sends. 
 *   
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:59:20 $
 */
public class ChannelPool implements Router {

    private static final Log log = LogFactory.getLog(ChannelPool.class);

    private final URI remoteURI;
    private URI backConnectURI;
    private final List available = new ArrayList();
    private final Correlator responseManager = new Correlator();
    private Router dispatcher;
    private int createdChannelCount = 0;
    private Executor workManager = Registry.instance.getWorkManager();

    private Semaphore maxOpenConnections = new Semaphore(Registry.MAX_CONNECTION_POOL_SIZE);

    /**
     * @param uri
     */
    public ChannelPool(URI uri, Router dispatcher) {
        this.remoteURI = uri;
        this.dispatcher = dispatcher;
        try {
            if (Registry.instance.getServerForClientRequest() == null) {
                backConnectURI = new URI("async://localhost:0");
            } else {
                backConnectURI = Registry.instance.getServerForClientRequest().getClientConnectURI();
            }
        } catch (Exception e) {
        }
    }
    
    public void dispose() {
        Iterator iterator;
        synchronized (available) {
            ArrayList al = new ArrayList();
            al.addAll(available);
            iterator = al.iterator();
        }
        while (iterator.hasNext()) {
            PooledAsynchChannel c = (PooledAsynchChannel) iterator.next();
            try {
                c.closeInternal();
            } catch (Exception e) {
            }
        }
    }

    /**
     * An PooledAsynchChannel wraps an AsynchChannel.
     * The PooledAsynchChannel will trap the close() call to 
     * return the channel to the pool or potentialy close it.
     * 
     * It also maintains usage data to be able to allow
     * the pool to expire old AsynchChannels.   
     *
     * Communication errors will flag the connection to be
     * removed from the pool. 
     */
    private class PooledAsynchChannel implements ChannelListner {

        private Channel next;
        boolean doCloseInternal;
        long lastUsed = System.currentTimeMillis();

        PooledAsynchChannel(Channel next) {
            this.next = next;
            createdChannelCount++;
        }

        public void open(URI uri, URI localuri) throws TransportException, TransportException {
            try {
                next.open(uri, localuri, this);
            } catch (TransportException e) {
                doCloseInternal = true;
                throw e;
            }
        }

        public void open() throws TransportException {
            try {
                next.open(this);
            } catch (TransportException e) {
                doCloseInternal = true;
                throw e;
            }
        }

        public void close() throws TransportException {
            if (doCloseInternal) {
                // Don't return. really close it out
                closeInternal();
            } else {
                returnToPool(this);
            }
        }

        public void closeInternal() throws TransportException {
            createdChannelCount--;
            next.close();
            maxOpenConnections.release();
        }

        public void send(AsyncMsg data) throws TransportException {
            try {
                lastUsed = System.currentTimeMillis();
                next.send(data);
            } catch (TransportException e) {
                doCloseInternal = true;
                throw e;
            }
        }

        /*
         * Fail safe in case connections are not being closed 
         * normally. 
         */
        protected void finalize() throws Throwable {
            try {
                closeInternal();
            } catch (TransportException ignore) {
            }
            super.finalize();
        }

        public void receiveEvent(AsyncMsg data) {
            lastUsed = System.currentTimeMillis();
            dispatch(data);
        }

        /* 
         * Connection was closed by the remote end.
         */
        public void closeEvent() {
            doCloseInternal = true;
            // If it was still the pool remove it.
            synchronized (available) {
                available.remove(this);
            }
            try {
                close();
            } catch (TransportException ignore) {
            }
        }
    }

    /**
     * Associate a channel to the pool.
     * When a AsynchChannelServer accepts a new channel it will
     * associate the existing AsynchChannel with the pool.
     * 
     * TODO: Add some logic to age out old idle connections.
     */
    public void associate(Channel c) throws TransportException {
        synchronized (available) {
            PooledAsynchChannel channel = new PooledAsynchChannel(c);
            channel.open();
            available.add(channel);
        }
    }

    private void returnToPool(PooledAsynchChannel c) {
        synchronized (available) {
            available.add(c);
        }
    }

    /**
     * Expires idle connections
     * 
     * @return
     */
    public void expireIdleConnections(long connectionTimeout) {
        synchronized (available) {
            if (available.isEmpty())
                return;
            long limit = System.currentTimeMillis() - connectionTimeout;
            for (int i = 0; i < available.size(); i++) {
                PooledAsynchChannel pc = (PooledAsynchChannel) available.get(i);
                // is it too old??
                if (pc.lastUsed < limit) {
                    available.remove(i);
                    try {
                        pc.closeInternal();
                    } catch (TransportException e) {
                        log.trace("Could not close out a channel correctly.", e);
                    }
                } else {
                    // no need to check the rest because they are in LRU order.
                    break;
                }
            }
        }
    }

    /**
     * Return the next available AsynchChannel object for a given invocation session.
     * It will automatically allocate a new AsynchChannel if none are available.
     *
     * @return
     * @throws RemotingException
     */
    synchronized public PooledAsynchChannel getNextAvailable() throws TransportException {

        try {
            do {

                synchronized (available) {
                    if (available.isEmpty() == false) {
                        // Remove last to avoid array copy.
                        return (PooledAsynchChannel) available.remove(available.size() - 1);
                    }
                }

                // We fall out of the loop once we aquire a permit to open a connection to the server.  
            } while (!maxOpenConnections.attempt(100));

        } catch (InterruptedException e1) {
            throw new TransportException("(" + remoteURI + "): " + e1);
        }

        // not available, make one on demand
        try {

            log.debug("channel connecting to: " + remoteURI);
            PooledAsynchChannel c = new PooledAsynchChannel(TransportFactory.instance.createAsynchChannel());
            c.open(remoteURI, backConnectURI);

            return c;
        } catch (Exception e) {
            // return the aquired permit.
            maxOpenConnections.release();
            log.debug("Connect Failed: ", e);
            if (log.isDebugEnabled()) {
                log.debug("channel connection to: " + remoteURI + " failed", e);
            }
            throw new TransportException("(" + remoteURI + "): " + e);
        }
    }

    /**
     * Used by a PooledAsynchChannel object to dispatch a message.  
     * 
     * @param data
     */
    private void dispatch(AsyncMsg message) {
        boolean trace = log.isTraceEnabled();
        try {
            switch (message.type) {
                case AsyncMsg.DATAGRAM_TYPE :
                    if (trace)
                        log.trace("received datagram request data.");
                    dispatchDatagram(new URI(message.to), message, this);
                    return;
                case AsyncMsg.REQUEST_TYPE :
                    if (trace)
                        log.trace("received request data for request: " + message.requestId);
                    dispatchRequest(new URI(message.to), message, this);
                    return;
                case AsyncMsg.RESPONE_TYPE :
                    if (trace)
                        log.trace("received response data for request: " + message.requestId);
                    responseManager.dispatchResponse(message.requestId, message);
                    return;
                default :
                    log.warn("Protocol Error: unknown message type: " + message.type);
                    return;
            }
        } catch (URISyntaxException e) {
            log.debug("Bad request: ", e);
        }
    }

    /**
     * A ChannelPool will receive data from a Channel and if it is 
     * new request, it will forward it to the AsynchChannelServer
     * for it to dispatch the work the appropriate subsystem.
     *
     * This a datagram that does not require a response message
     * to be sent back. 
     * 
     * @param data
     * @param source - the channel pool that the datagram came over.
     */
    public void dispatchDatagram(final URI to, final Msg data, final ChannelPool source) {

        if (dispatcher == null) {
            log.warn("Received a datagram but the dispatcher has not been registed.");
            return;
        }

        Runnable work = new Runnable() {
            public void run() {
                try {
                    dispatcher.sendDatagram(to, data);
                } catch (Throwable e) {
                    log.trace("Request Failed.", e);
                }
            }
        };
        try {
            workManager.execute(work);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * A ChannelPool will receive data from a Channel and if it is 
     * new request, it will forward it to the AsynchChannelServer
     * for it to dispatch the work the appropriate subsystem.
     * 
     * This a request and requires a response message
     * to be sent back. 
     * 
     * @param data
     * @param source - the channel pool that the request came over.
     * @param requestId - the requestid of the message.
     */
    public void dispatchRequest(final URI to, final AsyncMsg data, final ChannelPool source) {

        if (dispatcher == null) {
            log.warn("Received a request but the dispatcher has not been registed.");
            return;
        }

        Runnable work = new Runnable() {
            public void run() {
                try {
                    Msg result = dispatcher.sendRequest(to, data);
                    source.sendResponse(result, data.requestId);
                } catch (Throwable e) {
                    log.trace("Request failed.", e);
                }
            }
        };
        try {
            workManager.execute(work);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void safeClose(PooledAsynchChannel c) {
        if (c == null)
            return;
        try {
            c.close();
        } catch (TransportException e) {

        }
    }

    public void sendDatagram(URI to, Msg data) throws TransportException {
        AsyncMsg d = (AsyncMsg) data;
        PooledAsynchChannel c = getNextAvailable();
        try {
            d.type = AsyncMsg.DATAGRAM_TYPE;
            d.to = to.toString();
            c.send(d);
        } finally {
            safeClose(c);
        }
    }

    public Msg sendRequest(URI to, Msg data) throws TransportException {
        AsyncMsg d = (AsyncMsg) data;
        PooledAsynchChannel c = getNextAvailable();
        FutureResult requestId = responseManager.getNextFutureResult();

        try {
            d.type = AsyncMsg.REQUEST_TYPE;
            d.to = to.toString();
            d.requestId = requestId.getID();

            if (log.isTraceEnabled())
                log.trace("sending request data for request: " + requestId.getID());
            c.send(d);

        } finally {
            safeClose(c);
        }

        try {
            Msg result = (AsyncMsg) requestId.poll(Registry.REQUEST_TIMEOUT);
            if (log.isTraceEnabled())
                log.trace("response data was corelated for request: " + requestId.getID());
            if (result == null)
                throw new TransportException("Request time out.");
            return result;
        } catch (InterruptedException e) {
            throw new TransportException(e.getMessage());
        }

    }

    public void sendResponse(Msg data, int requestId) throws TransportException {
        AsyncMsg d = (AsyncMsg) data;
        PooledAsynchChannel c = getNextAvailable();

        try {
            d.type = AsyncMsg.RESPONE_TYPE;
            d.requestId = requestId;
            if (log.isTraceEnabled())
                log.trace("sending response data for request: " + requestId);
            c.send(d);
        } finally {
            safeClose(c);
        }
    }

    public int getCreatedChannelCount() {
        return createdChannelCount;
    }


    /**
     * @return Returns the backConnectURI.
     */
    public URI getBackConnectURI() {
        return backConnectURI;
    }

    /**
     * @param backConnectURI The backConnectURI to set.
     */
    public void setBackConnectURI(URI backConnectURI) {
        this.backConnectURI = backConnectURI;
    }

}
