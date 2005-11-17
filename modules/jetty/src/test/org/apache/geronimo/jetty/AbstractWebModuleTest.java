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

import java.io.File;
import java.net.URI;
import java.security.PermissionCollection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.management.ObjectName;

import junit.framework.TestCase;
import org.mortbay.jetty.servlet.FormAuthenticator;

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinatorGBean;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.jetty.connector.HTTPConnector;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.security.SecurityServiceImpl;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.security.deploy.PrincipalInfo;
import org.apache.geronimo.security.jaas.GeronimoLoginConfiguration;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.security.jaas.LoginModuleGBean;
import org.apache.geronimo.security.jaas.server.JaasLoginService;
import org.apache.geronimo.security.jacc.ApplicationPolicyConfigurationManager;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.security.realm.GenericSecurityRealm;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;
import org.apache.geronimo.transaction.context.OnlineUserTransaction;
import org.apache.geronimo.transaction.context.TransactionContextManagerGBean;
import org.apache.geronimo.transaction.manager.TransactionManagerImplGBean;


/**
 * @version $Rev$ $Date$
 */
public class AbstractWebModuleTest extends TestCase {
    protected Kernel kernel;
    private GBeanData container;
    private ObjectName containerName;
    private ObjectName connectorName;
    private GBeanData connector;
    private ObjectName webModuleName;
    private ObjectName tmName;
    private ObjectName ctcName;
    private GBeanData tm;
    private GBeanData ctc;
    private ObjectName tcmName;
    private GBeanData tcm;
    private ClassLoader cl;
    private J2eeContext moduleContext = new J2eeContextImpl("jetty.test", "test", "null", NameFactory.WEB_MODULE, "jettyTest", null, null);
    private GBeanData loginConfigurationGBean;
    protected ObjectName loginConfigurationName;
    private GBeanData securityServiceGBean;
    protected ObjectName securityServiceName;
    private ObjectName loginServiceName;
    private GBeanData loginServiceGBean;
    protected GBeanData propertiesLMGBean;
    protected ObjectName propertiesLMName;
    protected ObjectName propertiesRealmName;
    private GBeanData propertiesRealmGBean;
    private ObjectName serverInfoName;
    private GBeanData serverInfoGBean;
    protected final static String securityRealmName = "demo-properties-realm";

    public void testDummy() throws Exception {
    }

    protected void setUpStaticContentServlet() throws Exception {
        GBeanData staticContentServletGBeanData = new GBeanData(JettyServletHolder.GBEAN_INFO);
        staticContentServletGBeanData.setAttribute("servletName", "default");
        staticContentServletGBeanData.setAttribute("servletClass", "org.mortbay.jetty.servlet.Default");
        Map staticContentServletInitParams = new HashMap();
        staticContentServletInitParams.put("acceptRanges", "true");
        staticContentServletInitParams.put("dirAllowed", "true");
        staticContentServletInitParams.put("putAllowed", "false");
        staticContentServletInitParams.put("delAllowed", "false");
        staticContentServletInitParams.put("redirectWelcome", "false");
        staticContentServletInitParams.put("minGzipLength", "8192");
        staticContentServletGBeanData.setAttribute("initParams", staticContentServletInitParams);
        staticContentServletGBeanData.setAttribute("loadOnStartup", new Integer(0));
        staticContentServletGBeanData.setAttribute("servletMappings", Collections.singleton(new String("/")));
        ObjectName staticContentServletObjectName = NameFactory.getComponentName(null, null, null, NameFactory.WEB_MODULE, null, (String) staticContentServletGBeanData.getAttribute("servletName"), NameFactory.SERVLET, moduleContext);
        staticContentServletGBeanData.setName(staticContentServletObjectName);
        staticContentServletGBeanData.setReferencePattern("JettyServletRegistration", webModuleName);

        start(staticContentServletGBeanData);
    }

    protected void setUpInsecureAppContext() throws Exception {
        GBeanData app = new GBeanData(webModuleName, JettyWebAppContext.GBEAN_INFO);
        app.setAttribute("uri", URI.create("war1/"));
        app.setAttribute("componentContext", Collections.EMPTY_MAP);
        OnlineUserTransaction userTransaction = new OnlineUserTransaction();
        app.setAttribute("userTransaction", userTransaction);
        //we have no classes or libs.
        app.setAttribute("webClassPath", new URI[]{});
        app.setAttribute("contextPriorityClassLoader", Boolean.FALSE);
        app.setAttribute("configurationBaseUrl", new File("src/test-resources/deployables/").toURL());
        app.setReferencePattern("TransactionContextManager", tcmName);
        app.setReferencePattern("TrackedConnectionAssociator", ctcName);
        app.setReferencePattern("JettyContainer", containerName);

        app.setAttribute("contextPath", "/test");

        start(app);
    }

