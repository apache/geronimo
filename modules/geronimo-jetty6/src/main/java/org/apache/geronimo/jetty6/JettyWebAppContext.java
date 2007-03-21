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

package org.apache.geronimo.jetty6;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.PermissionCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.faces.FactoryFinder;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.annotation.Holder;
import org.apache.geronimo.j2ee.annotation.LifecycleMethod;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.impl.InvalidObjectNameException;
import org.apache.geronimo.jetty6.handler.AbstractImmutableHandler;
import org.apache.geronimo.jetty6.handler.ComponentContextHandler;
import org.apache.geronimo.jetty6.handler.InstanceContextHandler;
import org.apache.geronimo.jetty6.handler.JettySecurityHandler;
import org.apache.geronimo.jetty6.handler.LifecycleCommand;
import org.apache.geronimo.jetty6.handler.ThreadClassloaderHandler;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.ObjectNameUtil;
import org.apache.geronimo.management.J2EEApplication;
import org.apache.geronimo.management.J2EEServer;
import org.apache.geronimo.management.geronimo.WebConnector;
import org.apache.geronimo.management.geronimo.WebContainer;
import org.apache.geronimo.management.geronimo.WebModule;
import org.apache.geronimo.naming.enc.EnterpriseNamingContext;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.transaction.GeronimoUserTransaction;
import org.mortbay.jetty.MimeTypes;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.handler.AbstractHandlerContainer;
import org.mortbay.jetty.security.Authenticator;
import org.mortbay.jetty.servlet.ErrorPageErrorHandler;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletMapping;
import org.mortbay.jetty.servlet.SessionHandler;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * Wrapper for a WebApplicationContext that sets up its J2EE environment.
 *
 * @version $Rev$ $Date$
 */
public class JettyWebAppContext implements GBeanLifecycle, JettyServletRegistration, WebModule {
    private static Log log = LogFactory.getLog(JettyWebAppContext.class);

    private final String originalSpecDD;
    private final J2EEServer server;
    private final J2EEApplication application;

    private final ClassLoader webClassLoader;
    private final JettyContainer jettyContainer;

    private final String webAppRoot;
    private final URL configurationBaseURL;
    private String displayName;

    private final String objectName;
    private final WebAppContext webAppContext;//delegate
    private final AbstractHandlerContainer contextHandler;
    private final AbstractImmutableHandler lifecycleChain;
    private final Context componentContext;
    private final Holder holder;

    private final Set<String> servletNames = new HashSet<String>();

