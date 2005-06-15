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
package org.apache.geronimo.tomcat;

import java.io.File;
import java.net.URI;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.management.ObjectName;

import junit.framework.TestCase;

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.security.SecurityServiceImpl;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.security.deploy.Principal;
import org.apache.geronimo.security.jaas.GeronimoLoginConfiguration;
import org.apache.geronimo.security.jaas.JaasLoginService;
import org.apache.geronimo.security.jaas.LoginModuleGBean;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.security.jacc.ApplicationPolicyConfigurationManager;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.security.realm.GenericSecurityRealm;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.tomcat.util.SecurityHolder;
import org.apache.geronimo.transaction.context.OnlineUserTransaction;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;


/**
 * @version $Rev: 111239 $ $Date: 2004-12-08 02:29:11 -0700 (Wed, 08 Dec 2004) $
 */
public class AbstractWebModuleTest extends TestCase {

    protected static final String POLICY_CONTEXT_ID = "securetest";


    protected Kernel kernel;
    private GBeanData container;
    private ObjectName containerName;
    private ObjectName connectorName;
    private GBeanData connector;
    private ObjectName engineName;
    private GBeanData engine;
    private ObjectName hostName;
    private GBeanData host;
    private ObjectName realmName;
    private GBeanData realm;
    private ObjectName webModuleName;
    private ObjectName tmName;
    private ObjectName ctcName;
    private GBeanData tm;
    private GBeanData ctc;
    private ObjectName tcmName;
    private GBeanData tcm;
    private ClassLoader cl;
    private J2eeContext moduleContext = new J2eeContextImpl("tomcat.test", "test", "null", NameFactory.WEB_MODULE, "tomcatTest", null, null);
    private GBeanData securityServiceGBean;
    protected ObjectName securityServiceName;
    private ObjectName loginServiceName;
    private GBeanData loginServiceGBean;
    private GBeanData loginConfigurationGBean;
    protected ObjectName loginConfigurationName;
    protected GBeanData propertiesLMGBean;
    protected ObjectName propertiesLMName;
    protected ObjectName propertiesRealmName;
    private GBeanData propertiesRealmGBean;
    private ObjectName serverInfoName;
    private GBeanData serverInfoGBean;

    protected void setUpInsecureAppContext() throws Exception {

        GBeanData app = new GBeanData(webModuleName, TomcatWebAppContext.GBEAN_INFO);
        app.setAttribute("webAppRoot", new File("target/var/catalina/webapps/war1/").toURI());
        app.setAttribute("webClassPath", new URI[]{});
        app.setAttribute("configurationBaseUrl", new File("target/var/catalina/webapps/war1/WEB-INF/web.xml").toURL());
        app.setAttribute("componentContext", Collections.EMPTY_MAP);
        app.setReferencePattern("Container", containerName);
        OnlineUserTransaction userTransaction = new OnlineUserTransaction();
        app.setAttribute("userTransaction", userTransaction);
        app.setReferencePattern("transactionContextManager", tcmName);
        app.setReferencePattern("trackedConnectionAssociator", ctcName);
        app.setAttribute("path", "/test");

        start(app);
    }

