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

import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.naming.directory.DirContext;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.servlet.Servlet;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.apache.catalina.Container;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Engine;
import org.apache.catalina.Globals;
import org.apache.catalina.InstanceListener;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Loader;
import org.apache.catalina.Manager;
import org.apache.catalina.Valve;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardWrapper;
import org.apache.catalina.ha.CatalinaCluster;
import org.apache.catalina.valves.ValveBase;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.GeronimoSecurityException;
import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.geronimo.kernel.util.IOUtils;
import org.apache.geronimo.osgi.web.WebApplicationUtils;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.jaas.ConfigurationFactory;
import org.apache.geronimo.security.jacc.RunAsSource;
import org.apache.geronimo.tomcat.core.GeronimoApplicationContext;
import org.apache.geronimo.tomcat.interceptor.BeforeAfter;
import org.apache.geronimo.tomcat.interceptor.ComponentContextBeforeAfter;
import org.apache.geronimo.tomcat.interceptor.InstanceContextBeforeAfter;
import org.apache.geronimo.tomcat.interceptor.UserTransactionBeforeAfter;
import org.apache.geronimo.tomcat.listener.DispatchListener;
import org.apache.geronimo.tomcat.listener.JACCSecurityLifecycleListener;
import org.apache.geronimo.tomcat.listener.RunAsInstanceListener;
import org.apache.geronimo.tomcat.util.SecurityHolder;
import org.apache.geronimo.tomcat.valve.GeronimoBeforeAfterValve;
import org.apache.geronimo.tomcat.valve.ProtectedTargetValve;
import org.apache.geronimo.web.WebAttributeName;
import org.apache.geronimo.webservices.POJOWebServiceServlet;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.apache.geronimo.webservices.WebServiceContainerInvoker;
import org.apache.naming.resources.FileDirContext;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.WebApp;
import org.apache.tomcat.InstanceManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;


/**
 * @version $Rev$ $Date$
 */
public class GeronimoStandardContext extends StandardContext {

    private static final long serialVersionUID = 3834587716552831032L;

    private static final boolean allowLinking = Boolean.getBoolean("org.apache.geronimo.tomcat.GeronimoStandardContext.allowLinking");

    //private static final boolean FLUSH_STATIC_RESOURCES_ON_STARTUP = Boolean.getBoolean("org.apache.geronimo.tomcat.GeronimoStandardContext.flushStaticResourcesOnStartup");

    private Subject defaultSubject = null;
    private RunAsSource runAsSource = RunAsSource.NULL;

    private Map webServiceMap = null;

    private boolean pipelineInitialized;

    private BeforeAfter beforeAfter = null;
    private int contextCount = 0;

    private boolean authenticatorInstalled;
    private ConfigurationFactory configurationFactory;
    private String policyContextId;

    private Bundle bundle;
    private ServiceRegistration serviceRegistration;

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
        javax.naming.Context enc = ctx.getJndiContext();
        setInstanceManager(ctx.getInstanceManager());

