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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.security.auth.Subject;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.SessionCookieConfig;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.RuntimeCustomizer;
import org.apache.geronimo.j2ee.annotation.Holder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.jndi.ContextSource;
import org.apache.geronimo.j2ee.management.impl.InvalidObjectNameException;
import org.apache.geronimo.jetty8.handler.GeronimoWebAppContext;
import org.apache.geronimo.jetty8.handler.IntegrationContext;
import org.apache.geronimo.jetty8.security.SecurityHandlerFactory;
import org.apache.geronimo.kernel.ObjectNameUtil;
import org.apache.geronimo.management.J2EEApplication;
import org.apache.geronimo.management.J2EEServer;
import org.apache.geronimo.management.geronimo.WebContainer;
import org.apache.geronimo.management.geronimo.WebModule;
import org.apache.geronimo.security.jacc.ApplicationPolicyConfigurationManager;
import org.apache.geronimo.security.jacc.RunAsSource;
import org.apache.geronimo.transaction.GeronimoUserTransaction;
import org.apache.geronimo.web.WebApplicationConstants;
import org.apache.geronimo.web.info.ErrorPageInfo;
import org.apache.geronimo.web.info.SessionCookieConfigInfo;
import org.apache.geronimo.web.info.WebAppInfo;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for a WebApplicationContext that sets up its J2EE environment.
 *
 * @version $Rev$ $Date$
 */

@GBean(name="Jetty WebApplication Context",
j2eeType=NameFactory.WEB_MODULE)
public class WebAppContextWrapper implements GBeanLifecycle, WebModule {
    private static final Logger log = LoggerFactory.getLogger(WebAppContextWrapper.class);
    public static final String GBEAN_ATTR_SESSION_TIMEOUT = "sessionTimeoutSeconds";
    public static final String GBEAN_REF_SESSION_HANDLER_FACTORY = "SessionHandlerFactory";
    public static final String GBEAN_REF_PRE_HANDLER_FACTORY = "PreHandlerFactory";

    private final String originalSpecDD;
    private final J2EEServer server;
    private final J2EEApplication application;

    private final ClassLoader webClassLoader;
    private final JettyContainer jettyContainer;

//    private String displayName;

    private final String objectName;
    private final GeronimoWebAppContext webAppContext;

    //hack to keep jasper happy.  This is from org.apache.tomcat.util.scan.Constants in the tomcat util jar.
    private static final String JASPER_WEB_XML_NAME = "org.apache.tomcat.util.scan.MergedWebXml";


    public WebAppContextWrapper(@ParamSpecial(type = SpecialAttributeType.objectName) String objectName,
                                @ParamSpecial(type = SpecialAttributeType.abstractName) AbstractName abName,
                                @ParamAttribute(name = "contextPath") String contextPath,
                                @ParamAttribute(name = "deploymentDescriptor") String originalSpecDD,
                                @ParamAttribute(name = "modulePath") String modulePath,
                                @ParamSpecial(type = SpecialAttributeType.classLoader) ClassLoader classLoader,
                                @ParamSpecial(type = SpecialAttributeType.bundle) Bundle bundle,
                                @ParamAttribute(name = "workDir") String workDir,
                                @ParamAttribute(name = "unshareableResources") Set<String> unshareableResources,
                                @ParamAttribute(name = "applicationManagedSecurityResources") Set<String> applicationManagedSecurityResources,
                                @ParamAttribute(name = "tagLibMap") Map<String, String> tagLibMap,
                                @ParamAttribute(name = "compactPath") boolean compactPath,

                                @ParamReference(name = GBEAN_REF_SESSION_HANDLER_FACTORY) SessionHandlerFactory handlerFactory,
                                @ParamReference(name = GBEAN_REF_PRE_HANDLER_FACTORY) PreHandlerFactory preHandlerFactory,

                                @ParamAttribute(name = "policyContextID") String policyContextID,
                                @ParamReference(name = "SecurityHandlerFactory") SecurityHandlerFactory securityHandlerFactory,
                                @ParamReference(name = "RunAsSource") RunAsSource runAsSource,
                                @ParamReference(name = "applicationPolicyConfigurationManager") ApplicationPolicyConfigurationManager applicationPolicyConfigurationManager,

                                @ParamAttribute(name = "holder") Holder holder,
                                @ParamAttribute(name = "webAppInfo") WebAppInfo webAppInfo,

                                @ParamReference(name = "Host") Host host,
                                @ParamReference(name = "TrackedConnectionAssociator") TrackedConnectionAssociator trackedConnectionAssociator,
                                @ParamReference(name = "JettyContainer") JettyContainer jettyContainer,
                                @ParamReference(name = "ContextCustomizer") RuntimeCustomizer contextCustomizer,

                                @ParamReference(name = "J2EEServer") J2EEServer server,
                                @ParamReference(name = "J2EEApplication") J2EEApplication application,
                                @ParamReference(name = "ContextSource") ContextSource contextSource,
                                @ParamReference(name = "TransactionManager") TransactionManager transactionManager,

                                @ParamAttribute(name = "deploymentAttributes") Map<String, Object> deploymentAttributes
    ) throws Exception {

        assert contextSource != null;
        assert classLoader != null;
        assert trackedConnectionAssociator != null;
        assert jettyContainer != null;
        if (contextPath == null || !contextPath.startsWith("/")) {
            throw new IllegalArgumentException("context contextPath must be non-null and start with '/', not " + contextPath);
        }

        holder = holder == null ? Holder.EMPTY : holder;

        this.server = server;
        this.application = application;
        this.objectName = objectName;
        if (objectName != null) {
            ObjectName myObjectName = ObjectNameUtil.getObjectName(objectName);
            verifyObjectName(myObjectName);
        }
        this.jettyContainer = jettyContainer;
        this.originalSpecDD = originalSpecDD;

        RunAsSource runAsSource1 = runAsSource == null ? RunAsSource.NULL : runAsSource;

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
            Subject defaultSubject =  runAsSource1.getDefaultSubject();
            securityHandler = securityHandlerFactory.buildSecurityHandler(policyContextID, defaultSubject, runAsSource, true);
        } else {
            //TODO may need to turn off security with Context._options.
//            securityHandler = new NoSecurityHandler();
        }

