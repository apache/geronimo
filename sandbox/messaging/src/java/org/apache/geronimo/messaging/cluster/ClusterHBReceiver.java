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

package org.apache.geronimo.messaging.cluster;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.messaging.NodeException;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.pool.ClockPool;

import EDU.oswego.cs.dl.util.concurrent.ClockDaemon;
import EDU.oswego.cs.dl.util.concurrent.QueuedExecutor;

/**
 * Heartbeats listener.
 * <BR>
 * It joins the multicast group associated to the bound cluster and monitors
 * node heartbeats. When an heartbeat is received for the very first time, it
 * adds it to the underlying cluster. Conversely, when a configurable number of
 * heartbeats have been missed, it removes it from the underlying cluster.
 * <BR>
 * This service must be executed by a single node of the cluster. If the node
 * running this service fails, then another node of the cluster should start
 * automatically this service.
 *
 * @version $Rev$ $Date$
 */
public class ClusterHBReceiver
    implements GBeanLifecycle
{

    private static final Log log = LogFactory.getLog(ClusterHBReceiver.class);
    
    /**
     * Cluster to be notified when new nodes have been detected or have not
     * sent their heartbeats.
     */
    private final Cluster cluster;
    
    /**
     * To execute periodical tasks.
     */
    private final ClockPool clockPool;
    
    /**
     * Number of heartbeats which can be missed safely. If more than this 
     * number of heartbeat is missed for a given node, then the underlying
     * cluster is notified of the failure of a node.
     */
    private final int nbMissed; 

    /**
     * NodeInfo to HeartbeatTracker map.
     */
    private final Map trackers;

    /**
     * Queue the cluster operations (add or remove node).
     */
    private QueuedExecutor queuedExecutor;
    
    private MulticastSocket socket;

    /**
     * Is this service running?
     */
    private boolean running;
    
    /**
     * Creates an heartbeat listener.
     * 
     * @param aCluster Cluster to be notified when a new node has been detected
     * or when aNbMissed heartbeats have been missed and the associated node
     * needs to be drop from the cluster.
     * @param aClockPool To execute periodical operations.
     * @param aNbMissed Number of heartbeats which can be missed safely. If more
     * than this number of heartbeats are missed, then the associated node
     * is dropped.
     */
    public ClusterHBReceiver(Cluster aCluster, ClockPool aClockPool,
        int aNbMissed) {
        if ( null == aCluster ) {
            throw new IllegalArgumentException("Cluster is required.");
        } else if ( null == aClockPool ) {
            throw new IllegalArgumentException("ClockPool is required.");
        }
        cluster = aCluster;
        clockPool = aClockPool;
        nbMissed = aNbMissed;
        
        trackers = new HashMap();
    }
    
    public void doStart() throws WaitingException, Exception {
        ClusterInfo info = cluster.getClusterInfo();
        socket = new MulticastSocket(info.getPort());
        socket.joinGroup(info.getAddress());
        running = true;
        new Thread(new HearbeatListener()).start();
        queuedExecutor = new QueuedExecutor();
    }

    public void doStop() throws WaitingException, Exception {
        ClusterInfo info = cluster.getClusterInfo();
        running = false;
        stopTrackers();
        socket.leaveGroup(info.getAddress());
        socket.close();
        queuedExecutor.shutdownAfterProcessingCurrentlyQueuedTasks();
    }

    public void doFail() {
        ClusterInfo info = cluster.getClusterInfo();
        running = false;
        stopTrackers();
        try {
            socket.leaveGroup(info.getAddress());
        } catch (IOException e) {
            log.error("Can not leave group", e);
        }
        socket.close();
        queuedExecutor.shutdownAfterProcessingCurrentlyQueuedTasks();
    }

    /**
     * Stops the node trackers.
     */
    private void stopTrackers() {
        Collection tmpTrackers;
        synchronized(trackers) {
            tmpTrackers = new ArrayList(trackers.values());
        }
        for (Iterator iter = tmpTrackers.iterator(); iter.hasNext();) {
            HeartbeatTracker tracker = (HeartbeatTracker) iter.next();
            try {
                tracker.stop();
            } catch (NodeException e) {
                log.error(e);
            }
        }
    }
    
    /**
     * Listens to the heartbeat sent to this service.
     */
    private class HearbeatListener implements Runnable {
        public void run() {
            while ( running ) {
                try {
                    byte[] buf = new byte[32768];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    try {
                        socket.receive(packet);
                    } catch (SocketException e) {
                        // This can happen when the socket is closed. So we 
                        // simply break.
                        // TODO check if the state is really stopping. If it
                        // is not, then one needs to stop the service.
                        log.error(e);
                        break;
                    }
                    long timestamp = System.currentTimeMillis();
                    ByteArrayInputStream memIn =
                        new ByteArrayInputStream(buf, 0, packet.getLength());
                    ObjectInputStream in = new ObjectInputStream(memIn);
                    NodeInfo nodeInfo = (NodeInfo) in.readObject();
                    HeartbeatTracker tracker;
                    synchronized(trackers) {
                        tracker = (HeartbeatTracker) trackers.get(nodeInfo);
                        if ( null == tracker ) {
                            long tempo = in.readLong();
                            tracker = new HeartbeatTracker(nodeInfo, tempo);
                            trackers.put(nodeInfo, tracker);
                            // Does not start the tracker in this thread
                            // as one wants this loop to reflect "correct"
                            // timestamps. When a tracker is started, it
                            // adds its associated node to the cluster, which
                            // can takes some time.
                            queuedExecutor.execute(new StartTracker(tracker));
                        }
                    }
                    tracker.lastTimestamp = timestamp;
                } catch (Exception e) {
                    log.error("Error while listening heartbeat", e);
                }
            }
        }
    }
    
    /**
     * Starts an heartbeat tracker.
     */
    private class StartTracker implements Runnable {
        private final HeartbeatTracker tracker;
        private StartTracker(HeartbeatTracker aTracker) {
            tracker = aTracker;
        }
        public void run() {
            try {
                tracker.start();
            } catch (NodeException e) {
                synchronized(trackers) {
                    trackers.remove(tracker.node);
                }
                log.error("Can not start tracker", e);
            }
        }
    }
    
    /**
     * Tracks the heartbeat of a given node.
     */
    private class HeartbeatTracker implements Runnable {
        /**
         * Node to be tracked.
         */
        private final NodeInfo node;
        /**
         * Delay between two heartbeats sent by the node.
         */
        private final long delay;
        /**
         * Last time that an heartbeat has been received.
         */
        private long lastTimestamp;
        /**
         * Current number of missed heartbeats.
         */
        private int missed;
        /**
         * Opaque ClockPool ticket.
         */
        private Object ticket;
        private HeartbeatTracker(NodeInfo aNode, long aDelay) {
            node = aNode;
            delay = aDelay;
        }
        public void run() {
            long inactivePeriod = System.currentTimeMillis() - lastTimestamp;
            if ( nbMissed < inactivePeriod / delay ) {
                try {
                    stop();
                } catch (NodeException e) {
                    log.error(e);
                } catch (Throwable e) {
                    // Ensures that the underlying thread of ClockDaemon
                    // is not interrupted.
                    // TODO as a matter of fact, this happen if a node
                    // fails during a reconfiguration. There is a bug in
                    // the way exceptions are unwrapped via GBeans. 
                    log.error(e);
                }
            }
        }
        public void start() throws NodeException {
            cluster.addMember(node);
            ticket = clockPool.getClockDaemon().
                executePeriodically(delay, this, false);
        }
        public void stop() throws NodeException {
            synchronized(trackers) {
                trackers.remove(node);
            }
            ClockDaemon.cancel(ticket);
            cluster.removeMember(node);
        }
    }
    

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory factory = new GBeanInfoFactory(ClusterHBReceiver.class);
        factory.setConstructor(new String[] {"Cluster", "ClockPool", "nbMissed"});
        factory.addReference("Cluster", Cluster.class);
        factory.addReference("ClockPool", ClockPool.class);
        factory.addAttribute("nbMissed", int.class, true);
        GBEAN_INFO = factory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
