/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.jetty;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.resource.ResourceException;
import javax.security.jacc.PolicyContext;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.connector.outbound.connectiontracking.defaultimpl.DefaultComponentContext;
import org.apache.geronimo.connector.outbound.connectiontracking.defaultimpl.DefaultTransactionContext;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.kernel.config.ConfigurationParent;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.naming.java.RootContext;
import org.apache.geronimo.transaction.TransactionContext;
import org.apache.geronimo.transaction.InstanceContext;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.jetty.servlet.WebApplicationContext;

/**
 * Wrapper for a WebApplicationContext that sets up its J2EE environment.
 *
 * @version $Revision: 1.8 $ $Date: 2004/02/05 01:37:56 $
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

    // @todo this should be replaced by global tx context handling.
    private final Map transactionContextMap = Collections.synchronizedMap(new WeakHashMap());

    // @todo get these from DD
    private final Set unshareableResources = Collections.EMPTY_SET;

    private boolean contextPriorityClassLoader=false;

    public JettyWebApplicationContext(
            ConfigurationParent config,
            URI uri,
            JettyContainer container,
            ReadOnlyContext compContext,
            String policyContextID,
            TransactionManager txManager,
            TrackedConnectionAssociator associator) {
        super();
        this.config = config;
        this.uri = uri;
        this.container = container;
        this.componentContext = compContext;
        this.policyContextID = policyContextID;
        this.txManager = txManager;
        this.associator = associator;
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
        contextPriorityClassLoader= b;
    }
    
    /**
     * init the classloader. Uses the value of contextPriorityClassLoader to 
     * determine if the context needs to create its own classloader.
     */
    protected void initClassLoader(boolean forceContextLoader) 
        throws MalformedURLException, IOException
    {
        setClassLoaderJava2Compliant(!contextPriorityClassLoader);
        if (!contextPriorityClassLoader)
        {
            // TODO - once geronimo is correctly setting up the classpath, this should be uncommented.
            // At the moment, the g classloader does not appear to know about the WEB-INF classes and lib.
            // setClassLoader(Thread.currentThread().getContextClassLoader());
        }
        super.initClassLoader(forceContextLoader);
        
        if (log.isDebugEnabled())
            log.debug("classloader for "+getContextPath()+": "+getClassLoader());
    }
    
    public void handle(String pathInContext,
                       String pathParams,
                       HttpRequest httpRequest,
                       HttpResponse httpResponse)
            throws HttpException, IOException {


        // save previous state
        ReadOnlyContext oldComponentContext = RootContext.getComponentContext();
        String oldPolicyContextID = PolicyContext.getContextID();
        Set oldUnshareableResources = null;
        InstanceContext oldInstanceContext = null;
        TransactionContext oldTransactionContext = null;

        try {
            // set up java:comp JNDI Context
            RootContext.setComponentContext(componentContext);

            // set up Security Context
            PolicyContext.setContextID(policyContextID);

            // set up Transaction Context
            if (txManager != null) {
                TransactionContext newTxContext;

                // @todo this will not clean up properly if an exception occurs - we need to fix this API
                try {
                    Transaction tx = txManager.getTransaction();
                    if (tx == null) {
                        newTxContext = new DefaultTransactionContext(null);
                    } else {
                        newTxContext = (TransactionContext) transactionContextMap.get(tx);
                        if (newTxContext == null) {
                            newTxContext = new DefaultTransactionContext(tx);
                            transactionContextMap.put(tx, newTxContext);
                        }
                    }
                    oldUnshareableResources = associator.setUnshareableResources(unshareableResources);
                    oldInstanceContext = associator.enter(new DefaultComponentContext());
                    oldTransactionContext = associator.setTransactionContext(newTxContext);
                } catch (SystemException e) {
                    throw new RuntimeException(e);
                } catch (RollbackException e) {
                    throw new RuntimeException(e);
                } catch (ResourceException e) {
                    throw new RuntimeException(e);
                }
            }

            super.handle(pathInContext, pathParams, httpRequest, httpResponse);
        } finally {
            try {
                if (txManager != null) {
                    associator.exit(oldInstanceContext, unshareableResources);
                    associator.resetTransactionContext(oldTransactionContext);
                    associator.setUnshareableResources(oldUnshareableResources);
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
        infoFactory.addAttribute(new GAttributeInfo("URI", true));
        infoFactory.addAttribute(new GAttributeInfo("ContextPath", true));
        infoFactory.addAttribute(new GAttributeInfo("ContextPriorityClassLoader", true));
        infoFactory.addAttribute(new GAttributeInfo("ComponentContext", true));
        infoFactory.addAttribute(new GAttributeInfo("PolicyContextID", true));
        infoFactory.addReference(new GReferenceInfo("Configuration", ConfigurationParent.class.getName()));
        infoFactory.addReference(new GReferenceInfo("JettyContainer", JettyContainer.class.getName()));
        infoFactory.addReference(new GReferenceInfo("TransactionManager", TransactionManager.class.getName()));
        infoFactory.addReference(new GReferenceInfo("TrackedConnectionAssociator", TrackedConnectionAssociator.class.getName()));
        infoFactory.setConstructor(new GConstructorInfo(
                Arrays.asList(new Object[]{"Configuration", "URI", "JettyContainer", "ComponentContext", "PolicyContextID", "TransactionManager", "TrackedConnectionAssociator"}),
                Arrays.asList(new Object[]{ConfigurationParent.class, URI.class, JettyContainer.class, ReadOnlyContext.class, String.class, TransactionManager.class, TrackedConnectionAssociator.class})));

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
