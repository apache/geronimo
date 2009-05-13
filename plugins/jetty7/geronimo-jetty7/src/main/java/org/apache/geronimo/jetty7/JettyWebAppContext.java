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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.transaction.TransactionManager;

import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.RuntimeCustomizer;
import org.apache.geronimo.j2ee.annotation.Holder;
import org.apache.geronimo.j2ee.annotation.LifecycleMethod;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.impl.InvalidObjectNameException;
import org.apache.geronimo.jetty7.handler.AbstractImmutableHandler;
import org.apache.geronimo.jetty7.handler.ComponentContextHandler;
import org.apache.geronimo.jetty7.handler.InstanceContextHandler;
import org.apache.geronimo.jetty7.handler.LifecycleCommand;
import org.apache.geronimo.jetty7.handler.ThreadClassloaderHandler;
import org.apache.geronimo.jetty7.handler.TwistyWebAppContext;
import org.apache.geronimo.jetty7.handler.UserTransactionHandler;
import org.apache.geronimo.jetty7.security.SecurityHandlerFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.ObjectNameUtil;
import org.apache.geronimo.management.J2EEApplication;
import org.apache.geronimo.management.J2EEServer;
import org.apache.geronimo.management.geronimo.WebContainer;
import org.apache.geronimo.management.geronimo.WebModule;
import org.apache.geronimo.naming.enc.EnterpriseNamingContext;
import org.apache.geronimo.security.jacc.RunAsSource;
import org.apache.geronimo.transaction.GeronimoUserTransaction;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.http.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for a WebApplicationContext that sets up its J2EE environment.
 *
 * @version $Rev$ $Date$
 */

@GBean(name="Jetty WebApplication Context",
j2eeType=NameFactory.WEB_MODULE)
public class JettyWebAppContext implements GBeanLifecycle, JettyServletRegistration, WebModule {
    private static final Logger log = LoggerFactory.getLogger(JettyWebAppContext.class);

    private final String originalSpecDD;
    private final J2EEServer server;
    private final J2EEApplication application;

    private final ClassLoader webClassLoader;
    private final JettyContainer jettyContainer;

    private final String webAppRoot;
    private final URL configurationBaseURL;
    private String displayName;

    private final String objectName;
    private final TwistyWebAppContext webAppContext;//delegate
    private final AbstractImmutableHandler lifecycleChain;
    private final Context componentContext;
    private final Holder holder;
    private final RunAsSource runAsSource;

    private final Set<String> servletNames = new HashSet<String>();

    public static final String GBEAN_ATTR_SESSION_TIMEOUT = "sessionTimeoutSeconds";

