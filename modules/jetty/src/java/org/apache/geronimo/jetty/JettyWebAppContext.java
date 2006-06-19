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
import java.net.MalformedURLException;
import java.net.URL;
import java.security.PermissionCollection;
import java.util.Collection;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
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
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.impl.InvalidObjectNameException;
import org.apache.geronimo.jetty.interceptor.BeforeAfter;
import org.apache.geronimo.jetty.interceptor.ComponentContextBeforeAfter;
import org.apache.geronimo.jetty.interceptor.InstanceContextBeforeAfter;
import org.apache.geronimo.jetty.interceptor.RequestWrappingBeforeAfter;
import org.apache.geronimo.jetty.interceptor.SecurityContextBeforeAfter;
import org.apache.geronimo.jetty.interceptor.ThreadClassloaderBeforeAfter;
import org.apache.geronimo.jetty.interceptor.TransactionContextBeforeAfter;
import org.apache.geronimo.jetty.interceptor.WebApplicationContextBeforeAfter;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.ObjectNameUtil;
import org.apache.geronimo.management.J2EEApplication;
import org.apache.geronimo.management.J2EEServer;
import org.apache.geronimo.management.geronimo.WebConnector;
import org.apache.geronimo.management.geronimo.WebContainer;
import org.apache.geronimo.management.geronimo.WebModule;
import org.apache.geronimo.naming.enc.EnterpriseNamingContext;
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
import org.mortbay.jetty.servlet.SessionManager;
import org.mortbay.jetty.servlet.WebApplicationContext;
import org.mortbay.jetty.servlet.WebApplicationHandler;

/**
 * Wrapper for a WebApplicationContext that sets up its J2EE environment.
 *
 * @version $Rev$ $Date$
 */
public class JettyWebAppContext extends WebApplicationContext implements GBeanLifecycle, JettyServletRegistration, WebModule {
    private static Log log = LogFactory.getLog(JettyWebAppContext.class);

    private final String originalSpecDD;
    private final J2EEServer server;
    private final J2EEApplication application;

    private final ClassLoader webClassLoader;
    private final JettyContainer jettyContainer;

    private final String webAppRoot;
    private final URL configurationBaseURL;
    private final WebApplicationHandler handler;
    private String displayName;
    private final String[] welcomeFiles;

    private final BeforeAfter chain;
    private final int contextLength;
    private final SecurityContextBeforeAfter securityInterceptor;

    private final String objectName;

    private final Set servletNames = new HashSet();

    private String sessionManager;


    public static class SessionManagerConfiguration implements WebApplicationContext.Configuration {

        private WebApplicationContext webAppContext;


        public SessionManagerConfiguration() {
        }


        public void setWebApplicationContext(WebApplicationContext webAppContext) {
            this.webAppContext = webAppContext;
        }

        public WebApplicationContext getWebApplicationContext() {
            return this.webAppContext;
        }

        public void configureClassPath() throws Exception {
        }

        public void configureDefaults() throws Exception {
        }


        public void configureWebApp() throws Exception {
            //setup a SessionManager
            log.debug("About to configure a SessionManager");
            String sessionManagerClassName = ((JettyWebAppContext) webAppContext).getSessionManager();
            if (sessionManagerClassName != null) {
                Class clazz = Thread.currentThread().getContextClassLoader().loadClass(sessionManagerClassName);
                Object o = clazz.newInstance();
                log.debug("Setting SessionManager type=" + clazz.getName() + " instance=" + o);
                this.webAppContext.getServletHandler().setSessionManager((SessionManager) o);
            }
        }

    }

    /**
     * @deprecated never use this... this is only here because Jetty WebApplicationContext is externalizable
     */
    public JettyWebAppContext() {
        server = null;
        application = null;
        originalSpecDD = null;
        webClassLoader = null;
        jettyContainer = null;
        webAppRoot = null;
        handler = null;
        chain = null;
        contextLength = 0;
        securityInterceptor = null;
        welcomeFiles = null;
        objectName = null;
        sessionManager = null;
        configurationBaseURL = null;
    }