    protected void setUpSecureAppContext(Map roleDesignates, Map principalRoleMap, ComponentPermissions componentPermissions, DefaultPrincipal defaultPrincipal, PermissionCollection checked, Set securityRoles) throws Exception {
        ObjectName jaccBeanName = NameFactory.getComponentName(null, null, null, null, "foo", NameFactory.JACC_MANAGER, moduleContext);
        GBeanData jaccBeanData = new GBeanData(jaccBeanName, ApplicationPolicyConfigurationManager.GBEAN_INFO);
        Map contextIDToPermissionsMap = new HashMap();
        contextIDToPermissionsMap.put("TEST", componentPermissions);
        jaccBeanData.setAttribute("contextIdToPermissionsMap", contextIDToPermissionsMap);
        jaccBeanData.setAttribute("principalRoleMap", principalRoleMap);
        jaccBeanData.setAttribute("roleDesignates", roleDesignates);
        start(jaccBeanData);

        GBeanData app = new GBeanData(webModuleName, JettyWebAppContext.GBEAN_INFO);
        app.setAttribute("securityRealmName", "demo-properties-realm");
        app.setAttribute("defaultPrincipal", defaultPrincipal);
        app.setAttribute("checkedPermissions", checked);
        app.setAttribute("excludedPermissions", componentPermissions.getExcludedPermissions());
        app.setReferencePattern("RoleDesignateSource", jaccBeanName);

        FormAuthenticator formAuthenticator = new FormAuthenticator();
        formAuthenticator.setLoginPage("/auth/logon.html?param=test");
        formAuthenticator.setErrorPage("/auth/logonError.html?param=test");
        app.setAttribute("realmName", "Test JAAS Realm");
        app.setAttribute("authenticator", formAuthenticator);
        app.setAttribute("policyContextID", "TEST");
        app.setAttribute("uri", URI.create("war3/"));
        app.setAttribute("componentContext", Collections.EMPTY_MAP);

        OnlineUserTransaction userTransaction = new OnlineUserTransaction();
        app.setAttribute("userTransaction", userTransaction);
        //we have no classes or libs.
        app.setAttribute("webClassPath", new URI[]{});
        app.setAttribute("contextPriorityClassLoader", Boolean.FALSE);
        app.setAttribute("configurationBaseUrl", new File("src/test-resources/deployables/").toURL());
        app.setReferencePattern("TransactionContextManager", tcmName);
        app.setReferencePattern("TrackedConnectionAssociator", ctcName);
        app.setReferencePattern("JettyContainer", containerName);

        app.setAttribute("contextPath", "/test");

        start(app);
    }

    protected void setUpSecurity() throws Exception {

        loginConfigurationName = new ObjectName("geronimo.security:type=LoginConfiguration");
        loginConfigurationGBean = new GBeanData(loginConfigurationName, GeronimoLoginConfiguration.getGBeanInfo());
        Set configurations = new HashSet();
        configurations.add(new ObjectName("geronimo.server:j2eeType=SecurityRealm,*"));
        configurations.add(new ObjectName("geronimo.server:j2eeType=ConfigurationEntry,*"));
        loginConfigurationGBean.setReferencePatterns("Configurations", configurations);

        serverInfoName = new ObjectName("geronimo.system:name=ServerInfo");
        serverInfoGBean = new GBeanData(serverInfoName, BasicServerInfo.GBEAN_INFO);
        serverInfoGBean.setAttribute("baseDirectory", ".");

        securityServiceName = new ObjectName("geronimo.server:j2eeType=SecurityService");
        securityServiceGBean = new GBeanData(securityServiceName, SecurityServiceImpl.GBEAN_INFO);
        securityServiceGBean.setReferencePattern("ServerInfo", serverInfoName);
        securityServiceGBean.setAttribute("policyConfigurationFactory", "org.apache.geronimo.security.jacc.GeronimoPolicyConfigurationFactory");
        securityServiceGBean.setAttribute("policyProvider", "org.apache.geronimo.security.jacc.GeronimoPolicy");

        loginServiceName = new ObjectName("test:name=TestLoginService");
        loginServiceGBean = new GBeanData(loginServiceName, JaasLoginService.GBEAN_INFO);
        loginServiceGBean.setReferencePattern("Realms", new ObjectName("geronimo.server:j2eeType=SecurityRealm,*"));
//        loginServiceGBean.setAttribute("reclaimPeriod", new Long(1000 * 1000));
        loginServiceGBean.setAttribute("algorithm", "HmacSHA1");
        loginServiceGBean.setAttribute("password", "secret");

        propertiesLMName = new ObjectName("geronimo.security:type=LoginModule,name=demo-properties-login");
        propertiesLMGBean = new GBeanData(propertiesLMName, LoginModuleGBean.GBEAN_INFO);
        propertiesLMGBean.setAttribute("loginModuleClass", "org.apache.geronimo.security.realm.providers.PropertiesFileLoginModule");
        propertiesLMGBean.setAttribute("serverSide", Boolean.TRUE);
        Properties options = new Properties();
        options.setProperty("usersURI", "src/test-resources/data/users.properties");
        options.setProperty("groupsURI", "src/test-resources/data/groups.properties");
        propertiesLMGBean.setAttribute("options", options);
        propertiesLMGBean.setAttribute("wrapPrincipals", Boolean.TRUE);
        //TODO should this be called securityRealmName?
        propertiesLMGBean.setAttribute("loginDomainName", "demo-properties-realm");

        ObjectName testUseName = new ObjectName("geronimo.security:type=LoginModuleUse,name=properties");
        GBeanData lmUseGBean = new GBeanData(testUseName, JaasLoginModuleUse.getGBeanInfo());
        lmUseGBean.setAttribute("controlFlag", "REQUIRED");
        lmUseGBean.setReferencePattern("LoginModule", propertiesLMName);

        propertiesRealmName = new ObjectName("geronimo.server:j2eeType=SecurityRealm,name=demo-properties-realm");
        propertiesRealmGBean = new GBeanData(propertiesRealmName, GenericSecurityRealm.GBEAN_INFO);
        propertiesRealmGBean.setReferencePattern("ServerInfo", serverInfoName);
        propertiesRealmGBean.setAttribute("realmName", "demo-properties-realm");
        propertiesRealmGBean.setReferencePattern("LoginService", loginServiceName);
//        Properties config = new Properties();
//        config.setProperty("LoginModule.1.REQUIRED", propertiesLMName.getCanonicalName());
//        propertiesRealmGBean.setAttribute("loginModuleConfiguration", config);
        propertiesRealmGBean.setReferencePattern("LoginModuleConfiguration", testUseName);
        PrincipalInfo.PrincipalEditor principalEditor = new PrincipalInfo.PrincipalEditor();
        principalEditor.setAsText("metro,org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal,false");
        propertiesRealmGBean.setAttribute("defaultPrincipal", principalEditor.getValue());

        start(loginConfigurationGBean);
        start(serverInfoGBean);
        start(securityServiceGBean);
        start(loginServiceGBean);
        start(propertiesLMGBean);
        start(lmUseGBean);
        start(propertiesRealmGBean);

    }

