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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ClassLoadingRule;
import org.apache.geronimo.kernel.repository.ClassLoadingRules;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.util.IOUtils;
import org.apache.geronimo.openejb.xbeans.ejbjar.OpenejbEjbJarDocument;
import org.apache.geronimo.openejb.xbeans.ejbjar.OpenejbGeronimoEjbJarType;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.javaee6.EjbJarDocument;
import org.apache.geronimo.xbeans.javaee6.EjbJarType;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.PersistenceContextRef;
import org.apache.openejb.jee.PersistenceContextType;
import org.apache.openejb.jee.oejb2.ArtifactType;
import org.apache.openejb.jee.oejb2.DependencyType;
import org.apache.openejb.jee.oejb2.EnvironmentType;
import org.apache.openejb.jee.oejb2.GeronimoEjbJarType;
import org.apache.openejb.jee.oejb2.ImportType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlDocumentProperties;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

public final class XmlUtil {
    public static final QName OPENEJBJAR_QNAME = OpenejbEjbJarDocument.type.getDocumentElementName();
    private static final QName CMP_VERSION = new QName(SchemaConversionUtils.J2EE_NAMESPACE, "cmp-version");

    private XmlUtil() {
    }

    public static <T> String marshal(T object) throws DeploymentException {
        try {
            Class type = object.getClass();

            if (object instanceof JAXBElement) {
                JAXBElement element = (JAXBElement) object;
                type = element.getValue().getClass();
            }

            JAXBContext ctx = JAXBContext.newInstance(type);
            Marshaller marshaller = ctx.createMarshaller();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            marshaller.marshal(object, baos);

            String xml = new String(baos.toByteArray());
            return xml;
        } catch (JAXBException e) {
            throw new DeploymentException(e);
        }
    }

