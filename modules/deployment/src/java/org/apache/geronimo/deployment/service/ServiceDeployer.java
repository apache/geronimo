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
package org.apache.geronimo.deployment.service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import org.apache.geronimo.deployment.ModuleFactory;
import org.apache.geronimo.deployment.DeploymentModule;
import org.apache.geronimo.kernel.deployment.DeploymentException;
import org.apache.geronimo.kernel.deployment.scanner.URLInfo;
import org.apache.geronimo.kernel.deployment.scanner.URLType;
import org.apache.geronimo.kernel.deployment.service.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/01/16 22:19:51 $
 */
public class ServiceDeployer implements ModuleFactory {
    private final DocumentBuilder parser;


    public ServiceDeployer(DocumentBuilder parser) {
        this.parser = parser;
    }

    public DeploymentModule getModule(URLInfo urlInfo, URI moduleID) throws DeploymentException {
        URL baseURL = urlInfo.getUrl();
        URL metaDataURL;
        try {
            if (urlInfo.getType() == URLType.RESOURCE) {
                metaDataURL = baseURL;
            } else if (urlInfo.getType() == URLType.PACKED_ARCHIVE) {
                baseURL = new URL("jar:" + baseURL.toString() + "!/");
                metaDataURL = new URL(baseURL, "META-INF/geronimo-service.xml");
            } else if (urlInfo.getType() == URLType.UNPACKED_ARCHIVE) {
                metaDataURL = new URL(baseURL, "META-INF/geronimo-service.xml");
            } else {
                return null;
            }
        } catch (MalformedURLException e) {
            return null;
        }

        Document doc = null;
        try {
            doc = parser.parse(metaDataURL.openStream());
        } catch (Exception e) {
            // this is not an XML file we can parse - let someone else try
            return null;
        }
        Element documentElement = doc.getDocumentElement();
        if ("gbeans".equals(documentElement.getNodeName()) == false) {
            // document does not have correct root element - ignore
            return null;
        }

        LinkedHashSet path = new LinkedHashSet();
        List gbeanDefaults = new ArrayList();
        NodeList nl = documentElement.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element element = (Element) node;
            if ("path".equals(element.getNodeName())) {
                String uri = (String) XMLUtil.getContent(element);
                try {
                    path.add(new URI(uri));
                } catch (URISyntaxException e) {
                    throw new DeploymentException("Invalid path URI: "+uri, e);
                }
            } else if ("gbean".equals(element.getNodeName())) {
                gbeanDefaults.add(loadDefault(element));
            }
        }
        return new ServiceModule(moduleID, urlInfo, new ArrayList(path), gbeanDefaults);
    }

    private GBeanDefault loadDefault(Element gbeanElement) throws DeploymentException {
        String className = gbeanElement.getAttribute("class");
        String objectName = gbeanElement.getAttribute("objectName");
        NodeList nl = gbeanElement.getElementsByTagName("default");
        Map values = new HashMap(nl.getLength());
        for (int i = 0; i < nl.getLength(); i++) {
            Element defaultElement = (Element) nl.item(i);
            String attr = defaultElement.getAttribute("attribute");
            String value = (String) XMLUtil.getContent(defaultElement);
            values.put(attr, value);
        }
        NodeList endpointList = gbeanElement.getElementsByTagName("endpoint");
        Map endpoints = new HashMap(endpointList.getLength());
        for (int i = 0; i < endpointList.getLength(); i++) {
            Element endpointElement = (Element)endpointList.item(i);
            String endpointName = endpointElement.getAttribute("name");
            NodeList patternList = endpointElement.getElementsByTagName("pattern");
            Set patterns = new HashSet(patternList.getLength());
            for (int j = 0; j < patternList.getLength(); j++) {
                Element patternElement = (Element) patternList.item(j);
                ObjectName pattern = null;
                try {
                    pattern = ObjectName.getInstance((String) XMLUtil.getContent(patternElement));
                } catch (MalformedObjectNameException e) {
                    throw new DeploymentException("Invalid pattern for endpoint named: " + endpointName, e);
                }
                patterns.add(pattern);
            }
            endpoints.put(endpointName, patterns);
        }
        return new GBeanDefault(className, objectName, values, endpoints);
    }
}
