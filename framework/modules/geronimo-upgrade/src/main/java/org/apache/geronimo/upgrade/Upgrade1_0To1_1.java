/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.upgrade;

import java.io.InputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.namespace.QName;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlCursor;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;

/**
 * @version $Rev$ $Date$
 */
public class Upgrade1_0To1_1 {

    private static final Map NAMESPACE_UPDATES = new HashMap();

    static {
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/application-client", "http://geronimo.apache.org/xml/ns/j2ee/application-client-2.0");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/application-client-1.1", "http://geronimo.apache.org/xml/ns/j2ee/application-client-2.0");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/application-client-1.2", "http://geronimo.apache.org/xml/ns/j2ee/application-client-2.0");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/application", "http://geronimo.apache.org/xml/ns/j2ee/application-2.0");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/application-1.1", "http://geronimo.apache.org/xml/ns/j2ee/application-2.0");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/application-1.2", "http://geronimo.apache.org/xml/ns/j2ee/application-2.0");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/deployment", "http://geronimo.apache.org/xml/ns/deployment-1.2");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/deployment-1.0", "http://geronimo.apache.org/xml/ns/deployment-1.2");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/deployment-1.1", "http://geronimo.apache.org/xml/ns/deployment-1.2");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/connector", "http://geronimo.apache.org/xml/ns/j2ee/connector-1.2");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/connector-1.0", "http://geronimo.apache.org/xml/ns/j2ee/connector-1.2");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/connector-1.1", "http://geronimo.apache.org/xml/ns/j2ee/connector-1.2");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/deployment/javabean", "http://geronimo.apache.org/xml/ns/deployment/javabean-1.0");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/loginconfig", "http://geronimo.apache.org/xml/ns/loginconfig-1.0");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/loginconfig-1.0", "http://geronimo.apache.org/xml/ns/loginconfig-1.0");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/loginconfig-1.1", "http://geronimo.apache.org/xml/ns/loginconfig-1.0");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/naming", "http://geronimo.apache.org/xml/ns/naming-1.2");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/naming-1.0", "http://geronimo.apache.org/xml/ns/naming-1.2");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/naming-1.1", "http://geronimo.apache.org/xml/ns/naming-1.2");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/security", "http://geronimo.apache.org/xml/ns/security-1.2");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/security-1.0", "http://geronimo.apache.org/xml/ns/security-1.2");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/security-1.1", "http://geronimo.apache.org/xml/ns/security-1.2");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/web", "http://geronimo.apache.org/xml/ns/j2ee/web-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web-1.0", "http://geronimo.apache.org/xml/ns/j2ee/web-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web-1.1", "http://geronimo.apache.org/xml/ns/j2ee/web-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web-1.2", "http://geronimo.apache.org/xml/ns/j2ee/web-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web-2.0", "http://geronimo.apache.org/xml/ns/j2ee/web-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/web-1.0", "http://geronimo.apache.org/xml/ns/j2ee/web-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/web/jetty", "http://geronimo.apache.org/xml/ns/j2ee/web/jetty-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/web/jetty-1.0", "http://geronimo.apache.org/xml/ns/j2ee/web/jetty-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web/jetty-1.0", "http://geronimo.apache.org/xml/ns/j2ee/web/jetty-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web/jetty-1.1", "http://geronimo.apache.org/xml/ns/j2ee/web/jetty-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web/jetty-1.2", "http://geronimo.apache.org/xml/ns/j2ee/web/jetty-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web/jetty-2.0", "http://geronimo.apache.org/xml/ns/j2ee/web/jetty-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/web/jetty/config", "http://geronimo.apache.org/xml/ns/j2ee/web/jetty/config-1.0");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/web/tomcat", "http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/web/tomcat-1.0", "http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-1.0", "http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-1.1", "http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-1.2", "http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-2.0", "http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/web/tomcat/config", "http://geronimo.apache.org/xml/ns/j2ee/web/tomcat/config-1.0");
        NAMESPACE_UPDATES.put("http://www.openejb.org/xml/ns/openejb-jar", "http://www.openejb.org/xml/ns/openejb-jar-2.3");
        NAMESPACE_UPDATES.put("http://www.openejb.org/xml/ns/openejb-jar-2.0", "http://www.openejb.org/xml/ns/openejb-jar-2.3");
        NAMESPACE_UPDATES.put("http://www.openejb.org/xml/ns/openejb-jar-2.1", "http://www.openejb.org/xml/ns/openejb-jar-2.3");
        NAMESPACE_UPDATES.put("http://www.openejb.org/xml/ns/openejb-jar-2.2", "http://www.openejb.org/xml/ns/openejb-jar-2.3");
        NAMESPACE_UPDATES.put("http://www.openejb.org/xml/ns/pkgen", "http://www.openejb.org/xml/ns/pkgen-2.0");
        NAMESPACE_UPDATES.put("http://www.openejb.org/xml/ns/corba-css-config_1_0", "http://www.openejb.org/xml/ns/corba-css-config-2.0");
        NAMESPACE_UPDATES.put("http://www.openejb.org/xml/ns/corba-tss-config_1_0", "http://www.openejb.org/xml/ns/corba-tss-config-2.0");
    }