    public static EjbJarType convertToXmlbeans(EjbJar ejbJar) throws DeploymentException {
        //
        // it would be nice if Jaxb had a way to convert the object to a
        // sax reader that could be fed directly into xmlbeans
        //

        // the geronimo xml beans tree is totally broken... fix some obvious stuff here
        for (EnterpriseBean enterpriseBean : ejbJar.getEnterpriseBeans()) {
            for (PersistenceContextRef ref : enterpriseBean.getPersistenceContextRef()) {
                if (ref.getPersistenceContextType() == PersistenceContextType.TRANSACTION) {
                    ref.setPersistenceContextType(null);
                }
            }
        }

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

    public static OpenejbGeronimoEjbJarType convertToXmlbeans(GeronimoEjbJarType geronimoEjbJarType) throws DeploymentException {
        //
        // it would be nice if Jaxb had a way to convert the object to a
        // sax reader that could be fed directly into xmlbeans
        //
        JAXBElement root = new JAXBElement(new QName("http://geronimo.apache.org/xml/ns/j2ee/ejb/openejb-2.0","ejb-jar"), GeronimoEjbJarType.class, geronimoEjbJarType);

        // marshal to xml

        String xml = marshal(root);
        try {
            XmlObject xmlObject = XmlBeansUtil.parse(xml);
            //TODO Convert persistence version to 2.0, might be removed once OpenEJB begins to use latest JPA version
            convertPersistenceSchemaVersion(xmlObject);
            OpenejbGeronimoEjbJarType geronimoOpenejb = (OpenejbGeronimoEjbJarType) SchemaConversionUtils.fixGeronimoSchema(xmlObject, OPENEJBJAR_QNAME, OpenejbGeronimoEjbJarType.type);
            return geronimoOpenejb;
        } catch (Throwable e) {
            String filePath = "<error: could not be written>";
            FileOutputStream out = null;
            try {
                File tempFile = File.createTempFile("openejb-jar-", ".xml");
                out = new FileOutputStream(tempFile);
                out.write(xml.getBytes());
                filePath = tempFile.getAbsolutePath();
            } catch (Exception notImportant) {
            } finally {
                IOUtils.close(out);
            }
            throw new DeploymentException("Error parsing geronimo-openejb.xml with xmlbeans.  For debug purposes, XML content written to: " + filePath, e);
        }
    }

    public static Environment buildEnvironment(EnvironmentType environmentType, Environment defaultEnvironment) {
        Environment environment = new Environment();
        if (environmentType != null) {
            if (environmentType.getModuleId() != null) {
                environment.setConfigId(toArtifact(environmentType.getModuleId(), null));
            }

            if (environmentType.getDependencies() != null) {
                for (DependencyType dependencyType : environmentType.getDependencies().getDependency()) {
                    Dependency dependency = toDependency(dependencyType);
                    environment.addDependency(dependency);
                }
            }

            environment.setSuppressDefaultEnvironment(environmentType.isSuppressDefaultEnvironment());

            ClassLoadingRules classLoadingRules = environment.getClassLoadingRules();
            classLoadingRules.setInverseClassLoading(environmentType.isInverseClassloading());

            if (environmentType.getHiddenClasses() != null) {
                ClassLoadingRule hiddenRule = classLoadingRules.getHiddenRule();
                List<String> filter = environmentType.getHiddenClasses().getFilter();
                hiddenRule.setClassPrefixes(new HashSet<String>(filter));
            }

            if (environmentType.getNonOverridableClasses() != null) {
                ClassLoadingRule nonOverrideableRule = classLoadingRules.getNonOverrideableRule();
                List<String> filter = environmentType.getNonOverridableClasses().getFilter();
                nonOverrideableRule.setClassPrefixes(new HashSet<String>(filter));
            }
        }
        if (!environment.isSuppressDefaultEnvironment()) {
            EnvironmentBuilder.mergeEnvironments(environment, defaultEnvironment);
        }

        return environment;
    }

    private static Dependency toDependency(DependencyType dependencyType) {
        Artifact artifact = toArtifact(dependencyType, null);
        if (ImportType.CLASSES.equals(dependencyType.getImport())) {
            return new Dependency(artifact, org.apache.geronimo.kernel.repository.ImportType.CLASSES);
        } else if (ImportType.SERVICES.equals(dependencyType.getImport())) {
            return new Dependency(artifact, org.apache.geronimo.kernel.repository.ImportType.SERVICES);
        } else if (dependencyType.getImport() == null) {
            return new Dependency(artifact, org.apache.geronimo.kernel.repository.ImportType.ALL);
        } else {
            throw new IllegalArgumentException("Unknown import type: " + dependencyType.getImport());
        }
    }

    private static Artifact toArtifact(ArtifactType artifactType, String defaultType) {
        String groupId = artifactType.getGroupId();
        String type = artifactType.getType();
        if (type == null) type = defaultType;
        String artifactId = artifactType.getArtifactId();
        String version = artifactType.getVersion();
        return new Artifact(groupId, artifactId, version, type);
    }

    public static GeronimoEjbJarType createDefaultPlan(String name, EjbJar ejbJar) {
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


        ArtifactType artifactType = new ArtifactType();
        artifactType.setArtifactId(id);

        EnvironmentType environmentType = new EnvironmentType();
        environmentType.setModuleId(artifactType);

        GeronimoEjbJarType geronimoEjbJarType = new GeronimoEjbJarType();
        geronimoEjbJarType.setEnvironment(environmentType);

        return geronimoEjbJarType;
    }

    public static String getJ2eeStringValue(org.apache.geronimo.xbeans.javaee6.String string) {
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

    // TODO I don't think we need this since openejb will always generate the newest spec,
    // but this code is doing more than just schema conversion, it is also converting message
    // driven properties to activation-config
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

    private static void convertPersistenceSchemaVersion(XmlObject xmlObject) {
        XmlCursor cursor = null;
        try {
            cursor = xmlObject.newCursor();
            cursor.toStartDoc();
            if (cursor.toFirstChild()) {
                do {
                    QName name = cursor.getName();
                    if (name.getLocalPart().equals("persistence")) {
                        XmlCursor end = cursor.newCursor();
                        end.toEndToken();
                        cursor.push();
                        SchemaConversionUtils.convertSchemaVersion(cursor, end, SchemaConversionUtils.JPA_PERSISTENCE_NAMESPACE, "http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd", "2.0");
                        end.dispose();
                        cursor.pop();
                    }
                } while (cursor.toNextSibling());
            }
        } finally {
            if (cursor != null) {
                try {
                    cursor.dispose();
                } catch (Exception e) {
                }
            }
        }
    }
}
