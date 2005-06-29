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

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.PermissionCollection;
import java.util.Collection;
import java.util.EventListener;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.J2EEApplication;
import org.apache.geronimo.j2ee.management.J2EEServer;
import org.apache.geronimo.j2ee.management.impl.InvalidObjectNameException;
import org.apache.geronimo.j2ee.management.impl.Util;
import org.apache.geronimo.jetty.interceptor.BeforeAfter;
import org.apache.geronimo.jetty.interceptor.ComponentContextBeforeAfter;
import org.apache.geronimo.jetty.interceptor.InstanceContextBeforeAfter;
import org.apache.geronimo.jetty.interceptor.RequestWrappingBeforeAfter;
import org.apache.geronimo.jetty.interceptor.SecurityContextBeforeAfter;
import org.apache.geronimo.jetty.interceptor.ThreadClassloaderBeforeAfter;
import org.apache.geronimo.jetty.interceptor.TransactionContextBeforeAfter;
import org.apache.geronimo.jetty.interceptor.WebApplicationContextBeforeAfter;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.naming.java.SimpleReadOnlyContext;
import org.apache.geronimo.naming.reference.ClassLoaderAwareReference;
import org.apache.geronimo.naming.reference.KernelAwareReference;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.security.jacc.RoleDesignateSource;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.apache.geronimo.transaction.context.OnlineUserTransaction;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.mortbay.http.Authenticator;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.jetty.servlet.AbstractSessionManager;
import org.mortbay.jetty.servlet.Dispatcher;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.JSR154Filter;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.WebApplicationContext;
import org.mortbay.jetty.servlet.WebApplicationHandler;

/**
 * Wrapper for a WebApplicationContext that sets up its J2EE environment.
 *
 * @version $Rev$ $Date$
 */
public class JettyWebAppContext extends WebApplicationContext implements GBeanLifecycle, JettyServletRegistration {
    private static Log log = LogFactory.getLog(JettyWebAppContext.class);

    private final Kernel kernel;
    //jsr-77 stuff
    private final J2eeContext moduleContext;
    private final String originalSpecDD;
    private final J2EEServer server;
    private final J2EEApplication application;

    private final ClassLoader webClassLoader;
    private final JettyContainer jettyContainer;

    private final URI webAppRoot;
    private final WebApplicationHandler handler;
    private String displayName;
    private final String[] welcomeFiles;

    private final  BeforeAfter chain;
    private final  int contextLength;
    private final SecurityContextBeforeAfter securityInterceptor;
    private static final String[] J2EE_TYPES = {NameFactory.SERVLET};

    /**
     * @deprecated never use this... this is only here because Jetty WebApplicationContext is externalizable
     */
    public JettyWebAppContext() {
        kernel = null;
        server = null;
        application = null;
        moduleContext = null;
        originalSpecDD = null;
        webClassLoader = null;
        jettyContainer = null;
        webAppRoot = null;
        handler = null;
        chain = null;
        contextLength = 0;
        securityInterceptor = null;
        welcomeFiles = null;

    }

