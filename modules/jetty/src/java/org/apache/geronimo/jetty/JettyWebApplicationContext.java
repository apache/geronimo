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
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import javax.resource.ResourceException;
import javax.security.jacc.PolicyContext;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.connector.outbound.connectiontracking.defaultimpl.DefaultComponentContext;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.kernel.config.ConfigurationParent;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.naming.java.RootContext;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.apache.geronimo.transaction.TransactionContext;
import org.apache.geronimo.transaction.UnspecifiedTransactionContext;
import org.apache.geronimo.transaction.UserTransactionImpl;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.jetty.servlet.WebApplicationContext;

/**
 * Wrapper for a WebApplicationContext that sets up its J2EE environment.
 *
 * @version $Revision: 1.12 $ $Date: 2004/04/06 00:21:21 $
 */
public class JettyWebApplicationContext extends WebApplicationContext implements GBean {

    private static Log log = LogFactory.getLog(JettyWebApplicationContext.class);

    private final ConfigurationParent config;
    private final URI uri;
    private final JettyContainer container;
    private final ReadOnlyContext componentContext;
    private final String policyContextID;
    private final TransactionManager txManager;
    private final TrackedConnectionAssociator associator;
    private final UserTransactionImpl userTransaction;

    // @todo get these from DD
    private final Set unshareableResources = Collections.EMPTY_SET;

    private boolean contextPriorityClassLoader = false;

    public JettyWebApplicationContext(
            ConfigurationParent config,
            URI uri,
            JettyContainer container,
            ReadOnlyContext compContext,
            String policyContextID,
            TransactionManager txManager,
            TrackedConnectionAssociator associator,
            UserTransactionImpl userTransaction) {
        super();
        this.config = config;
        this.uri = uri;
        this.container = container;
        this.componentContext = compContext;
        this.policyContextID = policyContextID;
        this.txManager = txManager;
        this.associator = associator;
        this.userTransaction = userTransaction;
        userTransaction.setUp(txManager, associator);
    }


    /** getContextPriorityClassLoader.
     * @return True if this context should give web application class in preference over the containers
     * classes, as per the servlet specification recommendations.
     */
    public boolean getContextPriorityClassLoader() {
        return contextPriorityClassLoader;
    }

    /** setContextPriorityClassLoader.
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

        if (log.isDebugEnabled())
            log.debug("classloader for " + getContextPath() + ": " + getClassLoader());
    }

    public void handle(String pathInContext,
                       String pathParams,
                       HttpRequest httpRequest,
                       HttpResponse httpResponse)
            throws HttpException, IOException {


        // save previous state
        ReadOnlyContext oldComponentContext = RootContext.getComponentContext();
        String oldPolicyContextID = PolicyContext.getContextID();

        TrackedConnectionAssociator.ConnectorContextInfo oldConnectorContext = null;

        try {
            // set up java:comp JNDI Context
            RootContext.setComponentContext(componentContext);

            // set up Security Context
            PolicyContext.setContextID(policyContextID);

            if (TransactionContext.getContext() == null) {
                TransactionContext.setContext(new UnspecifiedTransactionContext());
            }
            try {
                oldConnectorContext = associator.enter(new DefaultComponentContext(), unshareableResources);
            } catch (ResourceException e) {
                throw new RuntimeException(e);
            }

            super.handle(pathInContext, pathParams, httpRequest, httpResponse);
        } finally {
            try {
                if (txManager != null) {
                    associator.exit(oldConnectorContext);
                }
            } catch (ResourceException e) {
                throw new RuntimeException(e);
            } finally {
                PolicyContext.setContextID(oldPolicyContextID);
                RootContext.setComponentContext(oldComponentContext);
            }
        }
    }

    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() throws WaitingException, Exception {
        if (uri.isAbsolute()) {
            setWAR(uri.toString());
        } else {
            setWAR(new URL(config.getBaseURL(), uri.toString()).toString());
        }
        if (userTransaction != null) {
            userTransaction.setOnline(true);
        }
        container.addContext(this);
        super.start();
    }

    public void doStop() throws WaitingException {
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
    }

    public void doFail() {
        try {
            super.stop();
        } catch (InterruptedException e) {
        }
        container.removeContext(this);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {

        GBeanInfoFactory infoFactory = new GBeanInfoFactory("Jetty WebApplication Context", JettyWebApplicationContext.class.getName());
        infoFactory.addAttribute("URI", true);
        infoFactory.addAttribute("ContextPath", true);
        infoFactory.addAttribute("ContextPriorityClassLoader", true);
        infoFactory.addAttribute("ComponentContext", true);
        infoFactory.addAttribute("PolicyContextID", true);
        infoFactory.addAttribute("UserTransaction", true);
        infoFactory.addReference("Configuration", ConfigurationParent.class);
        infoFactory.addReference("JettyContainer", JettyContainer.class);
        infoFactory.addReference("TransactionManager", TransactionManager.class);
        infoFactory.addReference("TrackedConnectionAssociator", TrackedConnectionAssociator.class);
        infoFactory.setConstructor(new GConstructorInfo(
                Arrays.asList(new Object[]{"Configuration", "URI", "JettyContainer", "ComponentContext", "PolicyContextID", "TransactionManager", "TrackedConnectionAssociator", "UserTransaction"}),
                Arrays.asList(new Object[]{ConfigurationParent.class, URI.class, JettyContainer.class, ReadOnlyContext.class, String.class, TransactionManager.class, TrackedConnectionAssociator.class, UserTransactionImpl.class})));

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
