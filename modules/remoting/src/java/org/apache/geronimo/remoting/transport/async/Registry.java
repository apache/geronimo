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

package org.apache.geronimo.remoting.transport.async;

import java.io.IOException;
import java.net.URI;
import java.rmi.Remote;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.proxy.ProxyContainer;
import org.apache.geronimo.proxy.ReflexiveInterceptor;
import org.apache.geronimo.remoting.DeMarshalingInterceptor;
import org.apache.geronimo.remoting.InterceptorRegistry;
import org.apache.geronimo.remoting.MarshalingInterceptor;
import org.apache.geronimo.remoting.TransportContext;
import org.apache.geronimo.remoting.router.InterceptorRegistryRouter;
import org.apache.geronimo.remoting.router.SubsystemRouter;
import org.apache.geronimo.remoting.transport.RemoteTransportInterceptor;
import org.apache.geronimo.remoting.transport.URISupport;
import org.apache.geronimo.core.service.Interceptor;

import EDU.oswego.cs.dl.util.concurrent.ClockDaemon;
import EDU.oswego.cs.dl.util.concurrent.Executor;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;

/**
 * An application wide registry to hold objects that
 * must be shared accross application components. 
 * 
 * @version $Revision: 1.6 $ $Date: 2004/03/10 09:59:20 $
 */
public class Registry {

    /** The amount of time that must pass before a request is considered timedout. */
    static public final long REQUEST_TIMEOUT =
        Long.parseLong(System.getProperty("org.apache.geronimo.remoting.transport.async.request_timeout", "60000"));
    // 1 min.
    /** The maximum number of open connections that are allowed per pool.  A new pool is allocated to each sever this vm connects to. */
    static public final int MAX_CONNECTION_POOL_SIZE =
        Integer.parseInt(System.getProperty("org.apache.geronimo.remoting.transport.async.max_connection_per_pool", "25"));

    static private final Log log = LogFactory.getLog(Registry.class);
    static public final Registry instance = new Registry();

    private AbstractServer dynamicServer;
    private AbstractServer defaultServer;
    private PooledExecutor workManager;

    /**
     * Manages the thread that can used to schedule short 
     * running tasks in the future.
     */
    protected ClockDaemon clockDaemon;
    public boolean MOCK_APPLET_SECURITY = false;

    private int nextWorkerID = 0;
    private int getNextWorkerID() {
        return nextWorkerID++;
    }
    /**
     * Provides a thread pool that can be shared accros components.
     */
    synchronized public Executor getWorkManager() {
        if (workManager != null)
            return workManager;

        PooledExecutor p = new PooledExecutor();
        p.setKeepAliveTime(1000 * 30);
        p.setMinimumPoolSize(5);
        p.setMaximumPoolSize(Integer.MAX_VALUE);
        p.setThreadFactory(new ThreadFactory() {
            public Thread newThread(Runnable arg0) {
                return new Thread(arg0, "Remoting 'async' protocol worker " + getNextWorkerID());
            }
        });

        workManager = p;
        return workManager;
    }