    private static final QName ENVIRONMENT_QNAME = new QName("http://geronimo.apache.org/xml/ns/deployment-1.2", "environment");
    private static final String DEFAULT_GROUPID = "default";
    private static final String DEFAULT_VERSION = "1-default";
    private static final QName CLIENT_ENVIRONMENT_QNAME = new QName("http://geronimo.apache.org/xml/ns/deployment-1.2", "client-environment");
    private static final QName SERVER_ENVIRONMENT_QNAME = new QName("http://geronimo.apache.org/xml/ns/deployment-1.2", "server-environment");
    private static final QName PATTERN_QNAME = new QName("http://geronimo.apache.org/xml/ns/naming-1.2", "pattern");
    private static final QName GROUP_QNAME = new QName("http://geronimo.apache.org/xml/ns/naming-1.2", "groupId");
    private static final QName ARTIFACT_QNAME = new QName("http://geronimo.apache.org/xml/ns/naming-1.2", "artifactId");
    private static final QName MODULE_QNAME = new QName("http://geronimo.apache.org/xml/ns/naming-1.2", "module");
    private static final QName NAME_QNAME = new QName("http://geronimo.apache.org/xml/ns/naming-1.2", "name");
    private static final QName NAME_QNAME2 = new QName("http://geronimo.apache.org/xml/ns/deployment-1.2", "name");
    private static final QName GBEAN_NAME_QNAME = new QName(null, "gbeanName");

    public void upgrade(InputStream source, Writer target) throws IOException, XmlException {
        XmlObject xmlObject = parse(source);
        xmlObject = upgrade(xmlObject);

        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setSavePrettyPrint();
        xmlObject.save(target, xmlOptions);

    }

    public XmlObject upgrade(XmlObject xmlObject) throws XmlException {
        XmlCursor cursor = xmlObject.newCursor();
        XmlCursor.TokenType token;
        while ((token = cursor.toNextToken()) != XmlCursor.TokenType.ENDDOC) {
            if (token == XmlCursor.TokenType.START) {
                Artifact configId = extractArtifact("configId", cursor);
                Artifact parentId = extractArtifact("parentId", cursor);
                Artifact clientConfigId = extractArtifact("clientConfigId", cursor);
                Artifact clientParentId = extractArtifact("clientParentId", cursor);
                boolean suppressDefaultEnvironment = extractSuppressDefaultEnvironment(cursor);
                if (clientConfigId != null) {

                    insertEnvironment(clientConfigId, clientParentId, cursor, CLIENT_ENVIRONMENT_QNAME, suppressDefaultEnvironment);

                    insertEnvironment(configId, parentId, cursor, SERVER_ENVIRONMENT_QNAME, false);

                } else if (configId != null) {

                    insertEnvironment(configId, parentId, cursor, ENVIRONMENT_QNAME, suppressDefaultEnvironment);
                } else {
                    cleanContextPriorityClassLoader(cursor);
                    cleanRef(cursor);
                }
                checkInvalid(cursor);
            }
        }
        return xmlObject;
    }

    private void cleanContextPriorityClassLoader(XmlCursor cursor) {
        String localName = getLocalName(cursor);
        if ("context-priority-classloader".equals(localName)) {
            String value = cursor.getTextValue();
            if ("false".equals(value)) {
                cursor.removeXml();
            } else if ("true".equals("true")) {
                cursor.removeXml();
                cursor.insertComment("YOU MUST INSERT THE ELEMENT <inverse-classloading/> INTO THE ENVIRONMENT ELEMENT FOR THIS MODULE");
            }
        }
    }

