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
import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestCase;

/**
 * Abstract superclass for collection editor tests.  Implement the two abstract methods and initialize
 * editor and ordered in the setUp() method.
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractCollectionEditorTest extends TestCase {
    PropertyEditor editor;
    boolean ordered;

    public void testGetValue_1Item() {
        String input = "item";

        editor.setAsText(input);
        Object output = editor.getValue();

        assertNotNull(output);
        checkType(output);

        Collection collection = (Collection) output;
        assertEquals(1, collection.size());
        assertEquals(input, collection.iterator().next());
    }

    public void testGetValue_2Items() {
        String input = "item1, item2";

        editor.setAsText(input);
        Object output = editor.getValue();

        assertNotNull(output);
        checkType(output);

        Collection collection = (Collection) output;
        checkContents(collection);
    }

    public void testEmpty() {
        String input = "[]";
        editor.setAsText(input);
        Object output = editor.getValue();
        assertNotNull(output);
        checkType(output);
        Collection collection = (Collection) output;
        assertEquals(0, collection.size());
    }

    private void checkContents(Collection collection) {
        assertEquals(2, collection.size());
        Iterator iterator = collection.iterator();
        if (ordered) {
            assertEquals("item1", iterator.next());
            assertEquals("item2", iterator.next());
        } else {
            Object item1 = iterator.next();
            Object item2 = iterator.next();
            assertTrue((item1.equals("item1") && item2.equals("item2")) || (item1.equals("item2") && item2.equals("item1")));
        }

    }

    public void testRoundTrip() {
        Collection collection = createCollection();
        collection.add("item1");
        collection.add("item2");
        editor.setValue(collection);
        String text = editor.getAsText();
        editor.setAsText(text);
        Collection result = (Collection) editor.getValue();
        checkContents(result);
    }

    protected abstract void checkType(Object output);

    protected abstract Collection createCollection();
}
