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

import java.net.URI;
import java.net.URL;

import org.apache.catalina.Realm;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;


/**
 * Wrapper for a WebApplicationContext that sets up its J2EE environment.
 *
 * @version $Rev: 56022 $ $Date: 2004-10-30 07:16:18 +0200 (Sat, 30 Oct 2004) $
 */
public class TomcatSecureWebAppContext extends TomcatWebAppContext implements GBeanLifecycle {

    private final static Log log = LogFactory.getLog(org.apache.geronimo.tomcat.TomcatSecureWebAppContext.class);

    private final LoginConfig loginConfig;

    private final Realm tomcatRealm;

    private final SecurityConstraint[] securityConstraints;

    private final String[] securityRoles;

    public TomcatSecureWebAppContext(URI webAppRoot, URI[] webClassPath, URL configurationBaseUrl, String authMethod,
                                     String realmName, String loginPage, String errorPage, Realm tomcatRealm,
                                     SecurityConstraint[] securityConstraints, String[] securityRoles, TomcatContainer container) {

        super(webAppRoot, webClassPath, configurationBaseUrl, container);

        assert authMethod != null;
        assert realmName != null;
        assert loginPage != null;
        assert errorPage != null;
        assert tomcatRealm != null;
        assert securityConstraints != null;
        assert securityRoles != null;

        this.tomcatRealm = tomcatRealm;
        this.securityConstraints = securityConstraints;
        this.securityRoles = securityRoles;

        loginConfig = new LoginConfig();
        loginConfig.setAuthMethod(authMethod);
        loginConfig.setRealmName(realmName);
        loginConfig.setLoginPage(loginPage);
        loginConfig.setErrorPage(errorPage);

    }

    public void setContextProperties() {
        super.setContextProperties();

        context.setRealm(tomcatRealm);
        context.setLoginConfig(loginConfig);

        // Add the security constraints
        for (int i = 0; i < securityConstraints.length; i++) {
            SecurityConstraint sc = securityConstraints[i];
            context.addConstraint(sc);
        }

        // Add the security roles
        for (int i = 0; i < securityRoles.length; i++) {
            context.addSecurityRole(securityRoles[i]);
        }
    }

    public void doStart() throws WaitingException, Exception {
        super.doStart();
        log.info("TomcatSecureWebAppContext started");
    }

    public void doStop() throws Exception {
        super.doStop();
        log.info("TomcatSecureWebAppContext stopped");
    }

    public void doFail() {
        super.doFail();
        log.info("TomcatSecureWebAppContext failed");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("Tomcat Secure WebApplication Context", TomcatSecureWebAppContext.class);

        infoFactory.addAttribute("webAppRoot", URI.class, true);
        infoFactory.addAttribute("webClassPath", URI[].class, true);
        infoFactory.addAttribute("configurationBaseUrl", URL.class, true);

        infoFactory.addAttribute("path", String.class, true);

        infoFactory.addAttribute("authMethod", String.class, true);
        infoFactory.addAttribute("realmName", String.class, true);
        infoFactory.addAttribute("loginPage", String.class, true);
        infoFactory.addAttribute("errorPage", String.class, true);

        infoFactory.addAttribute("tomcatRealm", Realm.class, true);
        infoFactory.addAttribute("securityConstraints", SecurityConstraint[].class, true);
        infoFactory.addAttribute("securityRoles", String[].class, true);

        infoFactory.addReference("Container", TomcatContainer.class);

        infoFactory.setConstructor(new String[]{"webAppRoot", "webClassPath", "configurationBaseUrl", "authMethod",
                                                "realmName", "loginPage", "errorPage", "tomcatRealm", "securityConstraints", "securityRoles",
                                                "Container"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
