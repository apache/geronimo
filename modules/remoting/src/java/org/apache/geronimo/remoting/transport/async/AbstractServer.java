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
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:58:04 $
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
