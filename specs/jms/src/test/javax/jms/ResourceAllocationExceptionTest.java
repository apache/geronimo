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

package javax.jms;

import junit.framework.TestCase;


/**
 * @version $Rev$ $Date$
 */
public class ResourceAllocationExceptionTest extends TestCase {
    public void testConstructorNull() {
        ResourceAllocationException ex = new ResourceAllocationException(null);
        assertNull(ex.getMessage());
        assertNull(ex.getErrorCode());
        assertNull(ex.getLinkedException());
    }

    public void testConstructorNullNull() {
        ResourceAllocationException ex = new ResourceAllocationException(null, null);
        assertNull(ex.getMessage());
        assertNull(ex.getErrorCode());
        assertNull(ex.getLinkedException());
    }

    public void testConstructorNullString() {
        String expected = "some code";
        ResourceAllocationException ex = new ResourceAllocationException(null, expected);
        assertNull(ex.getMessage());
        assertEquals(expected, ex.getErrorCode());
        assertNull(ex.getLinkedException());
    }

    public void testConstructorString() {
        String expected = "some message";
        ResourceAllocationException ex = new ResourceAllocationException(expected);
        assertEquals(expected, ex.getMessage());
        assertNull(ex.getErrorCode());
        assertNull(ex.getLinkedException());
    }

    public void testConstructorStringNull() {
        String expected = "some message";
        ResourceAllocationException ex = new ResourceAllocationException(expected, null);
        assertEquals(expected, ex.getMessage());
        assertNull(ex.getErrorCode());
        assertNull(ex.getLinkedException());
    }

    public void testConstructorStringString() {
        String expectedMessage = "some message";
        String expectedCode = "some code";
        ResourceAllocationException ex = new ResourceAllocationException(expectedMessage, expectedCode);
        assertEquals(expectedMessage, ex.getMessage());
        assertEquals(expectedCode, ex.getErrorCode());
        assertNull(ex.getLinkedException());
    }
}