    public JettyWebAppContext(String objectName,
            String originalSpecDD,
            Map<String, Object> componentContext,
            ClassLoader classLoader,
            URL configurationBaseUrl,
            Set unshareableResources,
            Set applicationManagedSecurityResources,
            String displayName,
            Map contextParamMap,
            Collection<String> listenerClassNames,
            boolean distributable,
            Map mimeMap,
            String[] welcomeFiles,
            Map<String, String> localeEncodingMapping,
            Map errorPages,
            Authenticator authenticator,
            String realmName,
            Map<String, String> tagLibMap,
            int sessionTimeoutSeconds,
            SessionHandlerFactory handlerFactory,
            PreHandlerFactory preHandlerFactory,

            String policyContextID,
            String securityRealmName,
            DefaultPrincipal defaultPrincipal,
            PermissionCollection checkedPermissions,
            PermissionCollection excludedPermissions,

            Holder holder,

            Host host,
            TransactionManager transactionManager,
            TrackedConnectionAssociator trackedConnectionAssociator,
            JettyContainer jettyContainer,
            J2EEServer server,
            J2EEApplication application,
            Kernel kernel) throws Exception {

        assert componentContext != null;
        assert classLoader != null;
        assert configurationBaseUrl != null;
        assert transactionManager != null;
        assert trackedConnectionAssociator != null;
        assert jettyContainer != null;

        this.holder = holder == null ? Holder.EMPTY : holder;

        SessionHandler sessionHandler;
        if (null != handlerFactory) {
            if (null == preHandlerFactory) {
                throw new IllegalStateException("A preHandlerFactory must be set if an handler factory is set.");
            }
            PreHandler preHandler = preHandlerFactory.createHandler();
            sessionHandler = handlerFactory.createHandler(preHandler);
        } else {
            sessionHandler = new SessionHandler();
        }
        JettySecurityHandler securityHandler = null;
        if (securityRealmName != null) {
            securityHandler = new JettySecurityHandler();
            InternalJAASJettyRealm internalJAASJettyRealm = jettyContainer.addRealm(securityRealmName);
            //wrap jetty realm with something that knows the dumb realmName
            JAASJettyRealm realm = new JAASJettyRealm(realmName, internalJAASJettyRealm);
            securityHandler.setUserRealm(realm);

            securityHandler.init(policyContextID, defaultPrincipal, checkedPermissions, excludedPermissions, classLoader);
        }

        ServletHandler servletHandler = new ServletHandler();

        webAppContext = new WebAppContext(securityHandler, sessionHandler, servletHandler, null);

        //wrap the web app context with the jndi handler
        GeronimoUserTransaction userTransaction = new GeronimoUserTransaction(transactionManager);
        this.componentContext = EnterpriseNamingContext.createEnterpriseNamingContext(componentContext, userTransaction, kernel, classLoader);
        contextHandler = new ComponentContextHandler(webAppContext, this.componentContext);

        // localize access to next
        {
            //install the other handlers inside the web app context
            AbstractHandler next = sessionHandler;
            next = new ThreadClassloaderHandler(next, classLoader);

            next = new InstanceContextHandler(next, unshareableResources, applicationManagedSecurityResources, trackedConnectionAssociator);
            webAppContext.setHandler(next);

            //install another component context handler for the lifecycle chain
            next = new ComponentContextHandler(next, this.componentContext);
            lifecycleChain = (AbstractImmutableHandler) next;
        }
        MimeTypes mimeTypes = new MimeTypes();
        mimeTypes.setMimeMap(mimeMap);
        webAppContext.setMimeTypes(mimeTypes);

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

        webAppContext.setConfigurationClasses(new String[]{"org.mortbay.jetty.webapp.TagLibConfiguration"});

        webAppRoot = configurationBaseUrl.toString();
        webClassLoader = classLoader;
        webAppContext.setClassLoader(webClassLoader);

        if (host != null) {
            webAppContext.setConnectorNames(host.getHosts());
            webAppContext.setVirtualHosts(host.getVirtualHosts());
        }

        //stuff from spec dd
        webAppContext.setDisplayName(displayName);
        webAppContext.setInitParams(contextParamMap);
        setListenerClassNames(listenerClassNames);
        webAppContext.setDistributable(distributable);
        webAppContext.setWelcomeFiles(welcomeFiles);
        setLocaleEncodingMapping(localeEncodingMapping);
        setErrorPages(errorPages);
        webAppContext.getSecurityHandler().setAuthenticator(authenticator);
        setTagLibMap(tagLibMap);

        if (!distributable) {
            setSessionTimeoutSeconds(sessionTimeoutSeconds);
        }

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
        Map<String, String> map = new HashMap<String, String>();
        for (WebConnector connector : connectors) {
            map.put(connector.getProtocol(), connector.getConnectUrl());
        }
        String urlPrefix;
        if ((urlPrefix = map.get("HTTP")) == null) {
            if ((urlPrefix = map.get("HTTPS")) == null) {
                urlPrefix = map.get("AJP");
            }
        }
        if (urlPrefix == null) {
            return null;
        }
        StringBuffer buf = new StringBuffer(urlPrefix);
        String contextPath = getContextPath();
        if (!contextPath.startsWith("/")) {
            buf.append("/");
        }
        buf.append(contextPath);
        try {
            return new URL(buf.toString());
        } catch (MalformedURLException e) {
            log.error("Bad URL to connect to web app", e);
            return null;
        }
    }

    public void setContextPath(String path) {
        if (path == null || !path.startsWith("/")) {
            throw new IllegalArgumentException("context path must be non-null and start with '/', not " + path);
        }
        this.webAppContext.setContextPath(path);
    }

    public String getContextPath() {
        return this.webAppContext.getContextPath();
    }

    public ClassLoader getWebClassLoader() {
        return webClassLoader;
    }

    public AbstractImmutableHandler getLifecycleChain() {
        return lifecycleChain;
    }

    public Object newInstance(String className) throws InstantiationException, IllegalAccessException {
        if (className == null) {
            throw new InstantiationException("no class loaded");
        }
        return holder.newInstance(className, webClassLoader, componentContext);
    }

    public void destroyInstance(Object o) throws Exception {
        Class clazz = o.getClass();
        if (holder != null) {
            Map<String, LifecycleMethod> preDestroy = holder.getPreDestroy();
            if (preDestroy != null) {
                Holder.apply(o, clazz, preDestroy);
            }
        }
    }

