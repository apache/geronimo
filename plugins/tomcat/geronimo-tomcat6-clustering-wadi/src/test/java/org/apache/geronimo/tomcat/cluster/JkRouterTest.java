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

import org.apache.catalina.Session;
import org.apache.catalina.connector.Request;

import com.agical.rmock.extension.junit.RMockTestCase;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class JkRouterTest extends RMockTestCase {

    private JkRouter router;
    private Request request;
    private Session mockSession;
    private String sessionId;
    private String nodeName;
    private String requestedSessionId;

    @Override
    protected void setUp() throws Exception {
        nodeName = "NODE";
        router = new JkRouter(nodeName);
        
        mockSession = (Session) mock(Session.class);
        request = new Request() {
            @Override
            public Session getSessionInternal() {
                return mockSession;
            }
        };

        sessionId = "ID";
        requestedSessionId = sessionId + "." + nodeName;
    }
    
    public void testReplaceRountingInfo() throws Exception {
        request.setRequestedSessionId(sessionId + ".NODE2");
        
        assertEquals(sessionId + ".NODE2", router.replaceRoutingInfoInRequestedSessionId(request));
        assertEquals(requestedSessionId, request.getRequestedSessionId());
    }

    public void testBuildAugmentedSessionIdWhenNewSession() throws Exception {
        request.setRequestedSessionId("OLDSession");
        mockSession.getId();
        modify().returnValue(requestedSessionId);

        startVerification();
        
        assertEquals(requestedSessionId, router.buildAugmentedSessionId(request, nodeName));
    }
    
    public void testBuildAugmentedSessionIdReturnsNullWhenNoSession() throws Exception {
        request = new Request() {
            @Override
            public Session getSessionInternal() {
                return null;
            }
        };
        assertNull(router.buildAugmentedSessionId(request, "NODE"));
    }
    
    public void testBuildAugmentedSessionIdReturnsNullWhenNoAugmentationIsRequired() throws Exception {
        request.setRequestedSessionId(requestedSessionId);
        mockSession.getId();
        modify().returnValue(requestedSessionId);

        startVerification();
        
        assertNull(router.buildAugmentedSessionId(request, nodeName));
    }
    
    public void testTransformGlobalSessionIdToSessionId() throws Exception {
        assertEquals(requestedSessionId, router.transformGlobalSessionIdToSessionId(sessionId));
    }

    public void testTransformSessionIdToGlobalSessionId() throws Exception {
        assertEquals(sessionId, router.transformSessionIdToGlobalSessionId(requestedSessionId));
    }
}