    protected ObjectName setUpJAASSecureAppContext() throws Exception {
        ObjectName jaccBeanName = NameFactory.getComponentName(null, null, null, null, "foo", NameFactory.JACC_MANAGER, moduleContext);
        GBeanData jaccBeanData = new GBeanData(jaccBeanName, ApplicationPolicyConfigurationManager.GBEAN_INFO);
        PermissionCollection excludedPermissions= new Permissions();
        PermissionCollection uncheckedPermissions= new Permissions();
        ComponentPermissions componentPermissions = new ComponentPermissions(excludedPermissions, uncheckedPermissions, new HashMap());
        Map contextIDToPermissionsMap = new HashMap();
        contextIDToPermissionsMap.put(POLICY_CONTEXT_ID, componentPermissions);
        jaccBeanData.setAttribute("contextIdToPermissionsMap", contextIDToPermissionsMap);
        jaccBeanData.setAttribute("principalRoleMap", new HashMap());
        jaccBeanData.setAttribute("roleDesignates", new HashMap());
        start(jaccBeanData);

        GBeanData app = new GBeanData(webModuleName, TomcatWebAppContext.GBEAN_INFO);
        app.setAttribute("webAppRoot", new File("target/var/catalina/webapps/war3/").toURI());
        app.setAttribute("webClassPath", new URI[]{});
        app.setAttribute("configurationBaseUrl", new File("target/var/catalina/webapps/war3/WEB-INF/web.xml").toURL());
        app.setAttribute("path", "/securetest");
        app.setReferencePattern("RoleDesignateSource", jaccBeanName);

        OnlineUserTransaction userTransaction = new OnlineUserTransaction();
        app.setAttribute("userTransaction", userTransaction);
        app.setReferencePattern("transactionContextManager", tcmName);
        app.setReferencePattern("trackedConnectionAssociator", ctcName);

        app.setAttribute("componentContext", Collections.EMPTY_MAP);
        app.setReferencePattern("Container", containerName);
        app.setAttribute("kernel", null);

        start(app);

        return webModuleName;
    }

    protected ObjectName setUpSecureAppContext(Map roleDesignates,
                                               Map principalRoleMap,
                                               ComponentPermissions componentPermissions,
                                               DefaultPrincipal defaultPrincipal,
                                               PermissionCollection checked)
            throws Exception {

        ObjectName jaccBeanName = NameFactory.getComponentName(null, null, null, null, "foo", NameFactory.JACC_MANAGER, moduleContext);
        GBeanData jaccBeanData = new GBeanData(jaccBeanName, ApplicationPolicyConfigurationManager.GBEAN_INFO);
        Map contextIDToPermissionsMap = new HashMap();
        contextIDToPermissionsMap.put(POLICY_CONTEXT_ID, componentPermissions);
        jaccBeanData.setAttribute("contextIdToPermissionsMap", contextIDToPermissionsMap);
        jaccBeanData.setAttribute("principalRoleMap", principalRoleMap);
        jaccBeanData.setAttribute("roleDesignates", roleDesignates);
        start(jaccBeanData);

        SecurityHolder securityHolder = new SecurityHolder();
        securityHolder.setChecked(checked);
        securityHolder.setExcluded(componentPermissions.getExcludedPermissions());
        securityHolder.setPolicyContextID(POLICY_CONTEXT_ID);
        securityHolder.setDefaultPrincipal(defaultPrincipal);
        GBeanData app = new GBeanData(webModuleName, TomcatWebAppContext.GBEAN_INFO);
        app.setAttribute("classLoader", cl);
        app.setAttribute("webAppRoot", new File("target/var/catalina/webapps/war3/").toURI());
        app.setAttribute("webClassPath", new URI[]{});
        app.setAttribute("contextPriorityClassLoader", Boolean.FALSE);
        app.setAttribute("securityHolder", securityHolder);
        app.setAttribute("configurationBaseUrl", new File("target/var/catalina/webapps/war3/WEB-INF/web.xml").toURL());
        app.setAttribute("path", "/securetest");
        app.setReferencePattern("RoleDesignateSource", jaccBeanName);

        OnlineUserTransaction userTransaction = new OnlineUserTransaction();
        app.setAttribute("userTransaction", userTransaction);
        app.setReferencePattern("transactionContextManager", tcmName);
        app.setReferencePattern("trackedConnectionAssociator", ctcName);

        app.setAttribute("componentContext", Collections.EMPTY_MAP);
        app.setReferencePattern("Container", containerName);
        start(app);

        return webModuleName;
    }

