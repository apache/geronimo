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
import java.util.Map;

/**
 * A property editor for {@link java.util.Properties}.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 02:56:06 -0700 (Tue, 14 Sep 2004) $
 */
public class MapEditor
   extends TextPropertyEditorSupport
{
    /**
     *
     * @throws PropertyEditorException  An IOException occured.
     */
    public void setAsText(String text) {
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(text == null? new byte[0]: text.getBytes());
            Properties p = new Properties();
            p.load(is);
            
            setValue((Map)p);
        } catch (IOException e) {
            throw new PropertyEditorException(e);
        }
    }

    public String getAsText() {
        Map map = (Map) getValue();
        if (!(map instanceof Properties)) {
            Properties p = new Properties();
            if (map != null) {
                p.putAll(map);
            }
            map = p;
        }
        return map.toString();
    }
}
