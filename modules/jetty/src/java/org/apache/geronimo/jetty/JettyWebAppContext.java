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
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.naming.java.RootContext;
import org.apache.geronimo.transaction.DefaultInstanceContext;
import org.apache.geronimo.transaction.InstanceContext;
import org.apache.geronimo.transaction.OnlineUserTransaction;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.apache.geronimo.transaction.context.InheritableTransactionContext;
import org.apache.geronimo.transaction.context.TransactionContext;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.jetty.servlet.WebApplicationContext;

/**
 * Wrapper for a WebApplicationContext that sets up its J2EE environment.
 *
 * @version $Rev$ $Date$
 */
public class JettyWebAppContext extends WebApplicationContext implements GBeanLifecycle {
    private static Log log = LogFactory.getLog(JettyWebAppContext.class);

    private final ReadOnlyContext componentContext;
    private final OnlineUserTransaction userTransaction;
    private final ClassLoader classLoader;
    private final Set unshareableResources;
    private final Set applicationManagedSecurityResources;
    private final TransactionContextManager transactionContextManager;
    private final TrackedConnectionAssociator trackedConnectionAssociator;
    private final JettyContainer jettyContainer;

    private final URI webAppRoot;

    /**
     * @deprecated never use this... this is only here because Jetty WebApplicationContext is externalizable
     */
    public JettyWebAppContext() {
        componentContext = null;
        userTransaction = null;
        classLoader = null;
        unshareableResources = null;
        applicationManagedSecurityResources = null;
        transactionContextManager = null;
        trackedConnectionAssociator = null;
        jettyContainer = null;
        webAppRoot = null;
    }

    public JettyWebAppContext(URI uri,
                              ReadOnlyContext componentContext,
                              OnlineUserTransaction userTransaction,
                              ClassLoader classLoader,
                              URI[] webClassPath,
                              boolean contextPriorityClassLoader,
                              URL configurationBaseUrl,
                              Set unshareableResources,
                              Set applicationManagedSecurityResources,
                              TransactionContextManager transactionContextManager,
                              TrackedConnectionAssociator trackedConnectionAssociator,
                              JettyContainer jettyContainer) throws MalformedURLException {

        assert uri != null;
        assert componentContext != null;
        assert userTransaction != null;
        assert classLoader != null;
        assert webClassPath != null;
        assert configurationBaseUrl != null;
        assert transactionContextManager != null;
        assert trackedConnectionAssociator != null;
        assert jettyContainer != null;

        this.componentContext = componentContext;
        this.userTransaction = userTransaction;
        this.unshareableResources = unshareableResources;
        this.applicationManagedSecurityResources = applicationManagedSecurityResources;
        this.transactionContextManager = transactionContextManager;
        this.trackedConnectionAssociator = trackedConnectionAssociator;
        this.jettyContainer = jettyContainer;

        setDefaultsDescriptor("META-INF/default-web.xml");
        setConfigurationClassNames(new String[]{"org.apache.geronimo.jetty.JettyXMLConfiguration"});

        URI root = URI.create(configurationBaseUrl.toString());
        webAppRoot = root.resolve(uri);

        URL[] urls = new URL[webClassPath.length];
        for (int i = 0; i < webClassPath.length; i++) {
            URI classPathEntry = webClassPath[i];
            classPathEntry = webAppRoot.resolve(classPathEntry);
            urls[i] = classPathEntry.toURL();
        }
        this.classLoader = new JettyClassLoader(urls, classLoader, contextPriorityClassLoader);
        setClassLoader(this.classLoader);
    }

    //TODO tx logic may not be complete.  exceptions are certainly wrong!
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


            TransactionContext oldTransactionContext = transactionContextManager.getContext();
            TransactionContext newTransactionContext = null;
            if (oldTransactionContext == null || !(oldTransactionContext instanceof InheritableTransactionContext)) {
                newTransactionContext = transactionContextManager.newUnspecifiedTransactionContext();
            }

