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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.management.ObjectName;
import javax.transaction.UserTransaction;
import javax.xml.parsers.DocumentBuilder;

import org.apache.geronimo.deployment.DeploymentModule;
import org.apache.geronimo.deployment.ModuleFactory;
import org.apache.geronimo.deployment.model.geronimo.web.GeronimoWebAppDocument;
import org.apache.geronimo.deployment.service.GBeanDefault;
import org.apache.geronimo.deployment.util.DeploymentHelper;
import org.apache.geronimo.kernel.deployment.DeploymentException;
import org.apache.geronimo.kernel.deployment.scanner.URLInfo;
import org.apache.geronimo.kernel.jmx.JMXKernel;
import org.apache.geronimo.naming.java.ComponentContextBuilder;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.naming.java.ReferenceFactory;
import org.apache.geronimo.naming.jmx.JMXReferenceFactory;
import org.apache.geronimo.transaction.manager.UserTransactionImpl;
import org.apache.geronimo.web.AbstractWebContainer;
import org.apache.geronimo.xml.deployment.GeronimoWebAppLoader;
import org.w3c.dom.Document;

/**
 * TODO issues
 * --JMXReferenceFactory relies on operating on the current machine since it uses JMXID of server
 * --UserTransaction is bound in ReadOnlyContext which we are constructing here.  Otherwise we could
 * construct it in the AbstractWebApplication.
 * --java2classloading compliance should be loaded from GeronimoWebDoc??
 * --we are using the Jetty classloader at the moment.
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/16 23:42:54 $
 *
 * */
public class WebDeployer implements ModuleFactory {

    private final DocumentBuilder parser;

    private final String type;//"Jetty"
    private ObjectName transactionManager;
    private ObjectName trackedConnectionAssociator;
    private String webApplicationClass;


    public WebDeployer(DocumentBuilder parser, String type, String webApplicationClass, ObjectName transactionManager, ObjectName trackedConnectionAssociator) {
        this.parser = parser;
        this.type = type;
        this.webApplicationClass = webApplicationClass;
        this.transactionManager = transactionManager;
        this.trackedConnectionAssociator = trackedConnectionAssociator;
    }

    public DeploymentModule getModule(URLInfo urlInfo, URI moduleID) throws DeploymentException {

        DeploymentHelper deploymentHelper = new DeploymentHelper(urlInfo, null, "web.xml", "geronimo-web.xml", "WEB-INF");
        //we require the Geronimo dd.
        if (deploymentHelper.locateGeronimoDD() == null || deploymentHelper.locateJ2EEDD() == null) {
            return null;
        }
        Document webAppDoc = deploymentHelper.getJ2EEDoc(parser);
        if (webAppDoc == null) {
            return null;
        }
        Document geronimoWebAppDocument = deploymentHelper.getGeronimoDoc(parser);
        if (geronimoWebAppDocument == null) {
            return null;
        }
        GeronimoWebAppDocument geronimoWebAppDoc = GeronimoWebAppLoader.load(geronimoWebAppDocument);

        LinkedHashSet path = new LinkedHashSet();
        //for now we rely on the Jetty/whatever classloader.

        //I wonder what this does
        URI baseURI = URI.create(urlInfo.getUrl().toString()).normalize();

        Map values = new HashMap(8);
        values.put("URI", baseURI);
        values.put("ParentClassLoader", null);//What do we put here?
        values.put("ContextPath", getContextPath(baseURI));
        values.put("DeploymentDescriptor", webAppDoc);
        values.put("GeronimoWebAppDoc", geronimoWebAppDoc);
        values.put("Java2ClassloadingCompliance", Boolean.FALSE);
        UserTransactionImpl userTransaction = new UserTransactionImpl();
        values.put("ComponentContext", getComponentContext(geronimoWebAppDoc, userTransaction));
        values.put("UserTransaction", userTransaction);

        Map endpoints = new HashMap(2);
        endpoints.put("TransactionManager", Collections.singleton(transactionManager));
        endpoints.put("TrackedConnectionAssociator", Collections.singleton(trackedConnectionAssociator));

        List gbeanDefaults = Collections.singletonList(new GBeanDefault(webApplicationClass, getWebApplicationObjectName(baseURI), values, endpoints));
        return new WebModule(moduleID, urlInfo, new ArrayList(path), gbeanDefaults);

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

    private String getWebApplicationObjectName(URI baseURI) {
        return AbstractWebContainer.BASE_WEB_APPLICATION_NAME + AbstractWebContainer.CONTAINER_CLAUSE + type + ",module=" + ObjectName.quote(baseURI.toString());
    }

}
