/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.openejb.deployment;

import java.net.URL;
import java.util.jar.JarFile;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.transform.sax.SAXSource;

import org.apache.geronimo.openejb.xbeans.ejbjar.OpenejbEjbJarDocument;
import org.apache.geronimo.openejb.xbeans.ejbjar.OpenejbGeronimoEjbJarType;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.xbeans.ArtifactType;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.javaee.EjbJarDocument;
import org.apache.geronimo.xbeans.javaee.EjbJarType;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlDocumentProperties;
import org.apache.openejb.jee.EjbJar;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

public final class XmlUtil {
    public static final QName OPENEJBJAR_QNAME = OpenejbEjbJarDocument.type.getDocumentElementName();
    private static final QName CMP_VERSION = new QName(SchemaConversionUtils.J2EE_NAMESPACE, "cmp-version");

    private XmlUtil() {
    }

    public static String loadEjbJarXml(URL ejbJarUrl, JarFile moduleFile) {
        String ejbJarXml;
        try {
            if (ejbJarUrl == null) {
                ejbJarUrl = DeploymentUtil.createJarURL(moduleFile, "META-INF/ejb-jar.xml");
            }

            // read in the entire specDD as a string, we need this for getDeploymentDescriptor
            // on the J2ee management object
            ejbJarXml = DeploymentUtil.readAll(ejbJarUrl);
            return ejbJarXml;
        } catch (Exception e) {
            return null;
        }
    }

    public static class EjbJarNamespaceFilter extends XMLFilterImpl {

        public EjbJarNamespaceFilter(XMLReader xmlReader) {
            super(xmlReader);
        }

        public void startElement(String uri, String localName, String qname, Attributes atts) throws SAXException {
            super.startElement("http://java.sun.com/xml/ns/javaee", localName, qname, atts);
        }
    }

    @SuppressWarnings({"unchecked"})
    public static <T> T unmarshal(Class<T> type, String xml) throws DeploymentException {
        if (xml == null){
            return null;
        }

        if (type.equals(EjbJar.class)){
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(true);

                SAXParser parser = factory.newSAXParser();

                EjbJarNamespaceFilter xmlFilter = new EjbJarNamespaceFilter(parser.getXMLReader());

                JAXBContext ctx = JAXBContext.newInstance(type);
                Unmarshaller unmarshaller = ctx.createUnmarshaller();

                xmlFilter.setContentHandler(unmarshaller.getUnmarshallerHandler());
                SAXSource source = new SAXSource(xmlFilter, new InputSource(new ByteArrayInputStream(xml.getBytes())));

                return (T) unmarshaller.unmarshal(source);
            } catch (Exception e) {
                throw new DeploymentException(e);
            }
        }