            try {
                try {
                    oldInstanceContext = trackedConnectionAssociator.enter(new DefaultInstanceContext(unshareableResources, applicationManagedSecurityResources));
                } catch (ResourceException e) {
                    throw new RuntimeException(e);
                }

                super.handle(pathInContext, pathParams, httpRequest, httpResponse);
            } finally {
                try {
                    if (newTransactionContext != null) {
                        if (newTransactionContext != transactionContextManager.getContext()) {
                            transactionContextManager.getContext().rollback();
                            newTransactionContext.rollback();
                            throw new HttpException(500, "WRONG EXCEPTION! returned from servlet call with wrong tx context");
                        }
                        newTransactionContext.commit();

                    } else {
                        if (oldTransactionContext != transactionContextManager.getContext()) {
                            if (transactionContextManager.getContext() != null) {
                                transactionContextManager.getContext().rollback();
                            }
                            throw new HttpException(500, "WRONG EXCEPTION! returned from servlet call with wrong tx context");
                        }
                    }
                } catch (SystemException e) {
                    throw (HttpException) new HttpException(500, "WRONG EXCEPTION!").initCause(e);
                } catch (HeuristicMixedException e) {
                    throw (HttpException) new HttpException(500, "WRONG EXCEPTION!").initCause(e);
                } catch (HeuristicRollbackException e) {
                    throw (HttpException) new HttpException(500, "WRONG EXCEPTION!").initCause(e);
                } catch (RollbackException e) {
                    throw (HttpException) new HttpException(500, "WRONG EXCEPTION!").initCause(e);
                } finally {
                    //this is redundant when we enter with an inheritable context and nothing goes wrong.
                    transactionContextManager.setContext(oldTransactionContext);
                }
            }
        } finally {
            try {
                trackedConnectionAssociator.exit(oldInstanceContext);
            } catch (ResourceException e) {
                throw new RuntimeException(e);
            } finally {
                RootContext.setComponentContext(oldComponentContext);
            }
        }
    }

    public void doStart() throws WaitingException, Exception {
        // reset the classsloader... jetty likes to set it to null when stopping
        setClassLoader(classLoader);

        // merge Geronimo and Jetty Lifecycles
        if (!isStarting()) {
            super.start();
            return;
        }

        setWAR(webAppRoot.toString());

        userTransaction.setUp(transactionContextManager, trackedConnectionAssociator);

        jettyContainer.addContext(this);

        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            ReadOnlyContext oldComponentContext = RootContext.getComponentContext();
            InstanceContext oldInstanceContext = null;
            try {
                RootContext.setComponentContext(componentContext);
//TODO FIXME!!!
                TransactionContext transactionContext = transactionContextManager.getContext();
                if (transactionContext == null) {
                    transactionContext = transactionContextManager.newUnspecifiedTransactionContext();
                } else {
                    transactionContext = null;
                }

                try {

                    try {
                        oldInstanceContext = trackedConnectionAssociator.enter(new DefaultInstanceContext(unshareableResources, applicationManagedSecurityResources));
                    } catch (ResourceException e) {
                        throw new RuntimeException(e);
                    }

                    super.doStart();
                } finally {
                    if (transactionContext != null) {
                        transactionContextManager.setContext(null);
                        try {
                            transactionContext.commit();
                        } catch (Exception e) {
                            //TODO this is undoubtedly the wrong error code!
                            throw (HttpException) new HttpException(500, "Problem committing unspecified transaction context").initCause(e);
                        }
                    }
                }
            } finally {
                try {
                    trackedConnectionAssociator.exit(oldInstanceContext);
                } catch (ResourceException e) {
                    throw new RuntimeException(e);
                } finally {
                    RootContext.setComponentContext(oldComponentContext);
                }
                //TODO should we reset the transactioncontext to null if we set it?
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldCL);
        }

        log.info("JettyWebAppContext started");
    }

    public void doStop() throws Exception {
        // merge Geronimo and Jetty Lifecycles
        if (!isStopping()) {
            super.stop();
            return;
        }

        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            ReadOnlyContext oldComponentContext = RootContext.getComponentContext();
            InstanceContext oldInstanceContext = null;
            try {
                RootContext.setComponentContext(componentContext);

                TransactionContext transactionContext = transactionContextManager.getContext();
                if (transactionContext == null) {
                    transactionContext = transactionContextManager.newUnspecifiedTransactionContext();
                } else {
                    transactionContext = null;
                }
                try {

                    try {
                        oldInstanceContext = trackedConnectionAssociator.enter(new DefaultInstanceContext(unshareableResources, applicationManagedSecurityResources));
                    } catch (ResourceException e) {
                        throw new RuntimeException(e);
                    }

                    while (true) {
                        try {
                            super.doStop();
                            break;
                        } catch (InterruptedException e) {
                            continue;
                        }
                    }
                } finally {
                    if (transactionContext != null) {
                        transactionContextManager.setContext(null);
                        try {
                            transactionContext.commit();
                        } catch (Exception e) {
                            //TODO this is undoubtedly the wrong error code!
                            throw (HttpException) new HttpException(500, "Problem committing unspecified transaction context").initCause(e);
                        }
                    }
                }
            } finally {
                try {
                    trackedConnectionAssociator.exit(oldInstanceContext);
                } catch (ResourceException e) {
                    throw new RuntimeException(e);
                } finally {
                    RootContext.setComponentContext(oldComponentContext);
                }
                //TODO should we reset the transactioncontext to null if we set it?
            }
            jettyContainer.removeContext(this);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCL);
        }

        log.info("JettyWebAppContext stopped");
    }

    public void doFail() {
        try {
            super.stop();
        } catch (InterruptedException e) {
        }

        jettyContainer.removeContext(this);
        log.info("JettyWebAppContext failed");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("Jetty WebApplication Context", JettyWebAppContext.class);

        infoFactory.addAttribute("uri", URI.class, true);
        infoFactory.addAttribute("componentContext", ReadOnlyContext.class, true);
        infoFactory.addAttribute("userTransaction", OnlineUserTransaction.class, true);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addAttribute("webClassPath", URI[].class, true);
        infoFactory.addAttribute("contextPriorityClassLoader", boolean.class, true);
        infoFactory.addAttribute("configurationBaseUrl", URL.class, true);
        infoFactory.addAttribute("unshareableResources", Set.class, true);
        infoFactory.addAttribute("applicationManagedSecurityResources", Set.class, true);

        infoFactory.addAttribute("contextPath", String.class, true);

        infoFactory.addReference("TransactionContextManager", TransactionContextManager.class);
        infoFactory.addReference("TrackedConnectionAssociator", TrackedConnectionAssociator.class);
        infoFactory.addReference("JettyContainer", JettyContainer.class);

        infoFactory.setConstructor(new String[]{
            "uri",
            "componentContext",
            "userTransaction",
            "classLoader",
            "webClassPath",
            "contextPriorityClassLoader",
            "configurationBaseUrl",
            "unshareableResources",
            "applicationManagedSecurityResources",
            "TransactionContextManager",
            "TrackedConnectionAssociator",
            "JettyContainer",
        });

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
