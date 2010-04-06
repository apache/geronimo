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

package org.apache.geronimo.tomcat.cluster;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.catalina.Context;
import org.apache.catalina.Session;
import org.apache.geronimo.clustering.SessionListener;
import org.apache.geronimo.clustering.SessionManager;

import com.agical.rmock.core.Action;
import com.agical.rmock.core.MethodHandle;
import com.agical.rmock.core.describe.ExpressionDescriber;
import com.agical.rmock.core.match.operator.AbstractExpression;
import com.agical.rmock.extension.junit.RMockTestCase;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class ClusteredManagerTest extends RMockTestCase {
    private SessionManager sessionManager;
    private SessionListener sessionListener;
    private String globalSessionId;
    private String sessionId;
    private Context context;
    private String nodeName;

    @Override
    protected void setUp() throws Exception {
        globalSessionId = "sessionId";
        nodeName = "NODE";
        sessionId = globalSessionId + "." + nodeName;

        sessionManager = (SessionManager) mock(SessionManager.class);
        sessionManager.registerListener(null);
        modify().args(new AbstractExpression() {
            public void describeWith(ExpressionDescriber arg0) throws IOException {
            }
            
            public boolean passes(Object arg0) {
                sessionListener = (SessionListener) arg0;
                return true;
            }
        });

        sessionManager.getNode().getName();
        modify().returnValue(nodeName);
        
        context = (Context) mock(Context.class);
        
        context.getSessionTimeout();
        modify().returnValue(10);

        context.addPropertyChangeListener(null);
        modify().args(is.NOT_NULL);

        context.getApplicationLifecycleListeners();
        modify().multiplicity(expect.from(0));

        context.getApplicationEventListeners();
        modify().multiplicity(expect.from(0));
    }

    public void testCreatedSession() throws Exception {
        recordCreateUnderlyingSession();
        
        startVerification();

        ClusteredManager manager = newManager();
        Session createdSession = manager.createSession(null);
        assertTrue(createdSession.isValid());

        HttpSession httpSession = createdSession.getSession();
        assertEquals("value", httpSession.getAttribute("key"));
        assertTrue(httpSession.isNew());
        
        assertSame(createdSession, manager.findSession(sessionId));
    }

    public void testSessionDestructionRemovesSession() throws Exception {
        org.apache.geronimo.clustering.Session underlyingSession = recordCreateUnderlyingSession();
        
        startVerification();
        
        ClusteredManager manager = newManager();
        manager.createSession(null);
        
        sessionListener.notifySessionDestruction(underlyingSession);
        
        assertNull(manager.findSession(sessionId));
    }
    
    public void testOutboundSessionDestructionRemovesSession() throws Exception {
        org.apache.geronimo.clustering.Session underlyingSession = recordCreateUnderlyingSession();
        
        startVerification();
        
        ClusteredManager manager = newManager();
        manager.createSession(null);
        
        sessionListener.notifyOutboundSessionMigration(underlyingSession);
        
        assertNull(manager.findSession(sessionId));
    }
    
    public void testInboundSessionMigrationAddsSession() throws Exception {
        org.apache.geronimo.clustering.Session underlyingSession =
            (org.apache.geronimo.clustering.Session) mock(org.apache.geronimo.clustering.Session.class);
        recordUnderlyingSessionState(underlyingSession);
        
        startVerification();
        
        ClusteredManager manager = newManager();
        
        sessionListener.notifyInboundSessionMigration(underlyingSession);
        
        Session foundSession = manager.findSession(sessionId);
        assertNotNull(foundSession);

        assertTrue(foundSession.isValid());

        HttpSession httpSession = foundSession.getSession();
        assertEquals("value", httpSession.getAttribute("key"));
        assertFalse(httpSession.isNew());
    }
    
    public void testInvalidateSessionReleasesUnderlyingSessionAndRemoveSessionFromManager() throws Exception {
        final org.apache.geronimo.clustering.Session underlyingSession =recordCreateUnderlyingSession();
        context.getLoader();
        modify().returnValue(null);
        underlyingSession.release();
        modify().perform(new Action() {
            public Object invocation(Object[] arg0, MethodHandle arg1) throws Throwable {
                sessionListener.notifySessionDestruction(underlyingSession);
                return null;
            }
        });
        
        startVerification();

        ClusteredManager manager = newManager();
        Session session = manager.createSession(null);
        HttpSession httpSession = session.getSession();
        httpSession.invalidate();
        
        assertNull(manager.findSession(sessionId));
    }
    
    public void testSessionEndAccessTriggersOnEndAccess() throws Exception {
        org.apache.geronimo.clustering.Session underlyingSession =recordCreateUnderlyingSession();
        underlyingSession.onEndAccess();
        
        startVerification();
        
        ClusteredManager manager = newManager();
        Session session = manager.createSession(null);
        session.endAccess();
    }
    
    private org.apache.geronimo.clustering.Session recordCreateUnderlyingSession() throws Exception {
        org.apache.geronimo.clustering.Session underlyingSession = sessionManager.createSession(globalSessionId);
        recordUnderlyingSessionState(underlyingSession);
        
        return underlyingSession;
    }

    private void recordUnderlyingSessionState(org.apache.geronimo.clustering.Session underlyingSession) {
        underlyingSession.getSessionId();
        modify().multiplicity(expect.from(0)).returnValue(globalSessionId);
        
        underlyingSession.getState();
        Map attributes = new HashMap();
        attributes.put("key", "value");
        modify().returnValue(attributes);
    }

    private ClusteredManager newManager() {
        ClusteredManager manager = new ClusteredManager(sessionManager) {
            @Override
            protected synchronized String generateSessionId() {
                return sessionId;
            }
        };
        manager.setContainer(context);
        return manager;
    }

}