        try {
            JAXBContext ctx = JAXBContext.newInstance(type);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();

            Object object = unmarshaller.unmarshal(new ByteArrayInputStream(xml.getBytes()));
            unmarshaller.setEventHandler(new ValidationEventHandler());
            return (T) object;
        } catch (JAXBException e) {
            throw new DeploymentException(e);
        }
    }

    public static <T> String marshal(T object) throws DeploymentException {
        try {
            JAXBContext ctx = JAXBContext.newInstance(object.getClass());
            Marshaller marshaller = ctx.createMarshaller();
            marshaller.setProperty("jaxb.formatted.output", true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            marshaller.marshal(object, baos);

            String xml = new String(baos.toByteArray());
            return xml;
        } catch (JAXBException e) {
            throw new DeploymentException(e);
        }
    }

    public static String loadOpenejbJarXml(XmlObject xmlObject, JarFile moduleFile) throws DeploymentException {
        // load the openejb-jar.xml from either the supplied plan or from the earFile
        try {
            String openejbJarXml;
            if (xmlObject instanceof XmlObject) {
                openejbJarXml = xmlObject.xmlText();
            } else {
                if (xmlObject != null) {
                    xmlObject = XmlBeansUtil.parse(((File) xmlObject).toURL(), XmlUtil.class.getClassLoader());
                    openejbJarXml = xmlObject.xmlText();
                } else {
                    URL path = DeploymentUtil.createJarURL(moduleFile, "META-INF/openejb-jar.xml");
                    if (path == null) {
                        return null;
                    }
                    openejbJarXml = DeploymentUtil.readAll(path);
                }
            }
            return openejbJarXml;
        } catch (IOException e) {
            return null;
        } catch (XmlException e) {
            throw new DeploymentException(e);
        }
    }

    public static EjbJarType convertToXmlbeans(EjbJar ejbJar) throws DeploymentException {
        //
        // it would be nice if Jaxb had a way to convert the object to a
        // sax reader that could be fed directly into xmlbeans
        //

        // marshal to xml
        String xml = marshal(ejbJar);
        try {
            // parse the xml
            EjbJarDocument ejbJarDoc = convertToEJBSchema(XmlBeansUtil.parse(xml));
            EjbJarType ejbJarType = ejbJarDoc.getEjbJar();
            return ejbJarType;
        } catch (XmlException e) {
            throw new DeploymentException("Error parsing ejb-jar.xml", e);
        }

    }
    public static OpenejbGeronimoEjbJarType loadGeronimOpenejbJar(Object plan, JarFile moduleFile, boolean standAlone, String targetPath, EjbJar ejbJar) throws DeploymentException {
        OpenejbGeronimoEjbJarType openejbJar;
        XmlObject rawPlan = null;
        try {
            // load the openejb-jar.xml from either the supplied plan or from the earFile
            try {
                if (plan instanceof XmlObject) {
                    rawPlan = (XmlObject) plan;
                } else {
                    if (plan != null) {
                        OpenejbEjbJarDocument document = (OpenejbEjbJarDocument) XmlBeansUtil.parse(((File) plan).toURL(), XmlUtil.class.getClassLoader());
                        rawPlan = document.getEjbJar();
                    } else {
                        URL path = DeploymentUtil.createJarURL(moduleFile, "META-INF/geronimo-openejb.xml");
                        rawPlan = XmlBeansUtil.parse(path, XmlUtil.class.getClassLoader());
                    }
                }
            } catch (IOException e) {
                //no plan, create a default
            }

            // if we got one extract, adjust, and validate it otherwise create a default one
            if (rawPlan != null) {
                openejbJar = (OpenejbGeronimoEjbJarType) SchemaConversionUtils.fixGeronimoSchema(rawPlan, OPENEJBJAR_QNAME, OpenejbGeronimoEjbJarType.type);
            } else {
                String path;
                if (standAlone) {
                    // default configId is based on the moduleFile name
                    path = new File(moduleFile.getName()).getName();
                } else {
                    // default configId is based on the module uri from the application.xml
                    path = targetPath;
                }
                openejbJar = createDefaultPlan(path, ejbJar);
            }
        } catch (XmlException e) {
            throw new DeploymentException(e);
        }
        return openejbJar;
    }

    public static OpenejbGeronimoEjbJarType createDefaultPlan(String name, EjbJar ejbJar) {
        String id = ejbJar.getId();
        if (id == null) {
            id = name;
            if (id.endsWith(".jar")) {
                id = id.substring(0, id.length() - 4);
            }
            if (id.endsWith("/")) {
                id = id.substring(0, id.length() - 1);
            }
        }

        OpenejbGeronimoEjbJarType openejbEjbJar = OpenejbGeronimoEjbJarType.Factory.newInstance();
        EnvironmentType environmentType = openejbEjbJar.addNewEnvironment();
        ArtifactType artifactType = environmentType.addNewModuleId();
        artifactType.setArtifactId(id);
        return openejbEjbJar;
    }

    public static String getJ2eeStringValue(org.apache.geronimo.xbeans.javaee.String string) {
        if (string == null) {
            return null;
        }
        return string.getStringValue();
    }

    public static class ValidationEventHandler implements javax.xml.bind.ValidationEventHandler {
        public boolean handleEvent(ValidationEvent validationEvent) {
            System.out.println(validationEvent.getMessage());
            return true;
        }
    }


    // coerce to newest spec... this shouldn't be necessary as the jaxb tree always creates the newest spec
    public static EjbJarDocument convertToEJBSchema(XmlObject xmlObject) throws XmlException {
        if (EjbJarDocument.type.equals(xmlObject.schemaType())) {
//            XmlBeansUtil.validateDD(xmlObject);
            return (EjbJarDocument) xmlObject;
        }
        XmlCursor cursor = xmlObject.newCursor();
        XmlCursor moveable = xmlObject.newCursor();
        //cursor is intially located before the logical STARTDOC token
        try {
            cursor.toFirstChild();
            if (EjbJarDocument.type.getDocumentElementName().getNamespaceURI().equals(cursor.getName().getNamespaceURI())) {
                XmlObject result = xmlObject.changeType(EjbJarDocument.type);
                // XmlBeansUtil.validateDD(result);
                return (EjbJarDocument) result;
            }
            // deployment descriptor is probably in EJB 1.1 or 2.0 format
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
            SchemaConversionUtils.convertToSchema(cursor, SchemaConversionUtils.J2EE_NAMESPACE, schemaLocationURL, version);
            //play with message-driven
            cursor.toStartDoc();
            convertBeans(cursor, moveable, cmpVersion);
        } finally {
            cursor.dispose();
            moveable.dispose();
        }
        XmlObject result = xmlObject.changeType(EjbJarDocument.type);
        if (result != null) {
            XmlBeansUtil.validateDD(result);
            return (EjbJarDocument) result;
        }
        XmlBeansUtil.validateDD(xmlObject);
        return (EjbJarDocument) xmlObject;
    }

    private static void convertBeans(XmlCursor cursor, XmlCursor moveable, String cmpVersion) {
        cursor.toChild(SchemaConversionUtils.J2EE_NAMESPACE, "ejb-jar");
        cursor.toChild(SchemaConversionUtils.J2EE_NAMESPACE, "enterprise-beans");
        if (cursor.toFirstChild()) {
            //there's at least one ejb...
            do {
                cursor.push();
                String type = cursor.getName().getLocalPart();
                if ("session".equals(type)) {
                    cursor.toChild(SchemaConversionUtils.J2EE_NAMESPACE, "transaction-type");
                    cursor.toNextSibling();
                    SchemaConversionUtils.convertToJNDIEnvironmentRefsGroup(SchemaConversionUtils.J2EE_NAMESPACE, cursor, moveable);
                } else if ("entity".equals(type)) {
                    cursor.toChild(SchemaConversionUtils.J2EE_NAMESPACE, "persistence-type");
                    String persistenceType = cursor.getTextValue();
                    //reentrant is the last required tag before jndiEnvironmentRefsGroup
                    cursor.toNextSibling(SchemaConversionUtils.J2EE_NAMESPACE, "reentrant");
                    //Convert 2.0 True/False to true/false for 2.1
                    cursor.setTextValue(cursor.getTextValue().toLowerCase());
                    if (cmpVersion != null && !cursor.toNextSibling(CMP_VERSION) && "Container".equals(persistenceType)) {
                        cursor.toNextSibling();
                        cursor.insertElementWithText(CMP_VERSION, cmpVersion);
                    }

                    cursor.toNextSibling(SchemaConversionUtils.J2EE_NAMESPACE, "abstract-schema-name");
                    while (cursor.toNextSibling(SchemaConversionUtils.J2EE_NAMESPACE, "cmp-field")) {
                    }
                    cursor.toNextSibling(SchemaConversionUtils.J2EE_NAMESPACE, "primkey-field");
                    cursor.toNextSibling();
                    SchemaConversionUtils.convertToJNDIEnvironmentRefsGroup(SchemaConversionUtils.J2EE_NAMESPACE, cursor, moveable);
                } else if ("message-driven".equals(type)) {
                    cursor.toFirstChild();
                    if (cursor.toNextSibling(SchemaConversionUtils.J2EE_NAMESPACE, "messaging-type")) {
                        cursor.toNextSibling(SchemaConversionUtils.J2EE_NAMESPACE, "transaction-type");
                    } else {
                        cursor.toNextSibling(SchemaConversionUtils.J2EE_NAMESPACE, "transaction-type");
                        //insert messaging-type (introduced in EJB 2.1 spec) before transaction-type
                        cursor.insertElementWithText("messaging-type", SchemaConversionUtils.J2EE_NAMESPACE, "javax.jms.MessageListener");
                        //cursor still on transaction-type
                    }
                    if (!cursor.toNextSibling(SchemaConversionUtils.J2EE_NAMESPACE, "activation-config")) {
                        //skip transaction-type
                        cursor.toNextSibling();
                        //convert EJB 2.0 elements to activation-config-properties.
                        moveable.toCursor(cursor);
                        cursor.push();
                        cursor.beginElement("activation-config", SchemaConversionUtils.J2EE_NAMESPACE);
                        boolean hasProperties = addActivationConfigProperty(moveable, cursor, "message-selector", "messageSelector");
                        hasProperties |= addActivationConfigProperty(moveable, cursor, "acknowledge-mode", "acknowledgeMode");
                        if (new QName(SchemaConversionUtils.J2EE_NAMESPACE, "message-driven-destination").equals(moveable.getName()) ||
                                moveable.toNextSibling(SchemaConversionUtils.J2EE_NAMESPACE, "message-driven-destination")) {
                            moveable.push();
                            moveable.toFirstChild();
                            hasProperties |= addActivationConfigProperty(moveable, cursor, "destination-type", "destinationType");
                            hasProperties |= addActivationConfigProperty(moveable, cursor, "subscription-durability", "subscriptionDurability");
                            moveable.pop();
                            moveable.removeXml();
                        }
                        cursor.pop();
                        if (!hasProperties) {
                            //the activation-config element that we created is empty so delete it
                            cursor.toPrevSibling();
                            cursor.removeXml();
                            //cursor should now be at first element in JNDIEnvironmentRefsGroup
                        }
                    } else {
                        //cursor pointing at activation-config
                        cursor.toNextSibling();
                        //cursor should now be at first element in JNDIEnvironmentRefsGroup
                    }
                    SchemaConversionUtils.convertToJNDIEnvironmentRefsGroup(SchemaConversionUtils.J2EE_NAMESPACE, cursor, moveable);
                }
                cursor.pop();
            } while (cursor.toNextSibling());
        }
    }

    private static boolean addActivationConfigProperty(XmlCursor moveable, XmlCursor cursor, String elementName, String propertyName) {
        QName name = new QName(SchemaConversionUtils.J2EE_NAMESPACE, elementName);
        if (name.equals(moveable.getName()) || moveable.toNextSibling(name)) {
            cursor.push();
            cursor.beginElement("activation-config-property", SchemaConversionUtils.J2EE_NAMESPACE);
            cursor.insertElementWithText("activation-config-property-name", SchemaConversionUtils.J2EE_NAMESPACE, propertyName);
            cursor.insertElementWithText("activation-config-property-value", SchemaConversionUtils.J2EE_NAMESPACE, moveable.getTextValue());
            moveable.removeXml();
            cursor.pop();
            cursor.toNextSibling();
            return true;
        }
        return false;
    }
}
