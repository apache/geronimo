/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.common;

import junit.framework.TestCase;

/**
 * Unit test for {@link ParameterizedException} class.
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/30 08:36:16 $
 */
public class ParameterizedExceptionTest extends TestCase {

    /**
     * Test constructor
     */
    public void testConstructorMessage() {
        ParameterizedException ex =
            new ParameterizedException("test {0} test {1}");
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
        ParameterizedException ex =
            new ParameterizedException("test {0} test {1}", params);
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
        ParameterizedException ex = new ParameterizedException(nested);
        assertNotNull("Message is null.", ex.getMessage());
        assertNotNull("Nested exception is null.", ex.getCause());
    }

    /**
     * Test constructor
     */
    public void testConstructorMessageAndCause() {
        Exception nested = new Exception("nested error");
        ParameterizedException ex =
            new ParameterizedException("test {0} test {1}", nested);
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
        ParameterizedException ex =
            new ParameterizedException("test {0} test {1}", params, nested);
        assertNotNull("Message is null.", ex.getMessage());
        assertTrue(
            "Message is incorrect.",
            ex.getMessage().startsWith("test 1 test 2"));
        assertNotNull("Nested exception is null.", ex.getCause());
    }

    /**
     * Test constructor
     */
    public void testConstructorNullParameter() {
        Object[] params = { "1", null };
        Exception nested = new Exception("nested error");
        ParameterizedException ex =
            new ParameterizedException("test {0} test {1}", params, nested);
        assertNotNull("Message is null.", ex.getMessage());
        assertTrue(
            "Message is incorrect.",
            ex.getMessage().startsWith("test 1 test ???"));
        assertNotNull("Nested exception is null.", ex.getCause());
    }
}
