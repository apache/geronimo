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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Properties;

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
    protected GBeanMBean propertiesLMGBean;
    protected ObjectName propertiesLMName;
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

        securityServiceGBean = new GBeanMBean("org.apache.geronimo.security.SecurityServiceImpl");
        securityServiceName = new ObjectName("geronimo.security:type=SecurityServiceImpl");
        securityServiceGBean.setReferencePatterns("Realms", Collections.singleton(new ObjectName("geronimo.security:type=SecurityRealm,*")));
        securityServiceGBean.setReferencePatterns("Mappers", Collections.singleton(new ObjectName("geronimo.security:type=SecurityRealm,*")));
        securityServiceGBean.setAttribute("policyConfigurationFactory", "org.apache.geronimo.security.jacc.GeronimoPolicyConfigurationFactory");

        loginServiceGBean = new GBeanMBean("org.apache.geronimo.security.jaas.JaasLoginService");
        loginServiceName = new ObjectName("geronimo.security:type=JaasLoginService");
        loginServiceGBean.setReferencePatterns("Realms", Collections.singleton(new ObjectName("geronimo.security:type=SecurityRealm,*")));
//        loginServiceGBean.setAttribute("reclaimPeriod", new Long(1000 * 1000));
        loginServiceGBean.setAttribute("algorithm", "HmacSHA1");
        loginServiceGBean.setAttribute("password", "secret");

        propertiesLMGBean = new GBeanMBean("org.apache.geronimo.security.jaas.LoginModuleGBean");
        propertiesLMName = new ObjectName("geronimo.security:type=LoginModule,name=demo-properties-login");
        propertiesLMGBean.setAttribute("loginModuleClass", "org.apache.geronimo.security.realm.providers.PropertiesFileLoginModule");
        propertiesLMGBean.setAttribute("serverSide", Boolean.TRUE);
        Properties options = new Properties();
        options.setProperty("usersURI", "src/test-resources/data/users.properties");
        options.setProperty("groupsURI", "src/test-resources/data/groups.properties");
        propertiesLMGBean.setAttribute("options", options);
        propertiesLMGBean.setAttribute("loginDomainName", "demo-properties-realm");

        propertiesRealmGBean = new GBeanMBean("org.apache.geronimo.security.realm.GenericSecurityRealm");
        propertiesRealmName = new ObjectName("geronimo.security:type=SecurityRealm,realm=demo-properties-realm");
        propertiesRealmGBean.setReferencePatterns("ServerInfo", Collections.singleton(serverInfoName));
        propertiesRealmGBean.setAttribute("realmName", "demo-properties-realm");
        Properties config = new Properties();
        config.setProperty("LoginModule.1.REQUIRED", propertiesLMName.getCanonicalName());
        propertiesRealmGBean.setAttribute("loginModuleConfiguration", config);
//        propertiesRealmGBean.setAttribute("autoMapPrincipalClasses", "demo-properties-realm=org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal");
        propertiesRealmGBean.setAttribute("defaultPrincipal", "metro=org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal");

        start(serverInfoName, serverInfoGBean);
        start(propertiesLMName, propertiesLMGBean);
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
        stop(propertiesLMName);
        stop(serverInfoName);
        kernel.shutdown();
    }
}