    public static final String GBEAN_REF_SESSION_HANDLER_FACTORY = "SessionHandlerFactory";
    public static final String GBEAN_REF_PRE_HANDLER_FACTORY = "PreHandlerFactory";

//    static {
//        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic("Jetty WebApplication Context", JettyWebAppContext.class, NameFactory.WEB_MODULE);
//        infoBuilder.addAttribute("deploymentDescriptor", String.class, true);
//        //from jetty7's webapp context
//
//        infoBuilder.addAttribute("displayName", String.class, true);
//        infoBuilder.addAttribute("contextParamMap", Map.class, true);
//        infoBuilder.addAttribute("listenerClassNames", Collection.class, true);
//        infoBuilder.addAttribute("distributable", boolean.class, true);
//
//        infoBuilder.addAttribute("mimeMap", Map.class, true);
//        infoBuilder.addAttribute("welcomeFiles", String[].class, true);
//        infoBuilder.addAttribute("localeEncodingMapping", Map.class, true);
//        infoBuilder.addAttribute("errorPages", Map.class, true);
//        infoBuilder.addAttribute("authenticator", Authenticator.class, true);
//        infoBuilder.addAttribute("realmName", String.class, true);
//        infoBuilder.addAttribute("tagLibMap", Map.class, true);
//        infoBuilder.addAttribute(GBEAN_ATTR_SESSION_TIMEOUT, int.class, true);
//        infoBuilder.addReference(GBEAN_REF_SESSION_HANDLER_FACTORY, SessionHandlerFactory.class,
//                NameFactory.GERONIMO_SERVICE);
//        infoBuilder.addReference(GBEAN_REF_PRE_HANDLER_FACTORY, PreHandlerFactory.class, NameFactory.GERONIMO_SERVICE);
//
//        infoBuilder.addAttribute("componentContext", Map.class, true);
//        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);
//        infoBuilder.addAttribute("configurationBaseUrl", URL.class, true);
//        infoBuilder.addAttribute("unshareableResources", Set.class, true);
//        infoBuilder.addAttribute("applicationManagedSecurityResources", Set.class, true);
//
//        infoBuilder.addAttribute("contextPath", String.class, true);
//        infoBuilder.addAttribute("compactPath", boolean.class, true);
//
//        infoBuilder.addAttribute("workDir", String.class, true);
//        infoBuilder.addReference("Host", Host.class, "Host");
//        infoBuilder.addReference("TransactionManager", TransactionManager.class, NameFactory.JTA_RESOURCE);
//        infoBuilder.addReference("TrackedConnectionAssociator", TrackedConnectionAssociator.class, NameFactory.JCA_CONNECTION_TRACKER);
//        infoBuilder.addReference("JettyContainer", JettyContainer.class, NameFactory.GERONIMO_SERVICE);
//        infoBuilder.addReference("ContextCustomizer", RuntimeCustomizer.class, NameFactory.GERONIMO_SERVICE);
//
//        infoBuilder.addInterface(JettyServletRegistration.class);
//
//        infoBuilder.addAttribute("policyContextID", String.class, true);
//        infoBuilder.addAttribute("securityRealmName", String.class, true);
//        infoBuilder.addReference("RunAsSource", RunAsSource.class, NameFactory.JACC_MANAGER);
//
//        infoBuilder.addAttribute("holder", Holder.class, true);
//
//        infoBuilder.addReference("J2EEServer", J2EEServer.class);
//        infoBuilder.addReference("J2EEApplication", J2EEApplication.class);
//
//        infoBuilder.addAttribute("kernel", Kernel.class, false);
//        infoBuilder.addAttribute("objectName", String.class, false);
//        infoBuilder.addAttribute("application", String.class, false);
//        infoBuilder.addAttribute("javaVMs", String[].class, false);
//        infoBuilder.addAttribute("servlets", String[].class, false);
//
//        infoBuilder.addInterface(WebModule.class);
//
//        infoBuilder.setConstructor(new String[]{
//                "objectName",
//                "deploymentDescriptor",
//                "componentContext",
//                "classLoader",
//                "configurationBaseUrl",
//                "unshareableResources",
//                "applicationManagedSecurityResources",
//
//                "displayName",
//                "contextParamMap",
//                "listenerClassNames",
//                "distributable",
//                "mimeMap",
//                "welcomeFiles",
//                "localeEncodingMapping",
//                "errorPages",
//                "authenticator",
//                "realmName",
//                "tagLibMap",
//                "compactPath",
//                GBEAN_ATTR_SESSION_TIMEOUT,
//                GBEAN_REF_SESSION_HANDLER_FACTORY,
//                GBEAN_REF_PRE_HANDLER_FACTORY,
//
//                "policyContextID",
//                "securityRealmName",
//                "RunAsSource",
//
//                "holder",
//
//                "Host",
//                "TransactionManager",
//                "TrackedConnectionAssociator",
//                "JettyContainer",
//                "ContextCustomizer",
//
//                "J2EEServer",
//                "J2EEApplication",
//                "kernel"
//        });
//
//    }

