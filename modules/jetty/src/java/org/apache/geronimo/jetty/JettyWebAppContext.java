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
import java.net.URI;
import java.net.URL;
import java.util.Set;

import javax.resource.ResourceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.kernel.config.ConfigurationParent;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.naming.java.RootContext;
import org.apache.geronimo.transaction.DefaultInstanceContext;
import org.apache.geronimo.transaction.InstanceContext;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.apache.geronimo.transaction.UserTransactionImpl;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.jetty.servlet.WebApplicationContext;

/**
 * Wrapper for a WebApplicationContext that sets up its J2EE environment.
 *
 * @version $Revision: 1.3 $ $Date: 2004/07/18 22:04:27 $
 */
public class JettyWebAppContext extends WebApplicationContext implements GBeanLifecycle {

    private static Log log = LogFactory.getLog(JettyWebAppContext.class);

    private final ConfigurationParent config;
    private final URI uri;
    private final JettyContainer container;
    private final ReadOnlyContext componentContext;
    private final TransactionContextManager transactionContextManager;
    private final TrackedConnectionAssociator associator;
    private final UserTransactionImpl userTransaction;
    private final ClassLoader classLoader;

    // @todo get these from DD
    private final Set unshareableResources;
    private final Set applicationManagedSecurityResources;

    private boolean contextPriorityClassLoader = false;

    public JettyWebAppContext() {
        this(null, null, null, null, null, null, null, null, null, null);
    }

    public JettyWebAppContext(URI uri,
                              ReadOnlyContext compContext,
                              UserTransactionImpl userTransaction,
                              ClassLoader classLoader,
                              Set unshareableResources,
                              Set applicationManagedSecurityResources,
                              TransactionContextManager transactionContextManager,
                              TrackedConnectionAssociator associator,
                              ConfigurationParent config,
                              JettyContainer container
                              ) {
        super();
        this.config = config;
        this.uri = uri;
        this.container = container;
        this.componentContext = compContext;
        this.unshareableResources = unshareableResources;
        this.applicationManagedSecurityResources = applicationManagedSecurityResources;
        this.transactionContextManager = transactionContextManager;
        this.associator = associator;
        this.userTransaction = userTransaction;
        this.classLoader = classLoader;

        setConfiguration(new JettyXMLConfiguration(this));
    }

    /**
     * getContextPriorityClassLoader.
     *
     * @return True if this context should give web application class in preference over the containers
     *         classes, as per the servlet specification recommendations.
     */
    public boolean getContextPriorityClassLoader() {
        return contextPriorityClassLoader;
    }

    /**
     * setContextPriorityClassLoader.
     *
     * @param b True if this context should give web application class in preference over the containers
     * classes, as per the servlet specification recommendations.
     */
    public void setContextPriorityClassLoader(boolean b) {
        contextPriorityClassLoader = b;
    }

    /**
     * init the classloader. Uses the value of contextPriorityClassLoader to
     * determine if the context needs to create its own classloader.
     */
    protected void initClassLoader(boolean forceContextLoader)
            throws MalformedURLException, IOException {

        setClassLoaderJava2Compliant(!contextPriorityClassLoader);
        if (!contextPriorityClassLoader) {
            // TODO - once geronimo is correctly setting up the classpath, this should be uncommented.
            // At the moment, the g classloader does not appear to know about the WEB-INF classes and lib.
            // setClassLoader(Thread.currentThread().getContextClassLoader());
        }
        super.initClassLoader(forceContextLoader);

        if (log.isDebugEnabled()) {
            log.debug("classloader for " + getContextPath() + ": " + getClassLoader());
        }
    }

    public void handle(String pathInContext,
                       String pathParams,
                       HttpRequest httpRequest,
                       HttpResponse httpResponse)
            throws HttpException, IOException {

        // save previous state
        ReadOnlyContext oldComponentContext = RootContext.getComponentContext();

        InstanceContext oldInstanceContext = null;

        try {
            // set up java:comp JNDI Context
            RootContext.setComponentContext(componentContext);

            // Turn on the UserTransaction
            userTransaction.setOnline(true);

            if (transactionContextManager.getContext() == null) {
                transactionContextManager.newUnspecifiedTransactionContext();
            }
            try {
                oldInstanceContext = associator.enter(new DefaultInstanceContext(unshareableResources, applicationManagedSecurityResources));
            } catch (ResourceException e) {
                throw new RuntimeException(e);
            }

            super.handle(pathInContext, pathParams, httpRequest, httpResponse);
        } finally {
            try {
                associator.exit(oldInstanceContext);
            } catch (ResourceException e) {
                throw new RuntimeException(e);
            } finally {
                userTransaction.setOnline(false);
                RootContext.setComponentContext(oldComponentContext);
            }
            //TODO should we reset the transactioncontext to null if we set it?
        }
    }

    public void doStart() throws WaitingException, Exception {

        if (uri.isAbsolute()) {
            setWAR(uri.toString());
        } else {
            setWAR(new URL(config.getBaseURL(), uri.toString()).toString());
        }
        if (userTransaction != null) {
            userTransaction.setUp(transactionContextManager, associator);
            userTransaction.setOnline(true);
        }
        container.addContext(this);

        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            super.start();
        } finally {
            Thread.currentThread().setContextClassLoader(oldCL);
        }

        log.info("JettyWebAppContext started");
    }

    public void doStop() throws WaitingException, Exception {

        while (true) {
            try {
                super.stop();
                break;
            } catch (InterruptedException e) {
                continue;
            }
        }
        container.removeContext(this);
        if (userTransaction != null) {
            userTransaction.setOnline(false);
        }

        log.info("JettyWebAppContext stopped");
    }

    public void doFail() {

        try {
            super.stop();
        } catch (InterruptedException e) {
        }
        container.removeContext(this);

        log.info("JettyWebAppContext failed");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory("Jetty WebApplication Context", JettyWebAppContext.class);

        infoFactory.addAttribute("uri", URI.class, true);
        infoFactory.addAttribute("contextPath", String.class, true);
        infoFactory.addAttribute("contextPriorityClassLoader", Boolean.TYPE, true);
        infoFactory.addAttribute("componentContext", ReadOnlyContext.class, true);
        infoFactory.addAttribute("unshareableResources", Set.class, true);
        infoFactory.addAttribute("applicationManagedSecurityResources", Set.class, true);
        infoFactory.addAttribute("userTransaction", UserTransactionImpl.class, true);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);

        infoFactory.addReference("Configuration", ConfigurationParent.class);
        infoFactory.addReference("JettyContainer", JettyContainer.class);
        infoFactory.addReference("TransactionContextManager", TransactionContextManager.class);
        infoFactory.addReference("TrackedConnectionAssociator", TrackedConnectionAssociator.class);

        infoFactory.setConstructor(new String[]{
            "uri",
            "componentContext",
            "userTransaction",
            "classLoader",
            "unshareableResources",
            "applicationManagedSecurityResources",
            "TransactionContextManager",
            "TrackedConnectionAssociator",
            "Configuration",
            "JettyContainer",
        });

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
