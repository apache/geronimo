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

package org.apache.geronimo.jetty;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.jetty.connector.HTTPConnector;
import junit.framework.TestCase;

/**
 *
 *
 * @version $Revision: 1.5 $ $Date: 2004/03/10 09:58:56 $
 */
public class ContainerTest extends TestCase {
    private Kernel kernel;
    private GBeanMBean container;
    private ObjectName containerName;
    private Set containerPatterns;
    private ObjectName connectorName;
    private MBeanServer mbServer;

    public void testServer() throws Exception {
        assertEquals(new Integer(State.RUNNING_INDEX), kernel.getMBeanServer().getAttribute(containerName, "state"));
    }

    public void testHTTPConnector() throws Exception {
        GBeanMBean connector = new GBeanMBean(HTTPConnector.GBEAN_INFO);
        connector.setAttribute("Port", new Integer(5678));
        connector.setReferencePatterns("JettyContainer", containerPatterns);
        start(connectorName, connector);

        assertEquals(new Integer(State.RUNNING_INDEX), kernel.getMBeanServer().getAttribute(connectorName, "state"));

        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:5678").openConnection();
        try {
            connection.getInputStream();
            fail();
        } catch (Exception e) {
            // 404 proves we spoke to the server even if we didn't get anything
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, connection.getResponseCode());
            connection.disconnect();
        }
        stop(connectorName);
    }

    private void start(ObjectName name, Object instance) throws Exception {
        mbServer.registerMBean(instance, name);
        mbServer.invoke(name, "start", null, null);
    }

    private void stop(ObjectName name) throws Exception {
        mbServer.invoke(name, "stop", null, null);
        mbServer.unregisterMBean(name);
    }

    protected void setUp() throws Exception {
        containerName = new ObjectName("geronimo.jetty:role=Container");
        containerPatterns = new HashSet();
        containerPatterns.add(containerName);
        connectorName = new ObjectName("geronimo.jetty:role=Connector");
        kernel = new Kernel("test.kernel", "test");
        kernel.boot();
        mbServer = kernel.getMBeanServer();
        container = new GBeanMBean(JettyContainerImpl.GBEAN_INFO);
        start(containerName, container);
    }

    protected void tearDown() throws Exception {
        stop(containerName);
        kernel.shutdown();
    }
}
