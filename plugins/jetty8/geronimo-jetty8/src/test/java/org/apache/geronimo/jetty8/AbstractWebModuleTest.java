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
package org.apache.geronimo.jetty8;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebUserDataPermission;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.transaction.TransactionManager;

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.connector.outbound.connectiontracking.GeronimoTransactionListener;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.annotation.Holder;
import org.apache.geronimo.j2ee.jndi.ContextSource;
import org.apache.geronimo.j2ee.jndi.WebContextSource;
import org.apache.geronimo.jetty8.connector.HTTPSocketConnector;
import org.apache.geronimo.jetty8.handler.GeronimoUserIdentity;
import org.apache.geronimo.jetty8.security.SecurityHandlerFactory;
import org.apache.geronimo.jetty8.security.ServerAuthenticationGBean;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.osgi.MockBundleContext;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.SecurityServiceImpl;
import org.apache.geronimo.security.deploy.SubjectInfo;
import org.apache.geronimo.security.jacc.ApplicationPolicyConfigurationManager;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.security.jacc.PrincipalRoleMapper;
import org.apache.geronimo.security.jacc.RunAsSource;
import org.apache.geronimo.security.jacc.mappingprovider.ApplicationPrincipalRoleConfigurationManager;
import org.apache.geronimo.security.jacc.mappingprovider.GeronimoPolicy;
import org.apache.geronimo.security.jacc.mappingprovider.GeronimoPolicyConfigurationFactory;
import org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal;
import org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.testsupport.TestSupport;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;
import org.apache.geronimo.web.WebApplicationConstants;
import org.apache.geronimo.web.info.ServletInfo;
import org.apache.geronimo.web.info.WebAppInfo;
import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.ServerAuthException;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.security.authentication.FormAuthenticator;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.UserIdentity;
import org.junit.After;
import org.junit.Before;
import org.osgi.framework.Bundle;


/**
 * @version $Rev$ $Date$
 */
public class AbstractWebModuleTest extends TestSupport {
    protected ClassLoader cl;
    protected final static String securityRealmName = "demo-properties-realm";
    protected HTTPSocketConnector connector;
    protected JettyContainerImpl container;
    private TransactionManager transactionManager;
    private ConnectionTrackingCoordinator connectionTrackingCoordinator;
    protected PreHandlerFactory preHandlerFactory = null;
    protected SessionHandlerFactory sessionHandlerFactory = null;
    private Bundle bundle;
    protected String appPath;
    protected int connectorPort = 1234;
    protected String hostURL = "http://localhost:" + connectorPort;

    protected void setUpStaticContentServlet(WebAppInfo webAppInfo) throws Exception {
        ServletInfo servletInfo = new ServletInfo();
        servletInfo.servletName = "default";
        servletInfo.servletClass = "org.eclipse.jetty.servlet.DefaultServlet";
        servletInfo.servletMappings.add("/");
        servletInfo.initParams.put("acceptRanges", "true");
        servletInfo.initParams.put("dirAllowed", "true");
        servletInfo.initParams.put("putAllowed", "false");
        servletInfo.initParams.put("delAllowed", "false");
        servletInfo.initParams.put("redirectWelcome", "false");
        servletInfo.initParams.put("minGzipLength", "8192");

        webAppInfo.servlets.add(servletInfo);
    }