        //try to make sure this mbean properties match those of the TomcatWebAppContext
        if (ctx instanceof TomcatWebAppContext) {
            TomcatWebAppContext tomcatWebAppContext = (TomcatWebAppContext) ctx;
            setJavaVMs(tomcatWebAppContext.getJavaVMs());
            setServer(tomcatWebAppContext.getServer());
            setJ2EEApplication(tomcatWebAppContext.getJ2EEApplication());
            setJ2EEServer(tomcatWebAppContext.getJ2EEServer());
            //install jasper injection support if required
            if (tomcatWebAppContext.getRuntimeCustomizer() != null) {
                Map<String, Object> servletContext = new HashMap<String, Object>();
                Map<Class, Object> customizerContext = new HashMap<Class, Object>();
                customizerContext.put(Map.class, servletContext);
                customizerContext.put(javax.naming.Context.class, enc);
                tomcatWebAppContext.getRuntimeCustomizer().customize(customizerContext);
                for (Map.Entry<String, Object> entry: servletContext.entrySet()) {
                    getServletContext().setAttribute(entry.getKey(), entry.getValue());
                }
            }
            if (tomcatWebAppContext.getSecurityHolder() != null) {
                configurationFactory = tomcatWebAppContext.getSecurityHolder().getConfigurationFactory();

                //Add JACCSecurityLifecycleListener, it will calculate the security configurations when web module is initialized
                addJACCSecurityLifecycleListener(tomcatWebAppContext);
            }

            ServletContext servletContext = getServletContext();
            servletContext.setAttribute(InstanceManager.class.getName(), ctx.getInstanceManager());

            //Set some attributes passed from the deployment process
            List<String> orderedLists = (List<String>) tomcatWebAppContext.getDeploymentAttribute(WebAttributeName.ORDERED_LIBS.name());
            if (orderedLists != null) {
                servletContext.setAttribute(ServletContext.ORDERED_LIBS, Collections.unmodifiableList(orderedLists));
            }
            //Set ServletContainerInitializer
            Map<String, Set<String>> servletContainerInitializerClassNamesMap = (Map<String, Set<String>>) tomcatWebAppContext.getDeploymentAttribute(WebAttributeName.SERVLET_CONTAINER_INITIALIZERS
                    .name());
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
                                    logger.warn("Fail to load class " + cls + " interested by ServletContainerInitializer " + servletContainerInitializerClassName, e);
                                }
                            }
                            addServletContainerInitializer(servletContainerInitializer, classSet);
                        }
                    } catch (IllegalAccessException e) {
                        logger.error("Fail to initialize ServletContainerInitializer " + servletContainerInitializerClassName, e);
                    } catch (InstantiationException e) {
                        logger.error("Fail to initialize ServletContainerInitializer " + servletContainerInitializerClassName, e);
                    } catch (ClassNotFoundException e) {
                        logger.error("Fail to initialize ServletContainerInitializer " + servletContainerInitializerClassName, e);
                    }
                }
            }
        }

        int index = 0;
        BeforeAfter interceptor = new InstanceContextBeforeAfter(null,
                index++,
                index++, ctx.getUnshareableResources(),
                ctx.getApplicationManagedSecurityResources(),
                ctx.getTrackedConnectionAssociator());

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

