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

package org.apache.geronimo.connector.deployment;

import java.net.URI;
import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.DeploymentModule;
import org.apache.geronimo.deployment.ModuleFactory;
import org.apache.geronimo.deployment.model.connector.ConnectorDocument;
import org.apache.geronimo.deployment.model.geronimo.connector.GeronimoConnectorDocument;
import org.apache.geronimo.deployment.util.DeploymentHelper;
import org.apache.geronimo.deployment.util.URLInfo;
import org.apache.geronimo.deployment.xml.ParserFactory;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.xml.deployment.ConnectorLoader;
import org.apache.geronimo.xml.deployment.GeronimoConnectorLoader;

import org.w3c.dom.Document;

/**
 * @version $Revision: 1.2 $ $Date: 2004/01/25 21:07:03 $
 *
 * */
public class ConnectorDeployer implements ModuleFactory {
    private final static GBeanInfo GBEAN_INFO;

    private final ParserFactory parserFactory;

    private ObjectName connectionTrackerNamePattern;

    public ConnectorDeployer(ObjectName connectionTrackerNamePattern, ParserFactory parserFactory) {
        this.connectionTrackerNamePattern = connectionTrackerNamePattern;
        this.parserFactory = parserFactory;
    }

    public ParserFactory getParserFactory() {
        return parserFactory;
    }

    public ObjectName getConnectionTrackerNamePattern() {
        return connectionTrackerNamePattern;
    }

    public DeploymentModule getModule(URLInfo urlInfo, URI moduleID) throws DeploymentException {
        DeploymentHelper deploymentHelper = new DeploymentHelper(urlInfo, "ra.xml", "geronimo-ra.xml", "META-INF");
        //we require both the standard dd and the Geronimo dd.
        if (deploymentHelper.locateGeronimoDD() == null || deploymentHelper.locateJ2EEDD() == null) {
            return null;
        }
        DocumentBuilder parser = null;
        try {
            parser = parserFactory.getParser();
        } catch (ParserConfigurationException e) {
            throw new DeploymentException("Could not configure parser", e);
        }
        Document connectorDoc = deploymentHelper.getJ2EEDoc(parser);
        if (connectorDoc == null) {
            return null;
        }
        ConnectorDocument connectorDocument = ConnectorLoader.load(connectorDoc);

        GeronimoConnectorDocument geronimoConnectorDocument = null;
        Document geronimoConnectorDoc = deploymentHelper.getGeronimoDoc(parser);
        if (geronimoConnectorDoc == null) {
            return null;
        }
        geronimoConnectorDocument = GeronimoConnectorLoader.load(geronimoConnectorDoc, connectorDocument);

        return new ConnectorModule(moduleID, geronimoConnectorDocument, this);
    }

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(ConnectorDeployer.class.getName());
        infoFactory.addAttribute(new GAttributeInfo("ConnectionTrackerNamePattern", true));
        infoFactory.addOperation(new GOperationInfo("getModule", new String[]{URLInfo.class.getName(), URI.class.getName()}));
        infoFactory.addReference(new GReferenceInfo("ParserFactory", ParserFactory.class.getName()));
        infoFactory.setConstructor(new GConstructorInfo(
                new String[]{"ConnectionTrackerNamePattern", "ParserFactory"},
                new Class[]{ObjectName.class, ParserFactory.class}));
        GBEAN_INFO = infoFactory.getBeanInfo();

    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
