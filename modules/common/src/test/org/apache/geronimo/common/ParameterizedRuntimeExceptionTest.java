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
 * Unit test for {@link ParameterizedRuntimeException} class.
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:58:27 $
 */
public class ParameterizedRuntimeExceptionTest extends TestCase {
    /**
     * Test constructor
     */
    public void testConstructorMessage() {
        ParameterizedRuntimeException ex =
            new ParameterizedRuntimeException("test {0} test {1}");
        assertNotNull("Message is null.", ex.getMessage());
        assertEquals(
            "Message is incorrect.",
            "test {0} test {1}",
            ex.getMessage());
        assertNull("Nested exception is not null.", ex.getCause());
    }

    /**
     * Test constructor
     */
    public void testConstructorMessageAndParameters() {
        Object[] params = { "1", "2" };
        ParameterizedRuntimeException ex =
            new ParameterizedRuntimeException("test {0} test {1}", params);
        assertNotNull("Message is null.", ex.getMessage());
        assertTrue(
            "Message is incorrect.",
            ex.getMessage().startsWith("test 1 test 2"));
        assertNull("Nested exception is not null.", ex.getCause());
    }

    /**
     * Test constructor
     */
    public void testConstructorCause() {
        Exception nested = new Exception("nested error");
        ParameterizedRuntimeException ex =
            new ParameterizedRuntimeException(nested);
        assertNotNull("Message is null.", ex.getMessage());
        assertNotNull("Nested exception is null.", ex.getCause());
    }

    /**
     * Test constructor
     */
    public void testConstructorMessageAndCause() {
        Exception nested = new Exception("nested error");
        ParameterizedRuntimeException ex =
            new ParameterizedRuntimeException("test {0} test {1}", nested);
        assertNotNull("Message is null.", ex.getMessage());
        assertTrue(
            "Message is incorrect.",
            ex.getMessage().startsWith("test {0} test {1}"));
        assertNotNull("Nested exception is null.", ex.getCause());
    }

    /**
     * Test constructor
     */
    public void testConstructorAll() {
        Object[] params = { "1", "2" };
        Exception nested = new Exception("nested error");
        ParameterizedRuntimeException ex =
            new ParameterizedRuntimeException(
                "test {0} test {1}",
                params,
                nested);
        assertNotNull("Message is null.", ex.getMessage());
        assertTrue(
            "Message is incorrect.",
            ex.getMessage().startsWith("test 1 test 2"));
        assertNotNull("Nested exception is null.", ex.getCause());
    }
}
