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

import java.beans.PropertyEditor;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Abstract superclass for map editor tests.  Implement the two abstract methods and initialize
 * editor in the setUp() method.
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractMapEditorTest extends TestCase {
    PropertyEditor editor;

    public void testRoundTrip() {
        Map map = createMap();
        map.put("key1","value1");
        map.put("key2","value2");
        editor.setValue(map);
        String text = editor.getAsText();
        editor.setAsText(text);
        Map result = (Map) editor.getValue();
        assertEquals(2, result.size());
        assertEquals("value1", result.get("key1"));
        assertEquals("value2", result.get("key2"));
    }

    protected abstract void checkType(Object output);

    protected abstract Map createMap();
}
