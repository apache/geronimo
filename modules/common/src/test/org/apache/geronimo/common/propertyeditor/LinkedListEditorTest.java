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

import java.util.Collection;
import java.util.LinkedList;

/**
 * Unit test for {@link LinkedListEditor} class.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 02:56:06 -0700 (Tue, 14 Sep 2004) $
 */
public class LinkedListEditorTest extends AbstractCollectionEditorTest {

    protected void setUp() {
        editor = PropertyEditors.findEditor(LinkedList.class);
        ordered = true;
    }

    public void testEditorClass() throws Exception {
        assertEquals(LinkedListEditor.class, editor.getClass());
    }

    protected void checkType(Object output) {
        assertTrue("editor returned a: " + output.getClass(), output instanceof LinkedList);
    }

    protected Collection createCollection() {
        return new LinkedList();
    }
}
