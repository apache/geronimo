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
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.security.SecurityServiceImpl;
import org.apache.geronimo.security.deploy.PrincipalInfo;
import org.apache.geronimo.security.jaas.GeronimoLoginConfiguration;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.security.jaas.LoginModuleGBean;
import org.apache.geronimo.security.jaas.server.JaasLoginService;
import org.apache.geronimo.security.jacc.ApplicationPolicyConfigurationManager;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.security.jacc.RoleDesignateSource;
import org.apache.geronimo.security.realm.GenericSecurityRealm;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.tomcat.util.SecurityHolder;
import org.apache.geronimo.transaction.context.OnlineUserTransaction;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;


/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractWebModuleTest extends TestCase {

    protected ClassLoader cl;
    protected final static String securityRealmName = "demo-properties-realm";
    private ConnectorGBean connector;
    protected TomcatContainer container;
    private TransactionContextManager transactionContextManager;
    private ConnectionTrackingCoordinator connectionTrackingCoordinator;

    protected static final String POLICY_CONTEXT_ID = "securetest";
    private GeronimoLoginConfiguration loginConfiguration;

    protected TomcatWebAppContext setUpInsecureAppContext(URI relativeWebAppRoot, URL configurationBaseURL, SecurityHolder securityHolder, RoleDesignateSource roleDesignateSource, ObjectRetriever tomcatRealm, ValveGBean valveChain) throws Exception {

        TomcatWebAppContext app = new TomcatWebAppContext(cl,
                null,
                null,
                new URL(configurationBaseURL, relativeWebAppRoot.getPath()),
                securityHolder,
                null,
                Collections.EMPTY_MAP,
                null,
                null,
                new OnlineUserTransaction(),
                transactionContextManager,
                connectionTrackingCoordinator,
                container,
                roleDesignateSource,
                tomcatRealm,
                valveChain,
                null,
                null,
                false,
                false,
                null,
                null,
                null,
                null);
        app.setContextPath("/test");
        app.doStart();
        return app;
    }

    protected TomcatWebAppContext setUpSecureAppContext(Map roleDesignates, Map principalRoleMap, ComponentPermissions componentPermissions, RealmGBean realm, SecurityHolder securityHolder) throws Exception {
        Map contextIDToPermissionsMap = new HashMap();
        contextIDToPermissionsMap.put(POLICY_CONTEXT_ID, componentPermissions);
        ApplicationPolicyConfigurationManager jacc = new ApplicationPolicyConfigurationManager(contextIDToPermissionsMap, principalRoleMap, roleDesignates, cl);
        jacc.doStart();

        URL configurationBaseURL = new File("target/var/catalina/webapps/war3/WEB-INF/web.xml").toURL();
        return setUpInsecureAppContext(new File("target/var/catalina/webapps/war3/").toURI(),
                configurationBaseURL,
                securityHolder,
                jacc,
                realm,
                null);
    }

    protected void setUpSecurity() throws Exception {
        String domainName = "demo-properties-realm";

        ServerInfo serverInfo = new BasicServerInfo(".");

        new SecurityServiceImpl(cl, serverInfo, "org.apache.geronimo.security.jacc.GeronimoPolicyConfigurationFactory", "org.apache.geronimo.security.jacc.GeronimoPolicy", null, null, null, null);

        Properties options = new Properties();
        options.setProperty("usersURI", "src/test-resources/data/users.properties");
        options.setProperty("groupsURI", "src/test-resources/data/groups.properties");

        LoginModuleGBean loginModule = new LoginModuleGBean("org.apache.geronimo.security.realm.providers.PropertiesFileLoginModule", null, true, true, cl);
        loginModule.setLoginDomainName(domainName);
        loginModule.setOptions(options);

        JaasLoginModuleUse loginModuleUse = new JaasLoginModuleUse(loginModule, null, "REQUIRED", null);

        JaasLoginService loginService = new JaasLoginService("HmacSHA1", "secret", cl, null);

        PrincipalInfo.PrincipalEditor principalEditor = new PrincipalInfo.PrincipalEditor();
        principalEditor.setAsText("metro,org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal,false");
        GenericSecurityRealm realm = new GenericSecurityRealm(domainName, loginModuleUse, true, true, (PrincipalInfo) principalEditor.getValue(), serverInfo, cl, null, loginService);

        loginService.setRealms(Collections.singleton(realm));
        loginService.doStart();

        loginConfiguration = new GeronimoLoginConfiguration();
        loginConfiguration.setConfigurations(Collections.singleton(realm));
        loginConfiguration.doStart();

    }

    protected void tearDownSecurity() throws Exception {
        loginConfiguration.doStop();
    }

    protected void init(String realmClass) throws Exception {
        cl = this.getClass().getClassLoader();

        RealmGBean realm = null;
        if (realmClass != null) {
            Map initParams = new HashMap();
            initParams.put("userClassNames", "org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal");
            initParams.put("roleClassNames", "org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal");
            realm = new RealmGBean(realmClass, initParams);
            realm.doStart();
        }

        //Default Host
        Map initParams = new HashMap();
        initParams.put("workDir", "work");
        initParams.put("name", "localhost");
        initParams.put("appBase", "");

        HostGBean host = new HostGBean("org.apache.catalina.core.StandardHost", initParams, null, realm, null, null, null);
        //Default Engine
        initParams = Collections.singletonMap("name", "Geronimo");

        EngineGBean engine = new EngineGBean("org.apache.geronimo.tomcat.TomcatEngine",
                initParams,
                host,
                Collections.singleton(host),
                realm,
                null,
                null,
                null);
        engine.doStart();

        ServerInfo serverInfo = new BasicServerInfo(".");
        container = new TomcatContainer(cl, "target/var/catalina", engine, serverInfo, null, null);
        container.doStart();

        connector = new ConnectorGBean("HTTP", null, "localhost", 8181, container);
        connector.doStart();

        TransactionManagerImpl tm = new TransactionManagerImpl(10, null, null, Collections.EMPTY_LIST);
        transactionContextManager = new TransactionContextManager(tm, tm);
        connectionTrackingCoordinator = new ConnectionTrackingCoordinator();
    }

    protected void tearDown() throws Exception {
        connector.doStop();
        super.tearDown();
    }
}
