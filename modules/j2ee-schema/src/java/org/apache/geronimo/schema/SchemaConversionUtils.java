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

package org.apache.geronimo.schema;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.net.URL;
import javax.xml.namespace.QName;

import org.apache.geronimo.xbeans.j2ee.ApplicationDocument;
import org.apache.geronimo.xbeans.j2ee.EjbJarDocument;
import org.apache.geronimo.xbeans.j2ee.WebAppDocument;
import org.apache.geronimo.xbeans.j2ee.ApplicationClientDocument;
import org.apache.geronimo.xbeans.j2ee.ConnectorDocument;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlDocumentProperties;
import org.w3c.dom.Element;

/**
 * @version $Rev$ $Date$
 */
public class SchemaConversionUtils {
    static final String J2EE_NAMESPACE = "http://java.sun.com/xml/ns/j2ee";

    static final String GERONIMO_NAMING_NAMESPACE = "http://geronimo.apache.org/xml/ns/naming";
    private static final String GERONIMO_SECURITY_NAMESPACE = "http://geronimo.apache.org/xml/ns/security";
    private static final String GERONIMO_SERVICE_NAMESPACE = "http://geronimo.apache.org/xml/ns/deployment";

    private static final QName RESOURCE_ADAPTER_VERSION = new QName(J2EE_NAMESPACE, "resourceadapter-version");
    private static final QName TAGLIB = new QName(J2EE_NAMESPACE, "taglib");
    private static final QName CMP_VERSION = new QName(J2EE_NAMESPACE, "cmp-version");

    private SchemaConversionUtils() {
    }

    public static XmlObject parse(URL url) throws IOException, XmlException {
        ArrayList errors = new ArrayList();
        XmlObject parsed = XmlObject.Factory.parse(url, createXmlOptions(errors));
        if (errors.size() != 0) {
            throw new XmlException(errors.toArray().toString());
        }
        return parsed;
    }

    public static XmlObject parse(InputStream is) throws IOException, XmlException {
        ArrayList errors = new ArrayList();
        XmlObject parsed = XmlObject.Factory.parse(is, createXmlOptions(errors));
        if (errors.size() != 0) {
            throw new XmlException(errors.toArray().toString());
        }
        return parsed;
    }

    public static XmlObject parse(String xml) throws XmlException {
        ArrayList errors = new ArrayList();
        XmlObject parsed = XmlObject.Factory.parse(xml, createXmlOptions(errors));
        if (errors.size() != 0) {
            throw new XmlException(errors.toArray().toString());
        }
        return parsed;
    }

    public static XmlObject parse(Element element) throws XmlException {
        ArrayList errors = new ArrayList();
        XmlObject parsed = XmlObject.Factory.parse(element, createXmlOptions(errors));
        if (errors.size() != 0) {
            throw new XmlException(errors.toArray().toString());
        }
        return parsed;
    }

    private static XmlOptions createXmlOptions(ArrayList errors) {
        XmlOptions options = new XmlOptions();
        options.setLoadLineNumbers();
        options.setErrorListener(errors);
        return options;
    }

    public static ApplicationDocument convertToApplicationSchema(XmlObject xmlObject) throws XmlException {
        if (ApplicationDocument.type.equals(xmlObject.schemaType())) {
            validateDD(xmlObject);
            return (ApplicationDocument) xmlObject;
        }
        XmlCursor cursor = xmlObject.newCursor();
        XmlCursor moveable = xmlObject.newCursor();
        String schemaLocationURL = "http://java.sun.com/xml/ns/j2ee/application_1_4.xsd";
        String version = "1.4";
        try {
            convertToSchema(cursor, J2EE_NAMESPACE, schemaLocationURL, version);
            cursor.toStartDoc();
            cursor.toChild(J2EE_NAMESPACE, "application");
            cursor.toFirstChild();
            convertToDescriptionGroup(cursor, moveable);
        } finally {
            cursor.dispose();
            moveable.dispose();
        }
        XmlObject result = xmlObject.changeType(ApplicationDocument.type);
        if (result != null) {
            validateDD(result);
            return (ApplicationDocument) result;
        }
        validateDD(xmlObject);
        return (ApplicationDocument) xmlObject;

    }

