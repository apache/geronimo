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

package org.apache.geronimo.naming.proxy;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;

import javax.naming.NamingException;

import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionEvent;
import org.apache.geronimo.gbean.ReferenceCollectionListener;
import org.apache.geronimo.gbean.WaitingException;

/**
 * This manages connecting and disconnecting the external proxies supplied by ProxyFactory
 * and bound into the ReadOnlyContext to the "internal" proxies supplied by EJBContainer and
 * CFContainer implementations.  This class works based on the notifications provided by GBean
 * Refereneces.  The external proxy has to supply and id for the internal proxy it is interested in,
 * and the CFContainer and EJBContainer have to supply such an id.  When a referenced container supplies
 * a previously requested id, they are matched up by a call to the external proxy.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:57 $
 *
 * */
public class ProxyManager implements GBean {

    private final Map idToEJBRemoteClientsMap = new HashMap();
    private final Map idToEJBLocalClientsMap = new HashMap();
    private final Map idToCFClientsMap = new HashMap();

    private final Map idToEJBContainerMap = new HashMap();
    private final Map idToCFContainerMap = new HashMap();

    private ReferenceCollection ejbReferences;
    private ReferenceCollection cfReferences;

    private final ReferenceCollectionListener ejbContainerListener = new ReferenceCollectionListener() {
        public void memberAdded(ReferenceCollectionEvent event) {
            EJBContainer container = (EJBContainer) event.getMember();
            ejbRegistered(container);
        }

        public void memberRemoved(ReferenceCollectionEvent event) {
            EJBContainer container = (EJBContainer) event.getMember();
            ejbUnregistered(container);
        }

    };

    private final ReferenceCollectionListener cfContainerListener = new ReferenceCollectionListener() {
        public void memberAdded(ReferenceCollectionEvent event) {
            CFContainer container = (CFContainer) event.getMember();
            cfRegistered(container);
        }

        public void memberRemoved(ReferenceCollectionEvent event) {
            CFContainer container = (CFContainer) event.getMember();
            cfUnregistered(container);
        }

    };

    public ProxyManager(ReferenceCollection ejbReferences, ReferenceCollection cfReferences) {
        this.ejbReferences = ejbReferences;
        this.cfReferences = cfReferences;
    }

    public URI encode(String remoteKernelId, String objectName, String operation) throws NamingException {
        try {
            return new URI("proxy", remoteKernelId, '/' + objectName, operation, null);
        } catch (URISyntaxException e) {
            throw (NamingException) new NamingException("Invalid syntax in generated URI").initCause(e);
        }
    }

    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() throws WaitingException, Exception {
        ejbReferences.addReferenceCollectionListener(ejbContainerListener);
        Collection copy;
        synchronized (ejbReferences) {
            copy = new ArrayList(ejbReferences);
        }
        for (Iterator iterator = copy.iterator(); iterator.hasNext();) {
            EJBContainer ejbContainer = (EJBContainer) iterator.next();
            ejbRegistered(ejbContainer);
        }
        cfReferences.addReferenceCollectionListener(cfContainerListener);
        synchronized (cfReferences) {
            copy = new ArrayList(cfReferences);
        }
        for (Iterator iterator = copy.iterator(); iterator.hasNext();) {
            CFContainer cfContainer = (CFContainer) iterator.next();
            cfRegistered(cfContainer);
        }
    }

    public void doStop() throws WaitingException, Exception {
        Collection copy;
        synchronized (ejbReferences) {
            copy = new ArrayList(ejbReferences);
        }
        for (Iterator iterator = copy.iterator(); iterator.hasNext();) {
            EJBContainer ejbContainer = (EJBContainer) iterator.next();
            ejbUnregistered(ejbContainer);
        }
        ejbReferences.removeReferenceCollectionListener(ejbContainerListener);
        synchronized (cfReferences) {
            copy = new ArrayList(cfReferences);
        }
        for (Iterator iterator = copy.iterator(); iterator.hasNext();) {
            CFContainer cfContainer = (CFContainer) iterator.next();
            cfUnregistered(cfContainer);
        }
        cfReferences.removeReferenceCollectionListener(cfContainerListener);
    }

    public void doFail() {
    }

