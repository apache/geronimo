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
package org.apache.geronimo.jetty.deployment;

import java.net.URI;
import java.util.Collections;
import java.util.Properties;
import java.util.Collection;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.IOException;

import javax.management.ObjectName;
import javax.naming.Context;
import javax.transaction.UserTransaction;

import org.apache.geronimo.deployment.ConfigurationCallback;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.DeploymentModule;
import org.apache.geronimo.deployment.util.UnclosableInputStream;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.jetty.JettyWebApplicationContext;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.naming.deployment.ComponentContextBuilder;
import org.apache.geronimo.naming.proxy.ProxyFactory;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyWebAppType;
import org.apache.geronimo.xbeans.j2ee.WebAppType;
import org.apache.geronimo.xbeans.j2ee.WebAppDocument;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlException;

/**
 *
 *
 * @version $Revision: 1.8 $ $Date: 2004/02/20 07:19:13 $
 */
public class AbstractModule implements DeploymentModule {

    private final ProxyFactory proxyFactory;
    protected final URI configID;
    protected URI uri;
    protected String contextPath;
    protected boolean contextPriorityClassLoader;
    protected WebAppType webApp;
    protected final JettyWebAppType jettyWebApp;

    public AbstractModule(URI configID, JettyWebAppType jettyWebApp, ProxyFactory proxyFactory) throws DeploymentException {
        this.configID = configID;
        this.jettyWebApp = jettyWebApp;
        this.proxyFactory = proxyFactory;
        contextPath = jettyWebApp.getContextRoot();
        if (contextPath == null) {
            throw new DeploymentException("No context root specified");
        }
        contextPriorityClassLoader=jettyWebApp.getContextPriorityClassloader();
    }

    public void init() throws DeploymentException {
    }

    public void generateClassPath(ConfigurationCallback callback) throws DeploymentException {
    }

    public void defineGBeans(ConfigurationCallback callback, ClassLoader cl) throws DeploymentException {
        try {
            // @todo tie name to configuration
            Properties nameProps = new Properties();
            nameProps.put("J2EEServer", "null");
            nameProps.put("J2EEApplication", "null");
            nameProps.put("J2EEType", "WebModule");
            nameProps.put("Path", contextPath);
            ObjectName name = new ObjectName("geronimo.jetty", nameProps);

            GBeanMBean app = new GBeanMBean(JettyWebApplicationContext.GBEAN_INFO);
            app.setAttribute("URI", uri);
            app.setAttribute("ContextPath", contextPath);
            app.setAttribute("ContextPriorityClassLoader", new Boolean(contextPriorityClassLoader));
            app.setAttribute("PolicyContextID", null);
            //jndi
/*
            if (proxyFactory != null) {
                UserTransaction userTransaction = null;
                Context componentContext = new ComponentContextBuilder(proxyFactory, cl).buildContext(
                        webApp.getEjbRefArray(), jettyWebApp.getEjbRefArray(),
                        webApp.getEjbLocalRefArray(), jettyWebApp.getEjbLocalRefArray(),
                        webApp.getEnvEntryArray(),
                        webApp.getMessageDestinationRefArray(), jettyWebApp.getMessageDestinationRefArray(),
                        webApp.getResourceEnvRefArray(), jettyWebApp.getResourceEnvRefArray(),
                        webApp.getResourceRefArray(), jettyWebApp.getResourceRefArray(),
                        userTransaction);
                app.setAttribute("ComponentContext", componentContext);
            }
*/

            app.setReferencePatterns("Configuration", Collections.singleton(Kernel.getConfigObjectName(configID)));
            app.setReferencePatterns("JettyContainer", Collections.singleton(new ObjectName("geronimo.web:type=WebContainer,container=Jetty"))); // @todo configurable
            app.setReferencePatterns("TransactionManager", Collections.EMPTY_SET);
            app.setReferencePatterns("TrackedConnectionAssociator", Collections.EMPTY_SET);
            callback.addGBean(name, app);

        } catch (Throwable e) {
            throw new DeploymentException("Unable to build GBean for web application", e);
        }
    }

    public void complete() {
    }

    protected void extractAndValidateDeploymentDescriptor(InputStream is) throws DeploymentException {
        try {
            WebAppDocument webAppDoc = WebAppDocument.Factory.parse(new UnclosableInputStream(is));
            //validate
            XmlOptions xmlOptions = new XmlOptions();
            xmlOptions.setLoadLineNumbers();
            Collection errors = new ArrayList();
            xmlOptions.setErrorListener(errors);
            if (!webAppDoc.validate(xmlOptions)) {
                throw new DeploymentException("Invalid deployment descriptor: errors: " + errors);
            }
            webApp = webAppDoc.getWebApp();
        } catch (IOException e) {
            throw new DeploymentException("Unable to read deployment descriptor", e);
        } catch (XmlException e) {
            throw new DeploymentException("Unable to parse deployment descriptor", e);
        }
    }
}
