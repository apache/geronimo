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

import java.rmi.NoSuchObjectException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.cache.InstanceCache;
import org.apache.geronimo.cache.LRUInstanceCache;
import org.apache.geronimo.cache.LRURunner;
import org.apache.geronimo.core.service.AbstractComponent;
import org.apache.geronimo.core.service.Container;
import org.apache.geronimo.core.service.RPCContainer;
import org.apache.geronimo.ejb.EnterpriseContext;
import org.apache.geronimo.ejb.container.EJBPlugins;

/**
 *
 * @version $Revision: 1.7 $ $Date: 2003/09/08 04:28:26 $
 */
public final class EnterpriseContextInstanceCache extends AbstractComponent implements InstanceCache {
    private LRUInstanceCache cache;
    private int highSize = 10;
    private int lowSize = 5;
    private Passivator passivator = new Passivator();
    private Log log = LogFactory.getLog(getClass());

    protected void doStart() throws Exception {
        super.doStart();
        cache = new LRUInstanceCache();
        passivator = new Passivator();
        passivator.start();
    }

    protected void doStop() throws Exception {
        passivator.stopRunning();
        passivator = null;

        // clean up cache
        cache = null;

        super.doStop();
    }

    public Object get(Object id) throws Exception {
        EnterpriseContext ctx = null;
        synchronized (cache) {
            ctx = (EnterpriseContext) cache.get(id);
            if (ctx == null) {
                //addBeanToTx();

                // verify that there is enough room in the cache
                passivator.checkSize();

                // create a new context
                Container container = getContainer();
                try {
                    ctx = (EnterpriseContext) EJBPlugins.getInstanceFactory((RPCContainer)container).createInstance();
                } catch (Exception e) {
                    throw new NoSuchObjectException("An error occured while getting a new context");
                }

                // set the id of the new context
                ctx.setId(id);

                EJBPlugins.getPersistenceManager((RPCContainer)container).activate(ctx);
                cache.putActive(id, ctx);
            }
        }
        return ctx;
    }

    public Object peek(Object id) {
        synchronized (cache) {
            return cache.peek(id);
        }
    }

    public void putActive(Object key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("Cannot insert an EnterpriseContext with a null id");
        }
        if (!(value instanceof EnterpriseContext)) {
            throw new IllegalArgumentException("Value is not an instance of EnterpriseContext");
        }
        assert key.equals(((EnterpriseContext) value).getId());
        synchronized (cache) {
            //addBeanToTx();

            // verify that there is enough room in the cache
            passivator.checkSize();

            cache.putActive(key, value);
        }
    }

    public void putInactive(Object key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("Cannot insert an EnterpriseContext with a null id");
        }
        if (!(value instanceof EnterpriseContext)) {
            throw new IllegalArgumentException("Value is not an instance of EnterpriseContext");
        }
        assert key.equals(((EnterpriseContext) value).getId());
        synchronized (cache) {
            //removeBeanFromTx();
            cache.putInactive(key, value);
        }
    }

    public Object remove(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("Cannot remove an EnterpriseContext with a null id");
        }

        synchronized (cache) {
            //removeBeanFromTx();
            return cache.remove(key);
        }
    }

    public boolean isActive(Object key) {
        synchronized (cache) {
            return cache.isActive(key);
        }
    }

    public long getSize() {
        synchronized (cache) {
            return cache.size();
        }
    }

    /*
    private TransactionLocal beanCount = new TransactionLocal();

    // @todo this is a dirty hack
    private void addBeanToTx() {
        Transaction tx = null;
        try {
            tx = EJBPlugins.getTransactionManager(getContainer()).getTransaction();
        } catch (SystemException ignore) {
        }
        if (tx == null) {
            return;
        }

        Integer integerCount = (Integer) beanCount.get();
        int count = 0;
        if (integerCount != null) {
            count = integerCount.intValue();
        }
        count++;
        beanCount.set(new Integer(count));
        if (count > maxBeansInTx) {
            log.info("******* Store Entities In Tx *******");
            EntityContainer.synchronizeEntitiesWithinTransaction(tx);
        }
    }

    // @todo this is a dirty hack
    private void removeBeanFromTx() {
        Transaction tx = null;
        try {
            tx = EJBPlugins.getTransactionManager(getContainer()).getTransaction();
        } catch (SystemException ignore) {
        }
        if (tx == null) {
            return;
        }

        Integer integerCount = (Integer) beanCount.get();
        int count = 0;
        if (integerCount != null) {
            count = integerCount.intValue();
            if (count > 0) {
                count--;
            }
        }
        beanCount.set(new Integer(count));
    }
    */
    private final class Passivator extends Thread implements LRURunner {
        private boolean running = true;

        public Passivator() {
            setDaemon(true);
        }

        public synchronized void stopRunning() {
            running = false;
            notify();
        }

        public synchronized void checkSize() {
            if (running && cache.size() > highSize) {
                notify();
            }
        }

        // todo This synchronization is all broken... will eventually deadlock
        public void run() {
            while (true) {
                synchronized (this) {
                    if (!running) {
                        return;
                    }
                    try {
                        wait();
                    } catch (InterruptedException ignored) {
                        // we have another way to signify exit so we can safely ignore this exception
                    }
                }
                if (cache.size() > highSize) {
                    cache.run(this);
                }
            }
        }

        public synchronized boolean shouldContinue() {
            return running && cache.size() > lowSize;
        }

        public boolean shouldRemove(Object key, Object value) {
            return true;
        }

        public void remove(Object key, Object value) {
            try {
                EnterpriseContext context = (EnterpriseContext) value;
                EJBPlugins.getPersistenceManager((RPCContainer)getContainer()).passivate(context);
            } catch (Throwable e) {
                log.error("Could not passivate ejb: id=" + key, e);
            }
        }
    }

}
