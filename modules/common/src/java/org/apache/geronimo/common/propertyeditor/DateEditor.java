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

package org.apache.geronimo.common.propertyeditor;

import java.text.DateFormat;
import java.text.ParseException;

/**
 * A property editor for {@link Date}.
 *
 * @version $Rev$ $Date$
 */
public class DateEditor
    extends TextPropertyEditorSupport
{
    /**
     * Returns a Date for the input object converted to a string.
     *
     * @return a Date object
     */
    public Object getValue()
    {
        try {
            DateFormat df = DateFormat.getDateInstance();
            return df.parse(getAsText());
        }
        catch (ParseException e) {
            throw new PropertyEditorException(e);
        }
    }
}
