/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.properties;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.geronimo.interop.SystemException;


public class PropertyMap extends HashMap {
    public PropertyMap() {
        super();
    }

    public PropertyMap(Map map) {
        super(map);
    }

    public Object put(Object key, Object value) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        if (value == null) {
            // Avoid exception in getProperties (Properties can't take null value)
            throw new NullPointerException("value");
        }
        return super.put(key, value);
    }

    public String getProperty(String name) {
        return (String) super.get(name);
    }

    public String getProperty(String name, String defaultValue) {
        String value = getProperty(name);
        if (value == null || value.length() == 0) {
            value = defaultValue;
        }
        return value;
    }

    public Properties getProperties() {
        Properties props = new Properties();
        for (Iterator i = entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            props.put(key, value);
        }
        return props;
    }

    public static PropertyMap readFile(String fileName) {
        try {
            Properties props = new Properties();
            BufferedInputStream input = new BufferedInputStream(new FileInputStream(fileName));
            props.load(input);
            input.close();
            PropertyMap map = new PropertyMap();
            for (Iterator i = props.entrySet().iterator(); i.hasNext();) {
                Map.Entry entry = (Map.Entry) i.next();
                String key = entry.getKey().toString().trim();
                String value = entry.getValue().toString().trim();
                map.put(key, value);
            }
            return map;
        } catch (Exception ex) {
            throw new SystemException(ex);
        }
    }
}