    public JettyWebAppContext(String objectName,
                              String originalSpecDD,
                              URI uri,
                              Map componentContext,
                              OnlineUserTransaction userTransaction,
                              ClassLoader classLoader,
                              URI[] webClassPath,
                              boolean contextPriorityClassLoader,
                              URL configurationBaseUrl,
                              Set unshareableResources,
                              Set applicationManagedSecurityResources,

                              String displayName,
                              Map contextParamMap,
                              Collection listenerClassNames,
                              boolean distributable,
                              Map mimeMap,
                              String[] welcomeFiles,
                              Map localeEncodingMapping,
                              Map errorPages,
                              Authenticator authenticator,
                              String realmName,
                              Map tagLibMap,
                              int sessionTimeoutSeconds,

                              String policyContextID,
                              String securityRealmName,
                              DefaultPrincipal defaultPrincipal,
                              PermissionCollection checkedPermissions,
                              PermissionCollection excludedPermissions,

                              TransactionContextManager transactionContextManager,
                              TrackedConnectionAssociator trackedConnectionAssociator,
                              JettyContainer jettyContainer,
                              RoleDesignateSource roleDesignateSource,
                              J2EEServer server,
                              J2EEApplication application,
                              Kernel kernel) throws Exception, IllegalAccessException, InstantiationException, ClassNotFoundException {

        assert uri != null;
        assert componentContext != null;
        assert userTransaction != null;
        assert classLoader != null;
        assert webClassPath != null;
        assert configurationBaseUrl != null;
        assert transactionContextManager != null;
        assert trackedConnectionAssociator != null;
        assert jettyContainer != null;

        this.kernel = kernel;
        this.server = server;
        this.application = application;
        ObjectName myObjectName = JMXUtil.getObjectName(objectName);
        verifyObjectName(myObjectName);
        moduleContext = J2eeContextImpl.newContext(myObjectName, NameFactory.WEB_MODULE);

        this.jettyContainer = jettyContainer;

        this.originalSpecDD = originalSpecDD;

        setConfigurationClassNames(new String[]{});

        URI root = URI.create(configurationBaseUrl.toString());
        webAppRoot = root.resolve(uri);
        URL webAppRootURL = webAppRoot.toURL();

        URL[] urls = new URL[webClassPath.length];
        for (int i = 0; i < webClassPath.length; i++) {
            URI classPathEntry = webClassPath[i];
            classPathEntry = root.resolve(classPathEntry);
            urls[i] = classPathEntry.toURL();
        }
        this.webClassLoader = new JettyClassLoader(urls, webAppRootURL, classLoader, contextPriorityClassLoader);
        setClassLoader(this.webClassLoader);

        handler = new WebApplicationHandler();
        addHandler(handler);

        userTransaction.setUp(transactionContextManager, trackedConnectionAssociator);

        //stuff from spec dd
        setDisplayName(displayName);
        setContextParamMap(contextParamMap);
        setListenerClassNames(listenerClassNames);
        setDistributable(distributable);
        setMimeMap(mimeMap);
        this.welcomeFiles = welcomeFiles;
        setLocaleEncodingMapping(localeEncodingMapping);
        setErrorPages(errorPages);
        setAuthenticator(authenticator);
        setRealmName(realmName);
        setTagLibMap(tagLibMap);
        setSessionTimeoutSeconds(sessionTimeoutSeconds);

        // create ReadOnlyContext
        Context enc = null;
        if (componentContext != null) {
            for (Iterator iterator = componentContext.values().iterator(); iterator.hasNext();) {
                Object value = iterator.next();
                if (value instanceof KernelAwareReference) {
                    ((KernelAwareReference) value).setKernel(kernel);
                }
                if (value instanceof ClassLoaderAwareReference) {
                    ((ClassLoaderAwareReference) value).setClassLoader(this.webClassLoader);
                }
            }
            enc = new SimpleReadOnlyContext(componentContext);
        }

        int index = 0;
        BeforeAfter interceptor = new InstanceContextBeforeAfter(null, index++, unshareableResources, applicationManagedSecurityResources, trackedConnectionAssociator);
        interceptor = new TransactionContextBeforeAfter(interceptor, index++, index++, transactionContextManager);
        interceptor = new ComponentContextBeforeAfter(interceptor, index++, enc);
        interceptor = new ThreadClassloaderBeforeAfter(interceptor, index++, index++, this.webClassLoader);
        interceptor = new WebApplicationContextBeforeAfter(interceptor, index++, this);
//JACC
        if (securityRealmName != null) {
            if (roleDesignateSource == null) {
                throw new IllegalArgumentException("RoleDesignateSource must be supplied for a secure web app");
            }
            Map roleDesignates = roleDesignateSource.getRoleDesignateMap();
            //set the JAASJettyRealm as our realm.
            JAASJettyRealm realm = new JAASJettyRealm(realmName, securityRealmName);
            setRealm(realm);
            this.securityInterceptor = new SecurityContextBeforeAfter(interceptor, index++, index++, policyContextID, defaultPrincipal, authenticator, checkedPermissions, excludedPermissions, roleDesignates, realm);
            interceptor = this.securityInterceptor;
        } else {
            securityInterceptor = null;
        }
//end JACC
        interceptor = new RequestWrappingBeforeAfter(interceptor, handler);
        chain = interceptor;
        contextLength = index;

        //cheat -- add jsr154 filter not as a gbean
        FilterHolder jsr154FilterHolder = new FilterHolder(handler, "jsr154", JSR154Filter.class.getName());
        handler.addFilterHolder(jsr154FilterHolder);
        jsr154FilterHolder.setInitParameter("unwrappedDispatch", "true");
        handler.addFilterPathMapping("/*", "jsr154", Dispatcher.__REQUEST | Dispatcher.__FORWARD | Dispatcher.__INCLUDE | Dispatcher.__ERROR );
    }

