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

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.jetty.connector.HTTPConnector;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.transaction.GeronimoTransactionManager;
import org.apache.geronimo.transaction.UserTransactionImpl;


/**
 * @version $Revision: 1.3 $ $Date: 2004/07/15 17:57:56 $
 */
public class SecurityTest extends TestCase {

    private Kernel kernel;
    private GBeanMBean container;
    private ObjectName containerName;
    private Set containerPatterns;
    private ObjectName connectorName;
    private MBeanServer mbServer;
    private GBeanMBean connectorGBean;
    private GBeanMBean serverInfoGBean;
    private ObjectName serverInfoName;
    private GBeanMBean jaasRealmGBean;
    private ObjectName jaasRealmName;
    private GBeanMBean propertiesRealmGBean;
    private ObjectName propertiesRealmName;
    private ObjectName loginServiceName;
    private GBeanMBean loginServiceGBean;
    private ObjectName appName;
    private ObjectName tmName;
    private ObjectName tcaName;
    private GBeanMBean tm;
    private GBeanMBean ctc;

    public void testDummy() throws Exception {
    }

    public void XtestApplication() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("deployables/war3/");
        GBeanMBean app = new GBeanMBean(JettyWebAppContext.GBEAN_INFO);
        app.setAttribute("URI", URI.create(url.toString()));
        app.setAttribute("contextPath", "/test");
        app.setAttribute("componentContext", null);
        UserTransactionImpl userTransaction = new UserTransactionImpl();
        app.setAttribute("userTransaction", userTransaction);
        app.setReferencePatterns("Configuration", Collections.EMPTY_SET);
        app.setReferencePatterns("JettyContainer", containerPatterns);
        app.setReferencePatterns("TransactionManager", Collections.singleton(tmName));
        app.setReferencePatterns("TrackedConnectionAssociator", Collections.singleton(tcaName));
        start(appName, app);

        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:5678/test/protected/hello.txt").openConnection();
        connection.setInstanceFollowRedirects(false);
        assertEquals(HttpURLConnection.HTTP_MOVED_TEMP, connection.getResponseCode());

        String cookie = connection.getHeaderField("Set-Cookie");
        String location = connection.getHeaderField("Location");

        cookie = cookie.substring(0, cookie.lastIndexOf(';'));
        location = location.substring(0, location.lastIndexOf('/')) + "/j_security_check?j_username=alan&j_password=starcraft";

        connection = (HttpURLConnection) new URL(location).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Cookie", cookie);
        connection.setInstanceFollowRedirects(false);
        assertEquals(HttpURLConnection.HTTP_MOVED_TEMP, connection.getResponseCode());

        connection = (HttpURLConnection) new URL("http://localhost:5678/test/protected/hello.txt").openConnection();
        connection.setRequestProperty("Cookie", cookie);
        connection.setInstanceFollowRedirects(false);
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
        tcaName = new ObjectName("geronimo.test:role=ConnectionTrackingCoordinator");

        kernel = new Kernel("geronimo.kernel", "test");
        kernel.boot();
        mbServer = kernel.getMBeanServer();
        container = new GBeanMBean(JettyContainerImpl.GBEAN_INFO);

        serverInfoGBean = new GBeanMBean(ServerInfo.GBEAN_INFO);
        serverInfoName = new ObjectName("geronimo.system:role=ServerInfo");
        serverInfoGBean.setAttribute("baseDirectory", ".");

        connectorGBean = new GBeanMBean(HTTPConnector.GBEAN_INFO);
        connectorGBean.setAttribute("port", new Integer(5678));
        connectorGBean.setReferencePatterns("JettyContainer", containerPatterns);

        jaasRealmGBean = new GBeanMBean("org.apache.geronimo.jetty.JAASJettyRealm");
        jaasRealmName = new ObjectName("geronimo.jetty:role=JaasRealm");
        jaasRealmGBean.setReferencePatterns("JettyContainer", containerPatterns);
        jaasRealmGBean.setAttribute("name", "Test JAAS Realm");
        jaasRealmGBean.setAttribute("loginModuleName", "jaasTest");

        loginServiceGBean = new GBeanMBean("org.apache.geronimo.security.jaas.LoginService");
        loginServiceName = new ObjectName("geronimo.security:type=LoginService");
        loginServiceGBean.setReferencePatterns("Realms", Collections.singleton(new ObjectName("geronimo.security:type=SecurityRealm,*")));
        loginServiceGBean.setAttribute("reclaimPeriod", new Long(1000 * 1000));
        loginServiceGBean.setAttribute("algorithm", "HmacSHA1");
        loginServiceGBean.setAttribute("password", "secret");

        propertiesRealmGBean = new GBeanMBean("org.apache.geronimo.security.realm.providers.PropertiesFileSecurityRealm");
        propertiesRealmName = new ObjectName("geronimo.security:type=SecurityRealm,realm=demo-properties-realm");
        propertiesRealmGBean.setReferencePatterns("ServerInfo", Collections.singleton(serverInfoName));
        propertiesRealmGBean.setAttribute("realmName", "demo-properties-realm");
        propertiesRealmGBean.setAttribute("maxLoginModuleAge", new Long(1 * 1000));
        propertiesRealmGBean.setAttribute("usersURI", (new File(new File("."), "src/test-resources/data/users.properties")).toURI());
        propertiesRealmGBean.setAttribute("groupsURI", (new File(new File("."), "src/test-resources/data/groups.properties")).toURI());

        start(serverInfoName, serverInfoGBean);
        start(propertiesRealmName, propertiesRealmGBean);
        start(containerName, container);
        start(loginServiceName, loginServiceGBean);
        start(jaasRealmName, jaasRealmGBean);
        start(connectorName, connectorGBean);

        tm = new GBeanMBean(GeronimoTransactionManager.GBEAN_INFO);
        Set patterns = new HashSet();
        patterns.add(ObjectName.getInstance("geronimo.management:J2eeType=ManagedConnectionFactory,*"));
        tm.setReferencePatterns("ResourceManagers", patterns);
        start(tmName, tm);
        ctc = new GBeanMBean(ConnectionTrackingCoordinator.GBEAN_INFO);
        start(tcaName, ctc);
    }

    protected void tearDown() throws Exception {
        stop(tcaName);
        stop(tmName);
        stop(connectorName);
        stop(jaasRealmName);
        stop(loginServiceName);
        stop(containerName);
        stop(propertiesRealmName);
        stop(serverInfoName);
        kernel.shutdown();
    }
}