    public JettyWebAppContext(String objectName,
                              String originalSpecDD,
                              String sessionManager,
                              Map componentContext,
                              OnlineUserTransaction userTransaction,
                              ClassLoader classLoader,
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

                              Host host,
                              TransactionContextManager transactionContextManager,
                              TrackedConnectionAssociator trackedConnectionAssociator,
                              JettyContainer jettyContainer,
                              RoleDesignateSource roleDesignateSource,
                              J2EEServer server,
                              J2EEApplication application,
                              Kernel kernel) throws Exception, IllegalAccessException, InstantiationException, ClassNotFoundException {

        assert componentContext != null;
        assert userTransaction != null;
        assert classLoader != null;
        assert configurationBaseUrl != null;
        assert transactionContextManager != null;
        assert trackedConnectionAssociator != null;
        assert jettyContainer != null;

        this.server = server;
        this.application = application;
        this.objectName = objectName;
        if (objectName != null) {
            ObjectName myObjectName = ObjectNameUtil.getObjectName(objectName);
            verifyObjectName(myObjectName);
        }
        this.configurationBaseURL = configurationBaseUrl;

        this.jettyContainer = jettyContainer;

        this.originalSpecDD = originalSpecDD;

        setConfigurationClassNames(new String[]{});

        webAppRoot = configurationBaseUrl.toString();
        this.webClassLoader = classLoader;
        setClassLoader(this.webClassLoader);

        if (host != null) {
            setHosts(host.getHosts());
            setVirtualHosts(host.getVirtualHosts());
        }

        //use our wrapper to avoid leaking subject back to the caller
        handler = new JettyWebApplicationHandler();
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
        for (Iterator iterator = componentContext.values().iterator(); iterator.hasNext();) {
            Object value = iterator.next();
            if (value instanceof KernelAwareReference) {
                ((KernelAwareReference) value).setKernel(kernel);
            }
            if (value instanceof ClassLoaderAwareReference) {
                ((ClassLoaderAwareReference) value).setClassLoader(this.webClassLoader);
            }
        }
        Context enc = EnterpriseNamingContext.createEnterpriseNamingContext(componentContext);

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
            InternalJAASJettyRealm internalJAASJettyRealm = jettyContainer.addRealm(securityRealmName);
            //wrap jetty realm with something that knows the dumb realmName
            JAASJettyRealm realm = new JAASJettyRealm(realmName, internalJAASJettyRealm);
            setRealm(realm);
            this.securityInterceptor = new SecurityContextBeforeAfter(interceptor, index++, index++, policyContextID, defaultPrincipal, authenticator, checkedPermissions, excludedPermissions, roleDesignates, realm, classLoader);
            interceptor = this.securityInterceptor;
        } else {
            securityInterceptor = null;
        }
//      end JACC
        interceptor = new RequestWrappingBeforeAfter(interceptor, handler);
        chain = interceptor;
        contextLength = index;

        //cheat -- add jsr154 filter not as a gbean
        FilterHolder jsr154FilterHolder = new FilterHolder(handler, "jsr154", JSR154Filter.class.getName());
        handler.addFilterHolder(jsr154FilterHolder);
        jsr154FilterHolder.setInitParameter("unwrappedDispatch", "true");
        handler.addFilterPathMapping("/*", "jsr154", Dispatcher.__REQUEST | Dispatcher.__FORWARD | Dispatcher.__INCLUDE | Dispatcher.__ERROR);

