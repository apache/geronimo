/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.jmxdebug.web.beanlib;

import java.beans.PropertyEditor;
import javax.management.MBeanAttributeInfo;

import org.apache.geronimo.common.propertyeditor.PropertyEditors;

/**
 * @version $Rev$ $Date$
 */
public class AttributeData {
    private final String name;
    private final String value;
    private final String type;
    private final boolean readable;
    private final boolean writable;

    public AttributeData(String name, String value, String type, boolean readable, boolean writable) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.readable = readable;
        this.writable = writable;
    }

    public AttributeData(MBeanAttributeInfo attributeInfo, Object value, ClassLoader cl) throws ClassNotFoundException {
        this.name = attributeInfo.getName();
        PropertyEditor editor = PropertyEditors.findEditor(attributeInfo.getType(), cl);
        editor.setValue(value);
        this.value = editor.getAsText();
        this.type = attributeInfo.getType();
        this.readable = attributeInfo.isReadable();
        this.writable = attributeInfo.isWritable();
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public boolean isReadable() {
        return readable;
    }

    public boolean isWritable() {
        return writable;
    }
}
