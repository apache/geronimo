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
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.deployment.DeploymentModule;
import org.apache.geronimo.deployment.ModuleFactory;
import org.apache.geronimo.deployment.model.geronimo.web.GeronimoWebAppDocument;
import org.apache.geronimo.deployment.service.GBeanDefault;
import org.apache.geronimo.deployment.util.DeploymentHelper;
import org.apache.geronimo.deployment.xml.ParserFactory;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GEndpointInfo;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.kernel.deployment.DeploymentException;
import org.apache.geronimo.kernel.deployment.scanner.URLInfo;
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
 * @version $Revision: 1.3 $ $Date: 2004/01/19 06:38:23 $
 *
 * */
public class WebDeployer implements ModuleFactory {

    private static final Log log = LogFactory.getLog(WebDeployer.class);

    private final static GBeanInfo GBEAN_INFO;

    private final ParserFactory parserFactory;

    private final String type;//"Jetty"
    private ObjectName transactionManagerNamePattern;
    private ObjectName trackedConnectionAssociatorNamePattern;
    private final String webApplicationClass;
    private boolean java2ClassLoadingCompliance;


    public WebDeployer(ParserFactory parserFactory, String type, String webApplicationClass, boolean java2ClassLoadingCompliance, ObjectName transactionManagerNamePattern, ObjectName trackedConnectionAssociatorNamePattern) {
        this.parserFactory = parserFactory;
        this.type = type;
        this.webApplicationClass = webApplicationClass;
        this.java2ClassLoadingCompliance = java2ClassLoadingCompliance;
        this.transactionManagerNamePattern = transactionManagerNamePattern;
        this.trackedConnectionAssociatorNamePattern = trackedConnectionAssociatorNamePattern;
    }

    public ObjectName getTransactionManagerNamePattern() {
        return transactionManagerNamePattern;
    }

    public void setTransactionManagerNamePattern(ObjectName transactionManagerNamePattern) {
        this.transactionManagerNamePattern = transactionManagerNamePattern;
    }

    public ObjectName getTrackedConnectionAssociatorNamePattern() {
        return trackedConnectionAssociatorNamePattern;
    }

    public void setTrackedConnectionAssociatorNamePattern(ObjectName trackedConnectionAssociatorNamePattern) {
        this.trackedConnectionAssociatorNamePattern = trackedConnectionAssociatorNamePattern;
    }

    public String getType() {
        return type;
    }

    public String getWebApplicationClass() {
        return webApplicationClass;
    }

    public boolean isJava2ClassLoadingCompliance() {
        return java2ClassLoadingCompliance;
    }

    public void setJava2ClassLoadingCompliance(boolean java2ClassLoadingCompliance) {
        this.java2ClassLoadingCompliance = java2ClassLoadingCompliance;
    }

    //endpoint
    public ParserFactory getParserFactory() {
        return parserFactory;
    }

    public DeploymentModule getModule(URLInfo urlInfo, URI moduleID) throws DeploymentException {

        DeploymentHelper deploymentHelper = new DeploymentHelper(urlInfo, "web.xml", "geronimo-web.xml", "WEB-INF");
        //we require the Geronimo dd.
        if (deploymentHelper.locateGeronimoDD() == null || deploymentHelper.locateJ2EEDD() == null) {
            return null;
        }
        DocumentBuilder parser = null;
        try {
            parser = parserFactory.getParser();
        } catch (ParserConfigurationException e) {
            throw new DeploymentException("Could not configure parser", e);
        }
        Document webAppDoc = deploymentHelper.getJ2EEDoc(parser);
        if (webAppDoc == null) {
            return null;
        }
        GeronimoWebAppDocument geronimoWebAppDoc = null;
        Document geronimoWebAppDocument = deploymentHelper.getGeronimoDoc(parser);
        if (geronimoWebAppDocument == null) {
            log.info("No Geronimo dd found, no local jndi context will be available");
        } else {
            geronimoWebAppDoc = GeronimoWebAppLoader.load(geronimoWebAppDocument);
        }

        return new WebModule(moduleID, urlInfo, webAppDoc, geronimoWebAppDoc, this);
    }


    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(WebDeployer.class.getName());
        infoFactory.addAttribute(new GAttributeInfo("Type", true));
        infoFactory.addAttribute(new GAttributeInfo("WebApplicationClass", true));
        infoFactory.addAttribute(new GAttributeInfo("Java2ClassLoadingCompliance", true));
        infoFactory.addAttribute(new GAttributeInfo("TransactionManagerNamePattern", true));
        infoFactory.addAttribute(new GAttributeInfo("TrackedConnectionAssociatorNamePattern", true));
        infoFactory.addOperation(new GOperationInfo("getModule", new String[]{URLInfo.class.getName(), URI.class.getName()}));
        infoFactory.addEndpoint(new GEndpointInfo("ParserFactory", ParserFactory.class.getName()));
        infoFactory.setConstructor(new GConstructorInfo(new String[]{"ParserFactory", "Type", "WebApplicationClass", "Java2ClassLoadingCompliance", "TransactionManagerNamePattern", "TrackedConnectionAssociatorNamePattern"},
                new Class[]{ParserFactory.class, String.class, String.class, Boolean.TYPE, ObjectName.class, ObjectName.class}));
        GBEAN_INFO = infoFactory.getBeanInfo();

    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
