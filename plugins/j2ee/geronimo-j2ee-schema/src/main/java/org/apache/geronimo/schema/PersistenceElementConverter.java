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
public class PersistenceElementConverter implements ElementConverter {

    private final String namespace = SchemaConversionUtils.JPA_PERSISTENCE_NAMESPACE;
    private final String version;

    public PersistenceElementConverter(String version) {
        this.version = version;
    }

    public void convertElement(XmlCursor cursor, XmlCursor end) {
        boolean isFirstStart = true;
        end.toCursor(cursor);
        end.toEndToken();
        while (cursor.hasNextToken() && cursor.isLeftOf(end)) {
            if (cursor.isStart()) {
                if (isFirstStart) {
                    if (version != null) {
                        cursor.setAttributeText(new QName("version"), version);
                    }
                    isFirstStart = false;
                }
                if (!namespace.equals(cursor.getName().getNamespaceURI())) {
                    cursor.setName(new QName(namespace, cursor.getName().getLocalPart()));
                }
            }
            cursor.toNextToken();
        }
    }
}
