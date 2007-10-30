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
import java.net.URL;

import junit.framework.TestCase;

/**
 * Unit test for {@link org.apache.geronimo.common.propertyeditor.ArrayPropertyEditorAdapter} class.
 *
 * @version $Rev$ $Date$
 */
public class ArrayPropertyEditorAdapterTest
    extends TestCase
{
    PropertyEditor editor;

    protected void setUp()
    {
        editor = PropertyEditors.findEditor(URL[].class);
    }

    public void testGetValue_Simple()
    {
        String input = "http://apache.org";

        editor.setAsText(input);
        Object output = editor.getValue();

        assertNotNull(output);
        assertEquals(URL[].class, output.getClass());

        URL[] urls = (URL[])output;
        assertEquals(1, urls.length);
        assertEquals(input, urls[0].toString());
    }

    public void testGetValue_2URLs()
    {
        String input = "http://apache.org, http://google.com";

        editor.setAsText(input);
        Object output = editor.getValue();

        assertNotNull(output);
        assertEquals(URL[].class, output.getClass());

        URL[] urls = (URL[])output;
        assertEquals(2, urls.length);
        assertEquals("http://apache.org", urls[0].toString());
        assertEquals("http://google.com", urls[1].toString());
    }
}