    protected WebAppContextWrapper setUpAppContext(String securityRealmName, SecurityHandlerFactory securityHandlerFactory, String policyContextId, RunAsSource runAsSource, String uriString, WebAppInfo webAppInfo) throws Exception {
        ApplicationPolicyConfigurationManager applicationPolicyConfigurationManager = null;
        //Setup default JSP Factory
        Class.forName("org.apache.jasper.compiler.JspRuntimeContext");
        if (securityHandlerFactory == null) {
            Permissions unchecked = new Permissions();
            unchecked.add(new WebUserDataPermission("/", null));
            unchecked.add(new WebResourcePermission("/", ""));
            ComponentPermissions componentPermissions = new ComponentPermissions(new Permissions(), unchecked, Collections.<String, PermissionCollection>emptyMap());
            applicationPolicyConfigurationManager = setUpJACC(Collections.<String, SubjectInfo>emptyMap(), Collections.<Principal, Set<String>>emptyMap(), componentPermissions, policyContextId);
            LoginService loginService = newLoginService();
//            final ServletCallbackHandler callbackHandler = new ServletCallbackHandler(loginService);
            final Subject subject = new Subject();
            final AccessControlContext acc = ContextManager.registerSubjectShort(subject, null, null);
            securityHandlerFactory = new ServerAuthenticationGBean(new Authenticator() {
                public Authentication validateRequest(ServletRequest request, ServletResponse response, boolean mandatory) throws ServerAuthException {
                    return new UserAuthentication("test", new GeronimoUserIdentity(subject, new GeronimoUserPrincipal("foo"), acc));
                }// most likely validatedUser is not needed here.

                public boolean secureResponse(ServletRequest request, ServletResponse response, boolean mandatory, Authentication.User validatedUser) throws ServerAuthException {
                    return true;
                }

                public void setConfiguration(AuthConfiguration configuration) {
                }

                public String getAuthMethod() {
                    return null;
                }

            }, loginService);
        }
        String contextPath = "/test";
        Map<String, Object> deploymentAttributes = new HashMap<String, Object>();
        deploymentAttributes.put(WebApplicationConstants.META_COMPLETE, Boolean.TRUE);
        deploymentAttributes.put(WebApplicationConstants.SCHEMA_VERSION, 3.0f);
        ContextSource contextSource = new WebContextSource(Collections.<String, Object>emptyMap(),
                Collections.<String, Object>emptyMap(),
                transactionManager,
                null,
                cl,
                null,
                null);
        WebAppContextWrapper app = new WebAppContextWrapper("geronimo:J2EEServer=geronimo,name=hello.war,J2EEApplication=null,j2eeType=WebModule",
                new AbstractName(new URI("default/test/1.0/war?J2EEApplication=null,j2eeType=WebModule,name=default/test/1.0/war")),
                contextPath,
                null,
                null,
                cl,
                bundle,
                null, null,
                null,
                null,
                false,
                sessionHandlerFactory,
                preHandlerFactory,
                policyContextId,
                securityHandlerFactory,
                runAsSource,
                applicationPolicyConfigurationManager == null ? (ApplicationPolicyConfigurationManager) runAsSource : applicationPolicyConfigurationManager,
                new Holder(),
                webAppInfo,
                null,
                connectionTrackingCoordinator,
                container,
                null,
                null,
                null,
                contextSource,
                transactionManager,
                deploymentAttributes
        );
        app.doStart();
        return app;
    }

    protected WebAppContextWrapper setUpSecureAppContext(String securityRealmName, Map<String, SubjectInfo> roleDesignates, Map<Principal, Set<String>> principalRoleMap, ComponentPermissions componentPermissions, SubjectInfo defaultSubjectInfo, PermissionCollection checked, Set securityRoles) throws Exception {
        String policyContextId = "TEST";
        ApplicationPolicyConfigurationManager jacc = setUpJACC(roleDesignates, principalRoleMap, componentPermissions, policyContextId);
        LoginService loginService = newLoginService();
//        Authenticator serverAuthentication = new FormAuthenticator("/auth/logon.html?param=test", "/auth/logonError.html?param=test", true);
        Authenticator serverAuthentication = new FormAuthenticator("/auth/logon.html?param=test", "/auth/logonError.html?param=test", true);
        SecurityHandlerFactory securityHandlerFactory = new ServerAuthenticationGBean(serverAuthentication, loginService);
        WebAppInfo webAppInfo = new WebAppInfo();
        setUpStaticContentServlet(webAppInfo);
        return setUpAppContext(
                securityRealmName,
                securityHandlerFactory,
                policyContextId,
                jacc,
                "war3/",
                webAppInfo);

    }

