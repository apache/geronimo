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
package org.apache.geronimo.remoting.transport.async;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.proxy.ProxyContainer;
import org.apache.geronimo.proxy.ReflexiveInterceptor;
import org.apache.geronimo.remoting.DeMarshalingInterceptor;
import org.apache.geronimo.remoting.InterVMRoutingInterceptor;
import org.apache.geronimo.remoting.InterceptorRegistry;
import org.apache.geronimo.remoting.IntraVMRoutingInterceptor;
import org.apache.geronimo.remoting.router.InterceptorRegistryRouter;
import org.apache.geronimo.remoting.router.SubsystemRouter;
import org.apache.geronimo.remoting.transport.RemoteTransportInterceptor;
import org.apache.geronimo.remoting.transport.URISupport;

import EDU.oswego.cs.dl.util.concurrent.ClockDaemon;
import EDU.oswego.cs.dl.util.concurrent.Executor;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;

/**
 * An application wide registry to hold objects that
 * must be shared accross application components. 
 * 
 * @version $Revision: 1.2 $ $Date: 2003/11/19 11:15:03 $
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
     * @param server
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
    HashMap exportedObjects = new HashMap();
    static class ExportedObjec {
        
    }
    
    /**
     * @param proxy
     * @return
     */
    public Object exportObject(Object object) throws IOException {

        ObjectKey key = new ObjectKey(object);

        // Have we allready exported that object??
        Object proxy = exportedObjects.get(key);
        if (proxy == null) {

            // we need to export it.
            AbstractServer server = getServerForClientRequest();
            // TODO Auto-generated method stub

            // Setup the server side contianer..
            DeMarshalingInterceptor demarshaller = new DeMarshalingInterceptor();
            demarshaller.setClassloader(object.getClass().getClassLoader());
            Long dmiid = InterceptorRegistry.instance.register(demarshaller);

            ProxyContainer serverContainer = new ProxyContainer();
            serverContainer.addInterceptor(demarshaller);
            serverContainer.addInterceptor(new ReflexiveInterceptor(object));

            // Setup client container...
            RemoteTransportInterceptor transport;
            try {
                // Setup the client side container..
                transport = new RemoteTransportInterceptor();
                URI uri = server.getClientConnectURI();
                uri = URISupport.setPath(uri, "/Remoting");
                uri = URISupport.setFragment(uri, "" + dmiid);
                transport.setRemoteURI(uri);
            } catch (URISyntaxException e) {
                throw new IOException("Remote URI could not be constructed.");
            }
            InterVMRoutingInterceptor remoteRouter = new InterVMRoutingInterceptor();
            IntraVMRoutingInterceptor localRouter = new IntraVMRoutingInterceptor();
            localRouter.setDeMarshalingInterceptorID(dmiid);
            localRouter.setNext(demarshaller.getNext());
            remoteRouter.setLocalInterceptor(localRouter);
            remoteRouter.setTransportInterceptor(transport);

            ProxyContainer clientContainer = new ProxyContainer();
            clientContainer.addInterceptor(remoteRouter);
            clientContainer.addInterceptor(localRouter);
            //clientContainer.addInterceptor(demarshaller.getNext());
            proxy = clientContainer.createProxy(object.getClass().getClassLoader(), object.getClass().getInterfaces());

            exportedObjects.put(key, proxy);
        }
        return proxy;
    }
    
    public boolean unexportObject(Object object) {
        ObjectKey key = new ObjectKey(object);
        return exportedObjects.remove(key)!=null;        
    }
}
