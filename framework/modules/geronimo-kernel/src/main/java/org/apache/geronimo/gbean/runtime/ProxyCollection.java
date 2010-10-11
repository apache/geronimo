/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.gbean.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionEvent;
import org.apache.geronimo.gbean.ReferenceCollectionListener;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.GBeanNotFoundException;

import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
class ProxyCollection implements ReferenceCollection {
    private static final Logger log = LoggerFactory.getLogger(ProxyCollection.class);
    private final String name;
    private final Kernel kernel;
    private final Map proxies = new HashMap();
    private final Set listeners = new HashSet();
    private boolean stopped = false;
    private final Class type;

    public ProxyCollection(String name, Class type, Set targets, Kernel kernel) {
        this.name = name;
        this.kernel = kernel;
        this.type = type;

        for (Iterator iterator = targets.iterator(); iterator.hasNext();) {
            addTarget((AbstractName) iterator.next());
        }
    }

    synchronized void destroy() {
        stopped = true;
        if (!AbstractGBeanReference.NO_PROXY) {
            for (Iterator iterator = proxies.values().iterator(); iterator.hasNext();) {
                kernel.getProxyManager().destroyProxy(iterator.next());
            }
        }
        proxies.clear();
        listeners.clear();
    }

    void addTarget(AbstractName target) {
        Object proxy;
        ArrayList listenerCopy;
        synchronized (this) {
            // if this is not a new target return
            if (proxies.containsKey(target)) {
                return;
            }

            // create and add the proxy
            if (AbstractGBeanReference.NO_PROXY) {
                try {
                    proxy = kernel.getGBean(target);
                } catch (GBeanNotFoundException e) {
                    // gbean disappeard on us
                    log.debug("GBean was unloaded before it could be added to reference collections: " + target);
                    return;
                }
            } else {
                proxy = kernel.getProxyManager().createProxy(target, type);
            }
            proxies.put(target, proxy);

            // make a snapshot of the listeners
            listenerCopy = new ArrayList(listeners);
        }

        // fire the member added event
        for (Iterator iterator = listenerCopy.iterator(); iterator.hasNext();) {
            ReferenceCollectionListener listener = (ReferenceCollectionListener) iterator.next();
            try {
                listener.memberAdded(new ReferenceCollectionEvent(name, proxy));
            } catch (Throwable t) {
                log.error("Listener threw exception", t);
            }
        }
    }

    void removeTarget(AbstractName target) {
        Object proxy;
        ArrayList listenerCopy;
        synchronized (this) {
            // remove the proxy
            proxy = proxies.remove(target);

            // if this was not a target return
            if (proxy == null) {
                return;
            }

            // make a snapshot of the listeners
            listenerCopy = new ArrayList(listeners);
        }

        // fire the member removed event
        for (Iterator iterator = listenerCopy.iterator(); iterator.hasNext();) {
            ReferenceCollectionListener listener = (ReferenceCollectionListener) iterator.next();
            try {
                listener.memberRemoved(new ReferenceCollectionEvent(name, proxy));
            } catch (Throwable t) {
                log.error("Listener threw exception", t);
            }
        }

        // destroy the proxy
        if (!AbstractGBeanReference.NO_PROXY) {
            kernel.getProxyManager().destroyProxy(proxy);
        }
    }

    public synchronized ObjectName[] getMemberObjectNames() {
        return (ObjectName[])proxies.keySet().toArray(new ObjectName[0]);
    }

    public synchronized boolean isStopped() {
        return stopped;
    }

    public synchronized void addReferenceCollectionListener(ReferenceCollectionListener listener) {
        listeners.add(listener);
    }

    public synchronized void removeReferenceCollectionListener(ReferenceCollectionListener listener) {
        listeners.remove(listener);
    }

    public synchronized int size() {
        if (stopped) {
            return 0;
        }
        return proxies.size();
    }

    public synchronized boolean isEmpty() {
        if (stopped) {
            return true;
        }
        return proxies.isEmpty();
    }

    public synchronized boolean contains(Object o) {
        if (stopped) {
            return false;
        }
        return proxies.containsValue(o);
    }

    public synchronized Iterator iterator() {
        if (stopped) {
            return new Iterator() {
                public boolean hasNext() {
                    return false;
                }

                public Object next() {
                    throw new NoSuchElementException();
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        return new Iterator() {
            // copy the proxies, so the client can iterate without concurrent modification
            // this is necssary since the client has nothing to synchronize on
            private final Iterator iterator = new ArrayList(proxies.values()).iterator();

            public boolean hasNext() {
                return iterator.hasNext();
            }

            public Object next() {
                return iterator.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public synchronized Object[] toArray() {
        if (stopped) {
            return new Object[0];
        }
        return proxies.values().toArray();
    }

    public synchronized Object[] toArray(Object a[]) {
        if (stopped) {
            if (a.length > 0) {
                a[0] = null;
            }
            return a;
        }
        return proxies.values().toArray(a);
    }

    public synchronized boolean containsAll(Collection c) {
        if (stopped) {
            return c.isEmpty();
        }
        return proxies.values().containsAll(c);
    }

    public boolean add(Object o) {
        throw new UnsupportedOperationException();
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }
    
    public String toString(){
        return proxies.keySet().toString();
        
    }
}
