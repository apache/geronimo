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
import org.apache.geronimo.kernel.Kernel;

/**
 * @version $Rev: 46019 $ $Date: 2004-09-14 02:56:06 -0700 (Tue, 14 Sep 2004) $
 */
public class GBeanCollectionProxy {
    private static final Log log = LogFactory.getLog(GBeanCollectionProxy.class);

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
    private Kernel kernel;

    public GBeanCollectionProxy(Kernel kernel, String name, Class type, Set targets) {
        this.kernel = kernel;
        this.name = name;
        factory = ProxyFactory.newProxyFactory(type);

        for (Iterator iterator = targets.iterator(); iterator.hasNext();) {
            addTarget((ObjectName) iterator.next());
        }
    }

    public synchronized void destroy() {
        for (Iterator iterator = interceptors.values().iterator(); iterator.hasNext();) {
            ProxyMethodInterceptor interceptor = (ProxyMethodInterceptor) iterator.next();
            interceptor.disconnect();
        }
        proxy.listeners = null;
        kernel = null;
        name = null;
        proxies = null;
        interceptors = null;
        proxy = null;
        factory = null;
    }

    public synchronized Object getProxy() {
        return proxy;
    }

    public synchronized void addTarget(ObjectName target) {
        // if this is a new target...
        if (!proxies.containsKey(target)) {
            // create the interceptor
            ProxyMethodInterceptor interceptor = factory.getMethodInterceptor();
            interceptor.connect(kernel.getMBeanServer(), target);
            interceptors.put(target, interceptor);

            // create the proxy
            Object targetProxy = factory.create(interceptor);
            proxies.put(target, targetProxy);
            proxy.fireMemberAdddedEvent(targetProxy);
        }
    }

    public synchronized void removeTarget(ObjectName target) {
        Object targetProxy = proxies.remove(target);
        if (targetProxy != null) {
            proxy.fireMemberRemovedEvent(targetProxy);
            ProxyMethodInterceptor interceptor = (ProxyMethodInterceptor) interceptors.remove(target);
            if (interceptor != null) {
                interceptor.disconnect();
            }
        }
    }

    private class ClientCollection implements ReferenceCollection {
        private Set listeners = new HashSet();

        public boolean isStopped() {
            synchronized (GBeanCollectionProxy.this) {
                return proxy == null;
            }
        }

        public void addReferenceCollectionListener(ReferenceCollectionListener listener) {
            synchronized (GBeanCollectionProxy.this) {
                listeners.add(listener);
            }
        }

        public void removeReferenceCollectionListener(ReferenceCollectionListener listener) {
            synchronized (GBeanCollectionProxy.this) {
                listeners.remove(listener);
            }
        }

        private void fireMemberAdddedEvent(Object member) {
            ArrayList listenerCopy;
            synchronized (GBeanCollectionProxy.this) {
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
            synchronized (GBeanCollectionProxy.this) {
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
            synchronized (GBeanCollectionProxy.this) {
                if (proxy == null) {
                    return 0;
                }
                return proxies.size();
            }
        }

        public boolean isEmpty() {
            synchronized (GBeanCollectionProxy.this) {
                if (proxy == null) {
                    return true;
                }
                return proxies.isEmpty();
            }
        }

        public boolean contains(Object o) {
            synchronized (GBeanCollectionProxy.this) {
                if (proxy == null) {
                    return false;
                }
                return proxies.containsValue(o);
            }
        }

        public Iterator iterator() {
            synchronized (GBeanCollectionProxy.this) {
                if (proxy == null) {
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
            synchronized (GBeanCollectionProxy.this) {
                if (proxy == null) {
                    return new Object[0];
                }
                return proxies.values().toArray();
            }
        }

        public Object[] toArray(Object a[]) {
            synchronized (GBeanCollectionProxy.this) {
                if (proxy == null) {
                    if (a.length > 0) {
                        a[0] = null;
                    }
                    return a;
                }
                return proxies.values().toArray(a);
            }
        }

        public boolean containsAll(Collection c) {
            synchronized (GBeanCollectionProxy.this) {
                if (proxy == null) {
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
