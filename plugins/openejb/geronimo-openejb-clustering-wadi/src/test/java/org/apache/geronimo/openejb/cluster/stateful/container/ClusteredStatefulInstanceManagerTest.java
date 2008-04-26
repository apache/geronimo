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

import java.io.IOException;
import java.rmi.dgc.VMID;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

import org.apache.geronimo.clustering.Session;
import org.apache.geronimo.clustering.SessionListener;
import org.apache.geronimo.clustering.wadi.WADISessionManager;
import org.apache.geronimo.openejb.cluster.stateful.container.ClusteredStatefulContainerTest.SFSB;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.DeploymentContext;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.stateful.BeanEntry;
import org.apache.openejb.persistence.JtaEntityManagerRegistry;
import org.apache.openejb.spi.SecurityService;

import com.agical.rmock.core.Action;
import com.agical.rmock.core.MethodHandle;
import com.agical.rmock.core.describe.ExpressionDescriber;
import com.agical.rmock.core.match.operator.AbstractExpression;
import com.agical.rmock.extension.junit.RMockTestCase;

/**
 * @version $Rev$ $Date$
 */
public class ClusteredStatefulInstanceManagerTest extends RMockTestCase {

    private ClusteredStatefulInstanceManager manager;
    private String deploymentId;
    private CoreDeploymentInfo deploymentInfo;
    private VMID primKey;
    private ThreadContext threadContext;
    private WADISessionManager sessionManager;
    private SessionListener sessionListener;

    @Override
    protected void setUp() throws Exception {
        TransactionManager txManager = (TransactionManager) mock(TransactionManager.class);
        SecurityService securityService = (SecurityService) mock(SecurityService.class);
        TransactionSynchronizationRegistry txSynchRegistry = (TransactionSynchronizationRegistry) mock(TransactionSynchronizationRegistry.class);
        JtaEntityManagerRegistry jtaEntityManagerRegistry = new JtaEntityManagerRegistry(txSynchRegistry);
        
        manager = (ClusteredStatefulInstanceManager) intercept(ClusteredStatefulInstanceManager.class,
            new Object[] { txManager, securityService, jtaEntityManagerRegistry, null, 1, 1, 1 });
        
        deploymentId = "deploymentId";
        deploymentInfo = new CoreDeploymentInfo(new DeploymentContext(deploymentId, null, null),
            SFSB.class,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null) {
            @Override
            public ClassLoader getClassLoader() {
                return getClass().getClassLoader();
            }
            @Override
            public Object getDeploymentID() {
                return deploymentId;
            }
        };

        primKey = new VMID();

        sessionManager = (WADISessionManager) mock(WADISessionManager.class);

        threadContext = new ThreadContext(deploymentInfo, primKey);
        ThreadContext.enter(threadContext);
    }
    
    @Override
    protected void tearDown() throws Exception {
        ThreadContext.exit(threadContext);
    }
    
    public void testNewBeanEntryForUnknownDeploymentThrowsISE() throws Exception {
        startVerification();
        
        try {
            manager.newBeanEntry("primaryKey", new Object());
            fail();
        } catch (IllegalStateException e) {
        }
    }
    
    public void testNewBeanEntryOK() throws Exception {
        VMID primaryKey = new VMID();

        recordAddSessionManagerAndCreateSession(primaryKey);
        
        startVerification();
        
        manager.addSessionManager(deploymentId, sessionManager);
        manager.newBeanEntry(primaryKey, new Object());
    }

    public void testSessionDestructionFreeInstance() throws Exception {
        Session session = recordAddSessionManagerAndCreateSession(primKey);
        FutureTask<BeanEntry> newBeanEntryTask = newBeanEntryTask(primKey, session);

        manager.freeInstance(null);
        modify().args(new ThreadContextArgAssertion());
        
        startVerification();
        
        manager.deploy(deploymentInfo, null);
        manager.addSessionManager(deploymentId, sessionManager);
        
        newBeanEntryTask.run();
        sessionListener.notifySessionDestruction(session);
    }