        ServletHandler servletHandler = new ServletHandler();

        Context componentContext = contextSource.getContext();
        UserTransaction userTransaction = new GeronimoUserTransaction(transactionManager);
        Map<ServletContainerInitializer, Set<Class<?>>> servletContainerInitializerMap = new LinkedHashMap<ServletContainerInitializer, Set<Class<?>>>();
        //Set ServletContainerInitializer
        Map<String, Set<String>> servletContainerInitializerClassNamesMap = (Map<String, Set<String>>) deploymentAttributes.get(WebApplicationConstants.SERVLET_CONTAINER_INITIALIZERS);
        if (servletContainerInitializerClassNamesMap != null) {
            for (Map.Entry<String, Set<String>> entry : servletContainerInitializerClassNamesMap.entrySet()) {
                String servletContainerInitializerClassName = entry.getKey();
                Set<String> classNames = entry.getValue();
                try {
                    ServletContainerInitializer servletContainerInitializer = (ServletContainerInitializer) bundle.loadClass(servletContainerInitializerClassName).newInstance();
                    if (classNames == null || classNames.size() == 0) {
                        servletContainerInitializerMap.put(servletContainerInitializer, null);
                    } else {
                        Set<Class<?>> classSet = new HashSet<Class<?>>();
                        for (String cls : classNames) {
                            try {
                                classSet.add(bundle.loadClass(cls));
                            } catch (ClassNotFoundException e) {
                                log.warn("Fail to load class " + cls + " interested by ServletContainerInitializer " + servletContainerInitializerClassName, e);
                            }
                        }
                        servletContainerInitializerMap.put(servletContainerInitializer, classSet);
                    }
                } catch (IllegalAccessException e) {
                    log.error("Fail to initialize ServletContainerInitializer " + servletContainerInitializerClassName, e);
                } catch (InstantiationException e) {
                    log.error("Fail to initialize ServletContainerInitializer " + servletContainerInitializerClassName, e);
                } catch (ClassNotFoundException e) {
                    log.error("Fail to initialize ServletContainerInitializer " + servletContainerInitializerClassName, e);
                }
            }
        }

        IntegrationContext integrationContext = new IntegrationContext(componentContext, unshareableResources, applicationManagedSecurityResources, trackedConnectionAssociator, userTransaction, bundle, holder, servletContainerInitializerMap, abName.getNameProperty(NameFactory.J2EE_NAME));
        List<String> webModuleListenerClassNames = (List<String>) deploymentAttributes.get(WebApplicationConstants.WEB_MODULE_LISTENERS);
        webAppContext = new GeronimoWebAppContext(securityHandler, sessionHandler, servletHandler, null, integrationContext, classLoader, modulePath, webAppInfo, policyContextID, applicationPolicyConfigurationManager,  webModuleListenerClassNames == null ? Collections.<String>emptyList() : webModuleListenerClassNames);
        webAppContext.setContextPath(contextPath);
        //See Jetty-386.  Setting this to true can expose secured content.
        webAppContext.setCompactPath(compactPath);
        webAppContext.setWebModuleName(getWARName());

        if (workDir == null) {
            workDir = contextPath.replace('/', '_');
        }
        webAppContext.setTempDirectory(jettyContainer.resolveToJettyHome(workDir));


        //install jasper injection support if required
        if (contextCustomizer != null) {
            Map<String, Object> servletContext = new HashMap<String, Object>();
            Map<Class, Object> customizerContext = new HashMap<Class, Object>();
            customizerContext.put(Map.class, servletContext);
            customizerContext.put(Context.class, componentContext);
            contextCustomizer.customize(customizerContext);
            for (Map.Entry<String, Object> entry: servletContext.entrySet()) {
                webAppContext.setAttribute(entry.getKey(), entry.getValue());
            }
        }

