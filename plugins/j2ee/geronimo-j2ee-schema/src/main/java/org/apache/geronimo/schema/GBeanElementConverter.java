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
public class GBeanElementConverter implements ElementConverter {

    private static final String GERONIMO_SERVICE_NAMESPACE = "http://geronimo.apache.org/xml/ns/deployment-1.2";


    public void convertElement(XmlCursor cursor, XmlCursor end) {
        end.toCursor(cursor);
        end.toEndToken();
        while (cursor.hasNextToken() && cursor.isLeftOf(end)) {
            if (cursor.isStart()) {
                String localPart = cursor.getName().getLocalPart();
                if (!GERONIMO_SERVICE_NAMESPACE.equals(cursor.getName().getNamespaceURI())) {
                    cursor.setName(new QName(GERONIMO_SERVICE_NAMESPACE, localPart));
                }
                if (localPart.equals("xml-attribute") || localPart.equals("xml-reference")) {
                    cursor.toEndToken();
                }
            }
            //this should not break because the xml-* elements are never top level.
            cursor.toNextToken();
        }
    }
}
