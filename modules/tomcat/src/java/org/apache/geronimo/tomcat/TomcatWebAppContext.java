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

import org.apache.catalina.Context;
import org.apache.catalina.Realm;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.deploy.LoginConfig;
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
public class TomcatWebAppContext implements GBeanLifecycle, TomcatContext {

    private static Log log = LogFactory.getLog(TomcatWebAppContext.class);

    protected final TomcatContainer container;

    protected Context context = null;

    private final URI webAppRoot;

    private String path = null;

    private String docBase = null;

    private final LoginConfig loginConfig;

    private final Realm tomcatRealm;

    private final SecurityConstraint[] securityConstraints;

    private final String[] securityRoles;


    public TomcatWebAppContext(URI webAppRoot, URI[] webClassPath, URL configurationBaseUrl, String authMethod,
                               String realmName, String loginPage, String errorPage, Realm tomcatRealm,
                               SecurityConstraint[] securityConstraints, String[] securityRoles,
                               TomcatContainer container) {
        assert webAppRoot != null;
        assert webClassPath != null;
        assert configurationBaseUrl != null;
        assert container != null;

        this.webAppRoot = webAppRoot;
        this.container = container;

        this.setDocBase(this.webAppRoot.getPath());
        this.tomcatRealm = tomcatRealm;
        this.securityConstraints = securityConstraints;
        this.securityRoles = securityRoles;

        if (authMethod != null){
            loginConfig = new LoginConfig();
            loginConfig.setAuthMethod(authMethod);
            loginConfig.setRealmName(realmName);
            loginConfig.setLoginPage(loginPage);
            loginConfig.setErrorPage(errorPage);
        } else {
            loginConfig = null;    
        }
    }

    public String getDocBase() {
        return docBase;
    }

    public void setDocBase(String docBase) {
        this.docBase = docBase;
    }

    public void setContextProperties() {
        context.setDocBase(webAppRoot.getPath());
        context.setPath(path);

        //Security
        if (tomcatRealm != null)
            context.setRealm(tomcatRealm);

        if (loginConfig != null)
            context.setLoginConfig(loginConfig);

        // Add the security constraints
        if (securityConstraints != null) {
            for (int i = 0; i < securityConstraints.length; i++) {
                SecurityConstraint sc = securityConstraints[i];
                context.addConstraint(sc);
            }
        }

        // Add the security roles
        if (securityRoles != null) {
            for (int i = 0; i < securityRoles.length; i++) {
                context.addSecurityRole(securityRoles[i]);
            }
        }
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void doStart() throws WaitingException, Exception {

        // See the note of TomcatContainer::addContext
        container.addContext(this);
        // Is it necessary - doesn't Tomcat Embedded take care of it?
        // super.start();

        log.info("TomcatWebAppContext started");
    }

    public void doStop() throws Exception {
        container.removeContext(this);

        log.info("TomcatWebAppContext stopped");
    }

    public void doFail() {
        container.removeContext(this);

        log.info("TomcatWebAppContext failed");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("Tomcat WebApplication Context", TomcatWebAppContext.class);

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
                                                "realmName", "loginPage", "errorPage", "tomcatRealm",
                                                "securityConstraints", "securityRoles", "Container"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