    protected void tearDownSecurity() throws Exception {
        stop(propertiesRealmName);
        stop(propertiesLMName);
        stop(loginServiceName);
        stop(securityServiceName);
        stop(serverInfoName);
        stop(loginConfigurationName);
    }

    private void start(GBeanData gbeanData) throws Exception {
        kernel.loadGBean(gbeanData, cl);
        kernel.startGBean(gbeanData.getName());
        if (kernel.getGBeanState(gbeanData.getName()) != State.RUNNING_INDEX) {
            fail("gbean not started: " + gbeanData.getName());
        }
    }

    private void stop(ObjectName name) throws Exception {
        kernel.stopGBean(name);
        kernel.unloadGBean(name);
    }

    protected void setUp() throws Exception {
        cl = this.getClass().getClassLoader();
        containerName = NameFactory.getWebComponentName(null, null, null, null, "jettyContainer", "WebResource", moduleContext);
        connectorName = NameFactory.getWebComponentName(null, null, null, null, "jettyConnector", "WebResource", moduleContext);
        webModuleName = NameFactory.getModuleName(null, null, null, null, "testModule", moduleContext);

        tmName = NameFactory.getComponentName(null, null, null, null, null, "TransactionManager", NameFactory.TRANSACTION_MANAGER, moduleContext);
        tcmName = NameFactory.getComponentName(null, null, null, null, null, "TransactionContextManager", NameFactory.TRANSACTION_CONTEXT_MANAGER, moduleContext);
        ctcName = new ObjectName("geronimo.test:role=ConnectionTrackingCoordinator");

        kernel = KernelFactory.newInstance().createKernel("test.kernel");
        kernel.boot();
        container = new GBeanData(containerName, JettyContainerImpl.GBEAN_INFO);

        connector = new GBeanData(connectorName, HTTPConnector.GBEAN_INFO);
        connector.setAttribute("port", new Integer(5678));
        connector.setAttribute("maxThreads", new Integer(50));
        connector.setAttribute("minThreads", new Integer(10));
        connector.setReferencePattern("JettyContainer", containerName);

        start(container);
        start(connector);

        tm = new GBeanData(tmName, TransactionManagerImplGBean.GBEAN_INFO);
        Set patterns = new HashSet();
        patterns.add(ObjectName.getInstance("geronimo.server:j2eeType=JCAManagedConnectionFactory,*"));
        tm.setAttribute("defaultTransactionTimeoutSeconds", new Integer(10));
        tm.setReferencePatterns("ResourceManagers", patterns);
        start(tm);
        tcm = new GBeanData(tcmName, TransactionContextManagerGBean.GBEAN_INFO);
        tcm.setReferencePattern("TransactionManager", tmName);
        start(tcm);
        ctc = new GBeanData(ctcName, ConnectionTrackingCoordinatorGBean.GBEAN_INFO);
        start(ctc);
    }

    protected void tearDown() throws Exception {
        stop(ctcName);
        stop(tmName);
        stop(connectorName);
        stop(containerName);
        kernel.shutdown();
    }
}
