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

package org.apache.geronimo.network;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.system.ThreadPool;


/**
 * The SelectorManager will manage one Selector and the thread that checks
 * the selector.
 *
 * @version $Revision: 1.4 $ $Date: 2004/04/03 00:07:51 $
 */
public class SelectorManager implements Runnable, GBean {

    final static private Log log = LogFactory.getLog(SelectorManager.class);

    private ThreadPool threadPool;

    /**
     * The running flag that all worker and server
     * threads check to determine if the service should
     * be stopped.
     */
    private volatile boolean running;

    /**
     * The guard
     */
    private Object guard = new Object();

    /**
     * The selector used to wait for non-blocking events.
     */
    private Selector selector;

    /**
     * timeout for select
     */
    private long timeout;

    /**
     * The groupd that threads created by this class will be a member of.
     */
    private ThreadGroup threadGroup;

    /**
     * The name that the selector thread is labeled
     */
    private String threadName;

    /**
     * how many times we have been started ++ and stoped --
     */
    private int startCounter;


    public SelectorManager() throws IOException {
        threadGroup = new ThreadGroup("Geronimo NIO Workers");
        selector = Selector.open();
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public Selector getSelector() {
        return selector;
    }

    public ThreadPool getThreadPool() {
        return threadPool;
    }

    public void setThreadPool(ThreadPool threadPool) {
        this.threadPool = threadPool;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    /**
     * Main processing method for the SelectionManager object
     */
    public void run() {
        try {

            log.debug("Selector Work thread has started.");
            while (running) {

                synchronized (guard) { /* do nothing */
                }

                log.trace("Waiting for selector to return");
                if (selector.select(timeout) == 0) continue;

                // Get a java.util.Set containing the SelectionKey objects for
                // all channels that are ready for I/O.
                Set keys = selector.selectedKeys();

                // Use a java.util.Iterator to loop through the selected keys
                for (Iterator i = keys.iterator(); i.hasNext();) {
                    final SelectionKey key = (SelectionKey) i.next();

                    try {
                        if (key.isReadable())key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
                        if (key.isWritable())key.interestOps(key.interestOps() & (~SelectionKey.OP_WRITE));
                        if (key.isAcceptable())key.interestOps(key.interestOps() & (~SelectionKey.OP_ACCEPT));

                        threadPool.getWorkManager().execute(new Runnable() {
                            public void run() {
                                try {
                                    ((SelectionEventListner) key.attachment()).selectionEvent(key);
                                } catch (Throwable e) {
                                    log.trace("Request Failed.", e);
                                }
                            }
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    i.remove(); // Remove the key from the set of selected keys
                }
            }
        } catch (CancelledKeyException e) {
        } catch (IOException e) {
            log.warn("IOException occured.", e);
        } finally {
            log.debug("Selector Work thread has stopped.");
        }
    }

    public SelectionKey register(SelectableChannel selectableChannel, int ops, SelectionEventListner listener) throws ClosedChannelException {
        synchronized (guard) {
            selector.wakeup();
            SelectionKey key = selectableChannel.register(selector, ops, listener);
            return key;
        }
    }

    public void setInterestOps(SelectionKey selectorKey, int setOps, int resetOps) {
        synchronized (guard) {
            selector.wakeup();
            selectorKey.interestOps((selectorKey.interestOps() & (~resetOps)) | setOps);
        }
    }

    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() throws WaitingException, Exception {
        startCounter++;
        if (startCounter == 1) {
            log.debug("Starting a Selector Work thread.");
            running = true;
            new Thread(threadGroup, (Runnable) this, threadName).start();
        }
    }

    public void doStop() throws WaitingException, Exception {
        startCounter--;
        if (startCounter == 0) {
            log.debug("Stopping a Selector Work thread.");
            running = false;
            selector.wakeup();
        }
    }

    public void doFail() {
    }

    private static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(SelectorManager.class.getName());

        infoFactory.addAttribute("Timeout", true);
        infoFactory.addAttribute("ThreadPool", true);
        infoFactory.addAttribute("ThreadName", true);
        infoFactory.addOperation("getSelector");
        infoFactory.addOperation("getStartCounter");

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
