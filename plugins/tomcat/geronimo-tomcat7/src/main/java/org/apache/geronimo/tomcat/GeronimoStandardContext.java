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
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.catalina.Container;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Loader;
import org.apache.catalina.Manager;
import org.apache.catalina.Valve;
import org.apache.catalina.Wrapper;
import org.apache.catalina.InstanceListener;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardWrapper;
import org.apache.catalina.ha.CatalinaCluster;
import org.apache.catalina.valves.ValveBase;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.GeronimoSecurityException;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.jaas.ConfigurationFactory;
import org.apache.geronimo.security.jacc.RunAsSource;
import org.apache.geronimo.tomcat.interceptor.BeforeAfter;
import org.apache.geronimo.tomcat.interceptor.ComponentContextBeforeAfter;
import org.apache.geronimo.tomcat.interceptor.InstanceContextBeforeAfter;
import org.apache.geronimo.tomcat.interceptor.UserTransactionBeforeAfter;
import org.apache.geronimo.tomcat.listener.DispatchListener;
import org.apache.geronimo.tomcat.listener.RunAsInstanceListener;
import org.apache.geronimo.tomcat.util.SecurityHolder;
import org.apache.geronimo.tomcat.valve.GeronimoBeforeAfterValve;
import org.apache.geronimo.webservices.POJOWebServiceServlet;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.apache.geronimo.webservices.WebServiceContainerInvoker;
import org.apache.tomcat.InstanceManager;


/**
 * @version $Rev$ $Date$
 */
public class GeronimoStandardContext extends StandardContext {

    private static final long serialVersionUID = 3834587716552831032L;
    private static final boolean allowLinking;

    static {
        allowLinking = new Boolean(System.getProperty("org.apache.geronimo.tomcat.GeronimoStandardContext.allowLinking", "false"));
    }

    private Subject defaultSubject = null;
    private RunAsSource runAsSource = RunAsSource.NULL;

    private Map webServiceMap = null;

    private boolean pipelineInitialized;

    private BeforeAfter beforeAfter = null;
    private int contextCount = 0;

    private boolean authenticatorInstalled;
    private ConfigurationFactory configurationFactory;
    private String policyContextId;

    public GeronimoStandardContext() {
    }

    public void setContextProperties(TomcatContext ctx) throws DeploymentException {
        setResources(new BundleDirContext(ctx.getBundle(), ctx.getModulePath()));

        // Create ReadOnlyContext
        javax.naming.Context enc = ctx.getJndiContext();
        setInstanceManager(ctx.getInstanceManager());

        //try to make sure this mbean properties match those of the TomcatWebAppContext
        if (ctx instanceof TomcatWebAppContext) {
            TomcatWebAppContext tctx = (TomcatWebAppContext) ctx;
            setJavaVMs(tctx.getJavaVMs());
            setServer(tctx.getServer());
            setJ2EEApplication(tctx.getJ2EEApplication());
            setJ2EEServer(tctx.getJ2EEServer());
            //install jasper injection support if required
            if (tctx.getRuntimeCustomizer() != null) {
                Map<String, Object> servletContext = new HashMap<String, Object>();
                Map<Class, Object> customizerContext = new HashMap<Class, Object>();
                customizerContext.put(Map.class, servletContext);
                customizerContext.put(javax.naming.Context.class, enc);
                tctx.getRuntimeCustomizer().customize(customizerContext);
                for (Map.Entry<String, Object> entry: servletContext.entrySet()) {
                    getServletContext().setAttribute(entry.getKey(), entry.getValue());
                }
            }
            if (tctx.getSecurityHolder() != null) {
                configurationFactory = tctx.getSecurityHolder().getConfigurationFactory();
            }

            getServletContext().setAttribute(InstanceManager.class.getName(), ctx.getInstanceManager());
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
                    if (wrapper instanceof Lifecycle)
                        ((Lifecycle) wrapper).addLifecycleListener(listener);
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

    public void init() throws Exception {
        String docBase = getDocBase();
        super.init();
        setDocBase(docBase);
    }

    public synchronized void start() throws LifecycleException {
        if (pipelineInitialized) {
            try {
                Valve valve = getFirst();
                valve.invoke(null, null);
                //Install the DefaultSubjectValve after the authentication valve so the default subject is supplied
                //only if no real subject is authenticated.

//                Valve defaultSubjectValve = new DefaultSubjectValve(defaultSubject);
//                addValve(defaultSubjectValve);

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
            super.start();
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

    private class SystemMethodValve extends ValveBase {

        public void invoke(Request request, Response response) throws IOException, ServletException {
            if (request == null && response == null) {
                try {
                    GeronimoStandardContext.super.start();
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
}
