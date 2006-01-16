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
package org.apache.geronimo.session.remote;

import org.apache.geronimo.session.Locator;
import org.apache.geronimo.session.Server;
import org.apache.geronimo.session.Session;
import org.apache.geronimo.session.SessionLocation;
import org.apache.geronimo.session.SessionNotLocalException;
import org.apache.geronimo.session.SessionTest;

import java.util.Arrays;

/**
 * 
 * @version $Revision: $
 */
public class RemoteSessionTest extends SessionTest {

    public void testMove() throws Exception {
        SessionLocation location = locator.getSessionLocation("remote");

        assertEquals("state.isLocal", false, location.isLocal());

        Session session = null;
        try {
            session = location.getSession();
            fail("Cannot access the session on a remote session");
        }
        catch (SessionNotLocalException e) {
            System.out.println("Caught expected exception: " + e);
        }

        // now lets move it
        session = location.moveLocally();

        assertEquals("state.isLocal", true, location.isLocal());
        session = location.getSession();
        useSession(session);
    }

    public void testRedirectOrProxy() throws Exception {
        SessionLocation location = locator.getSessionLocation("remote");

        assertEquals("state.isLocal", false, location.isLocal());

        Session session = null;
        try {
            session = location.getSession();
            fail("Cannot access the session on a remote session");
        }
        catch (SessionNotLocalException e) {
            System.out.println("Caught expected exception: " + e);
        }

        // now lets access the server information as we are going to
        // either redirect or proxy the request to where the session really is

        Server server = location.getServer();
        assertNotNull("server", server);

        String[] addresses = server.getAddresses("ejb");
        assertNotNull("addresses", addresses);
    }

    public void testServerList() throws Exception {
        RemoteServer[] servers = getLocator().getRemoteServers();
        assertNotNull("servers should not be null", servers);
        assertTrue("should be at least one server always", servers.length > 0);

        for (int i = 0; i < servers.length; i++) {
            RemoteServer server = servers[i];
            System.out.println("Server: " + server.getName() + " has addresses: "
                    + Arrays.asList(server.getAddresses("ejb")));

            assertNotNull("Should have a remote controller", server.getRemoteClient());
        }
    }

    public RemoteLocator getLocator() {
        return (RemoteLocator) locator;
    }

    protected Locator createLocator() throws Exception {
        RemoteLocator locator = new RemoteLocator("localServer");
        Server localServer = locator.getLocalServer();
        localServer.setAddresses("ejb", new String[] { "http://localhost" });

        RemoteLocator remoteLocator = new RemoteLocator("remoteServer");
        Server remoteServer = remoteLocator.getLocalServer();
        remoteServer.setAddresses("ejb", new String[] { "http://somehost" });

        // lets wire the servers together
        locator.onServerCreate(remoteServer, remoteLocator);
        remoteLocator.onServerCreate(localServer, locator);

        locator.setSessionListener(remoteLocator);
        remoteLocator.setSessionListener(locator);

        // add some sessions
        locator.createSession("local").release();
        remoteLocator.createSession("remote").release();

        return locator;
    }
}
