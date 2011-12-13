/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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
import java.net.URL;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.security.auth.login.LoginException;
import javax.security.jacc.PolicyContextException;
import javax.transaction.TransactionManager;

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.connector.outbound.connectiontracking.GeronimoTransactionListener;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.jndi.ContextSource;
import org.apache.geronimo.j2ee.jndi.WebContextSource;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.osgi.MockBundle;
import org.apache.geronimo.kernel.osgi.MockBundleContext;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.security.SecurityServiceImpl;
import org.apache.geronimo.security.deploy.PrincipalInfo;
import org.apache.geronimo.security.deploy.SubjectInfo;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.security.jaas.LoginModuleControlFlag;
import org.apache.geronimo.security.jaas.LoginModuleGBean;
import org.apache.geronimo.security.jacc.ApplicationPolicyConfigurationManager;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.security.jacc.PrincipalRoleMapper;
import org.apache.geronimo.security.jacc.RunAsSource;
import org.apache.geronimo.security.jacc.mappingprovider.ApplicationPrincipalRoleConfigurationManager;
import org.apache.geronimo.security.jacc.mappingprovider.GeronimoPolicy;
import org.apache.geronimo.security.jacc.mappingprovider.GeronimoPolicyConfigurationFactory;
import org.apache.geronimo.security.realm.GenericSecurityRealm;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.testsupport.TestSupport;
import org.apache.geronimo.tomcat.connector.ConnectorGBean;
import org.apache.geronimo.tomcat.connector.Http11ConnectorGBean;
import org.apache.geronimo.tomcat.util.SecurityHolder;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;
import org.apache.geronimo.web.WebApplicationConstants;
import org.apache.geronimo.web.info.ServletInfo;
import org.apache.geronimo.web.info.WebAppInfo;
import org.apache.karaf.jaas.boot.ProxyLoginModule;
import org.osgi.framework.Bundle;


