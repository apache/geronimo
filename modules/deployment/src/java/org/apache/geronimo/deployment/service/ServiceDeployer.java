/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.deployment.service;

import java.beans.PropertyEditor;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilder;

import org.apache.geronimo.common.propertyeditor.PropertyEditors;
import org.apache.geronimo.deployment.DeploymentModule;
import org.apache.geronimo.deployment.ModuleFactory;
import org.apache.geronimo.deployment.util.DeploymentHelper;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.util.URLInfo;
import org.apache.geronimo.deployment.util.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 *
 * @version $Revision: 1.9 $ $Date: 2004/02/25 09:57:38 $
 */
public class ServiceDeployer implements ModuleFactory {
    private final DocumentBuilder parser;


    public ServiceDeployer(DocumentBuilder parser) {
        this.parser = parser;
    }

    public DeploymentModule getModule(URLInfo urlInfo, URI moduleID) throws DeploymentException {
        DeploymentHelper deploymentHelper = new DeploymentHelper(urlInfo, null, "geronimo-service.xml");
        Document doc = deploymentHelper.getGeronimoDoc(parser);
        if (doc == null) {
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
            String type = defaultElement.getAttribute("type");
            Object value = XMLUtil.getContent(defaultElement);
            try {
                PropertyEditor editor = PropertyEditors.findEditor(type);
                if (editor != null) {
                    editor.setAsText((String) value);
                    value = editor.getValue();
                }
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load attribute class: attribute: " + attr + ", type: " + type + " for component: " + objectName, e);
            }
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
