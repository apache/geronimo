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
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.pool.ThreadPool;


/**
 * The SelectorManager will manage one Selector and the thread that checks
 * the selector.
 *
 * @version $Revision: 1.18 $ $Date: 2004/08/03 12:53:10 $
 */
public class SelectorManager implements Runnable, GBeanLifecycle {

    final static private Log log = LogFactory.getLog(SelectorManager.class);

    private ThreadPool threadPool;

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

    /**
     * A list of channels to be closed.
     */
    private Stack closing = new Stack();


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
            log.debug("Selector Manager timeout: " + timeout);
            while (running) {
                try {

                    synchronized (closing) {
                        if (!closing.isEmpty()) {
                            /**
                             * Close channels that have been queued up to be
                             * closed.  Closing channels in this manner prevents
                             * NullPointExceptions.
                             *
                             * http://developer.java.sun.com/developer/bugParade/bugs/4729342.html
                             */
                            Iterator iter = closing.iterator();

                            while (iter.hasNext()) {
                                SelectableChannel selectableChannel = (SelectableChannel) iter.next();
                                selectableChannel.close();
                            }
                            closing.clear();
                        }
                    }

                    log.trace("Waiting for selector to return.");
                    if (selector.select(timeout) == 0) {
                        log.trace("timeout == 0");

                        Iterator list = selector.keys().iterator();
                        while (list.hasNext()) {
                            SelectionKey key = (SelectionKey) list.next();
                            log.trace("Still watching " + key + " key is "
                                      + ((key.interestOps() & SelectionKey.OP_READ) != 0 ? "RF " : "")
                                      + ((key.interestOps() & SelectionKey.OP_WRITE) != 0 ? "WF " : "")
                                      + ((key.interestOps() & SelectionKey.OP_ACCEPT) != 0 ? "AF " : "")
                                      + (key.isValid() ? "V " : "IV ")
                                      + (key.isReadable() ? "RD " : "IRD ")
                                      + (key.isWritable() ? "WR " : "IWR ")
                                      + (key.isAcceptable() ? "AC " : "IAC ")
                                      + (key.isConnectable() ? "CN " : "ICN ")
                            );
                        }

                        /**
                         * Clean stale connections that do not have and data: select
                         * returns indicating that the count of active connections with
                         * input is 0.  However the list still has these "stale"
                         * connections lingering around.  We remove them since they
                         * are prematurely triggering selection to return w/o input.
                         *
                         * http://nagoya.apache.org/jira/secure/ViewIssue.jspa?key=DIR-18
                         */
                        list = selector.selectedKeys().iterator();

                        while (list.hasNext()) {
                            SelectionKey key = (SelectionKey) list.next();
                            log.trace("REMOVING " + key);
                            key.channel().close();
                            key.cancel();
                            list.remove();
                        }

                        continue;
                    }

                    Iterator list = selector.keys().iterator();
                    while (list.hasNext()) {
                        SelectionKey key = (SelectionKey) list.next();
                        log.trace("Still watching " + key + " key is "
                                  + ((key.interestOps() & SelectionKey.OP_READ) != 0 ? "RF " : "")
                                  + ((key.interestOps() & SelectionKey.OP_WRITE) != 0 ? "WF " : "")
                                  + ((key.interestOps() & SelectionKey.OP_ACCEPT) != 0 ? "AF " : "")
                                  + (key.isValid() ? "V " : "IV ")
                                  + (key.isReadable() ? "RD " : "IRD ")
                                  + (key.isWritable() ? "WR " : "IWR ")
                                  + (key.isAcceptable() ? "AC " : "IAC ")
                                  + (key.isConnectable() ? "CN " : "ICN ")
                        );
                    }

                    // Get a java.util.Set containing the SelectionKey objects for
                    // all channels that are ready for I/O.
                    Set keys = selector.selectedKeys();

                    // Use a java.util.Iterator to loop through the selected keys
                    for (Iterator i = keys.iterator(); i.hasNext();) {
                        SelectionKey key = (SelectionKey) i.next();

                        if (key.isReadable()) {
                            log.trace("-OP_READ " + key);
                            key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
                            threadPool.execute(new Event(key, SelectionKey.OP_READ));
                        }
                        if (key.isWritable()) {
                            log.trace("-OP_WRITE " + key);
                            key.interestOps(key.interestOps() & (~SelectionKey.OP_WRITE));
                            threadPool.execute(new Event(key, SelectionKey.OP_WRITE));
                        }
                        if (key.isAcceptable()) {
                            log.trace("-OP_ACCEPT " + key);
                            key.interestOps(key.interestOps() & (~SelectionKey.OP_ACCEPT));
                            threadPool.execute(new Event(key, SelectionKey.OP_ACCEPT));
                        }

                        i.remove(); // Remove the key from the set of selected keys
                    }

                } catch (CancelledKeyException e) {
                    log.debug("Key has Been Cancelled: " + e);
                }
            }
        } catch (IOException e) {
            log.warn("IOException occured.", e);
        } catch (InterruptedException e) {
            log.debug("Selector Work thread has been interrupted.");
        } finally {
            log.debug("Selector Work thread has stopped.");
        }
    }

    public SelectionKey register(SelectableChannel selectableChannel, int ops, SelectionEventListner listener) throws ClosedChannelException {
        synchronized (closing) {
            selector.wakeup();
            SelectionKey key = selectableChannel.register(selector, ops, listener);
            return key;
        }
    }

    public void closeChannel(SelectableChannel selectableChannel) throws IOException {
        synchronized (closing) {
            selector.wakeup();
            closing.push(selectableChannel);
        }
    }

    public void addInterestOps(SelectionKey selectorKey, int addOpts) {
        synchronized (closing) {
            selector.wakeup();
            selectorKey.interestOps(selectorKey.interestOps() | addOpts);
        }
    }

    public void doStart() throws WaitingException, Exception {
        startCounter++;
        if (startCounter == 1) {
            log.debug("Starting a Selector Work thread.");
            running = true;
            new Thread(threadGroup, this, threadName).start();
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
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(SelectorManager.class);

        infoFactory.addAttribute("timeout", long.class, true);
        infoFactory.addReference("ThreadPool", ThreadPool.class);
        infoFactory.addAttribute("threadPool", ThreadPool.class, false);
        infoFactory.addAttribute("threadName", String.class, true);

        infoFactory.addOperation("getSelector");
        infoFactory.addOperation("closeChannel", new Class[] {SelectableChannel.class});
        infoFactory.addOperation("addInterestOps", new Class[] {SelectionKey.class, int.class});
        infoFactory.addOperation("register", new Class[] {SelectableChannel.class, int.class, SelectionEventListner.class});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    public class Event implements Runnable {

        final int flags;
        final SelectionKey key;

        private Event(SelectionKey key, int flags) {
            this.flags = flags;
            this.key = key;
        }

        public SelectionKey getSelectionKey() {
            return key;
        }

        public final boolean isReadable() {
            return (flags & SelectionKey.OP_READ) != 0;
        }

        public final boolean isWritable() {
            return (flags & SelectionKey.OP_WRITE) != 0;
        }

        public final boolean isAcceptable() {
            return (flags & SelectionKey.OP_ACCEPT) != 0;
        }

        public void run() {
            try {
                ((SelectionEventListner) key.attachment()).selectionEvent(this);
            } catch (Throwable e) {
                log.trace("Request Failed.", e);
            }
        }
    }
}