/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractWebModuleTest extends TestSupport {

    protected ClassLoader cl = this.getClass().getClassLoader();
    protected final static String securityRealmName = "demo-properties-realm";
    protected ConnectorGBean connector;
    protected TomcatContainer container;
    protected static int port = 8181;
    private TransactionManager transactionManager;
    private ConnectionTrackingCoordinator connectionTrackingCoordinator;

    protected static final String POLICY_CONTEXT_ID = "securetest";
    protected GenericSecurityRealm realm;
    private Bundle bundle;

    protected void setUpStaticContentServlet(WebAppInfo webAppInfo) throws Exception {
        ServletInfo servletInfo = new ServletInfo();
        servletInfo.servletName = "default";
        servletInfo.servletClass = "org.apache.catalina.servlets.DefaultServlet";
        servletInfo.servletMappings.add("/");
        servletInfo.initParams.put("acceptRanges", "true");
        webAppInfo.servlets.add(servletInfo);
    }

    protected TomcatWebAppContext setUpInsecureAppContext(String relativeWebAppRoot, URL configurationBaseURL, SecurityHolder securityHolder, RunAsSource runAsSource, ObjectRetriever tomcatRealm, ValveGBean valveChain, WebAppInfo webAppInfo) throws Exception {
        //WebBeansFinder.clearInstances(getClass().getClassLoader());
        configurationBaseURL = cl.getResource("deployables/");
        //Setup default JSP Factory
        Class.forName("org.apache.jasper.compiler.JspRuntimeContext");
        URI locationURI = configurationBaseURL.toURI().resolve(relativeWebAppRoot);
        MockBundleContext bundleContext = new MockBundleContext(getClass().getClassLoader(), locationURI.toString(), new HashMap<Artifact, ConfigurationData>(), null);
        bundle = bundleContext.getBundle();
        ContextSource contextSource = new WebContextSource(Collections.<String, Object>emptyMap(),
                Collections.<String, Object>emptyMap(),
                transactionManager,
                null,
                cl,
               null,
                null);
        Map<String, Object> deploymentAttributes = new HashMap<String, Object>();
        deploymentAttributes.put(WebApplicationConstants.META_COMPLETE, Boolean.TRUE);
        deploymentAttributes.put(WebApplicationConstants.SCHEMA_VERSION, 3.0f);
        TomcatWebAppContext app = new TomcatWebAppContext(cl, //classLoader
                bundle, //bundle
                "geronimo:J2EEServer=geronimo,name=hello.war,J2EEApplication=null,j2eeType=WebModule",   //objectName
                "/test",    //contextPath
                null,   //originalSpecDD
                "", //modulePath
                securityHolder, //securityHolder
                null,   //virtualServer
                null,   //unshareableResources
                null,   //applicationManagedSecurityResources
                transactionManager, //transactionManager
                connectionTrackingCoordinator,  //trackedConnectionAssociator
                container,  //container
                runAsSource,    //runAsSource
                securityHolder == null? null: securityHolder.getConfigurationFactory(), //configurationFactory
                tomcatRealm,    //tomcatRealm
                null,   //clusteredValveRetriever
                valveChain, //tomcatValveChain
                null,   //lifecycleListenerChain
                null,   //cluster
                null,   //managerRetriever
                //displayName
                null,   //webServices
                null,   //holder
                null,   //contextCustomizer
                null,   //server
                contextSource,  //contextSource
                (ApplicationPolicyConfigurationManager)runAsSource,   //applicationPolicyConfigurationManager
                //listenerClassNames
                deploymentAttributes, //Map<String, String> deploymentAttributes
                webAppInfo, //webAppinfo
                new HashMap<String, String>(), // Map<String,String> contextAttributes;
                null,
                new AbstractName(new URI("default/test/1.0/war?J2EEApplication=null,j2eeType=WebModule,name=default/test/1.0/war")));  //kernel
        app.doStart();
        return app;
    }



    protected TomcatWebAppContext setUpSecureAppContext(Map roleDesignates, Map principalRoleMap, ComponentPermissions componentPermissions, RealmGBean realm, SecurityHolder securityHolder, WebAppInfo webAppInfo) throws Exception {
        ApplicationPolicyConfigurationManager jacc = setUpJACC(roleDesignates, principalRoleMap, componentPermissions, POLICY_CONTEXT_ID);
        securityHolder.setConfigurationFactory(this.realm);
        URL configurationBaseURL = new File(BASEDIR, "src/test/resources/deployables/war3/WEB-INF/web.xml").toURI().toURL();
        setUpStaticContentServlet(webAppInfo);
        return setUpInsecureAppContext("war3",
                configurationBaseURL,
                securityHolder,
                jacc,
                realm,
                null,
                webAppInfo);
    }

    private ApplicationPolicyConfigurationManager setUpJACC(Map<String, SubjectInfo> roleDesignates, Map<Principal, Set<String>> principalRoleMap, ComponentPermissions componentPermissions,
            String policyContextId) throws Exception {
        setUpSecurityService();
        PrincipalRoleMapper roleMapper = new ApplicationPrincipalRoleConfigurationManager(principalRoleMap, null, roleDesignates, null);
        Map<String, ComponentPermissions> contextIDToPermissionsMap = new HashMap<String, ComponentPermissions>();
        contextIDToPermissionsMap.put(policyContextId, componentPermissions);
        ApplicationPolicyConfigurationManager jacc = new ApplicationPolicyConfigurationManager(contextIDToPermissionsMap, roleMapper, cl) {

            @Override
            public void updateApplicationPolicyConfiguration(Map<String, ComponentPermissions> arg0) throws PolicyContextException, ClassNotFoundException, LoginException {
                //JACCSecurity Test build the ComponnentPermissions manually, use an empty update method to prevent JACCSecurityListener to update the permissions
            }
        };
        jacc.doStart();
        return jacc;
    }

    protected void setUpSecurityService() throws Exception {
        String domainName = "demo-properties-realm";
        ServerInfo serverInfo = new BasicServerInfo(".");

        new SecurityServiceImpl(cl, serverInfo, GeronimoPolicyConfigurationFactory.class.getName(), GeronimoPolicy.class.getName(), null, null, null, null);

        Map<String, Object> options = new HashMap<String, Object>();
        options.put("usersURI", new File(BASEDIR, "src/test/resources/data/users.properties").toURI().toString());
        options.put("groupsURI", new File(BASEDIR, "src/test/resources/data/groups.properties").toURI().toString());

        LoginModuleGBean loginModule = new LoginModuleGBean("org.apache.geronimo.security.realm.providers.PropertiesFileLoginModule", null, true, options, domainName, cl);

        JaasLoginModuleUse loginModuleUse = new JaasLoginModuleUse(loginModule, null, LoginModuleControlFlag.REQUIRED);

        PrincipalInfo.PrincipalEditor principalEditor = new PrincipalInfo.PrincipalEditor();
        principalEditor.setAsText("metro,org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal");
        Bundle bundle = new MockBundle(cl, "", -1);
        ProxyLoginModule.init(new MockBundleContext(bundle));
        realm = new GenericSecurityRealm(domainName, loginModuleUse, true, true, serverInfo, bundle, null);
    }


    protected void tearDownSecurity() throws Exception {
    }

    protected void init(String realmClass) throws Exception {

        RealmGBean realm = null;
//        if (realmClass != null) {
//            Map initParams = new HashMap();
//            initParams.put("userClassNames", "org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal");
//            initParams.put("roleClassNames", "org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal");
//            realm = new RealmGBean(realmClass, initParams, null, null);
//            realm.doStart();
//        }

        //Default Host
        Map initParams = new HashMap();
        initParams.put("workDir", "work");
        initParams.put("name", "localhost");
        initParams.put("appBase", "");

        HostGBean host = new HostGBean("org.apache.catalina.core.StandardHost", initParams, null, realm, null, null, null, null, null);
        //Default Engine
        initParams = Collections.singletonMap("name", "Geronimo");

        EngineGBean engine = new EngineGBean(null, null,
                "org.apache.geronimo.tomcat.TomcatEngine",
                initParams,
                host,
                realm,
                null,
                null,
                null,
                null,
                null,
                null);
        engine.doStart();

        ServerInfo serverInfo = new BasicServerInfo(".");
        MockBundleContext bundleContext = new MockBundleContext(getClass().getClassLoader(), "", new HashMap<Artifact, ConfigurationData>(), null);
        container = new TomcatContainer(cl,
                bundleContext,
            new File(BASEDIR, "target/var/catalina").toString(), null, null, null, engine, null, serverInfo, null, null);
        container.doStart();

        connector = new Http11ConnectorGBean("HTTP", null, "localhost", port++, container, serverInfo,null);
        connector.doStart();

        TransactionManagerImpl transactionManager = new TransactionManagerImpl();
        this.transactionManager = transactionManager;
        connectionTrackingCoordinator = new ConnectionTrackingCoordinator();
        transactionManager.addTransactionAssociationListener(new GeronimoTransactionListener(connectionTrackingCoordinator));
    }

    protected void tearDown() throws Exception {
        connector.doStop();
        super.tearDown();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("catalina.useNaming", "false");
        System.setProperty("catalina.base", BASEDIR.getAbsolutePath());
    }
}
