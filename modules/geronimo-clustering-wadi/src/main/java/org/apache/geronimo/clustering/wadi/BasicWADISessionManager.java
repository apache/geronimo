/**
 *
 * Copyright 2006 The Apache Software Foundation
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
package org.apache.geronimo.clustering.wadi;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.clustering.Node;
import org.apache.geronimo.clustering.Session;
import org.apache.geronimo.clustering.SessionAlreadyExistException;
import org.apache.geronimo.clustering.SessionListener;
import org.apache.geronimo.clustering.SessionManager;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.codehaus.wadi.Collapser;
import org.codehaus.wadi.Contextualiser;
import org.codehaus.wadi.Emoter;
import org.codehaus.wadi.Evicter;
import org.codehaus.wadi.Immoter;
import org.codehaus.wadi.Invocation;
import org.codehaus.wadi.InvocationException;
import org.codehaus.wadi.Manager;
import org.codehaus.wadi.ManagerConfig;
import org.codehaus.wadi.Motable;
import org.codehaus.wadi.PoolableInvocationWrapperPool;
import org.codehaus.wadi.SessionPool;
import org.codehaus.wadi.Streamer;
import org.codehaus.wadi.ValuePool;
import org.codehaus.wadi.group.Dispatcher;
import org.codehaus.wadi.impl.AbsoluteEvicter;
import org.codehaus.wadi.impl.ClusterContextualiser;
import org.codehaus.wadi.impl.ClusteredManager;
import org.codehaus.wadi.impl.DummyContextualiser;
import org.codehaus.wadi.impl.DummyReplicaterFactory;
import org.codehaus.wadi.impl.HashingCollapser;
import org.codehaus.wadi.impl.HybridRelocater;
import org.codehaus.wadi.impl.MemoryContextualiser;
import org.codehaus.wadi.impl.SerialContextualiser;
import org.codehaus.wadi.impl.SerialContextualiserFrontingMemory;
import org.codehaus.wadi.impl.SimpleSessionPool;
import org.codehaus.wadi.impl.SimpleStreamer;
import org.codehaus.wadi.impl.SimpleValuePool;
import org.codehaus.wadi.impl.StatelessContextualiser;
import org.codehaus.wadi.replication.manager.ReplicationManagerFactory;
import org.codehaus.wadi.replication.storage.ReplicaStorageFactory;
import org.codehaus.wadi.replication.strategy.BackingStrategyFactory;
import org.codehaus.wadi.web.AttributesFactory;
import org.codehaus.wadi.web.WebSession;
import org.codehaus.wadi.web.WebSessionPool;
import org.codehaus.wadi.web.WebSessionWrapperFactory;
import org.codehaus.wadi.web.impl.AtomicallyReplicableSessionFactory;
import org.codehaus.wadi.web.impl.DistributableAttributesFactory;
import org.codehaus.wadi.web.impl.DistributableValueFactory;
import org.codehaus.wadi.web.impl.DummyRouter;
import org.codehaus.wadi.web.impl.DummyStatefulHttpServletRequestWrapperPool;
import org.codehaus.wadi.web.impl.StandardSessionWrapperFactory;
import org.codehaus.wadi.web.impl.WebSessionToSessionPoolAdapter;

import EDU.oswego.cs.dl.util.concurrent.Sync;

/**
 * 
 * @version $Rev$ $Date$
 */
public class BasicWADISessionManager implements GBeanLifecycle, SessionManager, WADISessionManager {
    private static final Log log = LogFactory.getLog(BasicWADISessionManager.class);
    
    private final int sweepInterval;
    private final int numPartitions;
    private final ReplicationManagerFactory replicationManagerFactory;
    private final ReplicaStorageFactory replicaStorageFactory;
    private final BackingStrategyFactory backingStrategyFactory;
    private final DispatcherHolder dispatcherHolder;
    private final Set listeners;
    
    private ClusteredManager manager;

    public BasicWADISessionManager(int sweepInterval,
            int numPartitions,
            ReplicationManagerFactory replicationManagerFactory,
            ReplicaStorageFactory replicaStorageFactory,
            BackingStrategyFactory backingStrategyFactory,
            DispatcherHolder dispatcherHolder) {
        this.sweepInterval = sweepInterval;
        this.numPartitions = numPartitions;
        this.dispatcherHolder = dispatcherHolder;
        this.replicationManagerFactory = replicationManagerFactory;
        this.replicaStorageFactory = replicaStorageFactory;
        this.backingStrategyFactory = backingStrategyFactory;
        
        listeners = new HashSet();
    }

