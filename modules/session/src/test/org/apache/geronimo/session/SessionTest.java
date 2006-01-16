/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.session;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;

import org.apache.geronimo.session.local.LocalLocator;

import java.util.Map;

import junit.framework.TestCase;

/**
 * 
 * @version $Revision: $
 */
public class SessionTest extends TestCase {

    protected Locator locator;

    public void testLocalAccess() throws Exception {
        SessionLocation location = locator.getSessionLocation("local");

        // the state should be locally in this test
        assertEquals("state.isLocal", true, location.isLocal());

        Session session = location.getSession();
        useSession(session);
    }

    protected void useSession(Session session) throws SessionNotLocalException {

        // lets make some state
        session.addState("ejb:123", new ConcurrentHashMap());

        // now lets use it
        Map state = (Map) session.getState("ejb:123");

        state.put("foo", "123");

        session.release();
    }

    protected void setUp() throws Exception {
        this.locator = createLocator();
    }

    protected Locator createLocator() throws Exception {
        LocalLocator locator = new LocalLocator("localServer");

        locator.createSession("local").release();

        return locator;
    }

}