    public void doStart() throws Exception {
        // reset the classsloader... jetty likes to set it to null when stopping
        this.webAppContext.setClassLoader(webClassLoader);
        this.webAppContext.setWar(webAppRoot);

        getLifecycleChain().lifecycleCommand(new StartCommand());
    }

    public void doStop() throws Exception {
        getLifecycleChain().lifecycleCommand(new StopCommand());

        // No more logging will occur for this ClassLoader. Inform the LogFactory to avoid a memory leak.
        LogFactory.release(webClassLoader);

        // need to release the JSF factories. Otherwise, we'll leak ClassLoaders.
        FactoryFinder.releaseFactories();

        log.debug("JettyWebAppContext stopped");
    }

    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
            //ignore
        }

        log.warn("JettyWebAppContext failed");
    }

    public class StartCommand implements LifecycleCommand {

        public void lifecycleMethod() throws Exception {
            //order seems backwards... .maybe container is calling start itself???
            jettyContainer.addContext(contextHandler);
            contextHandler.start();
        }
    }

    public class StopCommand implements LifecycleCommand {

        public void lifecycleMethod() throws Exception {
            contextHandler.stop();
            //TODO is this order correct?
            for (EventListener listener : webAppContext.getEventListeners()) {
                destroyInstance(listener);
            }
            jettyContainer.removeContext(contextHandler);
        }
    }
    //pass through attributes.  They should be constructor params

    public void setLocaleEncodingMapping(Map<String, String> localeEncodingMap) {
        if (localeEncodingMap != null) {
            for (Map.Entry<String, String> entry : localeEncodingMap.entrySet()) {
                this.webAppContext.addLocaleEncoding(entry.getValue(), entry.getKey());
            }
        }
    }

    public void setListenerClassNames(Collection<String> eventListeners) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (eventListeners != null) {
            Collection<EventListener> listeners = new ArrayList<EventListener>();
            for (String listenerClassName : eventListeners) {
                EventListener listener = (EventListener) newInstance(listenerClassName);
                listeners.add(listener);
            }
            webAppContext.setEventListeners(listeners.toArray(new EventListener[listeners.size()]));
        }
    }

    public void setErrorPages(Map errorPageMap) {
        if (errorPageMap != null) {
            ((ErrorPageErrorHandler) this.webAppContext.getErrorHandler()).setErrorPages(errorPageMap);
        }
    }

    public void setTagLibMap(Map<String, String> tagLibMap) {
        if (tagLibMap != null) {
            for (Map.Entry<String, String> entry : tagLibMap.entrySet()) {
                this.webAppContext.setResourceAlias(entry.getKey(), entry.getValue());
            }
        }
    }

    public void setSessionTimeoutSeconds(int seconds) {
        this.webAppContext.getSessionHandler().getSessionManager().setMaxInactiveInterval(seconds);
    }


    //TODO this is really dumb, but jetty5 liked to set the displayname to null frequently.
    //we need to re-check for jetty6
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        this.webAppContext.setDisplayName(displayName);
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
        synchronized (servletNames) {
            return servletNames.toArray(new String[servletNames.size()]);
        }
    }

    public ServletHandler getServletHandler() {
        return this.webAppContext.getServletHandler();
    }

    /**
     * ObjectName must match this pattern:
     * <p/>
     * domain:j2eeType=WebModule,name=MyName,J2EEServer=MyServer,J2EEApplication=MyApplication
     *
     * @param objectName ObjectName to verify
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

    public void registerServletHolder(ServletHolder servletHolder, String servletName, Set<String> servletMappings, String objectName) throws Exception {
        webAppContext.getServletHandler().addServlet(servletHolder);
        if (servletMappings != null) {
            for (String urlPattern : servletMappings) {
                ServletMapping servletMapping = new ServletMapping();
                servletMapping.setPathSpec(urlPattern);
                servletMapping.setServletName(servletName);
                this.webAppContext.getServletHandler().addServletMapping(servletMapping);
            }
        }
//        LifecycleCommand lifecycleCommand = new LifecycleCommand.StartCommand(servletHolder);
//        lifecycleChain.lifecycleCommand(lifecycleCommand);
        if (objectName != null) {
            synchronized (servletNames) {
                servletNames.add(objectName);
            }
        }
    }

    public void unregisterServletHolder(ServletHolder servletHolder, String servletName, Set<String> servletMappings, String objectName) throws Exception {
        //no way to remove servlets
//        webAppContext.getServletHandler().removeServlet(servletHolder);
//        if (servletMappings != null) {
//            for (Iterator iterator = servletMappings.iterator(); iterator.hasNext();) {
//                String urlPattern = (String) iterator.next();
//                ServletMapping servletMapping = new ServletMapping();
//                servletMapping.setPathSpec(urlPattern);
//                servletMapping.setServletName(servletName);
//                webAppContext.getServletHandler().removeServletMapping(servletMapping);
//            }
//        }
//        LifecycleCommand lifecycleCommand = new LifecycleCommand.StopCommand(servletHolder);
//        lifecycleChain.lifecycleCommand(lifecycleCommand);
        if (objectName != null) {
            synchronized (servletNames) {
                servletNames.remove(objectName);
            }
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    public static final String GBEAN_ATTR_SESSION_TIMEOUT = "sessionTimeoutSeconds";

    public static final String GBEAN_REF_SESSION_HANDLER_FACTORY = "SessionHandlerFactory";
    public static final String GBEAN_REF_PRE_HANDLER_FACTORY = "PreHandlerFactory";

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic("Jetty WebApplication Context", JettyWebAppContext.class, NameFactory.WEB_MODULE);
        infoBuilder.addAttribute("deploymentDescriptor", String.class, true);
        //from jetty6's webapp context

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
        infoBuilder.addAttribute(GBEAN_ATTR_SESSION_TIMEOUT, int.class, true);
        infoBuilder.addReference(GBEAN_REF_SESSION_HANDLER_FACTORY, SessionHandlerFactory.class,
                NameFactory.GERONIMO_SERVICE);
        infoBuilder.addReference(GBEAN_REF_PRE_HANDLER_FACTORY, PreHandlerFactory.class, NameFactory.GERONIMO_SERVICE);

        infoBuilder.addAttribute("componentContext", Map.class, true);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);
        infoBuilder.addAttribute("configurationBaseUrl", URL.class, true);
        infoBuilder.addAttribute("unshareableResources", Set.class, true);
        infoBuilder.addAttribute("applicationManagedSecurityResources", Set.class, true);

        infoBuilder.addAttribute("contextPath", String.class, true);

        infoBuilder.addReference("Host", Host.class, "Host");
        infoBuilder.addReference("TransactionManager", TransactionManager.class, NameFactory.TRANSACTION_MANAGER);
        infoBuilder.addReference("TrackedConnectionAssociator", TrackedConnectionAssociator.class, NameFactory.JCA_CONNECTION_TRACKER);
        infoBuilder.addReference("JettyContainer", JettyContainer.class, NameFactory.GERONIMO_SERVICE);

        infoBuilder.addInterface(JettyServletRegistration.class);

        infoBuilder.addAttribute("policyContextID", String.class, true);
        infoBuilder.addAttribute("securityRealmName", String.class, true);
        infoBuilder.addAttribute("defaultPrincipal", DefaultPrincipal.class, true);

        infoBuilder.addAttribute("checkedPermissions", PermissionCollection.class, true);
        infoBuilder.addAttribute("excludedPermissions", PermissionCollection.class, true);

        infoBuilder.addAttribute("holder", Holder.class, true);

        infoBuilder.addReference("J2EEServer", J2EEServer.class);
        infoBuilder.addReference("J2EEApplication", J2EEApplication.class);

        infoBuilder.addAttribute("kernel", Kernel.class, false);
        infoBuilder.addAttribute("objectName", String.class, false);
        infoBuilder.addAttribute("application", String.class, false);
        infoBuilder.addAttribute("javaVMs", String[].class, false);
        infoBuilder.addAttribute("servlets", String[].class, false);

        infoBuilder.addInterface(WebModule.class);

        infoBuilder.setConstructor(new String[]{
                "objectName",
                "deploymentDescriptor",
                "componentContext",
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
                GBEAN_ATTR_SESSION_TIMEOUT,
                GBEAN_REF_SESSION_HANDLER_FACTORY,
                GBEAN_REF_PRE_HANDLER_FACTORY,

                "policyContextID",
                "securityRealmName",
                "defaultPrincipal",

                "checkedPermissions",
                "excludedPermissions",

                "holder",

                "Host",
                "TransactionManager",
                "TrackedConnectionAssociator",
                "JettyContainer",

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
