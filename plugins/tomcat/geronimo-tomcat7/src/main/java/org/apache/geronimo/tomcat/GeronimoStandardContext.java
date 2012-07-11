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

package org.apache.geronimo.tomcat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.servlet.Servlet;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRequest;
import javax.servlet.ServletSecurityElement;

import org.apache.catalina.Container;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Engine;
import org.apache.catalina.InstanceListener;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Loader;
import org.apache.catalina.Manager;
import org.apache.catalina.Valve;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.ApplicationServletRegistration;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardWrapper;
import org.apache.catalina.ha.CatalinaCluster;
import org.apache.catalina.valves.ValveBase;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.GeronimoSecurityException;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.geronimo.kernel.util.IOUtils;
import org.apache.geronimo.kernel.util.JarUtils;
import org.apache.geronimo.osgi.web.WebApplicationUtils;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.jaas.ConfigurationFactory;
import org.apache.geronimo.security.jacc.ApplicationPolicyConfigurationManager;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.security.jacc.RunAsSource;
import org.apache.geronimo.tomcat.interceptor.BeforeAfter;
import org.apache.geronimo.tomcat.interceptor.BeforeAfterContext;
import org.apache.geronimo.tomcat.interceptor.ComponentContextBeforeAfter;
import org.apache.geronimo.tomcat.interceptor.InstanceContextBeforeAfter;
import org.apache.geronimo.tomcat.interceptor.PolicyContextBeforeAfter;
import org.apache.geronimo.tomcat.interceptor.RequestListenerBeforeAfter;
import org.apache.geronimo.tomcat.interceptor.UserTransactionBeforeAfter;
import org.apache.geronimo.tomcat.interceptor.WebApplicationIdentityBeforeAfter;
import org.apache.geronimo.tomcat.listener.DispatchListener;
import org.apache.geronimo.tomcat.listener.RunAsInstanceListener;
import org.apache.geronimo.tomcat.util.SecurityHolder;
import org.apache.geronimo.tomcat.valve.GeronimoBeforeAfterValve;
import org.apache.geronimo.tomcat.valve.ProtectedTargetValve;
import org.apache.geronimo.web.WebApplicationConstants;
import org.apache.geronimo.web.WebModuleListener;
import org.apache.geronimo.web.info.WebAppInfo;
import org.apache.geronimo.web.security.SpecSecurityBuilder;
import org.apache.geronimo.web.security.WebSecurityConstraintStore;
import org.apache.geronimo.webservices.POJOWebServiceServlet;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.apache.geronimo.webservices.WebServiceContainerInvoker;
import org.apache.naming.resources.FileDirContext;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoStandardContext extends StandardContext {

    private static final long serialVersionUID = 3834587716552831032L;

    private static final boolean allowLinking = Boolean.getBoolean("org.apache.geronimo.tomcat.GeronimoStandardContext.allowLinking");

    private static final Logger logger = LoggerFactory.getLogger(GeronimoStandardContext.class);

    private Subject defaultSubject = null;

    private RunAsSource runAsSource = RunAsSource.NULL;

    private Map<String, WebServiceContainer> webServiceMap = null;

    private boolean pipelineInitialized;

    private BeforeAfter beforeAfter = null;

    private int contextCount = 0;

    private boolean authenticatorInstalled;

    private ConfigurationFactory configurationFactory;

    private String policyContextId;

    private WebSecurityConstraintStore webSecurityConstraintStore;

    private ApplicationPolicyConfigurationManager applicationPolicyConfigurationManager;

    private Bundle bundle;

    private ServiceRegistration serviceRegistration;

    private List<WebModuleListener> webModuleListeners;

    private ThreadLocal<Stack<BeforeAfterContext>> beforeAfterContexts = new ThreadLocal<Stack<BeforeAfterContext>>() {

        @Override
        protected Stack<BeforeAfterContext> initialValue() {
            return new Stack<BeforeAfterContext>();
        }
    };

    public GeronimoStandardContext() {
        setXmlNamespaceAware(true);
        // disable Tomcat startup TLD scanning
        setProcessTlds(false);
        // By default, we configure HttpOnly with false value, as it would cause DWR fail to work
        //On the runtime, Tomcat will determine the value based on the configurations in web.xml and context, so the users still have a chance to open it via web.xml
        setUseHttpOnly(false);
        //Turn off  webXmlValidation, Geronimo should have done there in the deployment process
        setXmlValidation(false);
    }

    public void setContextProperties(TomcatContext ctx) throws DeploymentException {
        bundle = ctx.getBundle();
        setResources(createDirContext(ctx));
        // Create ReadOnlyContext
        Context enc = ctx.getJndiContext();
        setInstanceManager(ctx.getInstanceManager());
        ServletContext servletContext = getServletContext();
        //try to make sure this mbean properties match those of the TomcatWebAppContext
        if (ctx instanceof TomcatWebAppContext) {
            TomcatWebAppContext tomcatWebAppContext = (TomcatWebAppContext) ctx;
            setJavaVMs(tomcatWebAppContext.getJavaVMs());
            setServer(tomcatWebAppContext.getServer());
            setJ2EEApplication(tomcatWebAppContext.getJ2EEApplication());
            setJ2EEServer(tomcatWebAppContext.getJ2EEServer());
            servletContext.setAttribute(WebApplicationConstants.WEB_APP_NAME, tomcatWebAppContext.getWARName());
            //install jasper injection support if required
            if (tomcatWebAppContext.getRuntimeCustomizer() != null) {
                Map<String, Object> attributes = new HashMap<String, Object>();
                Map<Class, Object> customizerContext = new HashMap<Class, Object>();
                customizerContext.put(Map.class, attributes);
                customizerContext.put(javax.naming.Context.class, enc);
                tomcatWebAppContext.getRuntimeCustomizer().customize(customizerContext);
                for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                    servletContext.setAttribute(entry.getKey(), entry.getValue());
                }
            }
            applicationPolicyConfigurationManager = tomcatWebAppContext.getApplicationPolicyConfigurationManager();
            if (tomcatWebAppContext.getSecurityHolder() != null) {
                configurationFactory = tomcatWebAppContext.getSecurityHolder().getConfigurationFactory();
            }
            float schemaVersion = (Float) tomcatWebAppContext.getDeploymentAttribute(WebApplicationConstants.SCHEMA_VERSION);
            boolean metaComplete = (Boolean) tomcatWebAppContext.getDeploymentAttribute(WebApplicationConstants.META_COMPLETE);
            webSecurityConstraintStore = new WebSecurityConstraintStore(tomcatWebAppContext.getWebAppInfo(), bundle, schemaVersion >= 2.5f && !metaComplete, getInternalServletContext());
            servletContext.setAttribute(InstanceManager.class.getName(), ctx.getInstanceManager());
            //Set some attributes passed from the deployment process
            List<String> orderedLists = (List<String>) tomcatWebAppContext.getDeploymentAttribute(WebApplicationConstants.ORDERED_LIBS);
            if (orderedLists != null) {
                servletContext.setAttribute(ServletContext.ORDERED_LIBS, Collections.unmodifiableList(orderedLists));
            }
            //Set ServletContainerInitializer
            Map<String, Set<String>> servletContainerInitializerClassNamesMap = (Map<String, Set<String>>) tomcatWebAppContext
                    .getDeploymentAttribute(WebApplicationConstants.SERVLET_CONTAINER_INITIALIZERS);
            Bundle bundle = tomcatWebAppContext.getBundle();
            if (servletContainerInitializerClassNamesMap != null) {
                for (Map.Entry<String, Set<String>> entry : servletContainerInitializerClassNamesMap.entrySet()) {
                    String servletContainerInitializerClassName = entry.getKey();
                    Set<String> classNames = entry.getValue();
                    try {
                        ServletContainerInitializer servletContainerInitializer = (ServletContainerInitializer) bundle.loadClass(servletContainerInitializerClassName).newInstance();
                        if (classNames == null || classNames.size() == 0) {
                            addServletContainerInitializer(servletContainerInitializer, null);
                        } else {
                            Set<Class<?>> classSet = new HashSet<Class<?>>();
                            for (String cls : classNames) {
                                try {
                                    classSet.add(bundle.loadClass(cls));
                                } catch (ClassNotFoundException e) {
                                    getLogger().warn("Fail to load class " + cls + " interested by ServletContainerInitializer " + servletContainerInitializerClassName, e);
                                }
                            }
                            addServletContainerInitializer(servletContainerInitializer, classSet);
                        }
                    } catch (IllegalAccessException e) {
                        getLogger().error("Fail to initialize ServletContainerInitializer " + servletContainerInitializerClassName, e);
                    } catch (InstantiationException e) {
                        getLogger().error("Fail to initialize ServletContainerInitializer " + servletContainerInitializerClassName, e);
                    } catch (ClassNotFoundException e) {
                        getLogger().error("Fail to initialize ServletContainerInitializer " + servletContainerInitializerClassName, e);
                    }
                }
            }
            //Get WebModuleListener List
            List<String> webModuleListenerClassNames = (List<String>) tomcatWebAppContext.getDeploymentAttribute(WebApplicationConstants.WEB_MODULE_LISTENERS);
            if (webModuleListenerClassNames != null && webModuleListenerClassNames.size() > 0) {
                webModuleListeners = new ArrayList(webModuleListenerClassNames.size());
                for (String webModuleListenerClassName : webModuleListenerClassNames) {
                    try {
                        Class<?> cls = bundle.loadClass(webModuleListenerClassName);
                        Object webModuleListener = cls.newInstance();
                        webModuleListeners.add((WebModuleListener) webModuleListener);
                    } catch (ClassNotFoundException e) {
                        logger.warn("Unable to load the listener class" + webModuleListenerClassName, e);
                    } catch (InstantiationException e) {
                        logger.warn("Unable to create the listener instance " + webModuleListenerClassName, e);
                    } catch (IllegalAccessException e) {
                        logger.warn("Unable to create the listener instance " + webModuleListenerClassName, e);
                    }
                }
            }
        }
        int index = 0;
        BeforeAfter interceptor = new RequestListenerBeforeAfter(null, index++, this);
        interceptor = new InstanceContextBeforeAfter(interceptor, index++, index++, ctx.getUnshareableResources(), ctx.getApplicationManagedSecurityResources(), ctx.getTrackedConnectionAssociator());
        // Set ComponentContext BeforeAfter
        if (enc != null) {
            interceptor = new ComponentContextBeforeAfter(interceptor, index++, enc);
        }
        //Set a PolicyContext BeforeAfter
        SecurityHolder securityHolder = ctx.getSecurityHolder();
        if (securityHolder != null) {
            // save the role designates for mapping servlets to their run-as roles
            runAsSource = securityHolder.getRunAsSource();
            if (securityHolder.getPolicyContextID() != null) {
                policyContextId = securityHolder.getPolicyContextID();
                PolicyContext.setContextID(policyContextId);
                /**
                 * Register our default subject with the ContextManager
                 */
                defaultSubject = securityHolder.getDefaultSubject();
                if (defaultSubject == null) {
                    defaultSubject = ContextManager.EMPTY;
                }
                interceptor = new PolicyContextBeforeAfter(interceptor, index++, index++, index++, policyContextId, defaultSubject);
            }
        }
        //Set a UserTransactionBeforeAfter
        interceptor = new UserTransactionBeforeAfter(interceptor, index++, ctx.getUserTransaction());
        interceptor = new WebApplicationIdentityBeforeAfter(interceptor, index++, ctx.getAbstractName().getNameProperty(NameFactory.J2EE_NAME));
        addValve(new ProtectedTargetValve());
        Valve clusteredValve = ctx.getClusteredValve();
        if (null != clusteredValve) {
            addValve(clusteredValve);
        }
        //Set the BeforeAfters as a valve
        GeronimoBeforeAfterValve geronimoBAValve = new GeronimoBeforeAfterValve(interceptor, index);
        addValve(geronimoBAValve);
        beforeAfter = interceptor;
        contextCount = index;
        //Not clear if user defined valves should be involved in init processing.  Probably not since
        //request and response are null.
        addValve(new SystemMethodValve());
        // Add User Defined Valves
        List<Valve> valveChain = ctx.getValveChain();
        if (valveChain != null) {
            for (Valve valve : valveChain) {
                addValve(valve);
            }
        }
        // Add User Defined Listeners
        List<LifecycleListener> listenerChain = ctx.getLifecycleListenerChain();
        if (listenerChain != null) {
            for (LifecycleListener listener : listenerChain) {
                addLifecycleListener(listener);
            }
        }
        CatalinaCluster cluster = ctx.getCluster();
        if (cluster != null)
            this.setCluster(cluster);
        Manager manager = ctx.getManager();
        if (manager != null)
            this.setManager(manager);
        pipelineInitialized = true;
        ClassLoader oldClassLoader = bindThread();
        try {
            webServiceMap = ctx.getWebServices();
        } finally {
            unbindThread(oldClassLoader);
        }
        Map<String, String> contextAttributes = ctx.getContextAttributes();
        if (!ctx.getContextAttributes().containsKey("allowLinking")) {
            contextAttributes.put("allowLinking", String.valueOf(allowLinking));
        }
        //Set context attributes via reflection
        for (Map.Entry<String, String> entry : contextAttributes.entrySet()) {
            if (!IntrospectionUtils.setProperty(this, entry.getKey(), entry.getValue())) {
                if (getLogger().isWarnEnabled()) {
                    getLogger().warn("Fail to configure attribute " + entry.getKey() + " with value " + entry.getValue() + ", please check whether the attribute exists or is typo correctly");
                }
            }
        }
        //Set the Dispatch listener
        this.addInstanceListener(DispatchListener.class.getName());
        //Set the run-as listener. listeners must be added before start() is called
        if (runAsSource != null) {
            this.addInstanceListener(RunAsInstanceListener.class.getName());
        }
    }

    private final Object instanceListenersLock = new Object();

    private final Object wrapperLifecyclesLock = new Object();

    private final Object wrapperListenersLock = new Object();

    @Override
    public Wrapper createWrapper() {
        Wrapper wrapper = null;
        if (getWrapperClass() != null) {
            try {
                wrapper = (Wrapper) getInstanceManager().newInstance(getWrapperClass());
            } catch (Throwable t) {
                getLogger().error("createWrapper", t);
                return (null);
            }
        } else {
            wrapper = new StandardWrapper();
        }
        synchronized (instanceListenersLock) {
            for (String instanceListener : findInstanceListeners()) {
                try {
                    InstanceListener listener = (InstanceListener) getInstanceManager().newInstance(instanceListener);
                    wrapper.addInstanceListener(listener);
                } catch (Throwable t) {
                    getLogger().error("createWrapper", t);
                    return (null);
                }
            }
        }
        synchronized (wrapperLifecyclesLock) {
            for (String wrapperLifecycle : findWrapperLifecycles()) {
                try {
                    LifecycleListener listener = (LifecycleListener) getInstanceManager().newInstance(wrapperLifecycle);
                    wrapper.addLifecycleListener(listener);
                } catch (Throwable t) {
                    getLogger().error("createWrapper", t);
                    return (null);
                }
            }
        }
        synchronized (wrapperListenersLock) {
            for (String wrapperListener : findWrapperListeners()) {
                try {
                    ContainerListener listener = (ContainerListener) getInstanceManager().newInstance(wrapperListener);
                    wrapper.addContainerListener(listener);
                } catch (Throwable t) {
                    getLogger().error("createWrapper", t);
                    return (null);
                }
            }
        }
        return (wrapper);
    }

    /* This method is called by a background thread to destroy sessions (among other things)
     * so we need to apply appropriate context to the thread to expose JNDI, etc.
     */
    @Override
    public void backgroundProcess() {
        BeforeAfterContext beforeAfterContext = null;
        if (beforeAfter != null) {
            beforeAfterContext = new BeforeAfterContext(contextCount);
            beforeAfter.before(beforeAfterContext, null, null, BeforeAfter.EDGE_SERVLET);
        }
        try {
            super.backgroundProcess();
        } finally {
            if (beforeAfter != null) {
                beforeAfter.after(beforeAfterContext, null, null, BeforeAfter.EDGE_SERVLET);
            }
        }
    }

    public void kill() throws Exception {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
        BeforeAfterContext beforeAfterContext = null;
        if (beforeAfter != null) {
            beforeAfterContext = new BeforeAfterContext(contextCount);
            beforeAfter.before(beforeAfterContext, null, null, BeforeAfter.EDGE_SERVLET);
        }
        try {
            stop();
            destroy();
        } finally {
            if (beforeAfter != null) {
                beforeAfter.after(beforeAfterContext, null, null, BeforeAfter.EDGE_SERVLET);
            }
        }
    }

    @Override
    protected void initInternal() throws LifecycleException {
        String docBase = getDocBase();
        super.initInternal();
        setDocBase(docBase);
    }

    @Override
    protected void startInternal() throws LifecycleException {
        if (pipelineInitialized) {
            try {
                Valve valve = getPipeline().getFirst();
                valve.invoke(null, null);
                // if a servlet uses run-as then make sure role designates have been provided
                if (hasRunAsServlet()) {
                    if (runAsSource == null) {
                        throw new GeronimoSecurityException("web.xml or annotation specifies a run-as role but no subject configuration supplied for run-as roles");
                    }
                } else {
                    // optimization
                    this.removeInstanceListener(RunAsInstanceListener.class.getName());
                }
            } catch (IOException e) {
                if (e.getCause() instanceof LifecycleException) {
                    throw (LifecycleException) e.getCause();
                }
                throw new LifecycleException(e);
            } catch (ServletException e) {
                throw new LifecycleException(e);
            }
            SpecSecurityBuilder specSecurityBuilder = new SpecSecurityBuilder(webSecurityConstraintStore.exportMergedWebAppInfo());
            Map<String, ComponentPermissions> contextIdPermissionsMap = new HashMap<String, ComponentPermissions>();
            contextIdPermissionsMap.put(getPolicyContextId(), specSecurityBuilder.buildSpecSecurityConfig());
            //Update ApplicationPolicyConfigurationManager
            if (applicationPolicyConfigurationManager != null) {
                try {
                    applicationPolicyConfigurationManager.updateApplicationPolicyConfiguration(contextIdPermissionsMap);
                } catch (LoginException e) {
                    getLogger().error("Fail to set application policy configurations", e);
                    throw new RuntimeException("Fail to set application policy configurations", e);
                } catch (PolicyContextException e) {
                    getLogger().error("Fail to set application policy configurations", e);
                    throw new RuntimeException("Fail to set application policy configurations", e);
                } catch (ClassNotFoundException e) {
                    getLogger().error("Fail to set application policy configurations", e);
                    throw new RuntimeException("Fail to set application policy configurations", e);
                } finally {
                    //Clear SpecSecurityBuilder
                    specSecurityBuilder.clear();
                }
            }
            // for OSGi Web Applications support register ServletContext in service registry
            if (WebApplicationUtils.isWebApplicationBundle(bundle)) {
                serviceRegistration = WebApplicationUtils.registerServletContext(bundle, getServletContext());
            }
        } else {
            super.startInternal();
        }
    }

    @Override
    public synchronized void setLoader(final Loader delegate) {
        super.setLoader(new GeronimoWebAppLoader(this, delegate));
    }

    public ServletContext getInternalServletContext() {
        return context;
    }

    protected DirContext createDirContext(TomcatContext tomcatContext) throws DeploymentException {
        File bundleFileRoot = BundleUtils.toFile(bundle);
        if (bundleFileRoot != null) {
            File applicationRoot = tomcatContext.getModulePath() == null ? bundleFileRoot : new File(bundleFileRoot, tomcatContext.getModulePath());
            if (applicationRoot.exists() && applicationRoot.isDirectory()) {
                return createFileDirContext(tomcatContext, applicationRoot);
            }
        }
        return createBundleDirContext(tomcatContext);
    }

    private DirContext createFileDirContext(TomcatContext tomcatContext, File applicationRoot) throws DeploymentException {
        FileDirContext fileDirContext = new FileDirContext();
        fileDirContext.setAllowLinking(allowLinking);
        setDocBase(applicationRoot.getAbsolutePath());
        File libraryDirectory = new File(applicationRoot, "WEB-INF/lib");
        if (libraryDirectory.exists()) {
            for (File libraryFile : libraryDirectory.listFiles()) {
                if (libraryFile.getName().toLowerCase().endsWith(".jar") && libraryFile.isFile()) {
                    try {
                        fileDirContext.addResourcesJar(new URL("jar:" + libraryFile.toURI().toURL().toExternalForm() + "!/ "));
                    } catch (MalformedURLException e) {
                        logger.warn("Unable to add jar file for resource searching", e);
                    }
                }
            }
        }
        return fileDirContext;
    }

    private DirContext createBundleDirContext(TomcatContext tomcatContext) throws DeploymentException {
        List<DirContext> altDirContexts = new ArrayList<DirContext>();
        Engine engine = (Engine) getParent().getParent();
        String serviceName = engine.getService().getName();
        String engineName = engine.getName();
        String hostName = getParent().getName();
        String tomcatHome = System.getProperty("catalina.home");
        File tempRootDirectory = new File(tomcatHome + File.separator + "resources" + File.separator + serviceName + File.separator + engineName + File.separator + hostName + File.separator
                + (getName().equals("/") ? "_" : getName()));
        WebAppInfo webAppInfo = tomcatContext.getWebAppInfo();
        String applicationStageConfiguration = webAppInfo.contextParams.get(WebApplicationConstants.WEB_APPLICATION_STAGE);
        String globalStageConfiguration = System.getProperty(WebApplicationConstants.WEB_APPLICATION_STAGE, WebApplicationConstants.WEB_APPLICATION_PRODUCTION_STAGE);
        boolean developmentStage = applicationStageConfiguration == null ? globalStageConfiguration.equalsIgnoreCase(WebApplicationConstants.WEB_APPLICATION_DEVELOPMENT_STAGE)
                : applicationStageConfiguration.equalsIgnoreCase(WebApplicationConstants.WEB_APPLICATION_DEVELOPMENT_STAGE);
        //By default, getRealPath is enabled, and user could configure in the web.xml to disable it.
        String globalGetRealPathConfiguration = System.getProperty(WebApplicationConstants.WEB_APPLICATION_GET_REAL_PATH_SUPPORT);
        String applicationGetRealPathConfiguration = webAppInfo.contextParams.get(WebApplicationConstants.WEB_APPLICATION_GET_REAL_PATH_SUPPORT);
        boolean getRealPathSupportRequired = applicationGetRealPathConfiguration == null ? !"false".equalsIgnoreCase(globalGetRealPathConfiguration) : !"false"
                .equalsIgnoreCase(applicationGetRealPathConfiguration);
        deleteTempDirectoryOnUndeployed(tomcatContext, tempRootDirectory);
        /**
         * Compute & check module checksum in order to determine if the expanded module
         * contents should be updated (old contents deleted & module re-expanded).
         */
        File checksumFile = new File(tempRootDirectory, "checksum.flag");
        boolean refreshmentRequired = false;
        byte[] checksum = null;
        try {
            URL bundleURL = new URL(bundle.getLocation());
            checksum = getChecksum(bundleURL);
            if (checksumFile.exists()) {
                byte[] savedChecksum = readChecksum(checksumFile);
                if (!Arrays.equals(checksum, savedChecksum)) {
                    refreshmentRequired = true;
                }
            } else {
                refreshmentRequired = true;
            }
        } catch (Exception e) {
            getLogger().warn("Unable to compute module checksum", e);
            refreshmentRequired = true;
        }
        if (refreshmentRequired && checksum != null) {
            tempRootDirectory.mkdirs();
            try {
                writeChecksum(checksumFile, checksum);
            } catch (Exception e) {
                getLogger().error("Unable to write module checksum file", e);
            }
        }
        //For embed resources in jar files, they are always required to extract no matter getRealPath and development stage support are required
        File jarResourceRootDirectory = new File(tempRootDirectory, "jar_resources");
        if (refreshmentRequired || !jarResourceRootDirectory.exists()) {
            try {
                FileUtils.recursiveDelete(jarResourceRootDirectory);
                jarResourceRootDirectory.mkdirs();
                Enumeration<URL> en = tomcatContext.getBundle().findEntries(tomcatContext.getModulePath() != null ? tomcatContext.getModulePath() + "/WEB-INF/lib" : "WEB-INF/lib", "*.jar", false);
                if (en != null) {
                    while (en.hasMoreElements()) {
                        URL jarUrl = en.nextElement();
                        File jarResourceDirectory = new File(jarResourceRootDirectory, jarUrl.getFile().substring(jarUrl.getFile().lastIndexOf('/') + 1));
                        jarResourceDirectory.mkdirs();
                        ZipInputStream in = null;
                        try {
                            in = new ZipInputStream(jarUrl.openStream());
                            JarUtils.unzipToDirectory(in, jarResourceDirectory, "META-INF/resources", false);
                        } finally {
                            IOUtils.close(in);
                        }
                    }
                }
            } catch (IOException e) {
                checksumFile.delete();
                throw new DeploymentException("Fail to create static resoruce cache for jar files in WEB-INF folder", e);
            }
        }
        for (File resourceDirectory : jarResourceRootDirectory.listFiles()) {
            if (resourceDirectory.isDirectory() && resourceDirectory.getName().endsWith(".jar") && resourceDirectory.listFiles().length > 0) {
                FileDirContext fileDirContext = new FileDirContext();
                fileDirContext.setAllowLinking(allowLinking);
                fileDirContext.setDocBase(resourceDirectory.getAbsolutePath());
                altDirContexts.add(fileDirContext);
            }
        }
        //If it is system artifact, or no getRealPath and development stage support is required, just use BundleDirContext
        if (!(getRealPathSupportRequired || developmentStage)) {
            return new BundleDirContext(tomcatContext.getBundle(), tomcatContext.getModulePath(), altDirContexts, null);
        }
        File realPathTempDirectory = new File(tempRootDirectory, "real_path");
        if (refreshmentRequired || !realPathTempDirectory.exists()) {
            FileUtils.recursiveDelete(realPathTempDirectory);
            realPathTempDirectory.mkdirs();
            String modulePath = tomcatContext.getModulePath() == null ? "" : tomcatContext.getModulePath();
            try {
                File bundleFile = BundleUtils.toFile(bundle);
                if (bundleFile != null) {
                    if (bundleFile.isFile()) {
                        JarUtils.unzipToDirectory(new ZipFile(bundleFile), realPathTempDirectory, modulePath, true);
                    } else {
                        FileUtils.copyFile(new File(bundleFile, modulePath), realPathTempDirectory);
                    }
                } else {
                    ZipInputStream zipIn = null;
                    try {
                        zipIn = new ZipInputStream(new URL(bundle.getLocation()).openStream());
                        JarUtils.unzipToDirectory(zipIn, realPathTempDirectory, modulePath, true);
                    } finally {
                        IOUtils.close(zipIn);
                    }
                }
            } catch (IOException e) {
                checksumFile.delete();
                getLogger().warn("fail to extract the bundle, getRealPath might not work", e);
            }
        }
        if (developmentStage) {
            GeronimoFileDirContext fileDirContext = new GeronimoFileDirContext(altDirContexts);
            fileDirContext.setAllowLinking(allowLinking);
            setDocBase(realPathTempDirectory.getAbsolutePath());
            return fileDirContext;
        } else {
            return new BundleDirContext(tomcatContext.getBundle(), tomcatContext.getModulePath(), altDirContexts, realPathTempDirectory);
        }
    }

    private void deleteTempDirectoryOnUndeployed(TomcatContext tomcatContext, File tempDirectory) {
        if (!(tomcatContext instanceof TomcatWebAppContext)) {
            return;
        }
        TomcatWebAppContext tomcatWebAppContext = (TomcatWebAppContext) tomcatContext;
        BundleContext bundleContext = bundle.getBundleContext();
        ServiceReference tomcatDeploymentWatcherReference = null;
        try {
            tomcatDeploymentWatcherReference = bundleContext.getServiceReference(TomcatDeploymentWatcher.class.getName());
            if (tomcatDeploymentWatcherReference == null) {
                return;
            }
            TomcatDeploymentWatcher tomcatDeploymentWatcher = (TomcatDeploymentWatcher) bundleContext.getService(tomcatDeploymentWatcherReference);
            tomcatDeploymentWatcher.deleteOnUndeployed(bundle, tomcatWebAppContext.getAbstractName(), tempDirectory);
        } catch (Exception e) {
            logger.warn("Unable to add temporary directory " + tempDirectory + "to the Tomcat deployment watcher, that folder will not be delete on the undeployment", e);
        } finally {
            if (tomcatDeploymentWatcherReference != null) {
                try {
                    bundleContext.ungetService(tomcatDeploymentWatcherReference);
                } catch (Exception e) {
                }
            }
        }
    }

    private class SystemMethodValve extends ValveBase {

        public SystemMethodValve() {
            super(true);
        }

        @Override
        public void invoke(Request request, Response response) throws IOException, ServletException {
            if (request == null && response == null) {
                //Execute WebModuleListeners
                ClassLoader oldClassLoader = null;
                try {
                    oldClassLoader = bindThread();
                    if (webModuleListeners != null) {
                        for (Object webModuleListener : webModuleListeners) {
                            if (webModuleListener instanceof WebModuleListener) {
                                ((WebModuleListener) webModuleListener).moduleInitialized(getServletContext());
                            } else {
                                logger.warn("Invalid WebModuleListener " + webModuleListener.getClass().getName());
                            }
                        }
                    }
                } finally {
                    unbindThread(oldClassLoader);
                }
                try {
                    GeronimoStandardContext.super.startInternal();
                } catch (LifecycleException e) {
                    throw (IOException) new IOException("wrapping lifecycle exception").initCause(e);
                }
                if (!GeronimoStandardContext.this.getAvailable()) {
                    throw new IOException("Context did not start for an unknown reason");
                }
            } else {
                getNext().invoke(request, response);
            }
        }
    }

    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        super.stopInternal();
        ClassLoader oldClassLoader = null;
        try {
            oldClassLoader = bindThread();
            if (webModuleListeners != null) {
                for (Object webModuleListener : webModuleListeners) {
                    if (webModuleListener instanceof WebModuleListener) {
                        ((WebModuleListener) webModuleListener).moduleDestoryed(getServletContext());
                    } else {
                        logger.warn("Invalid WebModuleListener " + webModuleListener.getClass().getName());
                    }
                }
            }
        } finally {
            unbindThread(oldClassLoader);
        }
    }

    public BeforeAfter getBeforeAfter() {
        return beforeAfter;
    }

    public int getContextCount() {
        return contextCount;
    }

    /**
     * Determine if the context has at least one servlet that specifies a run-as role
     * @return true if at least one servlet specifies a run-as role, false otherwise
     */
    protected boolean hasRunAsServlet() {
        for (Container servlet : findChildren()) {
            if (servlet instanceof Wrapper) {
                if (((Wrapper) servlet).getRunAs() != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the Subject for the servlet's run-as role
     * @param runAsRole Name of run as role to get Subject for
     * @return Subject for the servlet's run-as role, if specified.  otherwise null.
     */
    public Subject getSubjectForRole(String runAsRole) {
        return runAsSource.getSubjectForRole(runAsRole);
    }

    public boolean isAuthenticatorInstalled() {
        return authenticatorInstalled;
    }

    public void setAuthenticatorInstalled(boolean authenticatorInstalled) {
        this.authenticatorInstalled = authenticatorInstalled;
    }

    public ConfigurationFactory getConfigurationFactory() {
        return configurationFactory;
    }

    public Subject getDefaultSubject() {
        return defaultSubject;
    }

    public String getPolicyContextId() {
        return policyContextId;
    }

    @Override
    public String getBasePath() {
        //TODO Override setDocBase with an empty block to avoid NullPointerException
        if (getDocBase() == null) {
            return null;
        } else {
            return super.getBasePath();
        }
    }

    @Override
    public ServletRegistration.Dynamic dynamicServletAdded(Wrapper wrapper) {
        ServletRegistration.Dynamic registration = new ApplicationServletRegistration(wrapper, this);
        if (wrapper.getServlet() == null || webSecurityConstraintStore.isContainerCreatedDynamicServlet(wrapper.getServlet())) {
            webSecurityConstraintStore.addContainerCreatedDynamicServletEntry(registration, wrapper.getServletClass());
        }
        //Special  handle for web service
        ClassLoader cl = this.getParentClassLoader();
        Class<?> baseServletClass;
        Class<?> servletClass;
        try {
            baseServletClass = cl.loadClass(Servlet.class.getName());
            servletClass = cl.loadClass(wrapper.getServletClass());
            //Check if the servlet is of type Servlet class
            if (!baseServletClass.isAssignableFrom(servletClass)) {
                //Nope - its probably a webservice, so lets see...
                if (webServiceMap != null) {
                    WebServiceContainer webServiceContainer = webServiceMap.get(wrapper.getName());
                    if (webServiceContainer != null) {
                        //Yep its a web service
                        //So swap it out with a POJOWebServiceServlet
                        wrapper.setServletClass("org.apache.geronimo.webservices.POJOWebServiceServlet");
                        //Set the WebServiceContainer stuff
                        String webServicecontainerID = wrapper.getName() + WebServiceContainerInvoker.WEBSERVICE_CONTAINER + webServiceContainer.hashCode();
                        getServletContext().setAttribute(webServicecontainerID, webServiceContainer);
                        wrapper.addInitParameter(WebServiceContainerInvoker.WEBSERVICE_CONTAINER, webServicecontainerID);
                        //Set the SEI Class in the attribute
                        String pojoClassID = wrapper.getName() + POJOWebServiceServlet.POJO_CLASS + servletClass.hashCode();
                        getServletContext().setAttribute(pojoClassID, servletClass);
                        wrapper.addInitParameter(POJOWebServiceServlet.POJO_CLASS, pojoClassID);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return registration;
    }

    @Override
    public void dynamicServletCreated(Servlet servlet) {
        webSecurityConstraintStore.addContainerCreatedDynamicServlet(servlet);
    }

    @Override
    public Set<String> addServletSecurity(ApplicationServletRegistration registration, ServletSecurityElement servletSecurityElement) {
        return webSecurityConstraintStore.setDynamicServletSecurity(registration, servletSecurityElement);
    }

    @Override
    public void addSecurityRole(String role) {
        super.addSecurityRole(role);
        webSecurityConstraintStore.declareRoles(role);
    }

    @Override
    protected ClassLoader bindThread() {
        ClassLoader oldClassLoader = super.bindThread();
        BeforeAfterContext beforeAfterContext = null;
        if (beforeAfter != null) {
            beforeAfterContext = new BeforeAfterContext(contextCount);
            beforeAfter.before(beforeAfterContext, null, null, BeforeAfter.EDGE_SERVLET);
            //beforeAfterContext is pushed the stack only if every BeforeAfter element works fine
            beforeAfterContexts.get().push(beforeAfterContext);
        }
        return oldClassLoader;
    }

    @Override
    protected void unbindThread(ClassLoader oldContextClassLoader) {
        try {
            super.unbindThread(oldContextClassLoader);
        } finally {
            if (beforeAfter != null) {
                beforeAfter.after(beforeAfterContexts.get().pop(), null, null, 0);
            }
        }
    }

    @Override
    public boolean fireRequestInitEvent(ServletRequest request) {
        return true;
    }
    
    @Override
    public boolean fireRequestDestroyEvent(ServletRequest request) {
        return true;
    }
    
    
    public boolean fireRequestDestroyEventInBeforeAfter(ServletRequest request) {
        return super.fireRequestDestroyEvent(request);
    }

    public boolean fireRequestInitEventInBeforeAfter(ServletRequest request) {
        return super.fireRequestInitEvent(request);
    }

    private static byte[] getChecksum(URL url) throws Exception {
        InputStream in = null;
        try {
            URLConnection connection = url.openConnection();
            File file = BundleUtils.toFile(url);
            if (file != null) {
                if (file.isFile()) {
                    in = new FileInputStream(file);
                } else {
                    //Should never run to here, as if it is a directory, no need to create a bundle context
                    throw new IOException("Unable to calculate checksum for the directory");
                }
            } else {
                in = connection.getInputStream();
            }
            /*
             * Use URL's lastModified as the checksum if available, otherwise
             * calculate checksum based on the contents.
             */
            long lastModified = connection.getLastModified();
            if (lastModified == 0) {
                return calculateChecksum(in, "SHA-1");
            } else {
                return toByteArray(lastModified);
            }
        } finally {
            IOUtils.close(in);
        }
    }

    private static byte[] calculateChecksum(InputStream stream, String algorithm) throws NoSuchAlgorithmException, IOException {
        MessageDigest digester = MessageDigest.getInstance(algorithm);
        digester.reset();
        byte buf[] = new byte[4096];
        int len = 0;
        while ((len = stream.read(buf, 0, buf.length)) != -1) {
            digester.update(buf, 0, len);
        }
        return digester.digest();
    }

    private static void writeChecksum(File file, byte[] data) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        try {
            out.write(data);
        } finally {
            IOUtils.close(out);
        }
    }

    private static byte[] readChecksum(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        try {
            return IOUtils.getBytes(in);
        } finally {
            IOUtils.close(in);
        }
    }

    private static byte[] toByteArray(long value) {
        byte[] buffer = new byte[8];
        buffer[0] = (byte) (value >>> 56);
        buffer[1] = (byte) (value >>> 48);
        buffer[2] = (byte) (value >>> 40);
        buffer[3] = (byte) (value >>> 32);
        buffer[4] = (byte) (value >>> 24);
        buffer[5] = (byte) (value >>> 16);
        buffer[6] = (byte) (value >>> 8);
        buffer[7] = (byte) (value >>> 0);
        return buffer;
    }
}
