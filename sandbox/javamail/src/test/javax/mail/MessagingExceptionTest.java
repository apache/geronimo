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
//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//
package javax.mail;
import junit.framework.TestCase;
/**
 * @version $Revision $ $Date: 2004/01/29 04:20:05 $
 */
public class MessagingExceptionTest extends TestCase {
    private RuntimeException d;
    private MessagingException c;
    private MessagingException b;
    private MessagingException a;
    public MessagingExceptionTest(String name) {
        super(name);
    }
    protected void setUp() throws Exception {
        super.setUp();
        a = new MessagingException("A");
        b = new MessagingException("B");
        c = new MessagingException("C");
        d = new RuntimeException("D");
    }
    public void testMessagingExceptionString() {
        assertEquals("A", a.getMessage());
    }
    public void testNextException() {
        assertTrue(a.setNextException(b));
        assertEquals(b, a.getNextException());
        assertTrue(a.setNextException(c));
        assertEquals(b, a.getNextException());
        assertEquals(c, b.getNextException());
        String message = a.getMessage();
        int ap = message.indexOf("A");
        int bp = message.indexOf("B");
        int cp = message.indexOf("C");
        assertTrue("A does not contain 'A'", ap != -1);
        assertTrue("B does not contain 'B'", bp != -1);
        assertTrue("C does not contain 'C'", cp != -1);
    }
    public void testNextExceptionWrong() {
        assertTrue(a.setNextException(d));
        assertFalse(a.setNextException(b));
    }
    public void testNextExceptionWrong2() {
        assertTrue(a.setNextException(d));
        assertFalse(a.setNextException(b));
    }
    public void testMessagingExceptionStringException() {
        MessagingException x = new MessagingException("X", a);
        assertEquals("X (javax.mail.MessagingException: A)", x.getMessage());
        assertEquals(a, x.getNextException());
        assertEquals(a, x.getCause());
    }
}
