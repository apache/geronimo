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

package org.apache.geronimo.remoting.transport.async.nio;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The SelectorManager will manage one Selector and the thread that checks 
 * the selector.
 * 
 * We may need to consider running more than one thread to check the selector
 * if servicing the selector takes too long.
 * 
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:58:04 $
 */
public class SelectorManager implements Runnable {

    final static private Log log = LogFactory.getLog(SelectorManager.class);

    static private SelectorManager instance;
    /**
     * The running flag that all worker and server
     * threads check to determine if the service should
     * be stopped.
     */
    private volatile boolean running;

    /**
     * The selector used to wait for non-blocking events.
     */
    private Selector selector;

    /**
     * The groupd that threads created by this class will be a member of.
     */
    private ThreadGroup threadGroup;

    /**
     * how many times we have been started ++ and stoped --
     */
    private int startCounter;

    protected SelectorManager() throws IOException {
        threadGroup = new ThreadGroup("NIO remoting Workers");
        selector = Selector.open();
    }

    public synchronized static SelectorManager getInstance() throws IOException {
        if (instance == null)
            instance = new SelectorManager();
        return instance;
    }

    /**
     * Main processing method for the SelectionManager object
     */
    public void run() {
        try {

            log.debug("Selector Work thread has started.");
            while (running) {

                log.trace("Waiting for selector to return");
                int count = selector.select(500);
                if (count == 0)
                    continue;

                // Get a java.util.Set containing the SelectionKey objects for
                // all channels that are ready for I/O.
                Set keys = selector.selectedKeys();

                // Use a java.util.Iterator to loop through the selected keys
                for (Iterator i = keys.iterator(); i.hasNext();) {
                    final SelectionKey key = (SelectionKey) i.next();
                    ((SelectionEventListner) key.attachment()).selectionEvent(key);
                    i.remove(); // Remove the key from the set of selected keys
                }
            }
        } catch (SocketException e) {
            // There is no easy way (other than string comparison) to
            // determine if the socket exception is caused by connection
            // reset by peer. In this case, it's okay to ignore both
            // SocketException and IOException.
            log.warn("SocketException occured (Connection reset by peer?).");
        } catch (IOException e) {
            log.warn("IOException occured.", e);
        } finally {
            log.debug("Selector Work thread has stopped.");
        }
    }

    synchronized public void start() {
        startCounter++;
        if (startCounter == 1) {
            log.debug("Starting a Selector Work thread.");
            running = true;
            new Thread(threadGroup, (Runnable) this, "Selector Worker").start();
        }
    }

    synchronized public void stop() {
        startCounter--;
        if (startCounter == 0) {
            log.debug("Stopping a Selector Work thread.");
            running = false;
        }
    }

    /**
     * @param socketChannel
     * @param i
     * @param channel
     * @return
     */
    public SelectionKey register(SocketChannel socketChannel, int ops, SelectionEventListner listner)
        throws ClosedChannelException {
        SelectionKey key = socketChannel.register(selector, ops, listner);
        selector.wakeup();
        return key;
    }

    /**
     * @param selectionKey
     * @param i
     */
    public void setInterestOps(SelectionKey selectionKey, int ops) {
        selectionKey.interestOps(ops);
        selector.wakeup();
    }

}