//                interceptor = new PolicyContextBeforeAfter(interceptor, index++, index++, index++, policyContextId, defaultSubject);

            }
        }

        //Set a UserTransactionBeforeAfter
        interceptor = new UserTransactionBeforeAfter(interceptor, index++, ctx.getUserTransaction());

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
        List valveChain = ctx.getValveChain();
        if (valveChain != null) {
            for (Object valve : valveChain) {
                addValve((Valve)valve);
            }
        }

        // Add User Defined Listeners
        List listenerChain = ctx.getLifecycleListenerChain();
        if (listenerChain != null) {
            for (Object listener : listenerChain) {
                addLifecycleListener((LifecycleListener)listener);
            }
        }

        CatalinaCluster cluster = ctx.getCluster();
        if (cluster != null)
            this.setCluster(cluster);

        Manager manager = ctx.getManager();
        if (manager != null)
            this.setManager(manager);

        pipelineInitialized = true;
        this.webServiceMap = ctx.getWebServices();

        this.setCrossContext(ctx.isCrossContext());

        this.setWorkDir(ctx.getWorkDir());

        super.setAllowLinking(allowLinking);

        this.setCookies(!ctx.isDisableCookies());

        //Set the Dispatch listener
        this.addInstanceListener(DispatchListener.class.getName());

        //Set the run-as listener. listeners must be added before start() is called
        if (runAsSource != null) {
            this.addInstanceListener(RunAsInstanceListener.class.getName());
        }
    }

    private void addJACCSecurityLifecycleListener(TomcatWebAppContext tomcatWebAppContext) throws DeploymentException {
        float schemaVersion = (Float) tomcatWebAppContext.getDeploymentAttribute(WebAttributeName.SCHEMA_VERSION.name());
        boolean metaComplete = (Boolean) tomcatWebAppContext.getDeploymentAttribute(WebAttributeName.META_COMPLETE.name());
        try {
            WebApp webApp = tomcatWebAppContext.getDeploymentDescriptor() == null ? null : (WebApp) JaxbJavaee.unmarshalJavaee(WebApp.class, new ByteArrayInputStream(tomcatWebAppContext
                    .getDeploymentDescriptor().getBytes()));
            addLifecycleListener(new JACCSecurityLifecycleListener(bundle, webApp, schemaVersion >= 2.5f && !metaComplete, tomcatWebAppContext.getApplicationPolicyConfigurationManager(),
                    tomcatWebAppContext.getSecurityHolder().getPolicyContextID()));
        } catch (DeploymentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("fail to parse the web.xml file while starting the web application", e);
            throw new DeploymentException("fail to parse the web.xml file while starting the web application", e);
        }
    }

    private final Object instanceListenersLock = new Object();
    private final Object wrapperLifecyclesLock = new Object();
    private final Object wrapperListenersLock = new Object();
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
            for (String instanceListener: findInstanceListeners()) {
                try {
                    InstanceListener listener =
                      (InstanceListener) getInstanceManager().newInstance(instanceListener);
                    wrapper.addInstanceListener(listener);
                } catch (Throwable t) {
                    getLogger().error("createWrapper", t);
                    return (null);
                }
            }
        }

        synchronized (wrapperLifecyclesLock) {
            for (String wrapperLifecycle: findWrapperLifecycles()) {
                try {
                    LifecycleListener listener =
                      (LifecycleListener) getInstanceManager().newInstance(wrapperLifecycle);
                    wrapper.addLifecycleListener(listener);
                } catch (Throwable t) {
                    getLogger().error("createWrapper", t);
                    return (null);
                }
            }
        }

        synchronized (wrapperListenersLock) {
            for (String wrapperListener: findWrapperListeners()) {
                try {
                    ContainerListener listener =
                      (ContainerListener) getInstanceManager().newInstance(wrapperListener);
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
    public void backgroundProcess() {
        Object context[] = null;

        if (beforeAfter != null){
            context = new Object[contextCount];
            beforeAfter.before(context, null, null, BeforeAfter.EDGE_SERVLET);
        }

        try {
            super.backgroundProcess();
        } finally {
            if (beforeAfter != null){
                beforeAfter.after(context, null, null, 0);
            }
        }
    }

    public void kill() throws Exception {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }

        Object context[] = null;

        if (beforeAfter != null){
            context = new Object[contextCount];
            beforeAfter.before(context, null, null, BeforeAfter.EDGE_SERVLET);
        }

        try {
            stop();
            destroy();
        } finally {
            if (beforeAfter != null){
                beforeAfter.after(context, null, null, 0);
            }
        }
    }

    protected void initInternal()  throws LifecycleException {
        String docBase = getDocBase();
        super.initInternal();
        setDocBase(docBase);
    }

    protected void startInternal() throws LifecycleException {
        if (pipelineInitialized) {
            try {
                Valve valve = getPipeline().getFirst();
                valve.invoke(null, null);

                // if a servlet uses run-as then make sure role desgnates have been provided
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
        } else {
            super.startInternal();
        }

        // for OSGi Web Applications support register ServletContext in service registry
        if (WebApplicationUtils.isWebApplicationBundle(bundle)) {
            serviceRegistration = WebApplicationUtils.registerServletContext(bundle, getServletContext());
        }
    }

    public void addChild(Container child) {
        Wrapper wrapper = (Wrapper) child;

        String servletClassName = wrapper.getServletClass();
        if (servletClassName == null) {
            super.addChild(child);
            return;
        }

        ClassLoader cl = this.getParentClassLoader();

        Class baseServletClass;
        Class servletClass;
        try {
            baseServletClass = cl.loadClass(Servlet.class.getName());
            servletClass = cl.loadClass(servletClassName);
            //Check if the servlet is of type Servlet class
            if (!baseServletClass.isAssignableFrom(servletClass)) {
                //Nope - its probably a webservice, so lets see...
                if (webServiceMap != null) {
                    WebServiceContainer webServiceContainer = (WebServiceContainer) webServiceMap.get(wrapper.getName());

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

        super.addChild(child);
    }

    public synchronized void setLoader(final Loader delegate) {
        Loader loader = new Loader() {

            public void backgroundProcess() {
                delegate.backgroundProcess();
            }

            public ClassLoader getClassLoader() {
                // Implementation Note: the actual CL to be used by this
                // context is the Geronimo one and not the Tomcat one.
                return parentClassLoader;
            }

            public Container getContainer() {
                return delegate.getContainer();
            }

            public void setContainer(Container container) {
                delegate.setContainer(container);
            }

            public boolean getDelegate() {
                return delegate.getDelegate();
            }

            public void setDelegate(boolean delegateBoolean) {
                delegate.setDelegate(delegateBoolean);
            }

            public String getInfo() {
                return delegate.getInfo();
            }

            public boolean getReloadable() {
                return false;
            }

            public void setReloadable(boolean reloadable) {
                if (reloadable) {
                    throw new UnsupportedOperationException("Reloadable context is not supported.");
                }
            }

            public void addPropertyChangeListener(PropertyChangeListener listener) {
                delegate.addPropertyChangeListener(listener);
            }

            public void addRepository(String repository) {
                delegate.addRepository(repository);
            }

            public String[] findRepositories() {
                return delegate.findRepositories();
            }

            public boolean modified() {
                return delegate.modified();
            }

            public void removePropertyChangeListener(PropertyChangeListener listener) {
                delegate.removePropertyChangeListener(listener);
            }
        };

        super.setLoader(loader);
    }


    @Override
    public ServletContext getServletContext() {
        if (context == null) {
            context = new GeronimoApplicationContext(this);
            if (getAltDDName() != null)
                context.setAttribute(Globals.ALT_DD_ATTR, getAltDDName());
        }
        return super.getServletContext();
    }

    public ServletContext getInternalServletContext() {
        return context;
    }

    protected DirContext createDirContext(TomcatContext tomcatContext) throws DeploymentException {
        List<DirContext> altDirContexts = new ArrayList<DirContext>();
        Engine engine = (Engine)getParent().getParent();
        String serviceName = engine.getService().getName();
        String engineName = engine.getName();
        String hostName = getParent().getName();
        String tomcatHome = System.getProperty("catalina.home");
        File resourceRootDirectory = new File(tomcatHome + File.separator + "resources" + File.separator + serviceName + File.separator + engineName + File.separator + hostName + File.separator
                + (getName().equals("/") ? "_" : getName()));
        File completeFlagFile = new File(resourceRootDirectory, "complete.flag");
        //if (!resourceRootDirectory.exists() || FLUSH_STATIC_RESOURCES_ON_STARTUP || !completeFlagFile.exists()) {
            try {
                completeFlagFile.delete();
                FileUtils.recursiveDelete(resourceRootDirectory);
                resourceRootDirectory.mkdirs();
                Enumeration<URL> en = tomcatContext.getBundle().findEntries(tomcatContext.getModulePath() != null ? tomcatContext.getModulePath() + "/WEB-INF/lib" : "WEB-INF/lib", "*.jar", false);
                if (en != null) {
                    while (en.hasMoreElements()) {
                        URL jarUrl = en.nextElement();
                        File jarResourceDirectory = new File(resourceRootDirectory, jarUrl.getFile().substring(jarUrl.getFile().lastIndexOf('/') + 1));
                        jarResourceDirectory.mkdirs();
                        ZipInputStream in = null;
                        try {
                            in = new ZipInputStream(jarUrl.openStream());
                            ZipEntry zipEntry;
                            while ((zipEntry = in.getNextEntry()) != null) {
                                String name = zipEntry.getName();
                                if (name.indexOf("META-INF/resources") == 0) {
                                    if (zipEntry.isDirectory()) {
                                        new File(jarResourceDirectory, name).mkdirs();
                                    } else {
                                        File resourceFile = new File(jarResourceDirectory, name);
                                        resourceFile.getParentFile().mkdirs();
                                        OutputStream out = null;
                                        try {
                                            out = new FileOutputStream(resourceFile);
                                            IOUtils.copy(in, out);
                                        } finally {
                                            IOUtils.close(out);
                                        }
                                    }
                                }
                            }
                        } finally {
                            IOUtils.close(in);
                        }
                    }
                }
                completeFlagFile.createNewFile();
            } catch (IOException e) {
                throw new DeploymentException("Fail to create static resoruce cache for jar files in WEB-INF folder", e);
            }
        //}
        for (File resourceDirectory : resourceRootDirectory.listFiles()) {
            if (resourceDirectory.isDirectory() && resourceDirectory.getName().endsWith(".jar") && resourceDirectory.listFiles().length > 0) {
                FileDirContext fileDirContext = new FileDirContext();
                fileDirContext.setAllowLinking(allowLinking);
                fileDirContext.setDocBase(resourceDirectory.getAbsolutePath());
                altDirContexts.add(fileDirContext);
            }
        }
        return new BundleDirContext(tomcatContext.getBundle(), tomcatContext.getModulePath(), altDirContexts);
    }

    private class SystemMethodValve extends ValveBase {

        public SystemMethodValve(){
            super(true);
        }

        public void invoke(Request request, Response response) throws IOException, ServletException {
            if (request == null && response == null) {
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
                if (((Wrapper)servlet).getRunAs() != null) {
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
}