    private ApplicationPolicyConfigurationManager setUpJACC(Map<String, SubjectInfo> roleDesignates, Map<Principal, Set<String>> principalRoleMap, ComponentPermissions componentPermissions, String policyContextId) throws Exception {
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

    protected LoginService newLoginService() throws Exception {
//        String domainName = "demo-properties-realm";
//
        Map<String, String> users = new HashMap<String, String>();
        users.put("alan", "starcraft");
        users.put("izumi", "violin");
        Map<String, List<String>> groups = new HashMap<String, List<String>>();
        groups.put("alan", Collections.singletonList("it"));
        return new TestLoginService(users, groups);

    }

    protected void setUpSecurityService() throws Exception {
        ServerInfo serverInfo = new BasicServerInfo(".");

        new SecurityServiceImpl(cl, serverInfo, GeronimoPolicyConfigurationFactory.class.getName(), GeronimoPolicy.class.getName(), null, null, null, null);
    }

    protected void tearDownSecurity() throws Exception {
    }

    @Before
    protected void setUp() throws Exception {
        cl = this.getClass().getClassLoader();

        URL configurationBaseURL = cl.getResource("deployables/");

        ServerInfo serverInfo = new BasicServerInfo(".");
        String location = configurationBaseURL.toString();
        if (appPath != null) {
            location = configurationBaseURL.toURI().resolve(appPath).toString();
        }
        MockBundleContext bundleContext = new MockBundleContext(cl, location, new HashMap<Artifact, ConfigurationData>(), null);
        bundle = bundleContext.getBundle();
        container = new JettyContainerImpl("test:name=JettyContainer",
                bundleContext,
            null, new File(BASEDIR, "target/var/jetty").toString(), serverInfo);
        container.doStart();
        connector = new HTTPSocketConnector(container, null);
        connector.setPort(connectorPort);
        connector.setHost("localhost");
        connector.setMaxThreads(2);
        connector.doStart();

        TransactionManagerImpl transactionManager = new TransactionManagerImpl();
        this.transactionManager = transactionManager;
        connectionTrackingCoordinator = new ConnectionTrackingCoordinator();
        transactionManager.addTransactionAssociationListener(new GeronimoTransactionListener(connectionTrackingCoordinator));
    }

    @After
    protected void tearDown() throws Exception {
        connector.doStop();
        Thread.sleep(1000);
    }

    private static class TestLoginService implements LoginService {

        private final Map<String, String> users;
        private final Map<String, List<String>> groups;
        private IdentityService identityService;

        private TestLoginService(Map<String, String> users, Map<String, List<String>> groups) {
            this.users = users;
            this.groups = groups;
        }

//        public void login(LoginCallback loginCallback) throws ServerAuthException {
//            String userName = loginCallback.getUserName();
//            String pws = users.get(userName);
//            if (pws != null && pws.equals(new String((char[])loginCallback.getCredential()))) {
//                final GeronimoUserPrincipal userPrincipal = new GeronimoUserPrincipal(userName);
//                Subject subject = loginCallback.getSubject();
//                subject.getPrincipals().add(userPrincipal);
//                loginCallback.setUserPrincipal(userPrincipal);
//                List<String> usersGroups = groups.get(userName);
//                if (usersGroups != null) {
//                    for (String group: usersGroups) {
//                        subject.getPrincipals().add(new GeronimoGroupPrincipal(group));
//                    }
//                    loginCallback.setGroups(usersGroups);
//                }
//                loginCallback.setSuccess(true);
//            }
//        }

        @Deprecated
        public String getName() {
            return null;
        }

        public UserIdentity login(String userName, Object credentials) {
            String pws = users.get(userName);
            if (pws != null && pws.equals(credentials)) {
                final GeronimoUserPrincipal userPrincipal = new GeronimoUserPrincipal(userName);
                Subject subject = new Subject();
                subject.getPrincipals().add(userPrincipal);
                List<String> usersGroups = groups.get(userName);
                if (usersGroups != null) {
                    for (String group: usersGroups) {
                        subject.getPrincipals().add(new GeronimoGroupPrincipal(group));
                    }
                }
                return identityService.newUserIdentity(subject, userPrincipal, null);
            }
            return null;
        }

        public boolean validate(UserIdentity user) {
            return false;
        }

        public void logout(UserIdentity user) {
        }

        public IdentityService getIdentityService() {
            return identityService;
        }

        public void setIdentityService(IdentityService service) {
            this.identityService = service;
        }
    }
}
