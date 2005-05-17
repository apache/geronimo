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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.jetty.connector.HTTPConnector;
import org.apache.geronimo.jetty.app.MockWebServiceContainer;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.webservices.WebServiceContainer;

/**
 * @version $Rev$ $Date$
 */
public class ContainerTest extends TestCase {
    private ClassLoader cl = this.getClass().getClassLoader();
    private Kernel kernel;
    private GBeanData container;
    private ObjectName containerName;
    private Set containerPatterns;
    private ObjectName connectorName;

    public void testServer() throws Exception {
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(containerName));
    }

    public void testHTTPConnector() throws Exception {
        GBeanData connector = new GBeanData(connectorName, HTTPConnector.GBEAN_INFO);
        connector.setAttribute("port", new Integer(5678));
        connector.setReferencePatterns("JettyContainer", containerPatterns);
        start(connector);

        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(connectorName));

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

    public void testWebServiceHandler() throws Exception {
        GBeanData connector = new GBeanData(connectorName, HTTPConnector.GBEAN_INFO);
        connector.setAttribute("port", new Integer(5678));
        connector.setReferencePatterns("JettyContainer", containerPatterns);
        start(connector);

        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(connectorName));

        String contextPath = "/foo/webservice.ws";
        MockWebServiceContainer webServiceInvoker = new MockWebServiceContainer();
        kernel.invoke(containerName, "addWebService", new Object[] {contextPath, webServiceInvoker, null, null, null, null,cl}, new String[] {String.class.getName(), WebServiceContainer.class.getName(), String.class.getName(), String.class.getName(), String.class.getName(), String.class.getName(), ClassLoader.class.getName()});

        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:5678" + contextPath).openConnection();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
            assertEquals("Hello World", reader.readLine());
        } finally {
            connection.disconnect();
        }
        kernel.invoke(containerName, "removeWebService", new Object[] {contextPath}, new String[] {String.class.getName()});
        connection = (HttpURLConnection) new URL("http://localhost:5678" + contextPath).openConnection();
        try {
            connection.getInputStream();
            fail();
        } catch (Exception e) {
            // see if we removed the ws.
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, connection.getResponseCode());
            connection.disconnect();
        }
        stop(connectorName);
    }

    private void start(GBeanData instance) throws Exception {
        kernel.loadGBean(instance, cl);
        kernel.startGBean(instance.getName());
    }

    private void stop(ObjectName name) throws Exception {
        kernel.stopGBean(name);
        kernel.unloadGBean(name);
    }

    protected void setUp() throws Exception {
        containerName = new ObjectName("geronimo.jetty:role=Container");
        containerPatterns = new HashSet();
        containerPatterns.add(containerName);
        connectorName = new ObjectName("geronimo.jetty:role=Connector");
        kernel = KernelFactory.newInstance().createKernel("test.kernel");
        kernel.boot();
        container = new GBeanData(containerName, JettyContainerImpl.GBEAN_INFO);
        start(container);
    }

    protected void tearDown() throws Exception {
        stop(containerName);
        kernel.shutdown();
    }
}
