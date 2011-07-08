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
package org.apache.geronimo.security.deploy;

import java.beans.PropertyEditorManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.geronimo.common.propertyeditor.PropertyEditorException;
import org.apache.geronimo.common.propertyeditor.TextPropertyEditorSupport;

/**
 * @version $Rev$ $Date$
 */
public class MapOfSets extends HashMap {

    public MapOfSets() {
        super();
    }

    public MapOfSets(int size) {
        super(size);
    }

    public MapOfSets(Map map) {
        super(map);
    }

    static {
        PropertyEditorManager.registerEditor(MapOfSets.class, MapOfSetsEditor.class);
    }

    public static class MapOfSetsEditor extends TextPropertyEditorSupport {

        public void setAsText(String text) {
            if (text != null) {
                try {
                    ByteArrayInputStream is = new ByteArrayInputStream(text.getBytes());
                    Properties p = new Properties();
                    p.load(is);

                    Map result = new MapOfSets(p.size());
                    for (Iterator iterator = p.entrySet().iterator(); iterator.hasNext();) {
                        Map.Entry entry = (Map.Entry) iterator.next();
                        Set values = new HashSet(Arrays.asList(((String) entry.getValue()).split(",")));
                        result.put(entry.getKey(), values);
                    }
                    setValue(result);
                } catch (IOException e) {
                    throw new PropertyEditorException(e);
                }
            } else {
                setValue(null);
            }
        }

        public String getAsText() {
            Map map = (Map) getValue();
            if (map == null) {
                return null;
            }
            StringBuilder text = new StringBuilder();
            for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                text.append(entry.getKey()).append("=");
                Set values = (Set) entry.getValue();
                for (Iterator iterator1 = values.iterator(); iterator1.hasNext();) {
                    String value = (String) iterator1.next();
                    text.append(value);
                    if (iterator1.hasNext()) {
                        text.append(",");
                    }
                }
            }
            return text.toString();
        }

    }
}