    /**
     * @return
     */
    public ClockDaemon getClockDaemon() {
        if (clockDaemon != null)
            return clockDaemon;
        clockDaemon = new ClockDaemon();
        clockDaemon.setThreadFactory(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "Remoting 'async' protocol monitor");
                t.setDaemon(true);
                return t;
            }
        });
        return clockDaemon;
    }

    /**
     * Gets the system wide AbstractServer.  If a AbstractServer
     * has not been registed explicitly,
     * It attempts to create an AsynchChannelServer that listens on an 
     * annonymous port.  Returns a BackChannelServer if a normal
     * server could not be bound.
     */
    synchronized public AbstractServer getServerForClientRequest() {
        if (defaultServer != null)
            return defaultServer;
        if (dynamicServer != null)
            return dynamicServer;

        // This jvm did not have a server running.  try to start the
        // server on a dynamic port.
        try {

            if (MOCK_APPLET_SECURITY) {
                dynamicServer = createBackChannelServer();
                return dynamicServer;
            }

            dynamicServer = (AbstractServer) TransportFactory.instance.createSever();

            // Build a routing path so this transport can deliver messages back to 
            // clients.
            SubsystemRouter subsystemRouter = new SubsystemRouter();
            subsystemRouter.doStart();
            InterceptorRegistryRouter registryRouter = new InterceptorRegistryRouter();
            registryRouter.setSubsystemRouter(subsystemRouter);
            registryRouter.doStart();

            dynamicServer.bind(new URI("async://0.0.0.0:0"), subsystemRouter);
            dynamicServer.start();

            return dynamicServer;

        } catch (Throwable e) {
            dynamicServer = createBackChannelServer();
            return dynamicServer;
        }
    }

    /**
     * @return
     */
    private AbstractServer createBackChannelServer() {
        try {
            BackChannelServer server = new BackChannelServer();
            server.bind(new URI("async://0.0.0.0:0"), null);
            server.start();
            return server;
        } catch (Exception e) {
            // wont happen!
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Sets the application wide server.  This gets called when running
     * in the sever and the server is explicity configured.
     * 
     * @param server
     */
    synchronized public void setDefaultServer(AbstractServer server) {
        defaultServer = server;
    }

    /**
     * Sets the application wide server.  This gets called when running
     * in the sever and the server is explicity configured.
     * 
     */
    synchronized public AbstractServer getDefaultServer() {
        return defaultServer;
    }

    // Use for the keys in our map..  
    // since we want to do identity lookups on objects.
    static class ObjectKey {
        private Object key;
        ObjectKey(Object key) {
            this.key = key;
        }
        public boolean equals(Object obj) {
            return ((ObjectKey) obj).key == key;
        }
        public int hashCode() {
            return key.hashCode();
        }
    }

    // Keeps track of the exported objects.
    Map exportedObjects = Collections.synchronizedMap(new HashMap());

    static class ExportedObject {
        RemoteRef remoteRef;
        ProxyContainer serverContainer;
    }

    /**
     * @return
     */
    public RemoteRef exportObject(Object object) throws IOException {

        ObjectKey key = new ObjectKey(object);

        // Have we allready exported that object??
        ExportedObject eo = (ExportedObject) exportedObjects.get(key);
        if (eo == null) {

            // Setup the server side contianer..
            eo = new ExportedObject();
            Interceptor ri = new ReflexiveInterceptor(object);
            DeMarshalingInterceptor demarshaller = new DeMarshalingInterceptor(ri, object.getClass().getClassLoader());
            Long dmiid = InterceptorRegistry.instance.register(demarshaller);
            eo.serverContainer = new ProxyContainer(demarshaller);

            // Build the RemoteRef for the object.
            eo.remoteRef = new RemoteRef();
            try {

                AbstractServer server = getServerForClientRequest();
                URI uri = server.getClientConnectURI();
                uri = URISupport.setPath(uri, "/Remoting");
                uri = URISupport.setFragment(uri, "" + dmiid);
                eo.remoteRef.remoteURI = uri;
            } catch (Exception e) {
                throw new IOException("Remote URI could not be constructed.");
            }
            eo.remoteRef.interfaces = object.getClass().getInterfaces();

            exportedObjects.put(key, eo);
            log.debug("Exported object: "+eo.remoteRef.remoteURI);
        }
        return eo.remoteRef;
    }

    public boolean unexportObject(Object object) {
        ObjectKey key = new ObjectKey(object);
        return exportedObjects.remove(key) != null;
    }

    // Keep a weak map of the objects that we have imported.
    // This allows == comparions to work on previously imported objects.
    Map importedObjects = new WeakHashMap();

    /**
     * @return
     */
    synchronized protected Object importObject(RemoteRef ref) {

        Object object = importedObjects.get(ref);
        if (object == null) {

            RemoteTransportInterceptor transport = new RemoteTransportInterceptor(ref.remoteURI);
            MarshalingInterceptor mashaller = new MarshalingInterceptor(transport);
            IdentityInterceptor identity = new IdentityInterceptor(mashaller, ref);

            ProxyContainer clientContainer = new ProxyContainer(identity);

            object = clientContainer.createProxy(Thread.currentThread().getContextClassLoader(), ref.interfaces);
            log.trace("Imported object: "+ref.remoteURI);
            importedObjects.put(ref, object);
        }
        return object;
    }

    public static final TransportContext transportContext = new TransportContext() {
        public Object writeReplace(Object proxy) throws IOException {
            if (proxy instanceof Remote) {
                return Registry.instance.exportObject(proxy);
            }
            return proxy;
        }
        public Object readReplace(Object obj) throws IOException {
            if (obj instanceof RemoteRef) {
                return Registry.instance.importObject((RemoteRef) obj);
            }
            return obj;
        }
    };

}
