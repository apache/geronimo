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
import java.security.PermissionCollection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.catalina.Context;
import org.apache.catalina.Realm;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.security.deploy.Security;
import org.apache.geronimo.naming.reference.KernelAwareReference;
import org.apache.geronimo.naming.reference.ClassLoaderAwareReference;
import org.apache.geronimo.naming.java.SimpleReadOnlyContext;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.tomcat.valve.ComponentContextValve;
import org.apache.geronimo.tomcat.valve.TransactionContextValve;
import org.apache.geronimo.tomcat.valve.PolicyContextValve;
import org.apache.geronimo.transaction.OnlineUserTransaction;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.apache.geronimo.transaction.context.TransactionContextManager;

import javax.naming.NamingException;


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
    private final Set securityConstraints;
    private final Set securityRoles;
    private final Map componentContext;
    private final Kernel kernel;
    private final TransactionContextManager transactionContextManager;
    private final String policyContextID;

    public TomcatWebAppContext(URI webAppRoot,
                               URI[] webClassPath,
                               URL configurationBaseUrl,
                               LoginConfig loginConfig,
                               Realm tomcatRealm,
                               Set securityConstraints,

                               String policyContextID,
                               String loginDomainName,
                               Security securityConfig,
                               Set securityRoles,
                               PermissionCollection uncheckedPermissions,
                               PermissionCollection excludedPermissions,
                               Map rolePermissions,
                               Map componentContext,
                               OnlineUserTransaction userTransaction,
                               TransactionContextManager transactionContextManager,
                               TrackedConnectionAssociator trackedConnectionAssociator,
                               TomcatContainer container,
                               Kernel kernel) throws NamingException {

        assert webAppRoot != null;
        assert webClassPath != null;
        assert configurationBaseUrl != null;
        assert transactionContextManager != null;
        assert trackedConnectionAssociator != null;
        assert componentContext != null;
        assert container != null;

        this.webAppRoot = webAppRoot;
        this.container = container;

        this.setDocBase(this.webAppRoot.getPath());
        this.tomcatRealm = tomcatRealm;
        this.policyContextID = policyContextID;
        this.securityConstraints = securityConstraints;
        this.securityRoles = securityRoles;
        this.loginConfig = loginConfig;

        this.componentContext = componentContext;
        this.transactionContextManager = transactionContextManager;
        this.kernel = kernel;

        userTransaction.setUp(transactionContextManager, trackedConnectionAssociator);

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
        if (tomcatRealm != null) {
            if (tomcatRealm instanceof TomcatGeronimoRealm) {
                ((TomcatGeronimoRealm) tomcatRealm).setContext(context);
            }

            context.setRealm(tomcatRealm);
        }

        if (loginConfig != null)
            context.setLoginConfig(loginConfig);

        // Add the security constraints
        if (securityConstraints != null) {
            Iterator conIterator = securityConstraints.iterator();
            while (conIterator.hasNext()) {
                context.addConstraint((SecurityConstraint) conIterator.next());
            }
        }

        // Add the security roles
        if (securityRoles != null) {
            Iterator secIterator = securityRoles.iterator();
            while (secIterator.hasNext()) {
                context.addSecurityRole((String) secIterator.next());
            }
        }

        // create ReadOnlyContext
        javax.naming.Context enc = null;
        try {
            if (componentContext != null) {
                for (Iterator iterator = componentContext.values().iterator(); iterator.hasNext();) {
                    Object value = iterator.next();
                    if (value instanceof KernelAwareReference) {
                        ((KernelAwareReference) value).setKernel(kernel);
                    }
                    if (value instanceof ClassLoaderAwareReference) {
                        ((ClassLoaderAwareReference) value).setClassLoader(context.getLoader().getClassLoader());
                    }
                }
                enc = new SimpleReadOnlyContext(componentContext);
            }
        } catch (NamingException ne) {
            log.error(ne);
        }

        //Set the valves for the context
        if (enc != null){
            ComponentContextValve contextValve = new ComponentContextValve(enc);
            ((StandardContext) context).addValve(contextValve);
        }

        if (transactionContextManager != null){
            TransactionContextValve transactionValve = new TransactionContextValve(transactionContextManager);
            ((StandardContext) context).addValve(transactionValve);
        }

        if (policyContextID != null){
            PolicyContextValve policyValve = new PolicyContextValve(policyContextID);
            ((StandardContext) context).addValve(policyValve);
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

    public void doStart() throws Exception {

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

        infoFactory.addAttribute("loginConfig", LoginConfig.class, true);

        infoFactory.addAttribute("tomcatRealm", Realm.class, true);
        infoFactory.addAttribute("securityConstraints", Set.class, true);

        infoFactory.addAttribute("policyContextID", String.class, true);
        infoFactory.addAttribute("loginDomainName", String.class, true);
        infoFactory.addAttribute("securityConfig", Security.class, true);
        infoFactory.addAttribute("securityRoles", Set.class, true);
        infoFactory.addAttribute("uncheckedPermissions", PermissionCollection.class, true);
        infoFactory.addAttribute("excludedPermissions", PermissionCollection.class, true);
        infoFactory.addAttribute("rolePermissions", Map.class, true);

        infoFactory.addAttribute("componentContext", Map.class, true);
        infoFactory.addAttribute("userTransaction", OnlineUserTransaction.class, true);
        infoFactory.addReference("TransactionContextManager", TransactionContextManager.class);
        infoFactory.addReference("TrackedConnectionAssociator", TrackedConnectionAssociator.class);

        infoFactory.addReference("Container", TomcatContainer.class);
        infoFactory.addAttribute("kernel", Kernel.class, false);

        infoFactory.setConstructor(new String[]{
            "webAppRoot",
            "webClassPath",
            "configurationBaseUrl",
            "loginConfig",
            "tomcatRealm",
            "securityConstraints",
            "policyContextID",
            "loginDomainName",
            "securityConfig",
            "securityRoles",
            "uncheckedPermissions",
            "excludedPermissions",
            "rolePermissions",
            "componentContext",
            "userTransaction",
            "TransactionContextManager",
            "TrackedConnectionAssociator",
            "Container",
            "kernel"
        });

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
