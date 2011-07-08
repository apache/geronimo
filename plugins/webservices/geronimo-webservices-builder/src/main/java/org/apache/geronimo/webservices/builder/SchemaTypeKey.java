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
package org.apache.geronimo.webservices.builder;

import javax.xml.namespace.QName;

/**
 * @version $Rev$ $Date$
 */
public final class SchemaTypeKey {
    public static final String QNAME_SCOPE_COMPLEX_TYPE = "complexType";
    public static final String QNAME_SCOPE_SIMPLE_TYPE = "simpleType";
    public static final String QNAME_SCOPE_ELEMENT = "element";

    private final QName qName;
    private final String qNameScope;
    private final boolean isElement;
    private final boolean isSimpleType;
    private final boolean isAnonymous;

    private final QName elementQName;


    public SchemaTypeKey(QName qName, boolean element, boolean isSimpleType, boolean anonymous, QName elementQName) {
        assert qName != null;
        this.qName = qName;
        isElement = element;
        this.isSimpleType = isSimpleType;
        isAnonymous = anonymous;
        if (isElement) {
            qNameScope = QNAME_SCOPE_ELEMENT;
        } else if (isSimpleType) {
            qNameScope = QNAME_SCOPE_SIMPLE_TYPE;
        } else {
            qNameScope = QNAME_SCOPE_COMPLEX_TYPE;
        }
        this.elementQName = elementQName;
    }

    public QName getqName() {
        return qName;
    }

    public boolean isElement() {
        return isElement;
    }

    public boolean isSimpleType() {
        return isSimpleType;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public String getqNameScope() {
        return qNameScope;
    }

    public QName getElementQName() {
        return elementQName;
    }

    public int hashCode() {
        return qName.hashCode();
    }

    public boolean equals(Object other) {
        if (!(other instanceof SchemaTypeKey)) {
            return false;
        }
        SchemaTypeKey key = (SchemaTypeKey) other;
        return isElement == key.isElement && isSimpleType == key.isSimpleType && isAnonymous == key.isAnonymous && qName.equals(key.qName);
    }

    public String toString() {
        StringBuilder buf = new StringBuilder("\nSchemaTypeKey: scope: ").append(qNameScope);
        buf.append(" isElement: ").append(isElement);
        buf.append(" isAnonymous: ").append(isAnonymous);
        buf.append(" isSimpleType: ").append(isSimpleType);
        buf.append("\n QName: ").append(qName).append("\n");
        return buf.toString();
    }


}
