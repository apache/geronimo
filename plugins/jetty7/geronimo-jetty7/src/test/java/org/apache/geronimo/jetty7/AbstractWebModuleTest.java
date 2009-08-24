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
package org.apache.geronimo.jetty7;

import java.io.File;
import java.net.URL;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Principal;
import java.security.AccessControlContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebUserDataPermission;
import javax.transaction.TransactionManager;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.connector.outbound.connectiontracking.GeronimoTransactionListener;
import org.apache.geronimo.jetty7.connector.HTTPSocketConnector;
import org.apache.geronimo.jetty7.security.SecurityHandlerFactory;
import org.apache.geronimo.jetty7.security.ServerAuthenticationGBean;
import org.apache.geronimo.jetty7.handler.GeronimoUserIdentity;
import org.apache.geronimo.security.SecurityServiceImpl;
import org.apache.geronimo.security.ContextManager;
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
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.ServerAuthException;
import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.security.authentication.FormAuthenticator;


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
    private URL configurationBaseURL;
    protected PreHandlerFactory preHandlerFactory = null;
    protected SessionHandlerFactory sessionHandlerFactory = null;

    protected void setUpStaticContentServlet(JettyServletRegistration webModule) throws Exception {
        Map<String, String> staticContentServletInitParams = new HashMap<String, String>();
        staticContentServletInitParams.put("acceptRanges", "true");
        staticContentServletInitParams.put("dirAllowed", "true");
        staticContentServletInitParams.put("putAllowed", "false");
        staticContentServletInitParams.put("delAllowed", "false");
        staticContentServletInitParams.put("redirectWelcome", "false");
        staticContentServletInitParams.put("minGzipLength", "8192");

        new ServletHolderWrapper("test:name=staticservlet",
                "default",
                "org.eclipse.jetty.servlet.DefaultServlet",
                null,
                staticContentServletInitParams,
                null,
                Collections.singleton("/"),
                null,
                webModule);

    }

    protected WebAppContextWrapper setUpAppContext(String securityRealmName, SecurityHandlerFactory securityHandlerFactory, String policyContextId, RunAsSource runAsSource, String uriString) throws Exception {

        if (securityHandlerFactory == null) {
            Permissions unchecked = new Permissions();
            unchecked.add(new WebUserDataPermission("/", null));
            unchecked.add(new WebResourcePermission("/", ""));
            ComponentPermissions componentPermissions = new ComponentPermissions(new Permissions(), unchecked, Collections.<String, PermissionCollection>emptyMap());
            setUpJACC(Collections.<String, SubjectInfo>emptyMap(), Collections.<Principal, Set<String>>emptyMap(), componentPermissions, policyContextId);
            LoginService loginService = newLoginService();
//            final ServletCallbackHandler callbackHandler = new ServletCallbackHandler(loginService);
            final Subject subject = new Subject();
            final AccessControlContext acc = ContextManager.registerSubjectShort(subject, null, null);
            securityHandlerFactory = new ServerAuthenticationGBean(new Authenticator() {
                public Authentication validateRequest(ServletRequest request, ServletResponse response, boolean mandatory) throws ServerAuthException {
                    return new UserAuthentication(this, new GeronimoUserIdentity(subject, new GeronimoUserPrincipal("foo"), acc));
                }// most likely validatedUser is not needed here.

                public boolean secureResponse(ServletRequest request, ServletResponse response, boolean mandatory, Authentication.User validatedUser) throws ServerAuthException {
                    return true;
                }

                public void setConfiguration(Configuration configuration) {
                }

                public String getAuthMethod() {
                    return null;
                }

            }, loginService);
        }
        String contextPath = "/test";
        WebAppContextWrapper app = new WebAppContextWrapper(null,
                contextPath,
                null,
                Collections.<String, Object>emptyMap(),
                cl,
                new URL(configurationBaseURL, uriString),
                null, null,
                null,
                "context",
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                false,
                0,
                sessionHandlerFactory,
                preHandlerFactory,
                policyContextId,
                securityHandlerFactory,
                runAsSource,
                null,
                null,
                transactionManager,
                connectionTrackingCoordinator,
                container,
                null,
                null,
                null,
                null);
        app.doStart();
        return app;
    }

    protected WebAppContextWrapper setUpSecureAppContext(String securityRealmName, Map<String, SubjectInfo> roleDesignates, Map<Principal, Set<String>> principalRoleMap, ComponentPermissions componentPermissions, SubjectInfo defaultSubjectInfo, PermissionCollection checked, Set securityRoles) throws Exception {
        String policyContextId = "TEST";
        ApplicationPolicyConfigurationManager jacc = setUpJACC(roleDesignates, principalRoleMap, componentPermissions, policyContextId);
        LoginService loginService = newLoginService();
//        Authenticator serverAuthentication = new FormAuthenticator("/auth/logon.html?param=test", "/auth/logonError.html?param=test", true);
        Authenticator serverAuthentication = new FormAuthenticator("/auth/logon.html?param=test", "/auth/logonError.html?param=test");
        SecurityHandlerFactory securityHandlerFactory = new ServerAuthenticationGBean(serverAuthentication, loginService);
        return setUpAppContext(
                securityRealmName,
                securityHandlerFactory,
                policyContextId,
                jacc,
                "war3/");

    }

    private ApplicationPolicyConfigurationManager setUpJACC(Map<String, SubjectInfo> roleDesignates, Map<Principal, Set<String>> principalRoleMap, ComponentPermissions componentPermissions, String policyContextId) throws Exception {
        setUpSecurityService();
        PrincipalRoleMapper roleMapper = new ApplicationPrincipalRoleConfigurationManager(principalRoleMap, null, roleDesignates, null);
        Map<String, ComponentPermissions> contextIDToPermissionsMap = new HashMap<String, ComponentPermissions>();
        contextIDToPermissionsMap.put(policyContextId, componentPermissions);
        ApplicationPolicyConfigurationManager jacc = new ApplicationPolicyConfigurationManager(contextIDToPermissionsMap, roleMapper, cl);
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

    protected void setUp() throws Exception {
        cl = this.getClass().getClassLoader();

        configurationBaseURL = cl.getResource("deployables/");

        ServerInfo serverInfo = new BasicServerInfo(".");
        container = new JettyContainerImpl("test:name=JettyContainer", null, new File(BASEDIR, "target/var/jetty").toString(), serverInfo);
        container.doStart();
        connector = new HTTPSocketConnector(container, null);
        connector.setPort(5678);
        connector.setHost("localhost");
        connector.setMaxThreads(2);
        connector.doStart();

        TransactionManagerImpl transactionManager = new TransactionManagerImpl();
        this.transactionManager = transactionManager;
        connectionTrackingCoordinator = new ConnectionTrackingCoordinator();
        transactionManager.addTransactionAssociationListener(new GeronimoTransactionListener(connectionTrackingCoordinator));
    }

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
