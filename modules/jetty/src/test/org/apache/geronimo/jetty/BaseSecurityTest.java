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

import javax.management.ObjectName;
import java.io.File;
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
import org.apache.geronimo.transaction.context.TransactionContextManager;


/**
 * @version $Rev: 57351 $ $Date: 2004-11-10 14:39:50 -0500 (Wed, 10 Nov 2004) $
 */
public class BaseSecurityTest extends TestCase {

    protected Kernel kernel;
    protected GBeanMBean container;
    protected ObjectName containerName;
    protected Set containerPatterns;
    protected ObjectName connectorName;
    protected GBeanMBean connectorGBean;
    protected GBeanMBean serverInfoGBean;
    protected ObjectName serverInfoName;
    protected GBeanMBean jaasRealmGBean;
    protected ObjectName jaasRealmName;
    protected GBeanMBean propertiesRealmGBean;
    protected ObjectName propertiesRealmName;
    protected ObjectName loginServiceName;
    protected GBeanMBean loginServiceGBean;
    protected ObjectName securityServiceName;
    protected GBeanMBean securityServiceGBean;
    protected ObjectName appName;
    protected ObjectName tmName;
    protected ObjectName tcaName;
    protected GBeanMBean tm;
    protected GBeanMBean ctc;
    protected ObjectName tcmName;
    protected GBeanMBean tcm;

    public void testDummy() throws Exception {
    }

    protected void start(ObjectName name, GBeanMBean instance) throws Exception {
        kernel.loadGBean(name, instance);
        kernel.startGBean(name);
    }

    protected void stop(ObjectName name) throws Exception {
        kernel.stopGBean(name);
        kernel.unloadGBean(name);
    }

    protected void setUp() throws Exception {
        containerName = new ObjectName("geronimo.jetty:role=Container");
        containerPatterns = Collections.singleton(containerName);
        connectorName = new ObjectName("geronimo.jetty:role=Connector");
        appName = new ObjectName("geronimo.jetty:app=test");

        tmName = new ObjectName("geronimo.test:role=TransactionManager");
        tcmName = new ObjectName("geronimo.test:role=TransactionContextManager");
        tcaName = new ObjectName("geronimo.test:role=ConnectionTrackingCoordinator");

        kernel = new Kernel("geronimo.kernel");
        kernel.boot();

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

        securityServiceGBean = new GBeanMBean("org.apache.geronimo.security.SecurityService");
        securityServiceName = new ObjectName("geronimo.security:type=SecurityService");
        securityServiceGBean.setReferencePatterns("Realms", Collections.singleton(new ObjectName("geronimo.security:type=SecurityRealm,*")));
        securityServiceGBean.setAttribute("policyConfigurationFactory", "org.apache.geronimo.security.jacc.GeronimoPolicyConfigurationFactory");

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
        propertiesRealmGBean.setAttribute("defaultPrincipal", "metro");
        propertiesRealmGBean.setAttribute("maxLoginModuleAge", new Long(1 * 1000));
        propertiesRealmGBean.setAttribute("usersURI", (new File(new File("."), "src/test-resources/data/users.properties")).toURI());
        propertiesRealmGBean.setAttribute("groupsURI", (new File(new File("."), "src/test-resources/data/groups.properties")).toURI());

        start(serverInfoName, serverInfoGBean);
        start(propertiesRealmName, propertiesRealmGBean);
        start(containerName, container);
        start(securityServiceName, securityServiceGBean);
        start(loginServiceName, loginServiceGBean);
        start(jaasRealmName, jaasRealmGBean);
        start(connectorName, connectorGBean);

        tm = new GBeanMBean(GeronimoTransactionManager.GBEAN_INFO);
        tm.setAttribute("defaultTransactionTimeoutSeconds", new Integer(10));
        Set patterns = new HashSet();
        patterns.add(ObjectName.getInstance("geronimo.server:j2eeType=JCAManagedConnectionFactory,*"));
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
        stop(tcmName);
        stop(tmName);
        stop(connectorName);
        stop(jaasRealmName);
        stop(loginServiceName);
        stop(securityServiceName);
        stop(containerName);
        stop(propertiesRealmName);
        stop(serverInfoName);
        kernel.shutdown();
    }
}