    public static ApplicationClientDocument convertToApplicationClientSchema(XmlObject xmlObject) throws XmlException {
        if (ApplicationClientDocument.type.equals(xmlObject.schemaType())) {
            validateDD(xmlObject);
            return (ApplicationClientDocument) xmlObject;
        }
        XmlCursor cursor = xmlObject.newCursor();
        XmlCursor moveable = xmlObject.newCursor();
        String schemaLocationURL = "http://java.sun.com/xml/ns/j2ee/application-client_1_4.xsd";
        String version = "1.4";
        try {
            convertToSchema(cursor, J2EE_NAMESPACE, schemaLocationURL, version);
            cursor.toStartDoc();
            cursor.toChild(J2EE_NAMESPACE, "application-client");
            cursor.toFirstChild();
            convertToDescriptionGroup(cursor, moveable);
        } finally {
            cursor.dispose();
            moveable.dispose();
        }
        XmlObject result = xmlObject.changeType(ApplicationClientDocument.type);
        if (result != null) {
            validateDD(result);
            return (ApplicationClientDocument) result;
        }
        validateDD(xmlObject);
        return (ApplicationClientDocument) xmlObject;

    }

    public static ConnectorDocument convertToConnectorSchema(XmlObject xmlObject) throws XmlException {
        if (ConnectorDocument.type.equals(xmlObject.schemaType())) {
            validateDD(xmlObject);
            return (ConnectorDocument) xmlObject;
        }
        XmlCursor cursor = xmlObject.newCursor();
        XmlDocumentProperties xmlDocumentProperties = cursor.documentProperties();
        String publicId = xmlDocumentProperties.getDoctypePublicId();
        try {
            if ("-//Sun Microsystems, Inc.//DTD Connector 1.0//EN".equals(publicId)) {
                XmlCursor moveable = xmlObject.newCursor();
                try {
                    String schemaLocationURL = "http://java.sun.com/xml/ns/j2ee/connector_1_5.xsd";
                    String version = "1.5";
                    convertToSchema(cursor, J2EE_NAMESPACE, schemaLocationURL, version);
                    cursor.toStartDoc();
                    cursor.toChild(J2EE_NAMESPACE, "connector");
                    cursor.toFirstChild();
                    convertToDescriptionGroup(cursor, moveable);
                    cursor.toNextSibling(J2EE_NAMESPACE, "spec-version");
                    cursor.removeXml();
                    cursor.toNextSibling(J2EE_NAMESPACE, "version");
                    cursor.setName(RESOURCE_ADAPTER_VERSION);
                    cursor.toNextSibling(J2EE_NAMESPACE, "resourceadapter");
                    moveable.toCursor(cursor);
                    cursor.toFirstChild();
                    cursor.beginElement("outbound-resourceadapter", J2EE_NAMESPACE);
                    cursor.beginElement("connection-definition", J2EE_NAMESPACE);
                    moveable.toChild(J2EE_NAMESPACE, "managedconnectionfactory-class");
                    moveable.push();
                    //from moveable to cursor
                    moveable.moveXml(cursor);
                    while (moveable.toNextSibling(J2EE_NAMESPACE, "config-property")) {
                        moveable.moveXml(cursor);
                    }
                    moveable.pop();
                    moveable.toNextSibling(J2EE_NAMESPACE, "connectionfactory-interface");
                    moveable.moveXml(cursor);
                    moveable.toNextSibling(J2EE_NAMESPACE, "connectionfactory-impl-class");
                    moveable.moveXml(cursor);
                    moveable.toNextSibling(J2EE_NAMESPACE, "connection-interface");
                    moveable.moveXml(cursor);
                    moveable.toNextSibling(J2EE_NAMESPACE, "connection-impl-class");
                    moveable.moveXml(cursor);
                    //get out of connection-definition element
                    cursor.toNextToken();
                    moveable.toNextSibling(J2EE_NAMESPACE, "transaction-support");
                    moveable.moveXml(cursor);
                    while (moveable.toNextSibling(J2EE_NAMESPACE, "authentication-mechanism")) {
                        moveable.moveXml(cursor);
                    }
                    moveable.toNextSibling(J2EE_NAMESPACE, "reauthentication-support");
                    moveable.moveXml(cursor);
                } finally {
                    moveable.dispose();
                }

            }
        } finally {
            cursor.dispose();
        }
        XmlObject result = xmlObject.changeType(ConnectorDocument.type);
        if (result != null) {
            validateDD(result);
            return (ConnectorDocument) result;
        }
        validateDD(xmlObject);
        return (ConnectorDocument) xmlObject;

    }

