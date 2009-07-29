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

import org.apache.geronimo.gbean.annotation.EncryptionSetting;
import org.apache.geronimo.kernel.KernelRegistry;

import java.io.Serializable;

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
    private final EncryptionSetting encrypted;

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

    public GAttributeInfo(String name, String type, boolean persistent, boolean manageable, String getterName, String setterName) {
        this(name, type, persistent, manageable, getterName != null, setterName != null, getterName, setterName);
    }

    public GAttributeInfo(String name, String type, boolean persistent, boolean manageable, boolean readable, boolean writable, String getterName, String setterName) {
        this(name, type, persistent, manageable, EncryptionSetting.defaultEncryption(name, type), readable, writable, getterName,
                setterName);
    }

    public GAttributeInfo(String name, String type, boolean persistent, boolean manageable, boolean encrypted, String getterName, String setterName) {
        this(name, type, persistent, manageable, encrypted ? EncryptionSetting.ENCRYPTED : EncryptionSetting.PLAINTEXT, getterName != null, setterName != null, getterName,
                setterName);
    }

    public GAttributeInfo(String name, String type, boolean persistent, boolean manageable, EncryptionSetting encrypted, String getterName, String setterName) {
        this(name, type, persistent, manageable, encrypted, getterName != null, setterName != null, getterName,
                setterName);
    }

    public GAttributeInfo(String name, String type, boolean persistent, boolean manageable, EncryptionSetting encrypted, boolean readable, boolean writable, String getterName, String setterName) {
        if (encrypted == null) throw new NullPointerException("enctryption must be specified");
        if (encrypted == EncryptionSetting.ENCRYPTED && !"java.lang.String".equals(type)) {
            throw new IllegalArgumentException("Only attributes of String type can be encrypted.");
        }
        if (encrypted == EncryptionSetting.DEFAULT) {
            encrypted = EncryptionSetting.defaultEncryption(name, type);
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
        return encrypted == EncryptionSetting.ENCRYPTED;
    }

    public EncryptionSetting getEncryptedSetting() {
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
        xml.append("name='").append(name).append("' ");
        xml.append("type='").append(type).append("' ");
        xml.append("persistent='").append(persistent).append("' ");
        xml.append("manageable='").append(manageable).append("' ");
        xml.append("encrypted='").append(encrypted).append("' ");
        xml.append("readable='").append(readable).append("' ");
        xml.append("writable='").append(writable).append("' ");
        xml.append(">");

        xml.append("<getterName>").append(getterName).append("</getterName>");
        xml.append("<setterName>").append(setterName).append("</setterName>");

        if (readable) {
            try {
                Object value = KernelRegistry.getSingleKernel().getAttribute(abstractName, name);
                if (value != null) {
                    if (value instanceof String[]) {
                        for (String valueString : (String[])value) {
                            xml.append("<value>").append(valueString).append("</value>");
                        }
                    } else {
                        value = encrypted.encrypt((String) value);
                        xml.append("<value>").append(value).append("</value>");
                    }
                }
            } catch (Exception e) {
                xml.append("<value>[could not be determined:").append(e.getMessage()).append("]</value>");
            }
        }

        xml.append("</gAttributeInfo>");

        return xml.toString();
    }
}
