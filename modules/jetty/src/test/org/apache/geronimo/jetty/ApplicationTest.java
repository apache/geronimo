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
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
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
 * @version $Revision: 1.7 $ $Date: 2004/03/10 09:58:56 $
 */
public class ApplicationTest extends TestCase {
    private Kernel kernel;
    private GBeanMBean container;
    private ObjectName containerName;
    private Set containerPatterns;
    private ObjectName connectorName;
    private MBeanServer mbServer;
    private GBeanMBean connector;
    private ObjectName appName;

    public void testApplication() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("deployables/war1");
        GBeanMBean app = new GBeanMBean(JettyWebApplicationContext.GBEAN_INFO);
        app.setAttribute("URI", URI.create(url.toString()));
        app.setAttribute("ContextPath", "/test");
        app.setAttribute("ComponentContext", null);
        app.setAttribute("PolicyContextID", null);
        app.setReferencePatterns("Configuration", Collections.EMPTY_SET);
        app.setReferencePatterns("JettyContainer", containerPatterns);
        app.setReferencePatterns("TransactionManager", Collections.EMPTY_SET);
        app.setReferencePatterns("TrackedConnectionAssociator", Collections.EMPTY_SET);
        start(appName, app);

        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:5678/test/hello.txt").openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        assertEquals("Hello World", reader.readLine());
        connection.disconnect();
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
        containerPatterns = Collections.singleton(containerName);
        connectorName = new ObjectName("geronimo.jetty:role=Connector");
        appName = new ObjectName("geronimo.jetty:app=test");

        kernel = new Kernel("test.kernel", "test");
        kernel.boot();
        mbServer = kernel.getMBeanServer();
        container = new GBeanMBean(JettyContainerImpl.GBEAN_INFO);

        connector = new GBeanMBean(HTTPConnector.GBEAN_INFO);
        connector.setAttribute("Port", new Integer(5678));
        connector.setReferencePatterns("JettyContainer", containerPatterns);

        start(containerName, container);
        start(connectorName, connector);
    }

    protected void tearDown() throws Exception {
        stop(connectorName);
        stop(containerName);
        kernel.shutdown();
    }
}
