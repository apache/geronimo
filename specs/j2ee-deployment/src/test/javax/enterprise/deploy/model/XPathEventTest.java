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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.enterprise.deploy.model;

import junit.framework.TestCase;

public class XPathEventTest extends TestCase {

     public void testIsAddEvent() {
        XpathEvent addEvent = new XpathEvent(null, XpathEvent.BEAN_ADDED);
        assertTrue(addEvent.isAddEvent());
        assertFalse(addEvent.isChangeEvent());
        assertFalse(addEvent.isRemoveEvent());
    }

    public void testIsChangeEvent() {
        XpathEvent changeEvent = new XpathEvent(null, XpathEvent.BEAN_CHANGED);
        assertTrue(changeEvent.isChangeEvent());
        assertFalse(changeEvent.isAddEvent());
        assertFalse(changeEvent.isRemoveEvent());
    }

    public void testIsRemoveEvent() {
        XpathEvent removeEvent = new XpathEvent(null, XpathEvent.BEAN_REMOVED);
        assertTrue(removeEvent.isRemoveEvent());
        assertFalse(removeEvent.isAddEvent());
        assertFalse(removeEvent.isChangeEvent());
    }

}