    public static EjbJarDocument convertToEJBSchema(XmlObject xmlObject) throws XmlException {
        if (EjbJarDocument.type.equals(xmlObject.schemaType())) {
            validateDD(xmlObject);
            return (EjbJarDocument) xmlObject;
        }
        XmlCursor cursor = xmlObject.newCursor();
        XmlCursor moveable = xmlObject.newCursor();
        try {
            cursor.toFirstChild();
            if ("http://java.sun.com/xml/ns/j2ee".equals(cursor.getName().getNamespaceURI())) {
                XmlObject result = xmlObject.changeType(EjbJarDocument.type);
                validateDD(result);
                return (EjbJarDocument) result;
            }
            XmlDocumentProperties xmlDocumentProperties = cursor.documentProperties();
            String publicId = xmlDocumentProperties.getDoctypePublicId();
            String cmpVersion;
            if ("-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 1.1//EN".equals(publicId)) {
                cmpVersion = "1.x";
            } else if ("-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 2.0//EN".equals(publicId)) {
                cmpVersion = null;//2.x is the default "2.x";
            } else {
                throw new XmlException("Unrecognized document type: " + publicId);
            }
            String schemaLocationURL = "http://java.sun.com/xml/ns/j2ee/ejb-jar_2_1.xsd";
            String version = "2.1";
            convertToSchema(cursor, J2EE_NAMESPACE, schemaLocationURL, version);
            //play with message-driven
            cursor.toStartDoc();
            convertBeans(cursor, moveable, cmpVersion);
        } finally {
            cursor.dispose();
            moveable.dispose();
        }
        XmlObject result = xmlObject.changeType(EjbJarDocument.type);
        if (result != null) {
            validateDD(result);
            return (EjbJarDocument) result;
        }
        validateDD(xmlObject);
        return (EjbJarDocument) xmlObject;
    }

    public static WebAppDocument convertToServletSchema(XmlObject xmlObject) throws XmlException {
        if (WebAppDocument.type.equals(xmlObject.schemaType())) {
            validateDD(xmlObject);
            return (WebAppDocument) xmlObject;
        }
        XmlCursor cursor = xmlObject.newCursor();
        try {
            cursor.toStartDoc();
            cursor.toFirstChild();
            if ("http://java.sun.com/xml/ns/j2ee".equals(cursor.getName().getNamespaceURI())) {
                XmlObject result = xmlObject.changeType(WebAppDocument.type);
                validateDD(result);
                return (WebAppDocument) result;
            }

            XmlDocumentProperties xmlDocumentProperties = cursor.documentProperties();
            String publicId = xmlDocumentProperties.getDoctypePublicId();
            if ("-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN".equals(publicId) ||
                    "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN".equals(publicId)) {
                XmlCursor moveable = xmlObject.newCursor();
                try {
                    moveable.toStartDoc();
                    moveable.toFirstChild();
                    String schemaLocationURL = "http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd";
                    String version = "2.4";
                    convertToSchema(cursor, J2EE_NAMESPACE, schemaLocationURL, version);
                    cursor.toStartDoc();
                    cursor.toChild(J2EE_NAMESPACE, "web-app");
                    cursor.toFirstChild();
                    convertToDescriptionGroup(cursor, moveable);
                    convertToJNDIEnvironmentRefsGroup(cursor, moveable);
                    cursor.push();
                    if (cursor.toNextSibling(TAGLIB)) {
                        cursor.toPrevSibling();
                        moveable.toCursor(cursor);
                        cursor.beginElement("jsp-config", J2EE_NAMESPACE);
                        while (moveable.toNextSibling(TAGLIB)) {
                            moveable.moveXml(cursor);
                        }
                    }
                    cursor.pop();
                    do {
                        String name = cursor.getName().getLocalPart();
                        if ("filter".equals(name) || "servlet".equals(name)) {
                            cursor.push();
                            cursor.toFirstChild();
                            convertToDescriptionGroup(cursor, moveable);
                            if (cursor.toNextSibling(J2EE_NAMESPACE, "init-param")) {
                                cursor.toFirstChild();
                                convertToDescriptionGroup(cursor, moveable);
                            }
                            cursor.pop();
                        }
                    } while (cursor.toNextSibling());
                } finally {
                    moveable.dispose();
                }
            }
        } finally {
            cursor.dispose();
        }
        XmlObject result = xmlObject.changeType(WebAppDocument.type);
        if (result != null) {
            validateDD(result);
            return (WebAppDocument) result;
        }
        validateDD(xmlObject);
        return (WebAppDocument) xmlObject;
    }

