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

import java.nio.channels.SocketChannel;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import EDU.oswego.cs.dl.util.concurrent.ClockDaemon;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.system.ClockPool;


/**
 * @version $Revision: 1.2 $ $Date: 2004/03/10 02:20:08 $
 */
public class ProtocolFactory implements ServerSocketAcceptorListener, GBean {

    private final static Log log = LogFactory.getLog(ProtocolFactory.class);

    private AcceptableProtocol template;
    private ClockPool clockPool;
    private Map connectionCache = new Hashtable();
    private static volatile long nextConnectionId = 0;
    private long reclaimPeriod;
    private long maxAge;
    private long maxInactivity;


    public AcceptableProtocol getTemplate() {
        return template;
    }

    public void setTemplate(AcceptableProtocol template) {
        this.template = template;
    }

    public ClockPool getClockPool() {
        return clockPool;
    }

    public void setClockPool(ClockPool clockPool) {
        this.clockPool = clockPool;
    }

    public long getReclaimPeriod() {
        return reclaimPeriod;
    }

    public void setReclaimPeriod(long reclaimPeriod) {
        this.reclaimPeriod = reclaimPeriod;
    }

    public long getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }

    public long getMaxInactivity() {
        return maxInactivity;
    }

    public void setMaxInactivity(long maxInactivity) {
        this.maxInactivity = maxInactivity;
    }

    public void accept(SocketChannel socketChannel) {
        try {
            AcceptableProtocol protocol = (AcceptableProtocol) template.cloneProtocol();
            protocol.accept(socketChannel);
            protocol.doStart();

            Long id = new Long(nextConnectionId++);

            ConnectionCacheMonitor ccm = new ConnectionCacheMonitor(id, protocol, maxAge, maxInactivity);
            ccm.clockTicket = clockPool.getClockDaemon().executePeriodically(reclaimPeriod, ccm, true);

            connectionCache.put(id, ccm);

            log.trace("Connection [" + ccm.key + "] accepted");
        } catch (CloneNotSupportedException e) {
            log.error("Error accepting connection from " + socketChannel.socket().getInetAddress() + " " + e);
        } catch (ProtocolException e) {
            log.error("Error accepting connection from " + socketChannel.socket().getInetAddress() + " " + e);
        }
    }

    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() throws WaitingException, Exception {
    }

    public void doStop() throws WaitingException, Exception {
        synchronized (connectionCache) {
            Iterator keys = connectionCache.keySet().iterator();
            while (keys.hasNext()) {
                ConnectionCacheMonitor ccm = (ConnectionCacheMonitor) connectionCache.get(keys.next());

                ClockDaemon.cancel(ccm.clockTicket);
                ccm.connection.doStop();

                log.trace("Connection [" + ccm.key + "] reclaimed");
            }
            connectionCache.clear();
        }
    }

    public void doFail() {

    }

    /**
     * This class periodically checks one login module.
     */
    private class ConnectionCacheMonitor implements Runnable {

        final Long key;
        final AcceptableProtocol connection;
        final long maxAge;
        final long maxInactivity;
        Object clockTicket;

        ConnectionCacheMonitor(Long key, AcceptableProtocol connection, long maxAge, long maxInactivity) {
            this.key = key;
            this.connection = connection;
            this.maxAge = maxAge;
            this.maxInactivity = maxInactivity;
        }

        public void run() {
            try {
                synchronized (connectionCache) {
                    long currentTime = System.currentTimeMillis();
                    if (connection.isDone()
                            || (currentTime - connection.getCreated()) > maxAge
                            || (currentTime - connection.getLastUsed()) > maxInactivity) {
                        log.trace("Connection [" + key + "] reclaimed");

                        connection.doStop();

                        ClockDaemon.cancel(clockTicket);
                        connectionCache.remove(key);
                    }
                }
            } catch (ProtocolException e) {
                e.printStackTrace();
            }
        }
    }
}
