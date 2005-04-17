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
import java.util.Hashtable;
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
import org.apache.geronimo.security.jacc.RoleDesignateSource;
import org.apache.geronimo.naming.reference.KernelAwareReference;
import org.apache.geronimo.naming.reference.ClassLoaderAwareReference;
import org.apache.geronimo.naming.java.SimpleReadOnlyContext;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.tomcat.valve.ComponentContextValve;
import org.apache.geronimo.tomcat.valve.TransactionContextValve;
import org.apache.geronimo.tomcat.valve.PolicyContextValve;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.apache.geronimo.transaction.context.OnlineUserTransaction;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.J2EEApplication;
import org.apache.geronimo.j2ee.management.J2EEServer;
import org.apache.geronimo.j2ee.management.impl.InvalidObjectNameException;

import javax.management.ObjectName;
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

    private final RoleDesignateSource roleDesignateSource;

    private final J2EEServer server;

    private final J2EEApplication application;

    public TomcatWebAppContext(
            String objectName, 
            String originalSpecDD,
            URI webAppRoot, 
            URI[] webClassPath, 
            URL configurationBaseUrl,
            LoginConfig loginConfig, 
            Realm tomcatRealm,
            Set securityConstraints,
            String policyContextID, 
            String loginDomainName,
            Security securityConfig, 
            Set securityRoles,
            Map componentContext, 
            OnlineUserTransaction userTransaction,
            TransactionContextManager transactionContextManager,
            TrackedConnectionAssociator trackedConnectionAssociator,
            TomcatContainer container, 
            RoleDesignateSource roleDesignateSource,
            J2EEServer server, 
            J2EEApplication application, 
            Kernel kernel)
            throws NamingException {

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

        this.roleDesignateSource = roleDesignateSource;
        this.server = server;
        this.application = application;

        this.kernel = kernel;
        ObjectName myObjectName = JMXUtil.getObjectName(objectName);
        verifyObjectName(myObjectName);

        if (tomcatRealm != null){
            if (roleDesignateSource == null) {
                throw new IllegalArgumentException("RoleDesignateSource must be supplied for a secure web app");
            }            
        }
        userTransaction.setUp(transactionContextManager,
                trackedConnectionAssociator);

    }

    public String getServer() {
        return server.getObjectName();
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

        // Security
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
                for (Iterator iterator = componentContext.values().iterator(); iterator
                        .hasNext();) {
                    Object value = iterator.next();
                    if (value instanceof KernelAwareReference) {
                        ((KernelAwareReference) value).setKernel(kernel);
                    }
                    if (value instanceof ClassLoaderAwareReference) {
                        ((ClassLoaderAwareReference) value)
                                .setClassLoader(context.getLoader()
                                        .getClassLoader());
                    }
                }
                enc = new SimpleReadOnlyContext(componentContext);
            }
        } catch (NamingException ne) {
            log.error(ne);
        }

        // Set the valves for the context
        if (enc != null) {
            ComponentContextValve contextValve = new ComponentContextValve(enc);
            ((StandardContext) context).addValve(contextValve);
        }

        if (transactionContextManager != null) {
            TransactionContextValve transactionValve = new TransactionContextValve(
                    transactionContextManager);
            ((StandardContext) context).addValve(transactionValve);
        }

        if (policyContextID != null) {
            PolicyContextValve policyValve = new PolicyContextValve(
                    policyContextID);
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

    /**
     * ObjectName must match this pattern: <p/>
     * domain:j2eeType=WebModule,name=MyName,J2EEServer=MyServer,J2EEApplication=MyApplication
     */
    private void verifyObjectName(ObjectName objectName) {
        if (objectName.isPattern()) {
            throw new InvalidObjectNameException(
                    "ObjectName can not be a pattern", objectName);
        }
        Hashtable keyPropertyList = objectName.getKeyPropertyList();
        if (!NameFactory.WEB_MODULE.equals(keyPropertyList.get("j2eeType"))) {
            throw new InvalidObjectNameException(
                    "WebModule object name j2eeType property must be 'WebModule'",
                    objectName);
        }
        if (!keyPropertyList.containsKey(NameFactory.J2EE_NAME)) {
            throw new InvalidObjectNameException(
                    "WebModule object must contain a name property", objectName);
        }
        if (!keyPropertyList.containsKey(NameFactory.J2EE_SERVER)) {
            throw new InvalidObjectNameException(
                    "WebModule object name must contain a J2EEServer property",
                    objectName);
        }
        if (!keyPropertyList.containsKey(NameFactory.J2EE_APPLICATION)) {
            throw new InvalidObjectNameException(
                    "WebModule object name must contain a J2EEApplication property",
                    objectName);
        }
        if (keyPropertyList.size() != 4) {
            throw new InvalidObjectNameException(
                    "WebModule object name can only have j2eeType, name, J2EEApplication, and J2EEServer properties",
                    objectName);
        }
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
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(
                "Tomcat WebApplication Context", TomcatWebAppContext.class,
                NameFactory.WEB_MODULE);

        infoBuilder.addAttribute("objectName", String.class, false);
        infoBuilder.addAttribute("deploymentDescriptor", String.class, true);
        infoBuilder.addAttribute("webAppRoot", URI.class, true);
        infoBuilder.addAttribute("webClassPath", URI[].class, true);
        infoBuilder.addAttribute("configurationBaseUrl", URL.class, true);

        infoBuilder.addAttribute("path", String.class, true);

        infoBuilder.addAttribute("loginConfig", LoginConfig.class, true);

        infoBuilder.addAttribute("tomcatRealm", Realm.class, true);
        infoBuilder.addAttribute("securityConstraints", Set.class, true);

        infoBuilder.addAttribute("policyContextID", String.class, true);
        infoBuilder.addAttribute("loginDomainName", String.class, true);
        infoBuilder.addAttribute("securityConfig", Security.class, true);
        infoBuilder.addAttribute("securityRoles", Set.class, true);
        infoBuilder.addAttribute("componentContext", Map.class, true);
        infoBuilder.addAttribute("userTransaction",
                OnlineUserTransaction.class, true);
        infoBuilder.addReference("TransactionContextManager",
                TransactionContextManager.class, NameFactory.JTA_RESOURCE);
        infoBuilder.addReference("TrackedConnectionAssociator",
                TrackedConnectionAssociator.class, NameFactory.JCA_RESOURCE);

        infoBuilder.addReference("Container", TomcatContainer.class,
                NameFactory.GERONIMO_SERVICE);
        infoBuilder.addReference("RoleDesignateSource",
                RoleDesignateSource.class, NameFactory.JACC_MANAGER);
        infoBuilder.addReference("J2EEServer", J2EEServer.class);
        infoBuilder.addReference("J2EEApplication", J2EEApplication.class);
        infoBuilder.addAttribute("kernel", Kernel.class, false);

        infoBuilder.setConstructor(new String[] { 
                "objectName",
                "deploymentDescriptor",
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
                "componentContext",
                "userTransaction", 
                "TransactionContextManager",
                "TrackedConnectionAssociator", 
                "Container",
                "RoleDesignateSource", 
                "J2EEServer", 
                "J2EEApplication",
                "kernel" 
                }
        );

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
