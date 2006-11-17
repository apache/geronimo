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
import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

/**
 * Unit test for {@link URLEditor} class.
 *
 * @version $Rev$
 */
public class URLEditorTest extends TestCase {
    PropertyEditor editor;

    protected void setUp() {
        editor = PropertyEditors.findEditor(URL.class);
    }

    public void testEditorClass() throws Exception {
        assertEquals(URLEditor.class, editor.getClass());
    }

    public void testHTTP() throws Exception {
        checkURL("http://www.apache.org", null);
    }

    public void testFTP() throws Exception {
        checkURL("ftp://ftp.apache.org", null);
    }

    public void testFileURL() throws Exception {
        URL base = new URL("file://test.resource");
        base = new File(base.getFile()).toURI().toURL();
        checkURL("file://test.resource", base);
    }

    public void testFile() throws Exception {
        URL testValue = new File("test.resource").toURI().toURL();
        checkURL("test.resource", testValue);
    }

    protected void checkURL(String text, URL test) throws Exception {
        editor.setAsText(text);
        if (test == null) {
            test = new URL(text);
        }
        assertEquals(test, editor.getValue());
    }
}
