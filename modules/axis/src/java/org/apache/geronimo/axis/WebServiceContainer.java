/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.geronimo.axis;

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.jetty.JettyContainerImpl;
import org.apache.geronimo.jetty.JettyWebAppContext;
import org.apache.geronimo.jetty.connector.HTTPConnector;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.transaction.GeronimoTransactionManager;
import org.apache.geronimo.transaction.UserTransactionImpl;
import org.apache.geronimo.transaction.context.TransactionContextManager;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class WebServiceContainer {
    private final ObjectName axisGBeanName;
    private final Kernel kernel;
    private ObjectName containerName;
    private MBeanServer mbServer;
    private Set containerPatterns;
    private ObjectName tmName;
    private ObjectName tcmName;
    private ObjectName tcaName;
    private ObjectName appName;
    private ObjectName connectorName;
    private String webappsUrl;
    private final boolean startJetty = true;
    private GBeanMBean tcm;

    public WebServiceContainer(Kernel kernel, ObjectName axisGBeanName) {
        try {
            this.axisGBeanName = axisGBeanName;
            containerName = new ObjectName("geronimo.jetty:role=Container");
            this.kernel = kernel;
            mbServer = kernel.getMBeanServer();
            containerPatterns = Collections.singleton(containerName);
            connectorName = new ObjectName("geronimo.jetty:role=Connector");
            appName = new ObjectName("geronimo.jetty:app=test");
            tmName = new ObjectName("geronimo.test:role=TransactionManager");
            tcmName = new ObjectName("geronimo.test:role=TransactionContextManager");
            tcaName =
                    new ObjectName(
                            "geronimo.test:role=ConnectionTrackingCoordinator");
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void init() {
    }

    public void doStart() throws Exception {
        if (startJetty)
            startJetty();
        URL url = Thread.currentThread().getContextClassLoader().getResource(
                "deployables/axis/");
        GBeanMBean app = new GBeanMBean(JettyWebAppContext.GBEAN_INFO);
        app.setAttribute("uri", URI.create(url.toString()));
        app.setAttribute("contextPath", "/axis");
        app.setAttribute("componentContext", null);
        UserTransactionImpl userTransaction = new UserTransactionImpl();
        app.setAttribute("userTransaction", userTransaction);
        app.setReferencePatterns("Configuration", Collections.EMPTY_SET);
        app.setReferencePatterns("JettyContainer", containerPatterns);
        app.setReferencePatterns("TransactionContextManager",
                Collections.singleton(tcmName));
        app.setReferencePatterns("TrackedConnectionAssociator",
                Collections.singleton(tcaName));
        start(appName, app);
    }

    public void doStop() throws Exception {
        stop(tcaName);
        stop(tcmName);
        stop(connectorName);
        stop(containerName);
    }

    private void startJetty() throws Exception {
        GBeanMBean connector;
        GBeanMBean tm;
        GBeanMBean ctc;
        GBeanMBean container;
        container = new GBeanMBean(JettyContainerImpl.GBEAN_INFO);
        connector = new GBeanMBean(HTTPConnector.GBEAN_INFO);
        connector.setAttribute("port", new Integer(5678));
        connector.setReferencePatterns("JettyContainer", containerPatterns);
        start(containerName, container);
        start(connectorName, connector);
        tm = new GBeanMBean(GeronimoTransactionManager.GBEAN_INFO);
        Set patterns = new HashSet();
        patterns.add(
                ObjectName.getInstance(
                        "geronimo.server:j2eeType=JCAManagedConnectionFactory,*"));
        tm.setReferencePatterns("ResourceManagers", patterns);
        start(tmName, tm);
        tcm = new GBeanMBean(TransactionContextManager.GBEAN_INFO);
        tcm.setReferencePattern("TransactionManager", tmName);
        start(tcmName, tcm);
        ctc = new GBeanMBean(ConnectionTrackingCoordinator.GBEAN_INFO);
        start(tcaName, ctc);
    }

    private void start(ObjectName name, Object instance) throws Exception {
        mbServer.registerMBean(instance, name);
        mbServer.invoke(name, "start", null, null);
    }

    private void stop(ObjectName name) throws Exception {
        mbServer.invoke(name, "stop", null, null);
        mbServer.unregisterMBean(name);
    }
}