        configureSessionManager(sessionManager);

    }


    public String getObjectName() {
        return objectName;
    }

    public boolean isStateManageable() {
        return true;
    }

    public boolean isStatisticsProvider() {
        return false;
    }

    public boolean isEventProvider() {
        return true;
    }

    public URL getWARDirectory() {
        return configurationBaseURL;
    }

    public String getWARName() {
        //todo: make this return something more consistent
        try {
            return ObjectName.getInstance(objectName).getKeyProperty(NameFactory.J2EE_NAME);
        } catch (MalformedObjectNameException e) {
            return null;
        }
    }

    public WebContainer getContainer() {
        return jettyContainer;
    }

    public URL getURLFor() {
        WebConnector[] connectors = (WebConnector[]) jettyContainer.getConnectors();
        Map map = new HashMap();
        for (int i = 0; i < connectors.length; i++) {
            WebConnector connector = connectors[i];
            map.put(connector.getProtocol(), connector.getConnectUrl());
        }
        String urlPrefix;
        if((urlPrefix = (String) map.get("HTTP")) == null) {
            if((urlPrefix = (String) map.get("HTTPS")) == null) {
                urlPrefix = (String) map.get("AJP");
            }
        }
        if(urlPrefix == null) {
            return null;
        }
        try {
            return new URL(urlPrefix + getContextPath());
        } catch (MalformedURLException e) {
            log.error("Bad URL to connect to web app", e);
            return null;
        }
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

        ((AbstractSessionManager) getServletHandler().getSessionManager()).setUseRequestedId(true);

        setWAR(webAppRoot);

        jettyContainer.addContext(this);

        Object context = enterContextScope(null, null);
        try {
            super.doStart();
        } finally {
            leaveContextScope(null, null, context);
        }
        //super.doStart sets welcomefiles to null!!
        setWelcomeFiles(welcomeFiles);

        log.debug("JettyWebAppContext started");
    }

    public void doStop() throws Exception {
        // merge Geronimo and Jetty Lifecycles
        if (!isStopping()) {
            super.stop();
            return;
        }

        if (securityInterceptor != null) {
            securityInterceptor.stop(jettyContainer);
        }
        Object context = enterContextScope(null, null);
        try {
            super.doStop();
        } finally {
            leaveContextScope(null, null, context);
            jettyContainer.removeContext(this);
        }

        // No more logging will occur for this ClassLoader. Inform the LogFactory to avoid a memory leak.
        LogFactory.release(webClassLoader);

        log.debug("JettyWebAppContext stopped");
    }

    public void doFail() {
        try {
            //this will call doStop
            super.stop();
        } catch (InterruptedException e) {
        }

        log.warn("JettyWebAppContext failed");
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

    public String[] getServlets() {
        synchronized(servletNames) {
            return (String[]) servletNames.toArray(new String[servletNames.size()]);
        }
    }

    public String getSessionManager() {
        return this.sessionManager;
    }


    private void configureSessionManager(String sessionManagerClassName) {
        this.sessionManager = sessionManagerClassName;
        if (this.sessionManager != null) {
            addConfiguration(SessionManagerConfiguration.class.getName());
        }
    }

    private void addConfiguration(String configClassName) {
        String[] configClassNames = getConfigurationClassNames();
        String[] newConfigClassNames = new String[configClassNames == null ? 1 : configClassNames.length + 1];
        System.arraycopy(configClassNames, 0, newConfigClassNames, 0, configClassNames.length);

        newConfigClassNames[newConfigClassNames.length - 1] = configClassName;
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

    public void registerServletHolder(ServletHolder servletHolder, String servletName, Set servletMappings, String objectName) throws Exception {
        //TODO filters
        handler.addServletHolder(servletHolder);
        if (servletMappings != null) {
            for (Iterator iterator = servletMappings.iterator(); iterator.hasNext();) {
                String urlPattern = (String) iterator.next();
                handler.mapPathToServlet(urlPattern, servletName);
            }
        }
        Object context = enterContextScope(null, null);
        try {
            servletHolder.start();
        } finally {
            leaveContextScope(null, null, context);
        }
        if (objectName != null) {
            synchronized(servletNames) {
                servletNames.add(objectName);
            }
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
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic("Jetty WebApplication Context", JettyWebAppContext.class, NameFactory.WEB_MODULE);
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


        infoBuilder.addAttribute("sessionManager", String.class, true);
        infoBuilder.addAttribute("componentContext", Map.class, true);
        infoBuilder.addAttribute("userTransaction", OnlineUserTransaction.class, true);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);
        infoBuilder.addAttribute("configurationBaseUrl", URL.class, true);
        infoBuilder.addAttribute("unshareableResources", Set.class, true);
        infoBuilder.addAttribute("applicationManagedSecurityResources", Set.class, true);

        infoBuilder.addAttribute("contextPath", String.class, true);

        infoBuilder.addReference("Host", Host.class, "Host");
        infoBuilder.addReference("TransactionContextManager", TransactionContextManager.class, NameFactory.TRANSACTION_CONTEXT_MANAGER);
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

        infoBuilder.addInterface(WebModule.class);

        infoBuilder.setConstructor(new String[]{
                "objectName",
                "deploymentDescriptor",
                "sessionManager",
                "componentContext",
                "userTransaction",
                "classLoader",
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

                "Host",
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
