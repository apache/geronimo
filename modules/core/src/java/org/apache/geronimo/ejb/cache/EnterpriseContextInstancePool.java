/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
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
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.ejb.cache;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.cache.InstancePool;
import org.apache.geronimo.cache.SimpleInstancePool;
import org.apache.geronimo.common.AbstractComponent;
import org.apache.geronimo.ejb.EnterpriseContext;
import org.apache.geronimo.ejb.container.EJBPlugins;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2003/08/11 17:59:11 $
 */
public final class EnterpriseContextInstancePool extends AbstractComponent implements InstancePool {
    private SimpleInstancePool pool;
    private DiscardQueue discardQueue;
    private int maxSize = 100;
    private boolean hardLimit = false;

    public void create() throws Exception {
        super.create();
        pool = new SimpleInstancePool(EJBPlugins.getInstanceFactory(getContainer()), maxSize, hardLimit);
        discardQueue = new DiscardQueue();
    }

    public void start() throws Exception {
        super.start();
        pool.fill();
    }

    public void destroy() {
        List contexts = pool.stopPooling();
        pool = null;
        for (Iterator iter = contexts.iterator(); iter.hasNext();) {
            try {
                EnterpriseContext ctx = (EnterpriseContext) iter.next();
                ctx.discard();
            } catch (Throwable e) {
                log.error("Error while disposing of context", e);
            }
        }

        discardQueue.stop();
        discardQueue = null;

        super.destroy();
    }

    /**
     * Get an instance from the pool.  This method may block indefinately if the pool has a
     * strict limit.
     *
     * @return an instance
     * @throws java.lang.InterruptedException if pool is using hard limits and thread was interrupted
     * while waiting for an instance to become available
     */
    public Object acquire() throws Exception {
        return pool.acquire();
    }

    /**
     * Releases the hold on the instance.  This method may or may not reinsert the instance
     * into the pool. This method can not block.
     *
     * @param instance the instance to return to the pool
     * @return true is the instance was reinserted into the pool.
     */
    public boolean release(Object instance) {
        if (!(instance instanceof EnterpriseContext)) {
            throw new IllegalArgumentException("Instance is not an instance of EnterpriseContext");
        }
        EnterpriseContext ctx = (EnterpriseContext) instance;

        // first recycle the context (outside the sync block)
        ctx.clear();

        if (!pool.release(ctx)) {
            // instance was not reinserted into the pool so we need to discard it
            discardQueue.put(ctx);
            return false;
        }
        return true;
    }

    /**
     * Drop an instance permanently from the pool.  The instance will never be used again.
     * This method can not block.
     *
     * @param instance the instance to discard
     */
    public void remove(Object instance) {
        if (!(instance instanceof EnterpriseContext)) {
            throw new IllegalArgumentException("Instance is not an instance of EnterpriseContext");
        }
        pool.remove(instance);
    }

    public int getSize() {
        return pool.getSize();
    }

    public int getAllocatedSize() {
        return pool.getAllocatedSize();
    }

    public int getMaxSize() {
        return pool.getMaxSize();
    }

    public void setSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public boolean isHardLimit() {
        return pool.isHardLimit();
    }

    public void setHardLimit(boolean hardLimit) {
        this.hardLimit = hardLimit;
    }

    private static final class DiscardQueue implements Runnable {
        private final Log log = LogFactory.getLog(this.getClass());
        private final LinkedList discardQueue;
        private final Thread discardThread;
        private boolean running = true;

        public DiscardQueue() {
            discardQueue = new LinkedList();
            discardThread = new Thread(this, "EnterpriseContextInstancePool.DiscardQueue");
            discardThread.setDaemon(true);
            discardThread.start();
        }

        public void stop() {
            synchronized (discardQueue) {
                running = false;
                discardQueue.notify();
            }
        }

        public void put(EnterpriseContext ctx) {
            synchronized (discardQueue) {
                discardQueue.addLast(ctx);
                discardQueue.notify();
            }
        }

        public void run() {
            EnterpriseContext context = null;
            while (running) {
                synchronized (discardQueue) {
                    if (!discardQueue.isEmpty()) {
                        context = (EnterpriseContext) discardQueue.removeFirst();
                    } else {
                        try {
                            discardQueue.wait();
                        } catch (InterruptedException ignored) {
                            // ignore this exception... there is a better way be ended
                        }
                    }
                }
                if (context != null) {
                    try {
                        context.discard();
                    } catch (Throwable e) {
                        log.error("Error while disposing of context", e);
                    }
                    context = null;
                }
            }
        }
    }
}
