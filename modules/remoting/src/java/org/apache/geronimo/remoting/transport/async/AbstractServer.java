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
package org.apache.geronimo.remoting.transport.async;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.geronimo.proxy.SimpleContainer;
import org.apache.geronimo.remoting.router.Router;
import org.apache.geronimo.remoting.transport.TransportServer;

import EDU.oswego.cs.dl.util.concurrent.ClockDaemon;

/**
 * Provides a Blocking implemenation of the AsynchChannelServer interface.
 * 
 * Sets up a blocking ServerSocket to accept blocking client connections.
 * 
 * @version $Revision: 1.1 $ $Date: 2003/11/16 05:27:27 $
 */
abstract public class AbstractServer extends SimpleContainer implements TransportServer {

    /** 
     * The amount of time that must pass before an idle connection is closed. 
     */
    public final long CONNECTION_TIMEOUT =
        Long.parseLong(System.getProperty("org.apache.geronimo.remoting.transport.async.connection_timeout", "300000"));
    // 5 min.

    private HashMap uriTo= new HashMap();
    private HashMap channelPools = new HashMap();

    /**
     * Used as the key into the channelPools Map.
     * 
     * The ipaddress and port are the only needed fields
     * to locate a channel pool. 
     */
    private class URIKey {

        String ipaddress;
        int port;

        URIKey(URI uri) {
            ipaddress = uri.getHost();
            port = uri.getPort();
        }

        /*
         * The key is used in a homogenous map.  We can cheat. 
         */
        public boolean equals(Object obj) {
            return ((URIKey) obj).port == port && ((URIKey) obj).ipaddress.equals(ipaddress);
        }
        public int hashCode() {
            return ipaddress.hashCode() + port;
        }
    }

    /**
     * Keeps a map of uri->ChannelPool objects.  Creates the 
     * ChannelPool if it the first time you access the uri.
     * 
     * TODO: think of way to remove ChannelPool objects that are not being used.
     * 
     * @param server
     */
    public ChannelPool getChannelPool(URI uri) {

        URIKey key = new URIKey(uri);
        synchronized (channelPools) {
            ChannelPool rc = (ChannelPool) channelPools.get(key);
            if (rc == null) {
                rc = new ChannelPool(uri, getNextRouter());
                channelPools.put(key, rc);
                ChannelPoolMonitor pm = new ChannelPoolMonitor(key, rc);
                pm.clockTicket = Registry.instance.getClockDaemon().executePeriodically(CONNECTION_TIMEOUT, pm, true);
            }
            return rc;
        }
    }

    /**
     * This class periodically checks one channel pool. 
     */
    class ChannelPoolMonitor implements Runnable {
        long age = System.currentTimeMillis();
        URIKey key;
        ChannelPool channelPool;
        Object clockTicket;
        ChannelPoolMonitor(URIKey key, ChannelPool cp) {
            this.key = key;
            this.channelPool = cp;
        }
        public void run() {
            channelPool.expireIdleConnections(CONNECTION_TIMEOUT);

            // Should we start thinking bout aging out the whole pool??
            if ((System.currentTimeMillis() - age) < CONNECTION_TIMEOUT)
                return;

            synchronized (channelPools) {
                // Should we remove the channel pool??
                if (channelPool.getCreatedChannelCount() == 0) {
                    ClockDaemon.cancel(clockTicket);
                }
                channelPools.remove(key);
            }
        }
    }

    /**
     * @see org.apache.geronimo.remoting.transport.TransportServer#dispose()
     */
    public void dispose() throws Exception {
        synchronized (channelPools) {
            Iterator iterator = channelPools.values().iterator();
            while (iterator.hasNext()) {
                ChannelPool pool = (ChannelPool) iterator.next();
                pool.dispose();
                iterator.remove();
            }
        }
    }

    abstract public Router getNextRouter();

    /**
     * @see org.apache.geronimo.remoting.transport.TransportServer#start()
     */
    public void start() throws Exception {
        if (Registry.instance.getDefaultServer() == null)
            Registry.instance.setDefaultServer(this);
    }

    /**
     * @see org.apache.geronimo.remoting.transport.TransportServer#stop()
     */
    public void stop() throws Exception {
        if (Registry.instance.getDefaultServer() == this)
            Registry.instance.setDefaultServer(null);
    }

}
