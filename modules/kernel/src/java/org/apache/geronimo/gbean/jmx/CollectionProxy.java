/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.gbean.jmx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionEvent;
import org.apache.geronimo.gbean.ReferenceCollectionListener;
import org.apache.geronimo.gbean.WaitingException;

/**
 * @version $Rev$ $Date$
 */
public class CollectionProxy implements Proxy {
    private static final Log log = LogFactory.getLog(CollectionProxy.class);

    /**
     * The GBeanMBean to which this proxy belongs.
     */
    private GBeanMBean gmbean;

    /**
     * Name of the reference
     */
    public String name;

    /**
     * A map from object names to the proxy
     */
    private Map proxies = new HashMap();

    /**
     * A map from object names to the proxy interceptor
     */
    private Map interceptors = new HashMap();

    /**
     * Proxy collection implementation held by the component
     */
    private ClientCollection proxy = new ClientCollection();

    /**
     * Facotry for proxy instances.
     */
    private ProxyFactory factory;

    /**
     * Is this proxy currently stopped?
     */
    private boolean stopped;

    public CollectionProxy(GBeanMBean gmbean, String name, Class type) {
        this.gmbean = gmbean;
        this.name = name;
        factory = ProxyFactory.newProxyFactory(type);
    }

    public synchronized void destroy() {
        for (Iterator iterator = interceptors.values().iterator(); iterator.hasNext();) {
            ProxyMethodInterceptor interceptor = (ProxyMethodInterceptor) iterator.next();
            interceptor.disconnect();
        }
        proxy.listeners = null;
        gmbean = null;
        name = null;
        proxies = null;
        interceptors = null;
        proxy = null;
        factory = null;
        stopped = true;
    }

    public synchronized Object getProxy() {
        return proxy;
    }

    public synchronized Set getTargets() {
        return Collections.unmodifiableSet(proxies.keySet());
    }

    public synchronized void addTarget(ObjectName target) {
        // if this is a new target...
        if (!proxies.containsKey(target)) {
            ProxyMethodInterceptor interceptor = factory.getMethodInterceptor();
            interceptor.connect(gmbean.getServer(), target, proxy.isStopped());
            interceptors.put(target, interceptor);
            Object targetProxy = factory.create(interceptor);
            proxies.put(target, targetProxy);
            if (!stopped) {
                proxy.fireMemberAdddedEvent(targetProxy);
            }
        }
    }

    public synchronized void removeTarget(ObjectName target) {
        Object targetProxy = proxies.remove(target);
        if (targetProxy != null) {
            if (!stopped) {
                proxy.fireMemberRemovedEvent(targetProxy);
            }
            ProxyMethodInterceptor interceptor = (ProxyMethodInterceptor) interceptors.remove(target);
            if (interceptor != null) {
                interceptor.disconnect();
            }
        }
    }

    public synchronized void start() throws WaitingException {
        stopped = false;
        for (Iterator iterator = interceptors.values().iterator(); iterator.hasNext();) {
            ProxyMethodInterceptor interceptor = (ProxyMethodInterceptor) iterator.next();
            interceptor.start();
        }
    }

    public synchronized void stop() {
        stopped = true;
        for (Iterator iterator = interceptors.values().iterator(); iterator.hasNext();) {
            ProxyMethodInterceptor interceptor = (ProxyMethodInterceptor) iterator.next();
            interceptor.stop();
        }
    }

    private class ClientCollection implements ReferenceCollection {
        private Set listeners = new HashSet();

        public boolean isStopped() {
            synchronized (CollectionProxy.this) {
                return stopped;
            }
        }

        public void addReferenceCollectionListener(ReferenceCollectionListener listener) {
            synchronized (CollectionProxy.this) {
                listeners.add(listener);
            }
        }

        public void removeReferenceCollectionListener(ReferenceCollectionListener listener) {
            synchronized (CollectionProxy.this) {
                listeners.remove(listener);
            }
        }

        private void fireMemberAdddedEvent(Object member) {
            ArrayList listenerCopy;
            synchronized (CollectionProxy.this) {
                listenerCopy = new ArrayList(listeners);
            }
            for (Iterator iterator = listenerCopy.iterator(); iterator.hasNext();) {
                ReferenceCollectionListener listener = (ReferenceCollectionListener) iterator.next();
                try {
                    listener.memberAdded(new ReferenceCollectionEvent(name, member));
                } catch (Throwable t) {
                    log.error("Listener threw exception", t);
                }
            }
        }

        private void fireMemberRemovedEvent(Object member) {
            ArrayList listenerCopy;
            synchronized (CollectionProxy.this) {
                listenerCopy = new ArrayList(listeners);
            }
            for (Iterator iterator = listenerCopy.iterator(); iterator.hasNext();) {
                ReferenceCollectionListener listener = (ReferenceCollectionListener) iterator.next();
                try {
                    listener.memberRemoved(new ReferenceCollectionEvent(name, member));
                } catch (Throwable t) {
                    log.error("Listener threw exception", t);
                }
            }
        }

        public int size() {
            synchronized (CollectionProxy.this) {
                if (stopped) {
                    return 0;
                }
                return proxies.size();
            }
        }

        public boolean isEmpty() {
            synchronized (CollectionProxy.this) {
                if (stopped) {
                    return true;
                }
                return proxies.isEmpty();
            }
        }

        public boolean contains(Object o) {
            synchronized (CollectionProxy.this) {
                if (stopped) {
                    return false;
                }
                return proxies.containsValue(o);
            }
        }

        public Iterator iterator() {
            synchronized (CollectionProxy.this) {
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
        }

        public Object[] toArray() {
            synchronized (CollectionProxy.this) {
                if (stopped) {
                    return new Object[0];
                }
                return proxies.values().toArray();
            }
        }

        public Object[] toArray(Object a[]) {
            synchronized (CollectionProxy.this) {
                if (stopped) {
                    if (a.length > 0) {
                        a[0] = null;
                    }
                    return a;
                }
                return proxies.values().toArray(a);
            }
        }

        public boolean containsAll(Collection c) {
            synchronized (CollectionProxy.this) {
                if (stopped) {
                    return c.isEmpty();
                }
                return proxies.values().containsAll(c);
            }
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
    }
}