    public JettyWebAppContext(@ParamSpecial(type = SpecialAttributeType.objectName)String objectName,
                              @ParamAttribute(name = "contextPath")String contextPath,
                              @ParamAttribute(name = "deploymentDescriptor")String originalSpecDD,
                              @ParamAttribute(name = "componentContext")Map<String, Object> componentContext,
                              @ParamSpecial(type = SpecialAttributeType.classLoader)ClassLoader classLoader,
                              @ParamAttribute(name = "configurationBaseUrl")URL configurationBaseUrl,
                              @ParamAttribute(name = "unshareableResources")Set unshareableResources,
                              @ParamAttribute(name = "applicationManagedSecurityResources")Set applicationManagedSecurityResources,
                              @ParamAttribute(name = "displayName")String displayName,
                              @ParamAttribute(name = "contextParamMap")Map contextParamMap,
                              @ParamAttribute(name = "listenerClassNames")Collection<String> listenerClassNames,
                              @ParamAttribute(name = "distributable")boolean distributable,
                              @ParamAttribute(name = "mimeMap")Map mimeMap,
                              @ParamAttribute(name = "welcomeFiles")String[] welcomeFiles,
                              @ParamAttribute(name = "localeEncodingMapping")Map<String, String> localeEncodingMapping,
                              @ParamAttribute(name = "errorPages")Map errorPages,
                              @ParamAttribute(name = "tagLibMap")Map<String, String> tagLibMap,
                              @ParamAttribute(name = "compactPath")boolean compactPath,

                              @ParamAttribute(name = GBEAN_ATTR_SESSION_TIMEOUT)int sessionTimeoutSeconds,
                              @ParamReference(name = GBEAN_REF_SESSION_HANDLER_FACTORY)SessionHandlerFactory handlerFactory,
                              @ParamReference(name = GBEAN_REF_PRE_HANDLER_FACTORY)PreHandlerFactory preHandlerFactory,

                              @ParamAttribute(name = "policyContextID")String policyContextID,
                              @ParamAttribute(name = "securityRealmName")String securityRealmName,
                              @ParamReference(name = "SecurityHandlerFactory")SecurityHandlerFactory securityHandlerFactory,
                              @ParamReference(name = "RunAsSource")RunAsSource runAsSource,

                              @ParamAttribute(name = "holder")Holder holder,

                              @ParamReference(name = "Host")Host host,
                              @ParamReference(name = "TransactionManager")TransactionManager transactionManager,
                              @ParamReference(name = "TrackedConnectionAssociator")TrackedConnectionAssociator trackedConnectionAssociator,
                              @ParamReference(name = "JettyContainer")JettyContainer jettyContainer,
                              @ParamReference(name = "ContextCustomizer")RuntimeCustomizer contextCustomizer,

                              @ParamReference(name = "J2EEServer")J2EEServer server,
                              @ParamReference(name = "J2EEApplication")J2EEApplication application,
                              @ParamSpecial(type = SpecialAttributeType.kernel)Kernel kernel) throws Exception {

        assert componentContext != null;
        assert classLoader != null;
        assert configurationBaseUrl != null;
        assert transactionManager != null;
        assert trackedConnectionAssociator != null;
        assert jettyContainer != null;

        this.holder = holder == null ? Holder.EMPTY : holder;

        this.runAsSource = runAsSource == null? RunAsSource.NULL: runAsSource;

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
        SecurityHandler securityHandler = null;
//        if (securityRealmName != null) {
//            InternalJAASJettyRealm internalJAASJettyRealm = jettyContainer.addRealm(securityRealmName);
            //wrap jetty realm with something that knows the dumb realmName
//            JAASJettyRealm realm = new JAASJettyRealm(realmName, internalJAASJettyRealm);
        if (securityHandlerFactory != null) {
            Subject defaultSubject =  this.runAsSource.getDefaultSubject();
            securityHandler = securityHandlerFactory.buildSecurityHandler(policyContextID, defaultSubject, runAsSource);
        } else {
            //TODO may need to turn off security with Context._options.
//            securityHandler = new NoSecurityHandler();
        }
//        }

        ServletHandler servletHandler = new ServletHandler();

        webAppContext = new TwistyWebAppContext(securityHandler, sessionHandler, servletHandler, null);
        if (contextPath == null || !contextPath.startsWith("/")) {
            throw new IllegalArgumentException("context contextPath must be non-null and start with '/', not " + contextPath);
        }
        webAppContext.setContextPath(contextPath);
        //See Jetty-386.  Setting this to true can expose secured content.
        webAppContext.setCompactPath(compactPath);

        //wrap the web app context with the jndi handler
        GeronimoUserTransaction userTransaction = new GeronimoUserTransaction(transactionManager);
        this.componentContext = EnterpriseNamingContext.createEnterpriseNamingContext(componentContext, userTransaction, kernel, classLoader);

        //install jasper injection support if required
        if (contextCustomizer != null) {
            Map<String, Object> servletContext = new HashMap<String, Object>();
            Map<Class, Object> customizerContext = new HashMap<Class, Object>();
            customizerContext.put(Map.class, servletContext);
            customizerContext.put(Context.class, JettyWebAppContext.this.componentContext);
            contextCustomizer.customize(customizerContext);
            for (Map.Entry<String, Object> entry: servletContext.entrySet()) {
                webAppContext.setAttribute(entry.getKey(), entry.getValue());
            }
        }

        // localize access to next
        {
            //install the other handlers inside the web app context
            Handler next = webAppContext.newTwistyHandler();
            next = new ThreadClassloaderHandler(next, classLoader);

            next = new InstanceContextHandler(next, unshareableResources, applicationManagedSecurityResources, trackedConnectionAssociator);
            next = new UserTransactionHandler(next, userTransaction);
            next = new ComponentContextHandler(next, this.componentContext);
            webAppContext.setTwistyHandler(next);

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

        //DONT install the jetty TLD configuration as we find and create all the listeners ourselves
        webAppContext.setConfigurationClasses(new String[]{});

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

    public String getContextPath() {
        return this.webAppContext.getContextPath();
    }

    public void setWorkDir(@ParamAttribute(name = "workDir") String workDir) {
        if(workDir == null) {
            return;
        }
        this.webAppContext.setTempDirectory(jettyContainer.resolveToJettyHome(workDir));
    }
    
    public ClassLoader getWebClassLoader() {
        return webClassLoader;
    }

    public AbstractImmutableHandler getLifecycleChain() {
        return lifecycleChain;
    }

    public Subject getSubjectForRole(String role) throws LoginException {
        return runAsSource.getSubjectForRole(role);
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

        // need to release the JSF factories. Otherwise, we'll leak ClassLoaders.
        //should be done in a myfaces gbean
//        FactoryFinder.releaseFactories();

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
            jettyContainer.addContext(webAppContext);
            webAppContext.start();
        }
    }

    public class StopCommand implements LifecycleCommand {

        public void lifecycleMethod() throws Exception {
            webAppContext.stop();
            //TODO is this order correct?
            for (EventListener listener : webAppContext.getEventListeners()) {
                destroyInstance(listener);
            }
            jettyContainer.removeContext(webAppContext);
        }
    }
    //pass through attributes.  They should be constructor params

    public void setLocaleEncodingMapping(@ParamAttribute(name = "localeEncodingMapping")Map<String, String> localeEncodingMap) {
        if (localeEncodingMap != null) {
            for (Map.Entry<String, String> entry : localeEncodingMap.entrySet()) {
                this.webAppContext.addLocaleEncoding(entry.getKey(), entry.getValue());
            }
        }
    }

    public void setListenerClassNames(@ParamAttribute(name = "listenerClassNames")Collection<String> eventListeners) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (eventListeners != null) {
            Collection<EventListener> listeners = new ArrayList<EventListener>();
            for (String listenerClassName : eventListeners) {
                EventListener listener = (EventListener) newInstance(listenerClassName);
                listeners.add(listener);
            }
            webAppContext.setEventListeners(listeners.toArray(new EventListener[listeners.size()]));
        }
    }

    public void setErrorPages(@ParamAttribute(name = "errorPages")Map errorPageMap) {
        if (errorPageMap != null) {
            ((ErrorPageErrorHandler) this.webAppContext.getErrorHandler()).setErrorPages(errorPageMap);
        }
    }

    public void setTagLibMap(@ParamAttribute(name = "tagLibMap")Map<String, String> tagLibMap) {
        if (tagLibMap != null) {
            for (Map.Entry<String, String> entry : tagLibMap.entrySet()) {
                this.webAppContext.setResourceAlias(entry.getKey(), entry.getValue());
            }
        }
    }

    public void setSessionTimeoutSeconds(@ParamAttribute(name = "sessionTimeoutSeconds")int seconds) {
        this.webAppContext.getSessionHandler().getSessionManager().setMaxInactiveInterval(seconds);
    }


    //TODO this is really dumb, but jetty5 liked to set the displayname to null frequently.
    //we need to re-check for jetty7
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

}
