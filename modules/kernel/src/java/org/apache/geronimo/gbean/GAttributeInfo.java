/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.gbean;

import java.io.Serializable;

/**
 * Describes an attibute of a GBean.
 *
 * @version $Rev$ $Date$
 */
public class GAttributeInfo implements Serializable {
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
     * Is this attribute readable?
     */
    private final Boolean readable;

    /**
     * Is this attribute writiable?
     */
    private final Boolean writable;

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

    public GAttributeInfo(String name, String type, boolean persistent) {
        this(name, type, persistent, null, null, null, null);
    }

    public GAttributeInfo(String name, String type, boolean persistent, String getterName, String setterName) {
        this(name, type, persistent, Boolean.valueOf(getterName != null), Boolean.valueOf(setterName != null), getterName, setterName);
    }

    public GAttributeInfo(String name, String type, boolean persistent, Boolean readable, Boolean writable, String getterName, String setterName) {
        this.name = name;
        this.type = type;
        this.persistent = persistent;
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

    public Boolean isReadable() {
        return readable;
    }

    public Boolean isWritable() {
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
                " readable=" + readable +
                " writable=" + writable +
                " getterName=" + getterName +
                " setterName=" + setterName +
                "]";
    }
}
