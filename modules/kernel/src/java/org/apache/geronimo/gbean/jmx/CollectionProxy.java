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
package org.apache.geronimo.gbean.jmx;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.management.ObjectName;

import org.apache.geronimo.kernel.jmx.InterfaceCallbackFilter;
import org.apache.geronimo.gbean.WaitingException;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.SimpleCallbacks;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/12 01:38:55 $
 */
public class CollectionProxy implements Proxy {
    /**
     * The GMBean to which this proxy belongs.
     */
    private final GMBean gmbean;

    /**
     * The proxy type
     */
    private final Class type;

    /**
     * A map from object names to the proxy
     */
    private final Map proxies = new HashMap();

    /**
     * A map from object names to the proxy interceptor
     */
    private final Map interceptors = new HashMap();

    /**
     * Proxy collection implementation held by the component
     */
    private final ClientCollection proxy = new ClientCollection(proxies.values());

    /**
     * Facotry for proxy instances.
     */
    private final Factory factory;

    public CollectionProxy(GMBean gmbean, Class type) {
        this.gmbean = gmbean;
        this.type = type;
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(Object.class);
        enhancer.setInterfaces(new Class[]{type});
        enhancer.setCallbackFilter(new InterfaceCallbackFilter(type));
        enhancer.setCallbacks(new SimpleCallbacks());
        enhancer.setClassLoader(type.getClassLoader());
        factory = enhancer.create();
    }

    public Object getProxy() {
        return proxy;
    }

    public synchronized Set getTargets() {
        return Collections.unmodifiableSet(proxies.keySet());
    }

    public synchronized void addTarget(ObjectName target) {
        // if this is a new target...
        if (!proxies.containsKey(target)) {
            ProxyMethodInterceptor interceptor = new ProxyMethodInterceptor(type);
            interceptor.connect(gmbean.getServer(), target, proxy.isStopped());
            interceptors.put(target, interceptor);
            proxies.put(target, factory.newInstance(interceptor));
        }
    }

    public synchronized void removeTarget(ObjectName target) {
        // if this is one of our existing targets target...
        if (!proxies.containsKey(target)) {
            proxies.remove(target);
            ProxyMethodInterceptor interceptor = (ProxyMethodInterceptor) interceptors.remove(target);
            if (interceptor != null) {
                interceptor.disconnect();
            }
        }
    }

    public synchronized void start() throws WaitingException {
        proxy.start();
        for (Iterator iterator = interceptors.values().iterator(); iterator.hasNext();) {
            ProxyMethodInterceptor interceptor = (ProxyMethodInterceptor) iterator.next();
            interceptor.start();
        }
    }

    public synchronized void stop() {
        proxy.stop();
        for (Iterator iterator = interceptors.values().iterator(); iterator.hasNext();) {
            ProxyMethodInterceptor interceptor = (ProxyMethodInterceptor) iterator.next();
            interceptor.stop();
        }
    }

    private static class ClientCollection implements Collection {
        private Collection proxies;
        private boolean stopped;

        public ClientCollection(Collection proxies) {
            this.proxies = proxies;
            stopped = true;
        }

        private void start() {
            stopped = false;
        }

        private void stop() {
            stopped = true;
        }

        public boolean isStopped() {
            return stopped;
        }

        public int size() {
            if (stopped) {
                return 0;
            }
            return proxies.size();
        }

        public boolean isEmpty() {
            if (stopped) {
                return true;
            }
            return proxies.isEmpty();
        }

        public boolean contains(Object o) {
            if (stopped) {
                return false;
            }
            return proxies.contains(o);
        }

        public Iterator iterator() {
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
                private final Iterator iterator = proxies.iterator();

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

        public Object[] toArray() {
            if (stopped) {
                return new Object[0];
            }
            return proxies.toArray();
        }

        public Object[] toArray(Object a[]) {
            if (stopped) {
                if (a.length > 0) {
                    a[0] = null;
                }
                return a;
            }
            return proxies.toArray(a);
        }

        public boolean containsAll(Collection c) {
            if (stopped) {
                return c.isEmpty();
            }
            return proxies.containsAll(c);
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