    public Object enterContextScope(HttpRequest httpRequest, HttpResponse httpResponse) {
        Object[] context = new Object[contextLength];
        chain.before(context, httpRequest, httpResponse);
        return context;
    }

    public void leaveContextScope(HttpRequest httpRequest, HttpResponse httpResponse, Object oldScope) {
        Object[] context = (Object[]) oldScope;
        chain.after(context, httpRequest, httpResponse);
    }


    public ClassLoader getWebClassLoader() {
        return webClassLoader;
    }

    public void doStart() throws Exception {
        // reset the classsloader... jetty likes to set it to null when stopping
        setClassLoader(webClassLoader);

        // merge Geronimo and Jetty Lifecycles
        if (!isStarting()) {
            super.start();
            return;
        }

        ((AbstractSessionManager)getServletHandler().getSessionManager()).setUseRequestedId(true);

        setWAR(webAppRoot.toString());

        jettyContainer.addContext(this);

        Object context = enterContextScope(null, null);
        try {
            super.doStart();
        } finally {
            leaveContextScope(null, null, context);
        }
        //super.doStart sets welcomefiles to null!!
        setWelcomeFiles(welcomeFiles);

        log.info("JettyWebAppContext started");
    }

    public void doStop() throws Exception {
        // merge Geronimo and Jetty Lifecycles
        if (!isStopping()) {
            super.stop();
            return;
        }

        if (securityInterceptor != null) {
            securityInterceptor.stop();
        }
        Object context = enterContextScope(null, null);
        try {
            super.doStop();
        } finally {
            leaveContextScope(null, null, context);
            jettyContainer.removeContext(this);
        }
        log.info("JettyWebAppContext stopped");
    }

    public void doFail() {
        try {
            //this will call doStop
            super.stop();
        } catch (InterruptedException e) {
        }

        log.info("JettyWebAppContext failed");
    }


    //pass through attributes.  They should be constructor params

    //TODO encourage jetty to improve their naming convention.
    public void setContextParamMap(Map initParameters) {
        if (initParameters != null) {
            for (Iterator iterator = initParameters.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                setInitParameter((String) entry.getKey(), (String) entry.getValue());
            }
        }
    }

    public void setLocaleEncodingMapping(Map localeEncodingMap) {
        if (localeEncodingMap != null) {
            for (Iterator iterator = localeEncodingMap.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                addLocaleEncoding((String) entry.getKey(), (String) entry.getValue());
            }
        }
    }

