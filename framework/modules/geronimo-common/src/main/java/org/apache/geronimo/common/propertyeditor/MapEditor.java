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
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Map;

import org.apache.geronimo.crypto.EncryptionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A property editor for {@link java.util.Map}.
 * 
 * @version $Rev$ $Date$
 */
public class MapEditor extends TextPropertyEditorSupport {
    private static final Logger log = LoggerFactory.getLogger(MapEditor.class);

    /**
     * 
     * @throws PropertyEditorException
     *             An IOException occured.
     */
    @SuppressWarnings("rawtypes")
    public void setAsText(String text) {
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(text == null ? new byte[0] : text.getBytes());
            Properties p = new Properties();
            p.load(is);
            Iterator it = p.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next().toString();
                if (key.toLowerCase().endsWith("password")) {
                    String encryptedValue = (String) p.get(key);
                    String decryptedValue = (String) EncryptionManager.decrypt(encryptedValue);
                    p.put(key, decryptedValue);
                }
            }
            setValue((Map) p);
        } catch (IOException e) {
            throw new PropertyEditorException(e.getMessage(), e);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public String getAsText() {
        Map map = (Map) getValue();
        if (!(map instanceof Properties)) {
            Properties p = new Properties();
            if (map != null) {
                if (!map.containsKey(null) && !map.containsValue(null)) {
                    p.putAll(map);
                } else {
                    // Map contains null keys or values. Replace null with empty string.
                    log.warn("Map contains null keys or values.  Replacing null values with empty string.");
                    for (Iterator itr = map.entrySet().iterator(); itr.hasNext();) {
                        Map.Entry entry = (Map.Entry) itr.next();
                        Object key = entry.getKey();
                        Object value = entry.getValue();
                        if (key == null) {
                            key = "";
                        }
                        if (value == null) {
                            value = "";
                        }
                        p.put(key, value);
                    }
                }
                Iterator it = p.keySet().iterator();
                while (it.hasNext()) {
                    String key = it.next().toString();
                    if (key.toLowerCase().endsWith("password")) {
                        String value = (String) p.get(key);
                        String encryptedValue = EncryptionManager.encrypt(value);
                        p.put(key, encryptedValue);
                    }
                }
                map = p;
            }
        }
        PropertiesEditor pe = new PropertiesEditor();
        pe.setValue(map);
        return pe.getAsText();
    }
}