    public void doStart() throws Exception {
        Dispatcher dispatcher = dispatcherHolder.getDispatcher();
        
        boolean strictOrdering = true;
        Streamer streamer = new SimpleStreamer();
        Collapser collapser = new HashingCollapser(1024, 10000);
        Map mmap = Collections.synchronizedMap(new HashMap());

        // end of contextualiser stack
        Contextualiser contextualiser = new DummyContextualiser();
        
        // replica aware contextualiser
//        ReplicationManager replicationManager = replicationManagerFactory.factory(dispatcher,
//                replicaStorageFactory,
//                backingStrategyFactory);
//        DistributableManagerRehydrater sessionRehydrater = new DistributableManagerRehydrater();
//        ReplicationManager sessionRepManager = new SessionReplicationManager(replicationManager, sessionRehydrater);
//        contextualiser = new ReplicaAwareContextualiser(contextualiser, sessionRepManager);

        // cluster aware contextualiser
        contextualiser = new ClusterContextualiser(contextualiser, collapser, new HybridRelocater(5000, 5000, true));

        contextualiser = new StatelessContextualiser(contextualiser, Pattern.compile("GET|POST", 2), true, 
                Pattern.compile(".*\\.(JPG|JPEG|GIF|PNG|ICO|HTML|HTM)", 2), false);

        // serialize invocations bound to the same session id
        contextualiser = new SerialContextualiser(contextualiser, collapser, mmap);

        // in-memory contextualiser 
        WebSessionPool sessionPool = new SimpleSessionPool(new AtomicallyReplicableSessionFactory());
        Evicter mevicter = new AbsoluteEvicter(sweepInterval, strictOrdering, 10);
        SessionPool contextPool = new WebSessionToSessionPoolAdapter(sessionPool);
        PoolableInvocationWrapperPool requestPool = new DummyStatefulHttpServletRequestWrapperPool();
        contextualiser = new MotionTracker(contextualiser, mevicter, mmap, streamer, contextPool, requestPool);

        contextualiser = new SerialContextualiserFrontingMemory(contextualiser, new HashingCollapser(1024, 10000));
        
        // Manager
        AttributesFactory attributesFactory = new DistributableAttributesFactory();
        ValuePool valuePool = new SimpleValuePool(new DistributableValueFactory());
        WebSessionWrapperFactory wrapperFactory = new StandardSessionWrapperFactory();
        manager = new ClusteredManager(sessionPool, 
                        attributesFactory,
                        valuePool, 
                        wrapperFactory, 
                        null, 
                        contextualiser, 
                        mmap,
                        new DummyRouter(), 
                        true, 
                        streamer, 
                        true,
                        new DummyReplicaterFactory(),
//                        new ReplicaterAdapterFactory(replicationManager), 
                        null, 
                        null,
                        dispatcher, 
                        numPartitions, 
                        collapser);
//        sessionRehydrater.setManager(manager);

        manager.init(new ManagerConfig() {
            public void callback(Manager manager) {
            }

            public ServletContext getServletContext() {
                return null;
            }
        });
        
        manager.start();
    }

    public void doStop() throws Exception {
        manager.stop();
    }

    public void doFail() {
        try {
            manager.stop();
        } catch (Exception e) {
            log.error(e);
        }
    }

    public Session createSession(String sessionId) throws SessionAlreadyExistException {
        WebSession session;
        try {
            session = manager.createWithName(sessionId);
        } catch (org.codehaus.wadi.SessionAlreadyExistException e) {
            throw new SessionAlreadyExistException(sessionId);
        }
        return new WADISessionAdaptor(session);
    }

    public ClusteredManager getManager() {
        return manager;
    }
    
    public Node getNode() {
        return dispatcherHolder.getNode();
    }
    
