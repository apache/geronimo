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
package org.apache.geronimo.axis.server;

import java.io.Serializable;

import javax.xml.namespace.QName;

import org.apache.axis.description.FieldDesc;
import org.apache.axis.description.TypeDesc;

/**
 * @version $Rev:  $ $Date:  $
 */
public class TypeDescInfo implements Serializable {
    private final Class clazz;
    private final boolean canSearchParents;
    private final QName xmlType;
    private final FieldDesc[] fields;
    
    public TypeDescInfo(Class clazz, boolean canSearchParents, QName xmlType, FieldDesc[] fields) {
        this.clazz = clazz;
        this.canSearchParents = canSearchParents;
        this.xmlType = xmlType;
        this.fields = fields;
    }

    public boolean isCanSearchParents() {
        return canSearchParents;
    }

    public Class getClazz() {
        return clazz;
    }

    public FieldDesc[] getFields() {
        return fields;
    }

    public QName getXmlType() {
        return xmlType;
    }
    
    public TypeDesc buildTypeDesc() {
        TypeDesc typeDesc = new TypeDesc(clazz, canSearchParents);
        typeDesc.setXmlType(xmlType);
        typeDesc.setFields(fields);
        return typeDesc;
    }
}
