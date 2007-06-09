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

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlCursor;

/**
 * @version $Rev$ $Date$
 */
public class SecurityElementConverter implements ElementConverter {

    public static final String GERONIMO_SECURITY_NAMESPACE = "http://geronimo.apache.org/xml/ns/security-2.0";
    private static final QName PRINCIPAL_QNAME = new QName(GERONIMO_SECURITY_NAMESPACE, "principal");
    private static final QName REALM_NAME_QNAME = new QName("realm-name");
    private static final QName DESIGNATED_RUN_AS = new QName("designated-run-as");

    public void convertElement(XmlCursor cursor, XmlCursor end) {
        cursor.push();
        end.toCursor(cursor);
        end.toEndToken();
        while (cursor.hasNextToken() && cursor.isLeftOf(end)) {
            if (cursor.isStart()) {
                if (GERONIMO_SECURITY_NAMESPACE.equals(cursor.getName().getNamespaceURI())) {
                    break;
                }
                cursor.setName(new QName(GERONIMO_SECURITY_NAMESPACE, cursor.getName().getLocalPart()));

            }
            cursor.toNextToken();
        }
        cursor.pop();
        XmlCursor source = null;
        try {
            while (cursor.hasNextToken() && cursor.isLeftOf(end)) {
                if (cursor.isStart()) {
                    String localPart = cursor.getName().getLocalPart();
                    if (localPart.equals("realm")) {
                        if (source == null) {
                            source = cursor.newCursor();
                        } else {
                            source.toCursor(cursor);
                        }
                        cursor.push();
                        cursor.toEndToken();
                        cursor.toNextToken();
                        if (source.toChild(PRINCIPAL_QNAME)) {
                            do {
                                source.removeAttribute(DESIGNATED_RUN_AS);
                                source.copyXml(cursor);
                            } while (source.toNextSibling(PRINCIPAL_QNAME));
                        }

                        cursor.pop();
                        cursor.removeXml();
                    } else if (localPart.equals("default-subject")) {
//                    cursor.removeAttribute(REALM_NAME_QNAME);
                        cursor.toEndToken();
                    } else if (localPart.equals("default-principal")) {
                        cursor.removeXml();
                    } else if (localPart.equals("principal")) {
                        cursor.removeAttribute(DESIGNATED_RUN_AS);
                    } else if (localPart.equals("run-as-subject")) {
                        cursor.toEndToken();
                    }
                }
                cursor.toNextToken();
            }
        } finally {
            if (source != null) {
                source.dispose();
            }
        }
    }
}
