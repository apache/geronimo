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

package org.apache.geronimo.common;

import junit.framework.TestCase;


/**
 *
 * @version $Rev$ $Date$
 */
public class NotCoercibleExceptionTest extends TestCase {
    public void testConstructor() {
        NotCoercibleException ex = new NotCoercibleException();
        assertNull(ex.getMessage());
        assertNull(ex.getCause());
    }

    public void testConstructorNullMessage() {
        NotCoercibleException ex = new NotCoercibleException((String) null);
        assertNull(ex.getMessage());
        assertNull(ex.getCause());
    }

    public void testConstructorNullCause() {
        NotCoercibleException ex = new NotCoercibleException((Throwable) null);
        assertNull(ex.getMessage());
        assertNull(ex.getCause());
    }

    public void testConstructorNullNull() {
        NotCoercibleException ex = new NotCoercibleException(null, null);
        assertNull(ex.getMessage());
        assertNull(ex.getCause());
    }

    public void testConstructorMessage() {
        String expected = "message";
        NotCoercibleException ex = new NotCoercibleException(expected);
        assertEquals(expected, ex.getMessage());
        assertNull(ex.getCause());
    }

    public void testConstructorCause() {
        Exception expected = new Exception();
        NotCoercibleException ex = new NotCoercibleException(expected);
        assertEquals(expected, ex.getCause());
        assertNotNull(ex.getMessage());
    }

    public void testConstructorMessageCause() {
        String message = "message";
        Exception cause = new Exception();
        NotCoercibleException ex = new NotCoercibleException(message, cause);
        assertEquals(message, ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
}