    public void testInboundSessionMigrationActivateAndPoolBeanEntry() throws Exception {
        Session session = recordAddSessionManagerAndCreateSession(primKey);
        FutureTask<BeanEntry> newBeanEntryTask = newBeanEntryTask(primKey, session);

        manager.activateInstance(null, null);
        modify().args(new ThreadContextArgAssertion(), is.NOT_NULL);
        manager.poolInstance(null, null);
        modify().args(new ThreadContextArgAssertion(), is.NOT_NULL);
        
        startVerification();
        
        manager.deploy(deploymentInfo, null);
        manager.addSessionManager(deploymentId, sessionManager);
        
        newBeanEntryTask.run();
        sessionListener.notifyInboundSessionMigration(session);
    }
    
    public void testOutboundSessionMigrationPassivateBeanEntry() throws Exception {
        Session session = recordAddSessionManagerAndCreateSession(primKey);
        FutureTask<BeanEntry> newBeanEntryTask = newBeanEntryTask(primKey, session);
        
        manager.passivate(null, null);
        modify().args(new ThreadContextArgAssertion(), is.NOT_NULL);
        
        manager.freeInstance(null);
        modify().args(new ThreadContextArgAssertion());
        
        startVerification();
        
        manager.deploy(deploymentInfo, null);
        manager.addSessionManager(deploymentId, sessionManager);
        
        newBeanEntryTask.run();
        sessionListener.notifyOutboundSessionMigration(session);
    }
    
    public void testOnFreeBeanEntryReleaseSession() throws Exception {
        Session session = (Session) mock(Session.class);
        session.addState(ClusteredBeanEntry.SESSION_KEY_ENTRY, null);
        modify().args(is.AS_RECORDED, is.NOT_NULL);

        session.release();
        
        startVerification();

        manager.onFreeBeanEntry(threadContext, new ClusteredBeanEntry(session, deploymentId, new Object(), primKey, 0));
    }
    
    public void testOnPoolInstanceWithoutTransactionTriggersSessionOnEndAccess() throws Exception {
        Session session = (Session) mock(Session.class);
        session.addState(ClusteredBeanEntry.SESSION_KEY_ENTRY, null);
        modify().args(is.AS_RECORDED, is.NOT_NULL);
        
        session.addState(ClusteredBeanEntry.SESSION_KEY_ENTRY, null);
        modify().args(is.AS_RECORDED, is.NOT_NULL);
        session.onEndAccess();
        
        startVerification();
        
        manager.onPoolInstanceWithoutTransaction(threadContext, new ClusteredBeanEntry(session,
            deploymentId,
            new Object(),
            primKey,
            0));
    }
    
    private FutureTask<BeanEntry> newBeanEntryTask(final VMID primaryKey, Session session) {
        final FutureTask<BeanEntry> newBeanEntryTask = new FutureTask<BeanEntry>(new Callable<BeanEntry>() {
            public BeanEntry call() throws Exception {
                return manager.newBeanEntry(primaryKey, new Object());
            }
        });
        session.getState(ClusteredBeanEntry.SESSION_KEY_ENTRY);
        modify().multiplicity(expect.from(0)).perform(new Action() {
            public Object invocation(Object[] arg0, MethodHandle arg1) throws Throwable {
                return newBeanEntryTask.get();
            }
        });
        return newBeanEntryTask;
    }
    
    private Session recordAddSessionManagerAndCreateSession(VMID primaryKey) throws Exception {
        sessionManager.registerListener(null);
        modify().args(new AbstractExpression() {
            public void describeWith(ExpressionDescriber arg0) throws IOException {
            }

            public boolean passes(Object arg0) {
                sessionListener = (SessionListener) arg0;
                return true;
            }
        });
        
        Session session = sessionManager.createSession(primaryKey.toString());
        
        session.addState(ClusteredBeanEntry.SESSION_KEY_ENTRY, null);
        modify().args(is.AS_RECORDED, is.NOT_NULL);
        
        return session;
    }
    
    protected final class ThreadContextArgAssertion extends AbstractExpression {
        public void describeWith(ExpressionDescriber arg0) throws IOException {
        }

        public boolean passes(Object arg0) {
            ThreadContext context = (ThreadContext) arg0;
            assertSame(deploymentInfo, context.getDeploymentInfo());
            assertSame(primKey, context.getPrimaryKey());
            return true;
        }
    }

}
