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
package org.apache.geronimo.web.deployment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.xbeans.ModuleDocument;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.geronimo.security.GerSecurityDocument;
import org.apache.geronimo.xbeans.geronimo.web.GerWebAppDocument;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class GenericToSpecificPlanConverter {

    private static final QName GENERIC_QNAME = GerWebAppDocument.type.getDocumentElementName();
    private static final String GENERIC_NAMESPACE = GENERIC_QNAME.getNamespaceURI();
    private static final String OLD_GENERIC_NAMESPACE = "http://geronimo.apache.org/xml/ns/web";

    private static final QName GENERIC_CONFIG_QNAME = new QName(GENERIC_NAMESPACE, "container-config");
    private static final QName OLD_GENERIC_CONFIG_QNAME = new QName(OLD_GENERIC_NAMESPACE, "container-config");
    private static final String SYSTEM_NAMESPACE = ModuleDocument.type.getDocumentElementName().getNamespaceURI();
    private static final QName SECURITY_QNAME = GerSecurityDocument.type.getDocumentElementName();
    private final String configNamespace;
    private final String namespace;
    private final String element;
    private final Set<String> excludedNamespaces = new HashSet<String>();

    public GenericToSpecificPlanConverter(String configNamespace, String namespace, String element) {
        this.configNamespace = configNamespace;
        this.namespace = namespace;
        this.element = element;
        excludedNamespaces.add("http://geronimo.apache.org/xml/ns/geronimo-jaspi");
        excludedNamespaces.add("http://openejb.apache.org/xml/ns/openejb-jar-2.3");
    }

    public XmlObject convertToSpecificPlan(XmlObject plan) throws DeploymentException {
        XmlCursor rawCursor = plan.newCursor();
        try {
            if (SchemaConversionUtils.findNestedElement(rawCursor, "web-app")) {
                XmlCursor temp = rawCursor.newCursor();
                String namespace = temp.getName().getNamespaceURI();
                temp.dispose();
                if(!namespace.equals(GENERIC_NAMESPACE) && !namespace.equals(this.namespace) && !namespace.equals(OLD_GENERIC_NAMESPACE)) {
                    throw new DeploymentException("Cannot handle web plan with namespace "+namespace+" -- expecting "+GENERIC_NAMESPACE+" or "+this.namespace);
                }

                XmlObject webPlan = rawCursor.getObject().copy();

                XmlCursor cursor = webPlan.newCursor();
                XmlCursor end = cursor.newCursor();
                try {
                    cursor.push();
                    if (cursor.toChild(GENERIC_CONFIG_QNAME) || cursor.toChild(OLD_GENERIC_CONFIG_QNAME)) {
                        XmlCursor source = cursor.newCursor();
                        cursor.push();
                        cursor.toEndToken();
                        cursor.toNextToken();
                        try {
                            if (source.toChild(configNamespace, element)) {
                                source.copyXmlContents(cursor);
                            }

                        } finally {
                            source.dispose();
                        }
                        cursor.pop();
                        cursor.removeXml();
                    }
                    cursor.pop();

                    cursor.push();
                    while (cursor.hasNextToken()) {
                        if (cursor.isStart()) {
                            if (!excludedNamespaces.contains(cursor.getName().getNamespaceURI())
                                    && !SchemaConversionUtils.convertSingleElementToGeronimoSubSchemas(cursor, end)
                                    && !this.namespace.equals(cursor.getName().getNamespaceURI())) {
                                cursor.setName(new QName(this.namespace, cursor.getName().getLocalPart()));
                            }
                        }
                        cursor.toNextToken();
                    }
                    cursor.pop();

                    cursor.push();
                    Map<Object, List<XmlCursor>> map = createElementMap(cursor);
                    cursor.pop();

                    moveToBottom(cursor, map.get("security-realm-name"));
                    moveToBottom(cursor, map.get("authentication"));
                    moveToBottom(cursor, map.get("security"));
                    moveToBottom(cursor, map.get("gbean"));
                    // Convert Persistent Document
                    convertPersistenceSchemaVersion(cursor, map.get("persistence"));
                    moveToBottom(cursor, map.get("persistence"));

                    clearElementMap(map);

                    return webPlan;
                } finally {
                    cursor.dispose();
                    end.dispose();
                }
            } else {
                throw new DeploymentException("No web-app element");
            }
        } finally {
            rawCursor.dispose();
        }
    }

    private static Map<Object, List<XmlCursor>> createElementMap(XmlCursor cursor) {
        Map<Object, List<XmlCursor>> map = new HashMap<Object, List<XmlCursor>>();
        cursor.toStartDoc();
        if (cursor.toFirstChild()) {
            do {
                QName name = cursor.getName();
                List<XmlCursor> locations = map.get(name);
                if (locations == null) {
                    locations = new ArrayList<XmlCursor>();
                    map.put(name, locations);
                    map.put(name.getLocalPart(), locations);
                }
                locations.add(cursor.newCursor());
            } while(cursor.toNextSibling());
        }
        return map;
    }

    private static void clearElementMap(Map<Object, List<XmlCursor>> map) {
        for (Map.Entry<Object, List<XmlCursor>> entry : map.entrySet()) {
            for (XmlCursor cursor : entry.getValue()) {
                cursor.dispose();
            }
        }
        map.clear();
    }

    private static void moveToBottom(XmlCursor cursor, List<XmlCursor> locations) {
        if (locations != null) {
            for (XmlCursor location : locations) {
                cursor.toEndDoc();
                location.moveXml(cursor);
            }
        }
    }

    protected void convertPersistenceSchemaVersion(XmlCursor cursor, List<XmlCursor> locations) {
        if (locations != null) {
            for (XmlCursor location : locations) {
                location.push();
                XmlCursor end = null;
                try {
                    end = location.newCursor();
                    end.toCursor(location);
                    end.toEndToken();
                    SchemaConversionUtils.convertSchemaVersion(location, end, SchemaConversionUtils.JPA_PERSISTENCE_NAMESPACE, "http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd", "2.0");
                } finally {
                    if (end != null) {
                        try {
                            end.dispose();
                        } catch (Exception e) {
                        }
                    }
                    location.pop();
                }
            }
        }
    }
}