    private static void checkInvalid(XmlCursor cursor) throws XmlException {
        String localName = getLocalName(cursor);
        if ("gbean".equals(localName)) {
            if (cursor.getAttributeText(GBEAN_NAME_QNAME) != null) {
                throw new XmlException("You must replace the gbeanName attribute manually: " + cursor.getAttributeText(GBEAN_NAME_QNAME));
            }
        }
    }

    private static void cleanRef(XmlCursor cursor) throws XmlException {
        String localName = getLocalName(cursor);
        if ("ejb-ref".equals(localName)) {
            cursor.toFirstChild();
            String application = null;
            String module = null;
            String name = null;
            while (cursor.getName() != null) {
                localName = cursor.getName().getLocalPart();
                if ("ref-name".equals(localName)) {
//                    cursor.toNextSibling();
                } else if ("domain".equals(localName)) {
                    cursor.removeXml();
                } else if ("server".equals(localName)) {
                    cursor.removeXml();
                } else if ("application".equals(localName)) {
                    application = cursor.getTextValue();
                    if ("null".equals(application)) {
                        application = null;
                    }
                    cursor.removeXml();
                } else if ("module".equals(localName)) {
                    if (application == null) {
                        //this is a configuration name
                        application = cursor.getTextValue();
                    } else {
                        module = cursor.getTextValue();
                    }
                    cursor.removeXml();
                } else if ("type".equals(localName)) {
                    cursor.removeXml();
                } else if ("name".equals(localName)) {
                    name = cursor.getTextValue();
                    cursor.removeXml();
                } else if ("ejb-link".equals(localName)) {
                    break;
                } else if ("target-name".equals(localName)) {
                    ObjectName targetName = extractObjectName(cursor);
                    name = targetName.getKeyProperty("name");
                    application = targetName.getKeyProperty("J2EEApplication");
                    if ("null".equals(application)) {
                        application = targetName.getKeyProperty("EJBModule");
                    } else {
                        module = targetName.getKeyProperty("EJBModule");
                    }

                } else if ("ns-corbaloc".equals(localName)) {
                    cursor.toNextSibling();
//                    cursor.toNextSibling();
                } else if ("css".equals(localName)) {
                    //TODO fix this
//                    cursor.toNextSibling();
                } else if ("css-link".equals(localName)) {
//                    cursor.toNextSibling();
                } else if ("css-name".equals(localName)) {
//                    cursor.toNextSibling();
                } else {
                    throw new IllegalStateException("unrecognized element: " + cursor.getTextValue());
                }
                if (!cursor.toNextSibling()) {
                    break;
                }
            }
            if (name != null) {
                cursor.beginElement(PATTERN_QNAME);
                if (application != null) {
                    try {
                        Artifact artifact = Artifact.create(application);
                        cursor.insertElementWithText(GROUP_QNAME, artifact.getGroupId());
                        cursor.insertElementWithText(ARTIFACT_QNAME, artifact.getArtifactId());
                    } catch (Exception e) {
                        cursor.insertElementWithText(ARTIFACT_QNAME, application.replace('/', '_'));
                    }
//                    cursor.insertElementWithText(VERSION_QNAME, artifact.getVersion().toString());
                }
                if (module != null) {
                    cursor.insertElementWithText(MODULE_QNAME, module);
                }
                cursor.insertElementWithText(NAME_QNAME, name);
                cursor.toNextToken();
            }
        } else if ("gbean-name".equals(localName)) {
            ObjectName targetName = extractObjectName(cursor);
            String name = targetName.getKeyProperty("name");
            cursor.insertComment("CHECK THAT THE TARGET GBEAN IS IN THE ANCESTOR SET OF THIS MODULE AND THAT THE NAME UNIQUELY IDENTIFIES IT");
            cursor.insertElementWithText(NAME_QNAME2, name);
        }
    }

    private static ObjectName extractObjectName(XmlCursor cursor) throws XmlException {
        String targetNameString = cursor.getTextValue();
        cursor.removeXml();
        ObjectName targetName;
        try {
            targetName = ObjectName.getInstance(targetNameString);
        } catch (MalformedObjectNameException e) {
            throw (XmlException)new XmlException("Invalid object name: " + targetNameString).initCause(e);
        }
        return targetName;
    }

