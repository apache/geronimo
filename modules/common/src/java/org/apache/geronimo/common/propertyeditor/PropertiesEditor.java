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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * A property editor for {@link Properties}.
 *
 * @version $Rev$ $Date$
 */
public class PropertiesEditor
   extends TextPropertyEditorSupport
{
    /**
     * Returns a Properties object initialized with the input object
     * as a properties file based string.
     *
     * @return a Properties object
     *
     * @throws PropertyEditorException  An IOException occured.
     */
    public Object getValue()
    {
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(getAsText().getBytes());
            Properties p = new Properties();
            p.load(is);
            
            return p;
        }
        catch (IOException e) {
            throw new PropertyEditorException(e);
        }
    }
}
