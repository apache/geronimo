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
package org.apache.geronimo.kernel.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import org.apache.geronimo.kernel.deployment.DeploymentException;
import org.apache.geronimo.kernel.deployment.service.XMLUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Loads the GeronimoMBeanInfo from xml.
 *
 * @version $Revision: 1.4 $ $Date: 2003/11/10 20:42:01 $
 */
public class GeronimoMBeanInfoXMLLoader {
    private static final DocumentBuilder parser;

    static {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            parser = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AssertionError("No XML parser available");
        }
    }

    private GeronimoMBeanInfoXMLLoader() {
    }

    public static GeronimoMBeanInfo loadMBean(URI uri) throws DeploymentException {
        try {
            return loadMBean(uri.toURL());
        } catch (MalformedURLException e) {
            throw new DeploymentException(e);
        }
    }

    public static GeronimoMBeanInfo loadMBean(URL url) throws DeploymentException {
        InputStream is;
        try {
            is = url.openConnection().getInputStream();
        } catch (IOException e) {
            throw new DeploymentException("Failed to open stream for URL: " + url, e);
        }

        Document doc;
        try {
            doc = parser.parse(is);
        } catch (Exception e) {
            throw new DeploymentException("Failed to parse document", e);
        }

        return loadMBean(doc.getDocumentElement());
    }

    public static GeronimoMBeanInfo loadMBean(Element mbeanElement) {
        GeronimoMBeanInfo mbeanInfo = new GeronimoMBeanInfo();
        mbeanInfo.setName(mbeanElement.getAttribute("name"));
        mbeanInfo.setDescription(mbeanElement.getAttribute("description"));

        loadTargets(mbeanElement, mbeanInfo);
        loadAttributes(mbeanElement, mbeanInfo);
        loadOperations(mbeanElement, mbeanInfo);
        loadNotifications(mbeanElement, mbeanInfo);
        loadEndpoints(mbeanElement, mbeanInfo);

        return mbeanInfo;
    }

    public static void loadTargets(Element mbeanElement, GeronimoMBeanInfo mbeanInfo) {
        NodeList nl = mbeanElement.getElementsByTagName("target");
        for (int i = 0; i < nl.getLength(); i++) {
            Element targetElement = (Element) nl.item(i);
            String targetName = targetElement.getAttribute("name");
            if (targetName == null || targetName.length() == 0) {
                targetName = "default";
            }
            String targetClass = targetElement.getAttribute("class");
            mbeanInfo.setTargetClass(targetName, targetClass);
        }
    }

    public static void loadAttributes(Element mbeanElement, GeronimoMBeanInfo mbeanInfo) {
        NodeList nl;
        nl = mbeanElement.getElementsByTagName("attribute");
        for (int i = 0; i < nl.getLength(); i++) {
            Element attributeElement = (Element) nl.item(i);
            mbeanInfo.addAttributeInfo(loadAttribute(attributeElement));
        }
    }

    public static GeronimoAttributeInfo loadAttribute(Element attributeElement) {
        GeronimoAttributeInfo attributeInfo = new GeronimoAttributeInfo();

        attributeInfo.setName(attributeElement.getAttribute("name"));
        attributeInfo.setDescription(attributeElement.getAttribute("description"));

        String targetName = attributeElement.getAttribute("targetName");
        if (targetName == null || targetName.length() == 0) {
            targetName = "default";
        }
        attributeInfo.setTargetName(targetName);

        String readableString = attributeElement.getAttribute("readable");
        if (readableString == null || readableString.length() == 0) {
            attributeInfo.setReadable(true);
        } else {
            if (readableString.equalsIgnoreCase("true") || readableString.equals("1")) {
                attributeInfo.setReadable(true);
            } else {
                attributeInfo.setReadable(false);
            }
        }

        String writableString = attributeElement.getAttribute("writable");
        if (writableString == null || writableString.length() == 0) {
            attributeInfo.setWritable(true);
        } else {
            if (writableString.equalsIgnoreCase("true") || writableString.equals("1")) {
                attributeInfo.setWritable(true);
            } else {
                attributeInfo.setWritable(false);
            }
        }

        String getterName = attributeElement.getAttribute("getterName");
        if (getterName != null && getterName.length() > 0) {
            attributeInfo.setGetterName(getterName);
        }

        String setterName = attributeElement.getAttribute("setterName");
        if (setterName != null && setterName.length() > 0) {
            attributeInfo.setSetterName(setterName);
        }

        String cacheString = attributeElement.getAttribute("cache");
        if (cacheString == null || cacheString.length() == 0) {
            attributeInfo.setCacheTimeLimit(-1);
        } else {
            attributeInfo.setCachePolicy(cacheString);
        }
        return attributeInfo;
    }

    public static void loadOperations(Element mbeanElement, GeronimoMBeanInfo mbeanInfo) {
        NodeList nl;
        nl = mbeanElement.getElementsByTagName("operation");
        for (int i = 0; i < nl.getLength(); i++) {
            Element operationElement = (Element) nl.item(i);
            mbeanInfo.addOperationInfo(loadOperation(operationElement));
        }
    }

    public static GeronimoOperationInfo loadOperation(Element operationElement) {
        GeronimoOperationInfo operationInfo = new GeronimoOperationInfo();

        operationInfo.setName(operationElement.getAttribute("name"));
        operationInfo.setDescription(operationElement.getAttribute("description"));

        String targetName = operationElement.getAttribute("targetName");
        if (targetName == null || targetName.length() == 0) {
            targetName = "default";
        }
        operationInfo.setTargetName(targetName);

        String methodName = operationElement.getAttribute("methodName");
        if (methodName != null && methodName.length() > 0) {
            operationInfo.setMethodName(methodName);
        }

        String impactString = operationElement.getAttribute("impact");
        if (impactString == null || impactString.length() == 0) {
            operationInfo.setImpact(GeronimoOperationInfo.UNKNOWN);
        } else if (impactString.equals("ACTION")) {
            operationInfo.setImpact(GeronimoOperationInfo.ACTION);
        } else if (impactString.equals("INFO")) {
            operationInfo.setImpact(GeronimoOperationInfo.INFO);
        } else if (impactString.equals("ACTION_INFO")) {
            operationInfo.setImpact(GeronimoOperationInfo.ACTION_INFO);
        } else {
            operationInfo.setImpact(GeronimoOperationInfo.UNKNOWN);
        }

        String cacheString = operationElement.getAttribute("cache");
        if (cacheString == null || cacheString.length() == 0) {
            operationInfo.setCacheTimeLimit(-1);
        } else {
            operationInfo.setCachePolicy(cacheString);
        }

        loadParameters(operationElement, operationInfo);

        return operationInfo;
    }

    public static void loadParameters(Element operationElement, GeronimoOperationInfo operationInfo) {
        NodeList nl;
        nl = operationElement.getElementsByTagName("parameter");
        for (int i = 0; i < nl.getLength(); i++) {
            Element parameterElement = (Element) nl.item(i);
            operationInfo.addParameterInfo(loadParameter(parameterElement));
        }
    }

    public static GeronimoParameterInfo loadParameter(Element parameterElement) {
        GeronimoParameterInfo parameterInfo = new GeronimoParameterInfo();

        parameterInfo.setName(parameterElement.getAttribute("name"));
        parameterInfo.setDescription(parameterElement.getAttribute("description"));
        parameterInfo.setType(parameterElement.getAttribute("type"));

        return parameterInfo;
    }

    public static void loadNotifications(Element mbeanElement, GeronimoMBeanInfo mbeanInfo) {
        NodeList nl = mbeanElement.getElementsByTagName("notification");
        for (int i = 0; i < nl.getLength(); i++) {
            Element notificationElement = (Element) nl.item(i);
            mbeanInfo.addNotificationInfo(loadNotification(notificationElement));
        }
    }

    public static GeronimoNotificationInfo loadNotification(Element notificationElement) {
        GeronimoNotificationInfo notificationInfo = new GeronimoNotificationInfo();

        notificationInfo.setName(notificationElement.getAttribute("class"));
        notificationInfo.setDescription(notificationElement.getAttribute("description"));

        NodeList nl = notificationElement.getElementsByTagName("type");
        for (int i = 0; i < nl.getLength(); i++) {
            String typeString = (String) XMLUtil.getContent((Element) nl.item(i));
            notificationInfo.addNotificationType(typeString);
        }

        return notificationInfo;
    }

    public static void loadEndpoints(Element mbeanElement, GeronimoMBeanInfo mbeanInfo) {
        NodeList nl = mbeanElement.getElementsByTagName("endpoint");
        for (int i = 0; i < nl.getLength(); i++) {
            Element notificationElement = (Element) nl.item(i);
            mbeanInfo.addEndpoint(loadEndpoint(notificationElement));
        }
    }

    public static GeronimoMBeanEndpoint loadEndpoint(Element endpointElement) {
        GeronimoMBeanEndpoint endpoint = new GeronimoMBeanEndpoint();

        endpoint.setName(endpointElement.getAttribute("name"));
        endpoint.setType(endpointElement.getAttribute("type"));
        endpoint.setDescription(endpointElement.getAttribute("description"));

        String targetName = endpointElement.getAttribute("targetName");
        if (targetName == null || targetName.length() == 0) {
            targetName = "default";
        }
        endpoint.setTargetName(targetName);

        String requriedString = endpointElement.getAttribute("required");
        if (requriedString == null || requriedString.length() == 0) {
            endpoint.setRequired(false);
        } else {
            if (requriedString.equalsIgnoreCase("true") || requriedString.equals("1")) {
                endpoint.setRequired(true);
            } else {
                endpoint.setRequired(false);
            }
        }

        String setterName = endpointElement.getAttribute("setterName");
        if (setterName != null && setterName.length() > 0) {
            endpoint.setSetterName(setterName);
        }


        NodeList nl = endpointElement.getElementsByTagName("peer");
        for (int i = 0; i < nl.getLength(); i++) {
            final Element peerElement = (Element) nl.item(i);
            final String patternString = peerElement.getAttribute("pattern");
            try {
                ObjectName pattern = new ObjectName(patternString);
                endpoint.addPeer(pattern);
            } catch (MalformedObjectNameException e) {
                throw new IllegalArgumentException("Invalid object name pattern: pattern" + patternString);
            }
        }

        return endpoint;
    }
}
