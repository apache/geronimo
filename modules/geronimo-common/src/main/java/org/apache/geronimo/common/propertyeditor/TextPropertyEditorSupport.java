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

package org.apache.geronimo.common.propertyeditor;

import java.beans.PropertyEditorSupport;

/**
 * A base class for text based properties.  This class basically does nothing
 * but override the defaute PropertyEditorSupport setAsText() method, which
 * throws an IllegalArgumentException when called.
 *
 * @version $Rev$
 */
public class TextPropertyEditorSupport extends PropertyEditorSupport {
    /**
     * A property editor with a provided source object.
     *
     * @param source The source of the editted information.
     */
    protected TextPropertyEditorSupport(Object source) {
        super(source);
    }

    /**
     * Default no-argument constructor.
     */
    protected TextPropertyEditorSupport() {
        super();
    }

    /**
     * By default, set the property value by directly passing the
     * provided string value through to the setValue() method.
     *
     * @param value The new property value, as a string.
     */
    public void setAsText(String value) {
        setValue(value);
    }
}