    public static XmlObject convertToGeronimoNamingSchema(XmlObject xmlObject) {
        XmlCursor cursor = xmlObject.newCursor();
        XmlCursor end = xmlObject.newCursor();
        try {
            while (cursor.hasNextToken()) {
                if (cursor.isStart()) {
                    String localName = cursor.getName().getLocalPart();
                    if (localName.equals("ejb-ref")
                            || localName.equals("ejb-local-ref")
                            || localName.equals("service-ref")
                            || localName.equals("resource-ref")
                            || localName.equals("resource-env-ref")
                            || localName.equals("cmp-connection-factory")
                            || localName.equals("workmanager")
                            || localName.equals("resource-adapter")) {
                        convertElementToSchema(cursor, end, GERONIMO_NAMING_NAMESPACE);
                    }
                }
                cursor.toNextToken();
            }
        } finally {
            cursor.dispose();
            end.dispose();
        }
        return xmlObject;
    }

    public static XmlObject convertToGeronimoSecuritySchema(XmlObject xmlObject) {
        XmlCursor cursor = xmlObject.newCursor();
        XmlCursor end = xmlObject.newCursor();
        try {
            while (cursor.hasNextToken()) {
                if (cursor.isStart()) {
                    String localName = cursor.getName().getLocalPart();
                    if (localName.equals("security")) {
                        convertElementToSchema(cursor, end, GERONIMO_SECURITY_NAMESPACE);
                    }
                }
                cursor.toNextToken();
            }
        } finally {
            cursor.dispose();
            end.dispose();
        }
        return xmlObject;
    }

    public static XmlObject convertToGeronimoServiceSchema(XmlObject xmlObject) {
        XmlCursor cursor = xmlObject.newCursor();
        XmlCursor end = xmlObject.newCursor();
        try {
            while (cursor.hasNextToken()) {
                if (cursor.isStart()) {
                    String localName = cursor.getName().getLocalPart();
                    if (localName.equals("gbean")
                            || localName.equals("dependency")
                            || localName.equals("include")) {
                        convertElementToSchema(cursor, end, GERONIMO_SERVICE_NAMESPACE);
                    }
                }
                cursor.toNextToken();
            }
        } finally {
            cursor.dispose();
            end.dispose();
        }
        return xmlObject;
    }

    public static XmlObject getNestedObjectAsType(XmlObject xmlObject, String desiredElement, SchemaType type) {
        XmlCursor cursor = xmlObject.newCursor();
        try {
            while (cursor.hasNextToken()) {
                if (cursor.isStart()) {
                    String localName = cursor.getName().getLocalPart();
                    if (localName.equals(desiredElement)) {
                        XmlObject child = cursor.getObject();
                        //The copy seems to be needed to make the type change work for some documents!
                        XmlObject result = child.copy().changeType(type);
                        assert result.schemaType() == type;
                        return result;
                    }
                }
                cursor.toNextToken();
            }
        } finally {
            cursor.dispose();
        }
        throw new IllegalArgumentException("xmlobject did not have desired element: " + desiredElement + "/n" + xmlObject);
    }


