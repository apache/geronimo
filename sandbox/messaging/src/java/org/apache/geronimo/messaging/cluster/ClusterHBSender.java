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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.GBeanLifecycleController;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.messaging.Node;
import org.apache.geronimo.pool.ClockPool;

import EDU.oswego.cs.dl.util.concurrent.ClockDaemon;

/**
 * Node heartbeat sender.
 * <BR>
 * Sends heartbeats for a given node, which is a member of a given cluster. 
 *
 * @version $Rev$ $Date$
 */
public class ClusterHBSender
    implements GBeanLifecycle
{

    private static final Log log = LogFactory.getLog(ClusterHBSender.class);
    
    /**
     * Heartbeats are sent for this node.
     */
    private final Node node;
    
    private byte[] infoAsBytes;

    /**
     * To execure periodical operations.
     */
    private final ClockPool clockPool;

    /**
     * Heartbeats are sent to this cluster.
     */
    private final Cluster cluster;

    /**
     * Number of milliseconds between two heartbeats.
     */
    private final long delay;
    
    /**
     * To manage the state of this component when it is not possible to send
     * an heartbeat.
     */
    private final GBeanLifecycleController controller;
    
    private MulticastSocket socket;
    
    /**
     * Opaque ClockPool ticket.
     */
    private Object ticket;

    /**
     * Creates a node heartbeat sender.
     * 
     * @param aNode Node.
     * @param aCluster Cluster to which this node is a member.
     * @param aClockPool To execute periodical tasks.
     * @param aDelay Number of milliseconds between two heartbeats.
     * @param aController To control the lifecycle of this component.
     */
    public ClusterHBSender(Node aNode, Cluster aCluster, ClockPool aClockPool, 
        long aDelay, GBeanLifecycleController aController) {
        if ( null == aNode ) {
            throw new IllegalArgumentException("Node is required.");
        } else if ( null == aClockPool ) {
            throw new IllegalArgumentException("ClockPool is required.");
        } else if ( null == aCluster ) {
            throw new IllegalArgumentException("Cluster is required.");
        } else if ( null == aController ) {
            throw new IllegalArgumentException("Controller is required.");
        }
        node = aNode;
        clockPool = aClockPool;
        cluster = aCluster;
        delay = aDelay;
        controller = aController;
    }

    public void doStart() throws WaitingException, Exception {
        ByteArrayOutputStream memOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(memOut);
        out.writeObject(node.getNodeInfo());
        out.writeLong(delay);
        out.flush();
        out.close();
        infoAsBytes = memOut.toByteArray();
        
        socket = new MulticastSocket(cluster.getClusterInfo().getPort());
        ticket = clockPool.getClockDaemon().
            executePeriodically(delay, new HeartBeatTask(), true);
    }

    public void doStop() throws WaitingException, Exception {
        ClockDaemon.cancel(ticket);
        socket.close();
    }

    public void doFail() {
        ClockDaemon.cancel(ticket);
        socket.close();
    }

    private class HeartBeatTask implements Runnable {

        public void run() {
            ClusterInfo info = cluster.getClusterInfo();
            DatagramPacket packet = new DatagramPacket(infoAsBytes,
                infoAsBytes.length, info.getAddress(),info.getPort());
            try {
                socket.send(packet);
            } catch (IOException e) {
                if ( State.RUNNING.toInt() != controller.getState() ) {
                    log.error("Can not send heartbeat packet", e);
                    controller.fail();
                }
            }
        }
        
    }
    
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory factory = new GBeanInfoFactory(ClusterHBSender.class);
        factory.setConstructor(new String[] {"Node", "Cluster", "ClockPool",
            "delay", "gbeanLifecycleController"});
        factory.addReference("Node", Node.class);
        factory.addReference("Cluster", Cluster.class);
        factory.addReference("ClockPool", ClockPool.class);
        factory.addAttribute("delay", long.class, true);
        factory.addAttribute("gbeanLifecycleController", GBeanLifecycleController.class, false);
        GBEAN_INFO = factory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
    
}
