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
import java.util.Arrays;

import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.crypto.EncryptionManager;

/**
 * Describes an attibute of a GBean.
 *
 * @version $Rev$ $Date$
 */
public class GAttributeInfo implements Serializable {
    private static final long serialVersionUID = 2805493042418685048L;

    /**
     * Name of this attribute.
     */
    private final String name;

    /**
     * Type of this attribute.
     */
    private final String type;

    /**
     * Is this attribute persistent?
     */
    private final boolean persistent;

    /**
     * Is this attribute manageable?
     */
    private final boolean manageable;
    
    /**
     * Does this attribute need to be encrypted when persisted?
     */
    private final boolean encrypted;

    /**
     * Is this attribute readable?
     */
    private final boolean readable;

    /**
     * Is this attribute writiable?
     */
    private final boolean writable;

    /**
     * Name of the getter method.
     * The default is "get" + name.  In the case of a defualt value we do a caseless search for the name.
     */
    private final String getterName;

    /**
     * Name of the setter method.
     * The default is "set" + name.  In the case of a defualt value we do a caseless search for the name.
     */
    private final String setterName;
    
    /**
     * Determines whether the given attribute need to be encrypted by default.
     * 
     * @param name - Name of the attribute
     * @param type - Type of the attribute
     * @return
     */
    private static boolean defaultEncrypted(String name, String type) {
        if (name != null && (name.toLowerCase().contains("password") || name.toLowerCase().contains("keystorepass")) && "java.lang.String".equals(type)) {
            return true;
        } else {
           return false;
        }
    }

    public GAttributeInfo(String name, String type, boolean persistent, boolean manageable, String getterName, String setterName) {
        this(name, type, persistent, manageable, getterName != null, setterName != null, getterName, setterName);
    }

    public GAttributeInfo(String name, String type, boolean persistent, boolean manageable, boolean readable, boolean writable, String getterName, String setterName) {
        this(name, type, persistent, manageable, defaultEncrypted(name, type), readable, writable, getterName,
                setterName);       
    }

    public GAttributeInfo(String name, String type, boolean persistent, boolean manageable, boolean encrypted, String getterName, String setterName) {
        this(name, type, persistent, manageable, encrypted, getterName != null, setterName != null, getterName,
                setterName);
    }

    public GAttributeInfo(String name, String type, boolean persistent, boolean manageable, boolean encrypted, boolean readable, boolean writable, String getterName, String setterName) {
        if (encrypted && !"java.lang.String".equals(type)) {
            throw new IllegalArgumentException("Only attributes of String type can be encrypted.");
        }
        this.name = name;
        this.type = type;
        this.persistent = persistent;
        //non persistent attributes cannot be manageable
        this.manageable = manageable & persistent;
        this.encrypted = encrypted;
        this.readable = readable;
        this.writable = writable;
        this.getterName = getterName;
        this.setterName = setterName;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public boolean isManageable() {
        return manageable;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public boolean isReadable() {
        return readable;
    }

    public boolean isWritable() {
        return writable;
    }

    public String getGetterName() {
        return getterName;
    }

    public String getSetterName() {
        return setterName;
    }

    public String toString() {
        return "[GAttributeInfo: name=" + name +
                 " type=" + type +
                 " persistent=" + persistent +
                 " manageable=" + manageable +
                 " encrypted=" + encrypted +
                 " readable=" + readable +
                 " writable=" + writable +
                 " getterName=" + getterName +
                 " setterName=" + setterName +
                 "]";
    }

    public String toXML(AbstractName abstractName) {
        StringBuilder xml = new StringBuilder();

        xml.append("<gAttributeInfo ");
        xml.append("name='" + name + "' ");
        xml.append("type='" + type + "' ");
        xml.append("persistent='" + persistent + "' ");
        xml.append("manageable='" + manageable + "' ");
        xml.append("encrypted='" + encrypted + "' ");
        xml.append("readable='" + readable + "' ");
        xml.append("writable='" + writable + "' ");
        xml.append(">");

        xml.append("<getterName>" + getterName + "</getterName>");
        xml.append("<setterName>" + setterName + "</setterName>");

        if (readable) {
            try {
                Object value = KernelRegistry.getSingleKernel().getAttribute(abstractName, name);
                if (value != null) {
                    if (value instanceof String[]) {
                        for (String valueString : Arrays.asList((String[]) value)) {
                            xml.append("<value>" + valueString + "</value>");
                        }
                    } else {
                        if (encrypted && value instanceof String) {
                            value = EncryptionManager.encrypt((String) value);
                        }
                        xml.append("<value>" + value + "</value>");
                    }
                }
            } catch (Exception e) {

            }
        }

        xml.append("</gAttributeInfo>");

        return xml.toString();
    }
}
