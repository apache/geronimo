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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * A property editor for indirect property bundles.  This editor
 * transforms the text value of the propery into a Property resource bundle.
 *
 * @version $Rev$
 */
public class PropertiesEditor extends TextPropertyEditorSupport {
    /**
     * Treats the text value of this property as an input stream that
     * is converted into a Property bundle.
     *
     * @return a Properties object
     * @throws PropertyEditorException An error occurred creating the Properties object.
     */
    public Object getValue() {
        Object currentValue = super.getValue();
        if (currentValue instanceof Properties) {
            return (Properties) currentValue;
        } else {
            // convert the text value into an in-memory input stream we can used for
            // property loading.
            ByteArrayInputStream stream = new ByteArrayInputStream(currentValue.toString().getBytes());
            // load this into a properties instance.
            Properties bundle = new Properties();
            try {
                bundle.load(stream);
            } catch (IOException e) {
                // any errors here are just a property exception
                throw new PropertyEditorException(e.getMessage(), e);
            }
            return bundle;
        }
    }

    /**
     * Provides a String version of a Properties object suitable
     * for loading into a Properties table using the load method.
     *
     * @return The String value of the Properties object as created
     *         by the store method.
     * @throws PropertyEditorException An error occurred converting the Properties object
     * @see Properties#store(java.io.OutputStream, String)
     */
    public String getAsText() {
        Object value = getValue();
        if (value instanceof Properties) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ((Properties) value).store(baos, null);
            } catch (IOException e) {
                // any errors here are just a property exception
                throw new PropertyEditorException(e.getMessage(), e);
            }
            return baos.toString();
        }
        return ("" + value);
    }

}