    public static boolean convertToSchema(XmlCursor cursor, String namespace, String schemaLocationURL, String version) {
        //remove dtd
        XmlDocumentProperties xmlDocumentProperties = cursor.documentProperties();
        xmlDocumentProperties.remove(XmlDocumentProperties.DOCTYPE_NAME);
        xmlDocumentProperties.remove(XmlDocumentProperties.DOCTYPE_PUBLIC_ID);
        xmlDocumentProperties.remove(XmlDocumentProperties.DOCTYPE_SYSTEM_ID);
        //convert namespace
        boolean isFirstStart = true;
        while (cursor.hasNextToken()) {
            if (cursor.isStart()) {
                if (namespace.equals(cursor.getName().getNamespaceURI())) {
                    //already has correct schema, exit
                    return false;
                }
                cursor.setName(new QName(namespace, cursor.getName().getLocalPart()));
                cursor.toNextToken();
                if (isFirstStart) {
                    cursor.insertNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
                    cursor.insertAttributeWithValue(new QName("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation", "xsi"), namespace + schemaLocationURL);
                    cursor.insertAttributeWithValue(new QName("version"), version);
                    isFirstStart = false;
                }
            } else {
                cursor.toNextToken();
            }
        }
        return true;
    }

    public static boolean convertElementToSchema(XmlCursor cursor, XmlCursor end, String namespace) {
        end.toCursor(cursor);
        end.toEndToken();
        while (cursor.hasNextToken() && cursor.isLeftOf(end)) {
            if (cursor.isStart()) {
                if (namespace.equals(cursor.getName().getNamespaceURI())) {
                    //already has correct schema, exit
                    return false;
                }
                cursor.setName(new QName(namespace, cursor.getName().getLocalPart()));
                cursor.toNextToken();
            } else {
                cursor.toNextToken();
            }
        }
        return true;
    }

    public static void convertBeans(XmlCursor cursor, XmlCursor moveable, String cmpVersion) {
        cursor.toChild(J2EE_NAMESPACE, "ejb-jar");
        cursor.toChild(J2EE_NAMESPACE, "enterprise-beans");
        if (cursor.toFirstChild()) {
            //there's at least one ejb...
            do {
                cursor.push();
                String type = cursor.getName().getLocalPart();
                if ("session".equals(type)) {
                    cursor.toChild(J2EE_NAMESPACE, "transaction-type");
                    cursor.toNextSibling();
                    convertToJNDIEnvironmentRefsGroup(cursor, moveable);
                } else if ("entity".equals(type)) {
                    cursor.toChild(J2EE_NAMESPACE, "persistence-type");
                    String persistenceType = cursor.getTextValue();
                    //reentrant is the last required tag before jndiEnvironmentRefsGroup
                    cursor.toNextSibling(J2EE_NAMESPACE, "reentrant");
                    //Convert 2.0 True/False to true/false for 2.1
                    cursor.setTextValue(cursor.getTextValue().toLowerCase());
                    if (cmpVersion != null && !cursor.toNextSibling(CMP_VERSION) && "Container".equals(persistenceType)) {
                        cursor.toNextSibling();
                        cursor.insertElementWithText(CMP_VERSION, cmpVersion);
                    }

                    cursor.toNextSibling(J2EE_NAMESPACE, "abstract-schema-name");
                    while (cursor.toNextSibling(J2EE_NAMESPACE, "cmp-field")) {
                        ;
                    }
                    cursor.toNextSibling(J2EE_NAMESPACE, "primkey-field");
                    cursor.toNextSibling();
                    convertToJNDIEnvironmentRefsGroup(cursor, moveable);
                } else if ("message-driven".equals(type)) {
                    cursor.toFirstChild();
                    if (cursor.toNextSibling(J2EE_NAMESPACE, "messaging-type")) {
                        cursor.toNextSibling(J2EE_NAMESPACE, "transaction-type");
                    } else {
                        cursor.toNextSibling(J2EE_NAMESPACE, "transaction-type");
                        //add messaging-type
                        cursor.insertElementWithText("messaging-type", J2EE_NAMESPACE, "javax.jms.MessageListener");
                    }
                    if (!cursor.toNextSibling(J2EE_NAMESPACE, "activation-config")) {
                        boolean hasProperties = false;
                        //skip transaction-type
                        cursor.toNextSibling();
                        //add activation-config-properties.
                        moveable.toCursor(cursor);
                        cursor.push();
                        cursor.beginElement("activation-config", J2EE_NAMESPACE);
                        hasProperties |= addActivationConfigProperty(moveable, cursor, "message-selector", "messageSelector");
                        hasProperties |= addActivationConfigProperty(moveable, cursor, "acknowledge-mode", "acknowledgeMode");
                        if (new QName(J2EE_NAMESPACE, "message-driven-destination").equals(moveable.getName()) ||
                                moveable.toNextSibling(J2EE_NAMESPACE, "message-driven-destination")) {
                            moveable.push();
                            moveable.toFirstChild();
                            hasProperties |= addActivationConfigProperty(moveable, cursor, "destination-type", "destinationType");
                            hasProperties |= addActivationConfigProperty(moveable, cursor, "subscription-durability", "subscriptionDurability");
                            moveable.pop();
                            moveable.removeXml();
                        }
                        cursor.pop();
                        if (!hasProperties) {
                            cursor.toPrevSibling();
                            cursor.removeXml();
                        }
                    }
                    cursor.toNextSibling();
                    convertToJNDIEnvironmentRefsGroup(cursor, moveable);
                }
                cursor.pop();
            } while (cursor.toNextSibling());
        }
    }