    private static String getLocalName(XmlCursor cursor) {
        QName name = cursor.getName();
        return name == null ? null : name.getLocalPart();
    }

    private static void insertEnvironment(Artifact configId, Artifact parentId, XmlCursor cursor, QName environmentQname, boolean suppressDefaultEnvironment) {
        positionEnvironment(cursor);
        Environment environment = new Environment();
        environment.setConfigId(configId);
        if (parentId != null) {
            environment.addDependency(parentId, ImportType.ALL);
        }
        environment.setSuppressDefaultEnvironment(suppressDefaultEnvironment);
        extractDependencies(cursor, environment);
        EnvironmentType environmentType = EnvironmentBuilder.buildEnvironmentType(environment);
        cursor.beginElement(environmentQname);
        XmlCursor element = environmentType.newCursor();
        try {
            element.copyXmlContents(cursor);
        } finally {
            element.dispose();
        }
    }

    private static void extractDependencies(XmlCursor cursor, Environment environment) {
        if (cursor.getName() == null) {
            //no dependencies, do nothing
            return;
        }
        do {
            String localPart = getLocalName(cursor);
            if (localPart.equals("dependency") || localPart.equals("import")) {
                extractDependency(cursor, environment);
            } else {
                break;
            }
        } while (cursor.toNextSibling());
    }

    private static void extractDependency(XmlCursor cursor, Environment environment) {
        cursor.push();
        cursor.toFirstChild();
        Artifact artifact;
        if (cursor.getName().getLocalPart().equals("uri")) {
            String uri = cursor.getTextValue();
            artifact = toArtifact(uri);
        } else {
            checkName(cursor, "groupId");
            String groupId = cursor.getTextValue();
            cursor.toNextSibling();
            String type = "jar";
            if (cursor.getName().getLocalPart().equals("type")) {
                type = cursor.getTextValue();
                cursor.toNextSibling();
            }
            checkName(cursor, "artifactId");
            String artifactId = cursor.getTextValue();
            cursor.toNextSibling();
            checkName(cursor, "version");
            String version = cursor.getTextValue();
            artifact = new Artifact(groupId, artifactId, version, type);
        }
        environment.addDependency(artifact, ImportType.ALL);
        cursor.pop();
        cursor.removeXml();
    }

    private static void checkName(XmlCursor cursor, String localName) {
        if (!cursor.getName().getLocalPart().equals(localName)) {
            throw new IllegalArgumentException("Expected element: " + localName + " but actually: " + cursor.getName().getLocalPart());
        }

    }

    private static void positionEnvironment(XmlCursor cursor) {
        XmlCursor.TokenType token;
        while ((token = cursor.toNextToken()) != XmlCursor.TokenType.START && token != XmlCursor.TokenType.END) {
            //keep going
        }
    }

    private static Artifact extractArtifact(String attrName, XmlCursor cursor) {
        String attrValue;
        QName attrQName = new QName(null, attrName);
        if ((attrValue = cursor.getAttributeText(attrQName)) != null) {
            cursor.removeAttribute(attrQName);
            return toArtifact(attrValue);
        }
        return null;
    }

    private static Artifact toArtifact(String attrValue) {
        try {
            return Artifact.create(attrValue);
        } catch (Exception e) {
            return new Artifact(DEFAULT_GROUPID, attrValue.replace('/', '_'), DEFAULT_VERSION, "car");
        }
    }

    private static boolean extractSuppressDefaultEnvironment(XmlCursor cursor) {
        String attrValue;
        QName attrQName = new QName(null, "suppressDefaultParentId");
        if ((attrValue = cursor.getAttributeText(attrQName)) != null) {
            cursor.removeAttribute(attrQName);
            return Boolean.valueOf(attrValue).booleanValue();
        }
        return false;
    }

    public static XmlObject parse(InputStream is) throws IOException, XmlException {
        ArrayList errors = new ArrayList();
        XmlObject parsed = XmlObject.Factory.parse(is, createXmlOptions(errors));
        if (errors.size() != 0) {
            throw new XmlException(errors.toArray().toString());
        }
        return parsed;
    }

    public static XmlOptions createXmlOptions(Collection errors) {
        XmlOptions options = new XmlOptions();
        options.setLoadLineNumbers();
        options.setErrorListener(errors);
        options.setLoadSubstituteNamespaces(NAMESPACE_UPDATES);
        return options;
    }

}
