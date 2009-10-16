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

package org.apache.geronimo.gbean;

import java.io.Serializable;


/**
 * @version $Rev$ $Date$
 */
public class GReferenceInfo implements Serializable {
    private static final long serialVersionUID = 8817036672214905192L;

    /**
     * Name of this reference.
     */
    private final String name;

    /**
     * Type of this reference.
     */
    private final String referenceType;

    /**
     * Type of the proxy injected into the bean.
     */
    private final String proxyType;

    /**
     * Name of the setter method.
     */
    private final String setterName;

    /**
     * String for type component when constructing reference patterns. For jsr-77 this maps to j2eeType=nameTypeName
     */
    private final String nameTypeName;

    public GReferenceInfo(String name, String referenceType, String proxyType, String setterName, String nameTypeName) {
        this.name = name;
        this.referenceType = referenceType;
        this.setterName = setterName;
        this.proxyType = proxyType;
        this.nameTypeName = nameTypeName;
    }

    public String getName() {
        return name;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public String getProxyType() {
        return proxyType;
    }

    public String getSetterName() {
        return setterName;
    }

    public String getNameTypeName() {
        return nameTypeName;
    }

    public String toString() {
        return "[GReferenceInfo: name=" + name +
                " referenceType=" + referenceType +
                " proxyType=" + proxyType +
                " setterName=" + setterName +
                " naming system type name= " + nameTypeName +
                "]";
    }
    
    public String toXML() {
        StringBuilder xml = new StringBuilder();
        
        xml.append("<gReferenceInfo ");
        xml.append("name='" + name + "' ");
        xml.append("referenceType='" + referenceType + "' ");
        xml.append("proxyType='" + proxyType + "' ");
        xml.append("setterName='" + setterName + "' ");
        xml.append("namingSystem='" + nameTypeName + "' ");
        xml.append("/>");
        
        return xml.toString();
    }
}