    protected void setUpSecurity() throws Exception {

        loginConfigurationName = new ObjectName("geronimo.security:type=LoginConfiguration");
        loginConfigurationGBean = new GBeanData(loginConfigurationName, GeronimoLoginConfiguration.getGBeanInfo());
        Set configurations = new HashSet();
        configurations.add(new ObjectName("geronimo.server:j2eeType=SecurityRealm,*"));
        configurations.add(new ObjectName("geronimo.server:j2eeType=ConfigurationEntry,*"));
        loginConfigurationGBean.setReferencePatterns("Configurations", configurations);

        securityServiceName = new ObjectName("geronimo.server:j2eeType=SecurityService");
        securityServiceGBean = new GBeanData(securityServiceName, SecurityServiceImpl.GBEAN_INFO);
        securityServiceGBean.setReferencePattern("ServerInfo", serverInfoName);
        securityServiceGBean.setAttribute("policyConfigurationFactory", "org.apache.geronimo.security.jacc.GeronimoPolicyConfigurationFactory");
        securityServiceGBean.setAttribute("policyProvider", "org.apache.geronimo.security.jacc.GeronimoPolicy");

        loginServiceName = JaasLoginService.OBJECT_NAME;
        loginServiceGBean = new GBeanData(loginServiceName, JaasLoginService.GBEAN_INFO);
        loginServiceGBean.setReferencePattern("Realms", new ObjectName("geronimo.server:j2eeType=SecurityRealm,*"));
        loginServiceGBean.setAttribute("algorithm", "HmacSHA1");
        loginServiceGBean.setAttribute("password", "secret");

        propertiesLMName = new ObjectName("geronimo.security:type=LoginModule,name=Geronimo");
        propertiesLMGBean = new GBeanData(propertiesLMName, LoginModuleGBean.GBEAN_INFO);
        propertiesLMGBean.setAttribute("loginModuleClass", "org.apache.geronimo.security.realm.providers.PropertiesFileLoginModule");
        propertiesLMGBean.setAttribute("serverSide", Boolean.TRUE);
        Properties options = new Properties();
        options.setProperty("usersURI", "src/test-resources/data/users.properties");
        options.setProperty("groupsURI", "src/test-resources/data/groups.properties");
        propertiesLMGBean.setAttribute("options", options);
        propertiesLMGBean.setAttribute("loginDomainName", "geronimo-properties-realm");

        ObjectName testUseName = new ObjectName("geronimo.security:type=LoginModuleUse,name=properties");
        GBeanData lmUseGBean = new GBeanData(testUseName, JaasLoginModuleUse.getGBeanInfo());
        lmUseGBean.setAttribute("controlFlag", "REQUIRED");
        lmUseGBean.setReferencePattern("LoginModule", propertiesLMName);

        propertiesRealmName = new ObjectName("geronimo.server:j2eeType=SecurityRealm,name=geronimo-properties-realm");
        propertiesRealmGBean = new GBeanData(propertiesRealmName, GenericSecurityRealm.GBEAN_INFO);
        propertiesRealmGBean.setReferencePattern("ServerInfo", serverInfoName);
        propertiesRealmGBean.setAttribute("realmName", "Geronimo");
        propertiesRealmGBean.setReferencePattern("LoginModuleConfiguration", testUseName);
        Principal.PrincipalEditor principalEditor = new Principal.PrincipalEditor();
        principalEditor.setAsText("metro=org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal");
        propertiesRealmGBean.setAttribute("defaultPrincipal", principalEditor.getValue());

        start(loginConfigurationGBean);
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
        stop(loginConfigurationName);
    }

    private void start(GBeanData gbeanData) throws Exception {
        kernel.loadGBean(gbeanData, cl);
        kernel.startGBean(gbeanData.getName());
        if (kernel.getGBeanState(gbeanData.getName()) != State.RUNNING_INDEX) {
            fail("gbean not started: " + gbeanData.getName());
        }
    }

    protected void stop(ObjectName name) throws Exception {
        kernel.stopGBean(name);
        kernel.unloadGBean(name);
    }

