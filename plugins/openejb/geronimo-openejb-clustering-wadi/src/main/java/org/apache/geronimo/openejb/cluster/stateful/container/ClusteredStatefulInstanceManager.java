/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.openejb.cluster.stateful.container;

import java.lang.reflect.Method;
import java.rmi.dgc.VMID;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.clustering.Session;
import org.apache.geronimo.clustering.SessionAlreadyExistException;
import org.apache.geronimo.clustering.SessionListener;
import org.apache.geronimo.clustering.SessionManager;
import org.apache.geronimo.openejb.cluster.infra.SessionManagerTracker;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.SystemException;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.stateful.BeanEntry;
import org.apache.openejb.core.stateful.StatefulInstanceManager;
import org.apache.openejb.core.stateful.StatefulContainer.MethodType;
import org.apache.openejb.persistence.JtaEntityManagerRegistry;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Index;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class ClusteredStatefulInstanceManager extends StatefulInstanceManager implements SessionManagerTracker {
    private final Map<Object, SessionManager> sessionManagersById;
    private final Map<Object, CoreDeploymentInfo> deploymentsById;

    public ClusteredStatefulInstanceManager(TransactionManager transactionManager,
        SecurityService securityService,
        JtaEntityManagerRegistry jtaEntityManagerRegistry,
        Class passivatorClass,
        int timeout,
        int poolSize,
        int bulkPassivate) throws OpenEJBException {
        super(transactionManager,
            securityService,
            jtaEntityManagerRegistry,
            passivatorClass,
            timeout,
            poolSize,
            bulkPassivate);
        
        sessionManagersById = new HashMap<Object, SessionManager>();
        deploymentsById = new HashMap<Object, CoreDeploymentInfo>();
    }
    
    public void addSessionManager(Object deploymentId, SessionManager sessionManager) {
        synchronized (sessionManagersById) {
            sessionManagersById.put(deploymentId, sessionManager);
        }
        sessionManager.registerListener(new MigrationListener());
    }
    
    public void removeSessionManager(Object deploymentId, SessionManager sessionManager) {
        synchronized (sessionManagersById) {
            sessionManagersById.remove(deploymentId);
        }
    }
    
    @Override
    public void deploy(CoreDeploymentInfo deploymentInfo, Index<Method, MethodType> index) throws OpenEJBException {
        synchronized (deploymentsById) {
            deploymentsById.put(deploymentInfo.getDeploymentID(), deploymentInfo);
        }
        super.deploy(deploymentInfo, index);
    }
    
    @Override
    public void undeploy(CoreDeploymentInfo deploymentInfo) throws OpenEJBException {
        synchronized (deploymentsById) {
            deploymentsById.remove(deploymentInfo.getDeploymentID());
        }
        super.undeploy(deploymentInfo);
    }
    
    @Override
    protected BeanEntry newBeanEntry(Object primaryKey, Object bean) {
        ThreadContext threadContext = ThreadContext.getThreadContext();
        if (null == threadContext) {
            throw new IllegalStateException("No ThreadContext");
        }
        Object deploymentId = threadContext.getDeploymentInfo().getDeploymentID();

        SessionManager sessionManager;
        synchronized (sessionManagersById) {
            sessionManager = sessionManagersById.get(deploymentId);
        }
        if (null == sessionManager) {
            throw new IllegalStateException("No SessionManager registered for deployment [" + deploymentId + "]");
        }
        
        Session session;
        try {
            if (!(primaryKey instanceof VMID)) {
                // primaryKey.toString() must be an unique String representation for an unique identifier. Here, we
                // check that primaryKey is a VMID as its Object.toString implementation returns an unique String
                // representation. Other types may not implement Object.toString() "correctly".
                throw new AssertionError("primaryKey MUST be a " + VMID.class.getName());
            }
            session = sessionManager.createSession(primaryKey.toString());
        } catch (SessionAlreadyExistException e) {
            throw (IllegalStateException) new IllegalStateException().initCause(e);
        }
        
        return new ClusteredBeanEntry(session, deploymentId, bean, primaryKey, timeOut);
    }

    @Override
    protected void onFreeBeanEntry(ThreadContext callContext, BeanEntry entry) {
        SessionOperation operation = callContext.get(SessionOperation.class);
        if (null != operation) {
            if (SessionOperation.DESTRUCTION != operation && SessionOperation.OUTBOUND_MIGRATION != operation) {
                throw new AssertionError();
            }
            return;
        }
        ClusteredBeanEntry clusteredBeanEntry = (ClusteredBeanEntry) entry;
        clusteredBeanEntry.release();
    }
    
    @Override
    protected void onPoolInstanceWithoutTransaction(ThreadContext callContext, BeanEntry entry) {
        SessionOperation operation = callContext.get(SessionOperation.class);
        if (null != operation) {
            if (SessionOperation.INBOUND_MIGRATION != operation) {
                throw new AssertionError();
            }
            return;
        }
        ClusteredBeanEntry clusteredBeanEntry = (ClusteredBeanEntry) entry;
        clusteredBeanEntry.endAccess();
    }
    
    protected enum SessionOperation {
        INBOUND_MIGRATION,
        OUTBOUND_MIGRATION,
        DESTRUCTION
    }
    
    protected class MigrationListener implements SessionListener {
        private final Log log = LogFactory.getLog(MigrationListener.class);

        public void notifyInboundSessionMigration(org.apache.geronimo.clustering.Session session) {
            ClusteredBeanEntry beanEntry = new ClusteredBeanEntry(session);
            ThreadContext context = newThreadContext(beanEntry);
            if (null == context) {
                return;
            }
            context.set(SessionOperation.class, SessionOperation.INBOUND_MIGRATION);

            try {
                activateInstance(context, beanEntry);
                poolInstance(context, beanEntry.getBean());
            } catch (Exception e) {
                log.warn("Cannot activate migrated bean entry.", e);
            }
        }

        public void notifyOutboundSessionMigration(org.apache.geronimo.clustering.Session session) {
            ClusteredBeanEntry beanEntry = new ClusteredBeanEntry(session);
            ThreadContext context = newThreadContext(beanEntry);
            if (null == context) {
                return;
            }
            context.set(SessionOperation.class, SessionOperation.OUTBOUND_MIGRATION);

            passivate(context, beanEntry);
            try {
                freeInstance(context);
            } catch (SystemException e) {
                log.warn("Cannot free bean entry", e);
            }
        }

        public void notifySessionDestruction(org.apache.geronimo.clustering.Session session) {
            ClusteredBeanEntry beanEntry = new ClusteredBeanEntry(session);
            ThreadContext context = newThreadContext(beanEntry);
            if (null == context) {
                return;
            }
            context.set(SessionOperation.class, SessionOperation.DESTRUCTION);
            
            try {
                freeInstance(context);
            } catch (SystemException e) {
                log.warn("Cannot free bean entry", e);
            }
        }

        protected ThreadContext newThreadContext(ClusteredBeanEntry beanEntry) {
            Object deploymentId = beanEntry.getDeploymentId();
            CoreDeploymentInfo deploymentInfo;
            synchronized (deploymentsById) {
                deploymentInfo = deploymentsById.get(deploymentId);
            }
            if (null == deploymentInfo) {
                log.warn("Deployment [" + deploymentId + "] is unknown.");
                return null;
            }
            return new ThreadContext(deploymentInfo, beanEntry.getPrimaryKey());
        }
    }
    
}
