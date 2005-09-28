/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.web.deployment;

import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.xbeans.ConfigurationDocument;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.geronimo.security.GerSecurityDocument;
import org.apache.geronimo.xbeans.geronimo.web.GerWebAppDocument;
import org.apache.xmlbeans.XmlCursor;

/**
 * @version $Rev:  $ $Date:  $
 */
public class GenericToSpecificPlanConverter {

    private static final QName GENERIC_QNAME = GerWebAppDocument.type.getDocumentElementName();
    private static final String GENERIC_NAMESPACE = GENERIC_QNAME.getNamespaceURI();
    private static final QName GENERIC_CONFIG_QNAME = new QName(GENERIC_NAMESPACE, "container-config");
    private static final String SYSTEM_NAMESPACE = ConfigurationDocument.type.getDocumentElementName().getNamespaceURI();
    private static final QName SECURITY_QNAME = GerSecurityDocument.type.getDocumentElementName();
    private final String configNamespace;
    private final String namespace;

    public GenericToSpecificPlanConverter(String configNamespace, String namespace) {
        this.configNamespace = configNamespace;
        this.namespace = namespace;
    }

    public void convertToSpecificPlan(XmlCursor cursor) throws DeploymentException {
        if (SchemaConversionUtils.findNestedElement(cursor, "web-app")) {
            cursor.push();
            if (cursor.getName().equals(GENERIC_QNAME)) {
                if (cursor.toChild(GENERIC_CONFIG_QNAME)) {
                    XmlCursor source = cursor.newCursor();
                    cursor.push();
                    cursor.toEndToken();
                    cursor.toNextToken();
                    try {
                        if (source.toChild(configNamespace, "jetty")) {
                            source.copyXmlContents(cursor);
                        }

                    } finally {
                        source.dispose();
                    }
                    cursor.pop();
                    cursor.removeXml();
                }
                cursor.toStartDoc();
                while (cursor.hasNextToken()) {
                    if (cursor.isStart()) {
                        if (namespace.equals(cursor.getName().getNamespaceURI())) {
                            //already has correct schema, exit
                            break;
                        }
                        cursor.setName(new QName(namespace, cursor.getName().getLocalPart()));
                        cursor.toNextToken();
                    } else {
                        cursor.toNextToken();
                    }
                }
            }
            //move security elements after refs
            SchemaConversionUtils.convertToGeronimoSubSchemas(cursor);

            cursor.pop();
            cursor.push();
            if (cursor.toChild(namespace, "security-realm-name")) {
                XmlCursor other = cursor.newCursor();
                try {
                    other.toParent();
                    if (other.toChild(SYSTEM_NAMESPACE, "gbean")) {
                        other.toPrevToken();
                    } else {
                        other.toEndToken();
                        other.toPrevToken();
                    }
                    cursor.moveXml(other);
                    cursor.pop();
                    cursor.push();
                    if (cursor.toChild(SECURITY_QNAME)) {
                        cursor.moveXml(other);
                    }
                } finally {
                    other.dispose();
                }
            }
            cursor.pop();
        } else {
            throw new DeploymentException("No web-app element");
        }
    }

}