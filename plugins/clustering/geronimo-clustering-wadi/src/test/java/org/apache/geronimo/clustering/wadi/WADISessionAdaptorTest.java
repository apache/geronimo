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

package org.apache.geronimo.clustering.wadi;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.wadi.core.session.Session;

import com.agical.rmock.extension.junit.RMockTestCase;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class WADISessionAdaptorTest extends RMockTestCase {

    private Session session;

    @Override
    protected void setUp() throws Exception {
        session = (Session) mock(Session.class);
        session.getLocalStateMap();
        modify().multiplicity(expect.from(0)).returnValue(new HashMap());
    }
    
    public void testGetSessionIdDelegation() throws Exception {
        session.getName();
        String name = "name";
        modify().returnValue(name);
        
        startVerification();
        
        WADISessionAdaptor adaptor = new WADISessionAdaptor(session);
        assertEquals(name, adaptor.getSessionId());
    }
    
    public void testReleaseDelegation() throws Exception {
        session.destroy();
        
        startVerification();
        
        WADISessionAdaptor adaptor = new WADISessionAdaptor(session);
        adaptor.release();
    }
    
    public void testReleaseThrowsISEWhenDestroyThrowsException() throws Exception {
        session.destroy();
        modify().throwException(new Exception());
        
        startVerification();
        
        WADISessionAdaptor adaptor = new WADISessionAdaptor(session);
        try {
            adaptor.release();
            fail();
        } catch (IllegalStateException e) {
        }
    }
    
    public void testAddStateDelegation() throws Exception {
        String key = "key";
        String value = "value";
        session.addState(key, value);
        
        startVerification();
        
        WADISessionAdaptor adaptor = new WADISessionAdaptor(session);
        adaptor.addState(key, value);
    }
    
    public void testRemoveStateDelegation() throws Exception {
        String key = "key";
        session.removeState(key);

        startVerification();
        
        WADISessionAdaptor adaptor = new WADISessionAdaptor(session);
        adaptor.removeState(key);        
    }

    public void testGetStateDelegation() throws Exception {
        Map state = session.getState();
        
        startVerification();
        
        WADISessionAdaptor adaptor = new WADISessionAdaptor(session);
        assertSame(state, adaptor.getState());
    }
 
    public void testOnEndAccessDelegation() throws Exception {
        session.onEndProcessing();
        
        startVerification();
        
        WADISessionAdaptor adaptor = new WADISessionAdaptor(session);
        adaptor.onEndAccess();
    }

    public void testRetrieveAdaptorOK() throws Exception {
        startVerification();
        
        WADISessionAdaptor adaptor = new WADISessionAdaptor(session);
        assertSame(adaptor, WADISessionAdaptor.retrieveAdaptor(session));
    }
    
    public void testRetrieveThrowsISEIfNoAdaptor() throws Exception {
        startVerification();
        
        try {
            WADISessionAdaptor.retrieveAdaptor(session);
            fail();
        } catch (IllegalStateException e) {
        }
    }
}
