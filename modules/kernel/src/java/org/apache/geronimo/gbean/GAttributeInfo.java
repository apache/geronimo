/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.gbean;

import java.io.Serializable;

/**
 * Describes an attibute of a GMBean.
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/12 01:38:55 $
 */
public class GAttributeInfo implements Serializable {
    /**
     * Name of this attribute.
     */
    private final String name;

    /**
     * Is this attribute persistent?
     */
    private final boolean persistent;

    /**
     * A user displayable descrption of this attribute.
     */
    private final String description;

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

    public GAttributeInfo(String name) {
        this(name, false, null, null, null, null, null);
    }

    public GAttributeInfo(String name, boolean persistent) {
        this(name, persistent, null, null, null, null, null);
    }

    public GAttributeInfo(String name, boolean persistent, String description) {
        this(name, persistent, description, null, null, null, null);
    }

    public GAttributeInfo(String name, boolean persistent, String description, Boolean readable, Boolean writable) {
        this(name, persistent, description, readable, writable, null, null);
    }

    public GAttributeInfo(String name, boolean persistent, String description, Boolean readable, Boolean writable, String getterName, String setterName) {
        this.name = name;
        this.persistent = persistent;
        this.description = description;
        this.readable = readable;
        this.writable = writable;
        this.getterName = getterName;
        this.setterName = setterName;
    }

    public String getName() {
        return name;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public String getDescription() {
        return description;
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
                " persistent=" + persistent +
                " description=" + description +
                " readable=" + readable +
                " writable=" + writable +
                " getterName=" + getterName +
                " setterName=" + setterName +
                "]";
    }
}
