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

package org.apache.geronimo.web.deployment;

import java.net.URI;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.transaction.UserTransaction;

import org.apache.geronimo.deployment.ConfigurationCallback;
import org.apache.geronimo.deployment.DeploymentModule;
import org.apache.geronimo.deployment.model.geronimo.web.GeronimoWebAppDocument;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.deployment.DeploymentException;
import org.apache.geronimo.kernel.deployment.scanner.URLInfo;
import org.apache.geronimo.naming.java.ComponentContextBuilder;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.naming.java.ReferenceFactory;
import org.apache.geronimo.naming.jmx.JMXReferenceFactory;
import org.apache.geronimo.transaction.manager.UserTransactionImpl;
import org.apache.geronimo.web.AbstractWebContainer;
import org.w3c.dom.Document;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/01/19 06:38:23 $
 *
 * */
public class WebModule implements DeploymentModule {
    private final URI moduleID;
    private final URLInfo urlInfo;
    private Document deploymentDescriptorDoc;
    private GeronimoWebAppDocument geronimoWebAppDoc;
    private WebDeployer webDeployer;

    public WebModule(URI moduleID, URLInfo urlInfo, Document webAppDoc, GeronimoWebAppDocument geronimoWebAppDoc, WebDeployer webDeployer) {
        this.moduleID = moduleID;
        this.urlInfo = urlInfo;
        this.deploymentDescriptorDoc = webAppDoc;
        this.geronimoWebAppDoc = geronimoWebAppDoc;
        this.webDeployer = webDeployer;
    }


    public void init() throws DeploymentException {
    }

    public void generateClassPath(ConfigurationCallback callback) throws DeploymentException {
        //we're relying on the Jetty class loader right now
    }

    public void defineGBeans(ConfigurationCallback callback, ClassLoader cl) throws DeploymentException {
        GBeanInfo gbeanInfo;
        try {
            gbeanInfo = GBeanInfo.getGBeanInfo(webDeployer.getWebApplicationClass(), cl);
        } catch (InvalidConfigurationException e) {
            throw new DeploymentException("Unable to get GBeanInfo from class " + webDeployer.getWebApplicationClass(), e);
        }

        GBeanMBean gbean;
        try {
            gbean = new GBeanMBean(gbeanInfo);
        } catch (InvalidConfigurationException e) {
            throw new DeploymentException("Unable to create GMBean", e);
        }

        try {
            getClass().getClassLoader().loadClass("org.apache.jasper.servlet.JspServlet");
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Could not load jsp servlet class: urls: " + Arrays.asList(((URLClassLoader)getClass().getClassLoader()).getURLs()), e);
        }

        //I wonder what this does
        URI baseURI = URI.create(urlInfo.getUrl().toString()).normalize();

        try {
            gbean.setAttribute("URI", baseURI);
            //this needs to be an endpoint to ConfigurationParent
            gbean.setAttribute("ParentClassLoader", null);//What do we put here?
            gbean.setAttribute("ContextPath", getContextPath(baseURI));
            gbean.setAttribute("DeploymentDescriptorDoc", deploymentDescriptorDoc);
            gbean.setAttribute("GeronimoWebAppDoc", geronimoWebAppDoc);
            gbean.setAttribute("Java2ClassLoadingCompliance", Boolean.valueOf(webDeployer.isJava2ClassLoadingCompliance()));
            UserTransactionImpl userTransaction = new UserTransactionImpl();
            gbean.setAttribute("ComponentContext", getComponentContext(geronimoWebAppDoc, userTransaction));
            gbean.setAttribute("UserTransaction", userTransaction);
        } catch (Exception e) {
            throw new DeploymentException("Unable to set WebApplication attribute", e);
        }

        gbean.setEndpointPatterns("TransactionManager", Collections.singleton(webDeployer.getTransactionManagerNamePattern()));
        gbean.setEndpointPatterns("TrackedConnectionAssociator", Collections.singleton(webDeployer.getTrackedConnectionAssociatorNamePattern()));


        ObjectName name;
        try {
            name = ObjectName.getInstance(getWebApplicationObjectName());
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Invalid JMX ObjectName: " + getWebApplicationObjectName(), e);
        }

        callback.addGBean(name, gbean);
    }

    public void complete() {
    }

    private String getContextPath(URI baseURI) {
        String path = baseURI.getPath();

        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        int sepIndex = path.lastIndexOf('/');
        if (sepIndex > 0) {
            path = path.substring(sepIndex + 1);
        }

        if (path.endsWith(".war")) {
            path = path.substring(0, path.length() - 4);
        }

        return "/" + path;
    }


    private ReadOnlyContext getComponentContext(GeronimoWebAppDocument geronimoWebAppDoc, UserTransaction userTransaction) throws DeploymentException {
        if (geronimoWebAppDoc != null) {
            ReferenceFactory referenceFactory = new JMXReferenceFactory(null);//JMXKernel.getMBeanServerId(getServer()));
            ComponentContextBuilder builder = new ComponentContextBuilder(referenceFactory, userTransaction);
            ReadOnlyContext context = builder.buildContext(geronimoWebAppDoc.getWebApp());
            return context;
        } else {
            return null;
        }
    }

    private String getWebApplicationObjectName() {
        return AbstractWebContainer.BASE_WEB_APPLICATION_NAME + AbstractWebContainer.CONTAINER_CLAUSE + webDeployer.getType() + ",module=" + ObjectName.quote(moduleID.toString());
    }

}