    public synchronized void registerEJBLocalProxy(Object id, ExternalProxy proxy) {
        Collection proxies = (Collection)idToEJBLocalClientsMap.get(id);
        if (proxies == null) {
            proxies = new HashSet();
            idToEJBLocalClientsMap.put(id, proxies);
        }
        proxies.add(proxy);
    }

    public synchronized void unregisterEJBLocalProxy(Object id, ExternalProxy proxy) {
        Collection proxies = (Collection)idToEJBLocalClientsMap.get(id);
        assert proxies != null;
        proxies.remove(proxy);
    }

    public synchronized void registerEJBRemoteProxy(Object id, ExternalProxy proxy) {
        Collection proxies = (Collection)idToEJBRemoteClientsMap.get(id);
        if (proxies == null) {
            proxies = new HashSet();
            idToEJBRemoteClientsMap.put(id, proxies);
        }
        proxies.add(proxy);
    }

    public synchronized void unregisterEJBRemoteProxy(Object id, ExternalProxy proxy) {
        Collection proxies = (Collection)idToEJBRemoteClientsMap.get(id);
        assert proxies != null;
        proxies.remove(proxy);
    }

    public synchronized void registerCFProxy(Object id, ExternalProxy proxy) {
        Collection proxies = (Collection)idToCFClientsMap.get(id);
        if (proxies == null) {
            proxies = new HashSet();
            idToCFClientsMap.put(id, proxies);
        }
        proxies.add(proxy);
    }

    public synchronized void unregisterCFProxy(Object id, ExternalProxy proxy) {
        Collection proxies = (Collection)idToCFClientsMap.get(id);
        assert proxies != null;
        proxies.remove(proxy);
    }

    //TODO these private methods are slightly unsafe in that they call out to
    //the ejbcontainer and the external proxy within a synchronized block.
    //I think it is sufficiently unlikely that either call can produce a deadlock
    //that I have left these methods unsafe.
    private synchronized void cfRegistered(CFContainer container) {
        Object id = container.getId();
        idToCFContainerMap.put(id, container);
        Collection clients = (Collection)idToCFClientsMap.get(id);
        if (clients != null) {
            Object internalProxy = container.getProxy();
            for (Iterator iterator = clients.iterator(); iterator.hasNext();) {
                ExternalProxy proxy = (ExternalProxy) iterator.next();
                proxy.setTarget(internalProxy);
            }
        }
    }

    private synchronized void cfUnregistered(CFContainer container) {
        Object id = container.getId();
        idToCFContainerMap.remove(id);
        Collection clients = (Collection)idToCFClientsMap.get(id);
        if (clients != null) {
            for (Iterator iterator = clients.iterator(); iterator.hasNext();) {
                ExternalProxy proxy = (ExternalProxy) iterator.next();
                proxy.setTarget(null);
            }
        }
    }

    private synchronized void ejbRegistered(EJBContainer container) {
        Object id = container.getId();
        idToEJBContainerMap.put(id, container);
        Collection remoteClients = (Collection)idToEJBRemoteClientsMap.get(id);
        if (remoteClients != null) {
            Object internalProxy = container.getHomeProxy();
            for (Iterator iterator = remoteClients.iterator(); iterator.hasNext();) {
                ExternalProxy proxy = (ExternalProxy) iterator.next();
                proxy.setTarget(internalProxy);
            }
        }
        Collection localClients = (Collection)idToEJBLocalClientsMap.get(id);
        if (localClients != null) {
            Object internalProxy = container.getHomeProxy();
            for (Iterator iterator = localClients.iterator(); iterator.hasNext();) {
                ExternalProxy proxy = (ExternalProxy) iterator.next();
                proxy.setTarget(internalProxy);
            }
        }
    }

    private synchronized void ejbUnregistered(EJBContainer container) {
        Object id = container.getId();
        idToEJBContainerMap.remove(id);
        Collection remoteClients = (Collection)idToEJBRemoteClientsMap.get(id);
        if (remoteClients != null) {
            for (Iterator iterator = remoteClients.iterator(); iterator.hasNext();) {
                ExternalProxy proxy = (ExternalProxy) iterator.next();
                proxy.setTarget(null);
            }
        }
        Collection localClients = (Collection)idToEJBLocalClientsMap.get(id);
        if (localClients != null) {
            for (Iterator iterator = localClients.iterator(); iterator.hasNext();) {
                ExternalProxy proxy = (ExternalProxy) iterator.next();
                proxy.setTarget(null);
            }
        }
    }

}
