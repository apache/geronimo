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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.jetty.connector.HTTPConnector;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.transaction.GeronimoTransactionManager;
import org.apache.geronimo.transaction.OnlineUserTransaction;
import org.apache.geronimo.transaction.context.TransactionContextManager;

/**
 * @version $Rev$ $Date$
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
    private ObjectName tmName;
    private ObjectName tcaName;
    private GBeanMBean tm;
    private GBeanMBean ctc;
    private ObjectName tcmName;
    private GBeanMBean tcm;

    public void testDummy() throws Exception {
    }

    public void testApplication() throws Exception {
        GBeanMBean app = new GBeanMBean(JettyWebAppContext.GBEAN_INFO);
        app.setAttribute("uri", URI.create("war1/"));
        app.setAttribute("componentContext", null);
        OnlineUserTransaction userTransaction = new OnlineUserTransaction();
        app.setAttribute("userTransaction", userTransaction);
        app.setAttribute("webClassPath", new URI[0]);
        app.setAttribute("contextPriorityClassLoader", Boolean.FALSE);
        app.setAttribute("configurationBaseUrl", Thread.currentThread().getContextClassLoader().getResource("deployables/"));
        app.setReferencePattern("TransactionContextManager", tcmName);
        app.setReferencePattern("TrackedConnectionAssociator", tcaName);
        app.setReferencePatterns("JettyContainer", containerPatterns);

        app.setAttribute("contextPath", "/test");

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

        tmName = new ObjectName("geronimo.test:role=TransactionManager");
        tcmName = new ObjectName("geronimo.test:role=TransactionContextManager");
        tcaName = new ObjectName("geronimo.test:role=ConnectionTrackingCoordinator");

        kernel = new Kernel("test.kernel", "test");
        kernel.boot();
        mbServer = kernel.getMBeanServer();
        container = new GBeanMBean(JettyContainerImpl.GBEAN_INFO);

        connector = new GBeanMBean(HTTPConnector.GBEAN_INFO);
        connector.setAttribute("port", new Integer(5678));
        connector.setReferencePatterns("JettyContainer", containerPatterns);

        start(containerName, container);
        start(connectorName, connector);

        tm = new GBeanMBean(GeronimoTransactionManager.GBEAN_INFO);
        Set patterns = new HashSet();
        patterns.add(ObjectName.getInstance("geronimo.server:j2eeType=JCAManagedConnectionFactory,*"));
        tm.setAttribute("defaultTransactionTimeoutSeconds", new Integer(10));
        tm.setReferencePatterns("ResourceManagers", patterns);
        start(tmName, tm);
        tcm = new GBeanMBean(TransactionContextManager.GBEAN_INFO);
        tcm.setReferencePattern("TransactionManager", tmName);
        start(tcmName, tcm);
        ctc = new GBeanMBean(ConnectionTrackingCoordinator.GBEAN_INFO);
        start(tcaName, ctc);
    }

    protected void tearDown() throws Exception {
        stop(tcaName);
        stop(tmName);
        stop(connectorName);
        stop(containerName);
        kernel.shutdown();
    }
}