    public void registerListener(SessionListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void unregisterListener(SessionListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    private void notifyInboundSessionMigration(WebSession webSession) {
        synchronized (listeners) {
            for (Iterator iter = listeners.iterator(); iter.hasNext();) {
                SessionListener listener = (SessionListener) iter.next();
                listener.notifyInboundSessionMigration(new WADISessionAdaptor(webSession));
            }
        }
    }

    private WebSession notifyOutboundSessionMigration(WebSession webSession) {
        synchronized (listeners) {
            for (Iterator iter = listeners.iterator(); iter.hasNext();) {
                SessionListener listener = (SessionListener) iter.next();
                listener.notifyOutboundSessionMigration(new WADISessionAdaptor(webSession));
            }
        }
        return webSession;
    }

    private class MotionTracker extends MemoryContextualiser {
        private final Immoter immoter;
        private final Emoter emoter;
        
        public MotionTracker(Contextualiser next, Evicter evicter, Map map, Streamer streamer, SessionPool pool,
                PoolableInvocationWrapperPool requestPool) {
            super(next, evicter, map, streamer, pool, requestPool);
            
            Immoter immoterDelegate = super.getImmoter();
            immoter = new InboundSessionTracker(immoterDelegate);

            Emoter emoterDelegate = super.getEmoter();
            emoter = new OutboundSessionTracker(emoterDelegate);
        }

        public Immoter getPromoter(Immoter immoter) {
            Immoter delegate = super.getPromoter(immoter);
            if (null == immoter) {
                return new InboundSessionTracker(delegate);
            } else {
                return delegate;
            }
        }

        public Immoter getImmoter() {
            return immoter;
        }
        
        public Emoter getEmoter() {
            return emoter;
        }
    }

    private class OutboundSessionTracker implements Emoter {
        private final Emoter delegate;

        public OutboundSessionTracker(Emoter delegate) {
            this.delegate = delegate;
        }

        public void commit(String arg0, Motable arg1) {
            notifyOutboundSessionMigration((WebSession) arg1);
            delegate.commit(arg0, arg1);
        }

        public String getInfo() {
            return delegate.getInfo();
        }

        public boolean prepare(String arg0, Motable arg1, Motable arg2) {
            return delegate.prepare(arg0, arg1, arg2);
        }

        public void rollback(String arg0, Motable arg1) {
            delegate.rollback(arg0, arg1);
        }
    }
    
    private class InboundSessionTracker implements Immoter {
        private final Immoter delegate;

        public InboundSessionTracker(Immoter delegate) {
            this.delegate = delegate;
        }

        public void commit(String arg0, Motable arg1) {
            notifyInboundSessionMigration((WebSession) arg1);
            delegate.commit(arg0, arg1);
        }

        public boolean contextualise(Invocation arg0, String arg1, Motable arg2, Sync arg3) throws InvocationException {
            return delegate.contextualise(arg0, arg1, arg2, arg3);
        }

        public String getInfo() {
            return delegate.getInfo();
        }

        public Motable nextMotable(String arg0, Motable arg1) {
            return delegate.nextMotable(arg0, arg1);
        }

        public boolean prepare(String arg0, Motable arg1, Motable arg2) {
            return delegate.prepare(arg0, arg1, arg2);
        }

        public void rollback(String arg0, Motable arg1) {
            delegate.rollback(arg0, arg1);
        }
    }
    
    
    public static final GBeanInfo GBEAN_INFO;
    
    public static final String GBEAN_ATTR_SWEEP_INTERVAL = "sweepInterval";
    public static final String GBEAN_ATTR_NUM_PARTITIONS = "numPartitions";

    public static final String GBEAN_REF_REPLICATION_MANAGER_FACTORY = "ReplicationManagerFactory";
    public static final String GBEAN_REF_REPLICA_STORAGE_FACTORY = "ReplicaStorageFactory";
    public static final String GBEAN_REF_BACKING_STRATEGY_FACTORY = "BackingStrategyFactory";
    public static final String GBEAN_REF_DISPATCHER_HOLDER = "DispatcherHolder";

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic("WADI Session Manager", 
                BasicWADISessionManager.class, 
                NameFactory.GERONIMO_SERVICE);
        
        infoBuilder.addAttribute(GBEAN_ATTR_SWEEP_INTERVAL, int.class, true);
        infoBuilder.addAttribute(GBEAN_ATTR_NUM_PARTITIONS, int.class, true);

        infoBuilder.addReference(GBEAN_REF_REPLICATION_MANAGER_FACTORY, ReplicationManagerFactory.class,
                NameFactory.GERONIMO_SERVICE);
        infoBuilder.addReference(GBEAN_REF_REPLICA_STORAGE_FACTORY, ReplicaStorageFactory.class,
                NameFactory.GERONIMO_SERVICE);
        infoBuilder.addReference(GBEAN_REF_BACKING_STRATEGY_FACTORY, BackingStrategyFactory.class,
                NameFactory.GERONIMO_SERVICE);
        infoBuilder.addReference(GBEAN_REF_DISPATCHER_HOLDER, DispatcherHolder.class, NameFactory.GERONIMO_SERVICE);
        
        infoBuilder.addInterface(SessionManager.class);
        infoBuilder.addInterface(WADISessionManager.class);
        
        infoBuilder.setConstructor(new String[]{GBEAN_ATTR_SWEEP_INTERVAL,
                GBEAN_ATTR_NUM_PARTITIONS,
                GBEAN_REF_REPLICATION_MANAGER_FACTORY,
                GBEAN_REF_REPLICA_STORAGE_FACTORY,
                GBEAN_REF_BACKING_STRATEGY_FACTORY,
                GBEAN_REF_DISPATCHER_HOLDER});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }
    
    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
