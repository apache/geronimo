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
 * @version $Revision: 1.1 $ $Date: 2003/08/22 02:23:27 $
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
