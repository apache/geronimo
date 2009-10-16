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

import java.text.DateFormat;
import java.text.ParseException;

/**
 * A property editor for Date typed properties.
 *
 * @version $Rev$
 */
public class DateEditor extends TextPropertyEditorSupport {
    /**
     * Convert the text value of the property into a Date object instance.
     *
     * @return a Date object constructed from the property text value.
     * @throws PropertyEditorException Unable to parse the string value into a Date.
     */
    public Object getValue() {
        try {
            // Get a date formatter to parse this.
            // This retrieves the formatter using the current execution locale,
            // which could present an intererting problem when applied to deployment
            // plans written in other locales.  Sort of a Catch-22 situation.
            DateFormat formatter = DateFormat.getDateInstance();
            return formatter.parse(getAsText().trim());
        } catch (ParseException e) {
            // any format errors show up as a ParseException, which we turn into a PropertyEditorException.
            throw new PropertyEditorException(e.getMessage(), e);
        }
    }
}
