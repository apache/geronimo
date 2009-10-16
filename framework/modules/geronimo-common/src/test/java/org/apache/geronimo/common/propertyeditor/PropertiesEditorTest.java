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

import java.util.Map;
import java.util.Properties;

/**
 * Unit test for {@link PropertiesEditor} class.
 *
 * @version $Rev$ $Date$
 */
public class PropertiesEditorTest extends AbstractMapEditorTest {

    protected void setUp() {
        editor = PropertyEditors.findEditor(Properties.class);
    }

    public void testEditorClass() throws Exception {
        assertEquals(PropertiesEditor.class, editor.getClass());
    }

    protected void checkType(Object output) {
        assertTrue("editor returned a: " + output.getClass(), output instanceof Properties);
    }

    public void testGetValue_1Item() {
        String input = "key1=value1";

        editor.setAsText(input);
        Object output = editor.getValue();

        assertNotNull(output);
        checkType(output);

        Map map = (Map) output;
        assertEquals(1, map.size());
        assertEquals("value1", map.get("key1"));
    }

    public void testGetValue_2Items() {
        String input = "key1=value1\nkey2=value2";

        editor.setAsText(input);
        Object output = editor.getValue();

        assertNotNull(output);
        checkType(output);

        Map map = (Map) output;
        assertEquals(2, map.size());
        assertEquals("value1", map.get("key1"));
        assertEquals("value2", map.get("key2"));
    }
    
    protected Map createMap() {
        return new Properties();
    }
}
