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

package org.apache.geronimo.schema;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class SchemaConversionUtils {
    public static final String J2EE_NAMESPACE = "http://java.sun.com/xml/ns/j2ee";

    static final String GERONIMO_NAMING_NAMESPACE = "http://geronimo.apache.org/xml/ns/naming-1.2";
    private static final String GERONIMO_SERVICE_NAMESPACE = "http://geronimo.apache.org/xml/ns/deployment-1.2";
    public static final String JPA_PERSISTENCE_NAMESPACE = "http://java.sun.com/xml/ns/persistence";

    

    private static final Map<String, ElementConverter> GERONIMO_SCHEMA_CONVERSIONS = new HashMap<String, ElementConverter>();

    static {

        GERONIMO_SCHEMA_CONVERSIONS.put("gbean-ref", new NamespaceElementConverter(GERONIMO_NAMING_NAMESPACE));
        GERONIMO_SCHEMA_CONVERSIONS.put("ejb-ref", new NamespaceElementConverter(GERONIMO_NAMING_NAMESPACE));
        GERONIMO_SCHEMA_CONVERSIONS.put("ejb-local-ref", new NamespaceElementConverter(GERONIMO_NAMING_NAMESPACE));
        GERONIMO_SCHEMA_CONVERSIONS.put("service-ref", new NamespaceElementConverter(GERONIMO_NAMING_NAMESPACE));
        GERONIMO_SCHEMA_CONVERSIONS.put("resource-ref", new NamespaceElementConverter(GERONIMO_NAMING_NAMESPACE));
        GERONIMO_SCHEMA_CONVERSIONS.put("resource-env-ref", new NamespaceElementConverter(GERONIMO_NAMING_NAMESPACE));
        GERONIMO_SCHEMA_CONVERSIONS.put("message-destination", new NamespaceElementConverter(GERONIMO_NAMING_NAMESPACE));
        GERONIMO_SCHEMA_CONVERSIONS.put("cmp-connection-factory", new NamespaceElementConverter(GERONIMO_NAMING_NAMESPACE));
        GERONIMO_SCHEMA_CONVERSIONS.put("workmanager", new NamespaceElementConverter(GERONIMO_NAMING_NAMESPACE));
        GERONIMO_SCHEMA_CONVERSIONS.put("resource-adapter", new NamespaceElementConverter(GERONIMO_NAMING_NAMESPACE));
        GERONIMO_SCHEMA_CONVERSIONS.put("web-container", new NamespaceElementConverter(GERONIMO_NAMING_NAMESPACE));
        GERONIMO_SCHEMA_CONVERSIONS.put("env-entry", new NamespaceElementConverter(GERONIMO_NAMING_NAMESPACE));

        GERONIMO_SCHEMA_CONVERSIONS.put("gbean", new GBeanElementConverter());
        GERONIMO_SCHEMA_CONVERSIONS.put("environment", new NamespaceElementConverter(GERONIMO_SERVICE_NAMESPACE));
        GERONIMO_SCHEMA_CONVERSIONS.put("client-environment", new NamespaceElementConverter(GERONIMO_SERVICE_NAMESPACE));
        GERONIMO_SCHEMA_CONVERSIONS.put("server-environment", new NamespaceElementConverter(GERONIMO_SERVICE_NAMESPACE));
        GERONIMO_SCHEMA_CONVERSIONS.put("persistence", new PersistenceElementConverter("2.0"));
    }

    private SchemaConversionUtils() {
    }

    public static void registerNamespaceConversions(Map<String, ? extends ElementConverter> conversions) {
        GERONIMO_SCHEMA_CONVERSIONS.putAll(conversions);
    }

    public static void unregisterNamespaceConversions(Map<String, ? extends ElementConverter> conversions) {
        GERONIMO_SCHEMA_CONVERSIONS.keySet().removeAll(conversions.keySet());
    }

    public static void convertToGeronimoSubSchemas(XmlCursor cursor) {
        cursor.toStartDoc();
        XmlCursor end = cursor.newCursor();
        try {
            while (cursor.hasNextToken()) {
                convertSingleElementToGeronimoSubSchemas(cursor, end);
                cursor.toNextToken();
            }
        } finally {
            end.dispose();
        }
    }

    public static boolean convertSingleElementToGeronimoSubSchemas(XmlCursor cursor, XmlCursor end) {
        if (cursor.isStart()) {
            String localName = cursor.getName().getLocalPart();
            ElementConverter converter = GERONIMO_SCHEMA_CONVERSIONS.get(localName);
            if (converter != null) {
                converter.convertElement(cursor, end);
                return true;
            }
            return false;
        }
        //you should only call this method at a start token
        return false;
    }

    public static XmlObject fixGeronimoSchema(XmlObject rawPlan, QName desiredElement, SchemaType desiredType) throws XmlException {
        XmlCursor cursor = rawPlan.newCursor();
        try {
            if (findNestedElement(cursor, desiredElement)) {
                cursor.push();
                convertToGeronimoSubSchemas(cursor);
                cursor.pop();
                XmlObject temp = cursor.getObject();

                XmlObject result = temp.changeType(desiredType);
                if (result == null || result.schemaType() != desiredType) {
                    result = temp.copy().changeType(desiredType);
                }
                XmlBeansUtil.validateDD(result);
                return result;
            } else {
                return null;
            }
        } finally {
            cursor.dispose();
        }
    }

    public static boolean findNestedElement(XmlCursor cursor, QName desiredElement) {
        while (cursor.hasNextToken()) {
            if (cursor.isStart()) {
                QName element = cursor.getName();
                if (element.equals(desiredElement)) {
                    return true;
                }
            }
            cursor.toNextToken();
        }
        return false;
    }

    public static boolean findNestedElement(XmlCursor cursor, String desiredElement) {
        while (cursor.hasNextToken()) {
            if (cursor.isStart()) {
                String element = cursor.getName().getLocalPart();
                if (element.equals(desiredElement)) {
                    return true;
                }
            }
            cursor.toNextToken();
        }
        return false;
    }

    public static XmlObject getNestedObjectAsType(XmlObject xmlObject, QName desiredElement, SchemaType type) {
        XmlCursor cursor = xmlObject.newCursor();
        try {
            if (findNestedElement(cursor, desiredElement)) {
                XmlObject child = cursor.getObject();
                //The copy seems to be needed to make the type change work for some documents!
                XmlObject result = child.copy().changeType(type);
                assert result.schemaType() == type;
                return result;
            }
        } finally {
            cursor.dispose();
        }
        throw new IllegalArgumentException("xmlobject did not have desired element: " + desiredElement + "\n" + xmlObject);
    }

    public static boolean convertSchemaVersion(XmlCursor start, XmlCursor end, String namespace, String schemaLocationURL, String version) {
        boolean isFirstStart = true;
        end.toCursor(start);
        end.toEndToken();
        while (start.hasNextToken() && start.isLeftOf(end)) {
            if (start.isStart()) {
                if (isFirstStart) {
                    if (start.getAttributeText(new QName("xmlns")) != null) {
                        start.removeAttribute(new QName("xmlns"));
                    }
                    start.setAttributeText(new QName("version"), version);
                    start.setAttributeText(new QName("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation", "xsi"), namespace + "  " + schemaLocationURL);
                    isFirstStart = false;
                }
                //convert namespace of each starting element
                start.setName(new QName(namespace, start.getName().getLocalPart()));
                start.toNextToken();
            } else {
                start.toNextToken();
            }
        }
        return true;
    }

    /**
     * Reorders elements to match descriptionGroup
     *
     * @param namespace new namespace
     * @param cursor XmlCursor positioned at first element of "group" to be reordered
     * @param moveable XmlCursor to use as a temp pointer
     */
    public static void convertToDescriptionGroup(String namespace, XmlCursor cursor, XmlCursor moveable) {
        moveable.toCursor(cursor);
        moveElements("description", namespace, moveable, cursor);
        moveElements("display-name", namespace, moveable, cursor);
        moveElements("icon", namespace, moveable, cursor);
    }

    public static void convertToJNDIEnvironmentRefsGroup(String namespace, XmlCursor cursor, XmlCursor moveable) {
        moveElements("env-entry", namespace, moveable, cursor);
        moveElements("ejb-ref", namespace, moveable, cursor);
        moveElements("ejb-local-ref", namespace, moveable, cursor);
        moveElements("resource-ref", namespace, moveable, cursor);
        moveElements("resource-env-ref", namespace, moveable, cursor);
        moveElements("message-destination-ref", namespace, moveable, cursor);

        do {
            String name = cursor.getName().getLocalPart();
            if ("env-entry".equals(name)) {
                cursor.push();
                cursor.toFirstChild();
                convertToDescriptionGroup(namespace, cursor, moveable);
                convertToEnvEntryGroup(namespace, cursor, moveable);
                cursor.pop();
            }
        } while (cursor.toPrevSibling());
    }

    public static void convertToEnvEntryGroup(String namespace, XmlCursor cursor, XmlCursor moveable) {
        moveElements("env-entry-name", namespace, moveable, cursor);
        moveElements("env-entry-type", namespace, moveable, cursor);
        moveElements("env-entry-value", namespace, moveable, cursor);
    }

    private static void moveElements(String localName, String namespace, XmlCursor moveable, XmlCursor toHere) {
        QName name = new QName(namespace, localName);
        //skip elements already in the correct order.
        while (name.equals(toHere.getName()) && toHere.toNextSibling()) {
        }
        moveable.toCursor(toHere);
        while (moveable.toNextSibling(name)) {
            moveable.moveXml(toHere);
        }
    }

}
