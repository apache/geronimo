/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.common;

import java.sql.SQLWarning;
import java.util.ArrayList;

import junit.framework.TestCase;

/**
 * Unit test for {@link ThrowableHandler} class.
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:57:04 $
 */

public class ThrowableHandlerTest extends TestCase {

    MockThrowableListener listenerOne;
    MockThrowableListener listenerTwo;

    protected void setUp() throws Exception {

        super.setUp();

        listenerOne = new MockThrowableListener();
        listenerTwo = new MockThrowableListener();
    }

    public void testAddAndFire() {

        ThrowableHandler.addThrowableListener(listenerOne);
        ThrowableHandler.addThrowableListener(listenerTwo);

        //Duplicate listener.Should not be registered.else our expected size of lists will differ
        ThrowableHandler.addThrowableListener(listenerOne);

        ThrowableHandler.addError(new IllegalArgumentException());
        ThrowableHandler.addError(new NullPointerException());
        ThrowableHandler.addWarning(new SQLWarning());
        ThrowableHandler.add(new Exception());

        ArrayList errorListOne = listenerOne.getErrorList();
        ArrayList errorListTwo = listenerTwo.getErrorList();

        assertEquals(2, errorListOne.size());
        assertEquals(2, errorListTwo.size());

        assertEquals("java.lang.IllegalArgumentException", ((errorListOne.get(0)).getClass().getName()));
        assertEquals("java.lang.NullPointerException", ((errorListOne.get(1)).getClass().getName()));

        assertEquals("java.lang.IllegalArgumentException", ((errorListTwo.get(0)).getClass().getName()));
        assertEquals("java.lang.NullPointerException", ((errorListTwo.get(1)).getClass().getName()));


        assertEquals(1, listenerOne.getWarningList().size());
        assertEquals(1, listenerTwo.getWarningList().size());

        assertEquals("java.sql.SQLWarning", ((listenerOne.getWarningList().get(0)).getClass().getName()));
        assertEquals("java.sql.SQLWarning", ((listenerTwo.getWarningList().get(0)).getClass().getName()));

        assertEquals(1, listenerOne.getUnknownList().size());
        assertEquals(1, listenerTwo.getUnknownList().size());

        assertEquals("java.lang.Exception", ((listenerOne.getUnknownList().get(0)).getClass().getName()));
        assertEquals("java.lang.Exception", ((listenerTwo.getUnknownList().get(0)).getClass().getName()));

    }

    protected void tearDown() throws Exception {

        listenerOne = null;
        listenerTwo = null;

        super.tearDown();
    }
}