    protected void setUp(String realmClass) throws Exception {
        cl = this.getClass().getClassLoader();
        containerName = NameFactory.getWebComponentName(null, null, null, null, "tomcatContainer", "WebResource", moduleContext);
        connectorName = NameFactory.getWebComponentName(null, null, null, null, "tomcatConnector", "WebResource", moduleContext);
        if (realmClass != null)
            realmName = NameFactory.getWebComponentName(null, null, null, null, "tomcatRealm", "WebResource", moduleContext);
        engineName = NameFactory.getWebComponentName(null, null, null, null, "tomcatEngine", "WebResource", moduleContext);
        hostName = NameFactory.getWebComponentName(null, null, null, null, "tomcatHost", "WebResource", moduleContext);
        webModuleName = NameFactory.getModuleName(null, null, null, null, "testModule", moduleContext);

        tmName = NameFactory.getComponentName(null, null, null, null, "TransactionManager", NameFactory.JTA_RESOURCE, moduleContext);
        tcmName = NameFactory.getComponentName(null, null, null, null, "TransactionContextManager", NameFactory.JTA_RESOURCE, moduleContext);

        ctcName = new ObjectName("geronimo.test:role=ConnectionTrackingCoordinator");

        kernel = KernelFactory.newInstance().createKernel("test.kernel");
        kernel.boot();

        //ServerInfo
        serverInfoName = new ObjectName("geronimo.system:role=ServerInfo");
        serverInfoGBean = new GBeanData(serverInfoName, ServerInfo.GBEAN_INFO);
        serverInfoGBean.setAttribute("baseDirectory", ".");

        start(serverInfoGBean);

        //Default Realm
        Map initParams = new HashMap();

        if (realmClass != null){
            initParams.put("userClassNames","org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal");
            initParams.put("roleClassNames","org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal");
            realm = new GBeanData(realmName, RealmGBean.GBEAN_INFO);
            realm.setAttribute("className", realmClass);
            realm.setAttribute("initParams", initParams);
            start(realm);
        }

        //Default Engine
        initParams.clear();
        initParams.put("name","Geronimo");
        initParams.put("defaultHost","localhost");
        engine = new GBeanData(engineName, EngineGBean.GBEAN_INFO);
        engine.setAttribute("className", "org.apache.geronimo.tomcat.TomcatEngine");
        engine.setAttribute("initParams", initParams);
        if (realmClass != null)
            engine.setReferencePattern("realmGBean", realmName);
        start(engine);

        //Default Host
        initParams.clear();
        initParams.put("workDir","work");
        initParams.put("name","localhost");
        initParams.put("appBase","");
        host = new GBeanData(hostName, HostGBean.GBEAN_INFO);
        host.setAttribute("className", "org.apache.catalina.core.StandardHost");
        host.setAttribute("initParams", initParams);
        host.setReferencePattern("engineGBean", engineName);
        start(host);

        // Need to override the constructor for unit tests
        container = new GBeanData(containerName, TomcatContainer.GBEAN_INFO);
        container.setAttribute("classLoader", cl);
        container.setAttribute("catalinaHome", "target/var/catalina");
        container.setReferencePattern("engineGBean", engineName);
        container.setReferencePattern("ServerInfo", serverInfoName);

        initParams.clear();
        initParams.put("port","8080");
        connector = new GBeanData(connectorName, ConnectorGBean.GBEAN_INFO);
        connector.setAttribute("initParams", initParams);
        connector.setReferencePattern("TomcatContainer", containerName);

        start(container);
        start(connector);

        tm = new GBeanData(tmName, TransactionManagerImpl.GBEAN_INFO);
        Set patterns = new HashSet();
        patterns.add(ObjectName.getInstance("geronimo.server:j2eeType=JCAManagedConnectionFactory,*"));
        tm.setAttribute("defaultTransactionTimeoutSeconds", new Integer(10));
        tm.setReferencePatterns("ResourceManagers", patterns);
        start(tm);
        tcm = new GBeanData(tcmName, TransactionContextManager.GBEAN_INFO);
        tcm.setReferencePattern("TransactionManager", tmName);
        start(tcm);
        ctc = new GBeanData(ctcName, ConnectionTrackingCoordinator.GBEAN_INFO);
        start(ctc);
    }

    protected void tearDown() throws Exception {
        stop(ctcName);
        stop(tmName);
        if (realmName != null)
            stop(realmName);
        stop(hostName);
        stop(engineName);
        stop(connectorName);
        stop(containerName);
        stop(serverInfoName);
        kernel.shutdown();
    }
}
