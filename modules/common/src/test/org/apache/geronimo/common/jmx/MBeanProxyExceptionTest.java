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

package org.apache.geronimo.common.jmx;

import junit.framework.TestCase;


/**
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:04 $
 */
public class MBeanProxyExceptionTest extends TestCase {
    public void testConstructor() {
        MBeanProxyException ex = new MBeanProxyException();
        assertNull(ex.getMessage());
        assertNull(ex.getCause());
    }

    public void testConstructorNullMessage() {
        MBeanProxyException ex = new MBeanProxyException((String) null);
        assertNull(ex.getMessage());
        assertNull(ex.getCause());
    }

    public void testConstructorNullCause() {
        MBeanProxyException ex = new MBeanProxyException((Throwable) null);
        assertNull(ex.getMessage());
        assertNull(ex.getCause());
    }

    public void testConstructorNullNull() {
        MBeanProxyException ex = new MBeanProxyException(null, null);
        assertNull(ex.getMessage());
        assertNull(ex.getCause());
    }

    public void testConstructorMessage() {
        String expected = "message";
        MBeanProxyException ex = new MBeanProxyException(expected);
        assertEquals(expected, ex.getMessage());
        assertNull(ex.getCause());
    }

    public void testConstructorCause() {
        Exception expected = new Exception();
        MBeanProxyException ex = new MBeanProxyException(expected);
        assertEquals(expected, ex.getCause());
        assertNotNull(ex.getMessage());
    }

    public void testConstructorMessageCause() {
        String message = "message";
        Exception cause = new Exception();
        MBeanProxyException ex = new MBeanProxyException(message, cause);
        assertEquals(message, ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
}