    public void setListenerClassNames(Collection eventListeners) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (eventListeners != null) {
            for (Iterator iterator = eventListeners.iterator(); iterator.hasNext();) {
                String listenerClassName = (String) iterator.next();
                Class clazz = loadClass(listenerClassName);
                EventListener listener = (EventListener) clazz.newInstance();
                addEventListener(listener);
                handler.addEventListener(listener);
            }
        }
    }

    public void setErrorPages(Map errorPageMap) {
        if (errorPageMap != null) {
            for (Iterator iterator = errorPageMap.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                setErrorPage((String) entry.getKey(), (String) entry.getValue());
            }
        }
    }

    public void setTagLibMap(Map tagLibMap) {
        if (tagLibMap != null) {
            for (Iterator iterator = tagLibMap.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                setResourceAlias((String) entry.getKey(), (String) entry.getValue());
            }
        }
    }

    public void setSessionTimeoutSeconds(int seconds) {
        handler.setSessionInactiveInterval(seconds);
    }


    //TODO this is really dumb, but jetty likes to set the displayname to null frequently.
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        super.setDisplayName(displayName);
    }

    public String getDeploymentDescriptor() {
        return originalSpecDD;
    }

    public String getServer() {
        return server.getObjectName();
    }

    public String getApplication() {
        if (application == null) {
            return null;
        }
        return application.getObjectName();
    }

    public String[] getJavaVMs() {
        return server.getJavaVMs();
    }

    public String[] getServlets() throws MalformedObjectNameException {
        return Util.getObjectNames(kernel, moduleContext, J2EE_TYPES);
    }

    /**
     * ObjectName must match this pattern:
     * <p/>
     * domain:j2eeType=WebModule,name=MyName,J2EEServer=MyServer,J2EEApplication=MyApplication
     */
    private void verifyObjectName(ObjectName objectName) {
        if (objectName.isPattern()) {
            throw new InvalidObjectNameException("ObjectName can not be a pattern", objectName);
        }
        Hashtable keyPropertyList = objectName.getKeyPropertyList();
        if (!NameFactory.WEB_MODULE.equals(keyPropertyList.get("j2eeType"))) {
            throw new InvalidObjectNameException("WebModule object name j2eeType property must be 'WebModule'", objectName);
        }
        if (!keyPropertyList.containsKey(NameFactory.J2EE_NAME)) {
            throw new InvalidObjectNameException("WebModule object must contain a name property", objectName);
        }
        if (!keyPropertyList.containsKey(NameFactory.J2EE_SERVER)) {
            throw new InvalidObjectNameException("WebModule object name must contain a J2EEServer property", objectName);
        }
        if (!keyPropertyList.containsKey(NameFactory.J2EE_APPLICATION)) {
            throw new InvalidObjectNameException("WebModule object name must contain a J2EEApplication property", objectName);
        }
        if (keyPropertyList.size() != 4) {
            throw new InvalidObjectNameException("WebModule object name can only have j2eeType, name, J2EEApplication, and J2EEServer properties", objectName);
        }
    }
    public void registerServletHolder(ServletHolder servletHolder, String servletName, Set servletMappings, Map webRoleRefPermissions) throws Exception {
        //TODO filters
        handler.addServletHolder(servletHolder);
        if (servletMappings != null) {
            for (Iterator iterator = servletMappings.iterator(); iterator.hasNext();) {
                String urlPattern = (String) iterator.next();
                handler.mapPathToServlet(urlPattern, servletName);
            }
        }
//        if (securityInterceptor != null) {
//            securityInterceptor.registerServletHolder(webRoleRefPermissions);
//        }
        Object context = enterContextScope(null, null);
        try {
            servletHolder.start();
        } finally {
            leaveContextScope(null, null, context);
        }
    }

    public boolean checkSecurityConstraints(String pathInContext, HttpRequest request, HttpResponse response) throws HttpException, IOException {
         if (securityInterceptor != null) {
             return securityInterceptor.checkSecurityConstraints(pathInContext, request, response);
         }
         return super.checkSecurityConstraints(pathInContext, request, response);
     }


    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder("Jetty WebApplication Context", JettyWebAppContext.class, NameFactory.WEB_MODULE);
        infoBuilder.addAttribute("deploymentDescriptor", String.class, true);
        //from jetty's webapp context

        infoBuilder.addAttribute("displayName", String.class, true);
        infoBuilder.addAttribute("contextParamMap", Map.class, true);
        infoBuilder.addAttribute("listenerClassNames", Collection.class, true);
        infoBuilder.addAttribute("distributable", boolean.class, true);

        infoBuilder.addAttribute("mimeMap", Map.class, true);
        infoBuilder.addAttribute("welcomeFiles", String[].class, true);
        infoBuilder.addAttribute("localeEncodingMapping", Map.class, true);
        infoBuilder.addAttribute("errorPages", Map.class, true);
        infoBuilder.addAttribute("authenticator", Authenticator.class, true);
        infoBuilder.addAttribute("realmName", String.class, true);
        infoBuilder.addAttribute("tagLibMap", Map.class, true);
        infoBuilder.addAttribute("sessionTimeoutSeconds", int.class, true);


        infoBuilder.addAttribute("uri", URI.class, true);
        infoBuilder.addAttribute("componentContext", Map.class, true);
        infoBuilder.addAttribute("userTransaction", OnlineUserTransaction.class, true);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);
        infoBuilder.addAttribute("webClassPath", URI[].class, true);
        infoBuilder.addAttribute("contextPriorityClassLoader", boolean.class, true);
        infoBuilder.addAttribute("configurationBaseUrl", URL.class, true);
        infoBuilder.addAttribute("unshareableResources", Set.class, true);
        infoBuilder.addAttribute("applicationManagedSecurityResources", Set.class, true);

        infoBuilder.addAttribute("contextPath", String.class, true);

        infoBuilder.addReference("TransactionContextManager", TransactionContextManager.class, NameFactory.JTA_RESOURCE);
        infoBuilder.addReference("TrackedConnectionAssociator", TrackedConnectionAssociator.class, NameFactory.JCA_CONNECTION_TRACKER);
        infoBuilder.addReference("JettyContainer", JettyContainer.class, NameFactory.GERONIMO_SERVICE);
        infoBuilder.addReference("RoleDesignateSource", RoleDesignateSource.class, NameFactory.JACC_MANAGER);

        infoBuilder.addInterface(JettyServletRegistration.class);

        infoBuilder.addAttribute("policyContextID", String.class, true);
        infoBuilder.addAttribute("securityRealmName", String.class, true);
        infoBuilder.addAttribute("defaultPrincipal", DefaultPrincipal.class, true);

        infoBuilder.addAttribute("checkedPermissions", PermissionCollection.class, true);
        infoBuilder.addAttribute("excludedPermissions", PermissionCollection.class, true);

        infoBuilder.addReference("J2EEServer", J2EEServer.class);
        infoBuilder.addReference("J2EEApplication", J2EEApplication.class);

        infoBuilder.addAttribute("kernel", Kernel.class, false);
        infoBuilder.addAttribute("objectName", String.class, false);
        infoBuilder.addAttribute("server", String.class, false);
        infoBuilder.addAttribute("application", String.class, false);
        infoBuilder.addAttribute("javaVMs", String[].class, false);
        infoBuilder.addAttribute("servlets", String[].class, false);

        infoBuilder.setConstructor(new String[]{
            "objectName",
            "deploymentDescriptor",
            "uri",
            "componentContext",
            "userTransaction",
            "classLoader",
            "webClassPath",
            "contextPriorityClassLoader",
            "configurationBaseUrl",
            "unshareableResources",
            "applicationManagedSecurityResources",

            "displayName",
            "contextParamMap",
            "listenerClassNames",
            "distributable",
            "mimeMap",
            "welcomeFiles",
            "localeEncodingMapping",
            "errorPages",
            "authenticator",
            "realmName",
            "tagLibMap",
            "sessionTimeoutSeconds",

            "policyContextID",
            "securityRealmName",
            "defaultPrincipal",

            "checkedPermissions",
            "excludedPermissions",

            "TransactionContextManager",
            "TrackedConnectionAssociator",
            "JettyContainer",
            "RoleDesignateSource",

            "J2EEServer",
            "J2EEApplication",
            "kernel"
        });

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