    private static boolean addActivationConfigProperty(XmlCursor moveable, XmlCursor cursor, String elementName, String propertyName) {
        QName name = new QName(J2EE_NAMESPACE, elementName);
        if (name.equals(moveable.getName()) || moveable.toNextSibling(name)) {
            cursor.push();
            cursor.beginElement("activation-config-property", J2EE_NAMESPACE);
            cursor.insertElementWithText("activation-config-property-name", J2EE_NAMESPACE, propertyName);
            cursor.insertElementWithText("activation-config-property-value", J2EE_NAMESPACE, moveable.getTextValue());
            moveable.removeXml();
            cursor.pop();
            cursor.toNextSibling();
            return true;
        }
        return false;
    }

    /**
     * Reorders elements to match descriptionGroup
     *
     * @param cursor XmlCursor positioned at first element of "group" to be reordered
     */
    public static void convertToDescriptionGroup(XmlCursor cursor, XmlCursor moveable) {
        moveable.toCursor(cursor);
        moveElements("description", moveable, cursor);
        moveElements("display-name", moveable, cursor);
        moveElements("icon", moveable, cursor);
    }

    public static void convertToJNDIEnvironmentRefsGroup(XmlCursor cursor, XmlCursor moveable) {
        moveElements("env-entry", moveable, cursor);
        moveElements("ejb-ref", moveable, cursor);
        moveElements("ejb-local-ref", moveable, cursor);
        moveElements("resource-ref", moveable, cursor);
        moveElements("resource-env-ref", moveable, cursor);
        moveElements("message-destination-ref", moveable, cursor);
        if (cursor.toPrevSibling()) {
            do {
                String name = cursor.getName().getLocalPart();
                if ("env-entry".equals(name)) {
                    cursor.push();
                    cursor.toFirstChild();
                    convertToDescriptionGroup(cursor, moveable);
                    convertToEnvEntryGroup(cursor, moveable);
                    cursor.pop();
                }
            } while (cursor.toPrevSibling());
        }
    }

    public static void convertToEnvEntryGroup(XmlCursor cursor, XmlCursor moveable) {
        moveElements("env-entry-name", moveable, cursor);
        moveElements("env-entry-type", moveable, cursor);
        moveElements("env-entry-value", moveable, cursor);
    }

    public static void validateDD(XmlObject dd) throws XmlException {
        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setLoadLineNumbers();
        Collection errors = new ArrayList();
        xmlOptions.setErrorListener(errors);
        if (!dd.validate(xmlOptions)) {
            throw new XmlException("Invalid deployment descriptor: " + errors + "\nDescriptor: " + dd.toString(), null, errors);
        }
//        System.out.println("descriptor: " + dd.toString());
    }

    private static void moveElements(String localName, XmlCursor moveable, XmlCursor toHere) {
        QName name = new QName(J2EE_NAMESPACE, localName);
        //skip elements already in the correct order.
        while (name.equals(toHere.getName()) && toHere.toNextSibling()) {
        }
        moveable.toCursor(toHere);
        while (moveable.toNextSibling(name)) {
            moveable.moveXml(toHere);
        }
    }

}