        MimeTypes mimeTypes = new MimeTypes();
        mimeTypes.setMimeMap(webAppInfo.mimeMappings);
        webAppContext.setMimeTypes(mimeTypes);

        //DONT install the jetty TLD configuration as we find and create all the listeners ourselves
        webAppContext.setConfigurationClasses(new String[]{});

        webClassLoader = classLoader;
        webAppContext.setClassLoader(webClassLoader);

        if (host != null) {
            webAppContext.setConnectorNames(host.getHosts());
            webAppContext.setVirtualHosts(host.getVirtualHosts());
        }

        //stuff from spec dd
        webAppContext.setDisplayName(webAppInfo.displayName);
        webAppContext.getInitParams().putAll(webAppInfo.contextParams);
        webAppContext.setDistributable(webAppInfo.distributable);
        webAppContext.setWelcomeFiles(webAppInfo.welcomeFiles.toArray(new String[webAppInfo.welcomeFiles.size()]));
        for (Map.Entry<String, String> entry : webAppInfo.localeEncodingMappings.entrySet()) {
            this.webAppContext.addLocaleEncoding(entry.getKey(), entry.getValue());
        }
        ErrorPageErrorHandler errorHandler = (ErrorPageErrorHandler) this.webAppContext.getErrorHandler();
        for (ErrorPageInfo errorPageInfo: webAppInfo.errorPages) {
            if (errorPageInfo.exceptionType != null) {
                errorHandler.addErrorPage(errorPageInfo.exceptionType, errorPageInfo.location);
            } else {
                errorHandler.addErrorPage(errorPageInfo.errorCode, errorPageInfo.location);
            }
        }
        if (tagLibMap != null) {
            for (Map.Entry<String, String> entry : tagLibMap.entrySet()) {
                this.webAppContext.setResourceAlias(entry.getKey(), entry.getValue());
            }
        }

        if (!webAppInfo.distributable && webAppInfo.sessionConfig != null) {
            SessionManager sessionManager = this.webAppContext.getSessionHandler().getSessionManager();
            if (webAppInfo.sessionConfig.sessionTimeoutMinutes != null) {
                sessionManager.setMaxInactiveInterval(webAppInfo.sessionConfig.sessionTimeoutMinutes * 60);
            }
            if (webAppInfo.sessionConfig.sessionTrackingModes != null) {
                sessionManager.setSessionTrackingModes(webAppInfo.sessionConfig.sessionTrackingModes);
            }
            SessionCookieConfigInfo sessionCookieConfigInfo = webAppInfo.sessionConfig.sessionCookieConfig;
            if (sessionCookieConfigInfo != null) {
                SessionCookieConfig cookieConfig = sessionManager.getSessionCookieConfig();
                if (sessionCookieConfigInfo.name != null) {
                    cookieConfig.setName(sessionCookieConfigInfo.name);
                }
                if (sessionCookieConfigInfo.comment != null) {
                    cookieConfig.setComment(sessionCookieConfigInfo.comment);
                }
                if (sessionCookieConfigInfo.domain != null) {
                    cookieConfig.setDomain(sessionCookieConfigInfo.domain);
                }
                if (sessionCookieConfigInfo.httpOnly != null) {
                    cookieConfig.setHttpOnly(sessionCookieConfigInfo.httpOnly);
                }
                if (sessionCookieConfigInfo.maxAge != null) {
                    cookieConfig.setMaxAge(sessionCookieConfigInfo.maxAge);
                }
                if (sessionCookieConfigInfo.path != null) {
                    cookieConfig.setPath(sessionCookieConfigInfo.path);
                }
                if (sessionCookieConfigInfo.secure != null) {
                    cookieConfig.setSecure(sessionCookieConfigInfo.secure);
                }
            }
        }
        //supply web.xml to jasper
        webAppContext.setAttribute(JASPER_WEB_XML_NAME, originalSpecDD);
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

//    public URL getWARDirectory() {
//        throw new RuntimeException("don't call this");
//    }

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

    public void fullyStarted() {
        webAppContext.registerServletContext();
    }

    public void doStart() throws Exception {
        // reset the classsloader... jetty likes to set it to null when stopping
        webAppContext.setClassLoader(webClassLoader);
        jettyContainer.addContext(webAppContext);
        webAppContext.start();
    }

    public void doStop() throws Exception {
        webAppContext.unregisterServletContext();
        webAppContext.stop();
        jettyContainer.removeContext(webAppContext);
        log.debug("WebAppContextWrapper stopped");
    }

    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
            //ignore
        }

        log.warn("WebAppContextWrapper failed");
    }


    //TODO this is really dumb, but jetty5 liked to set the displayname to null frequently.
    //we need to re-check for jetty8
    public String getDisplayName() {
        return webAppContext.getDisplayName();
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
        //todo see about getting the info from jetty?
        return new String[0];
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

}
