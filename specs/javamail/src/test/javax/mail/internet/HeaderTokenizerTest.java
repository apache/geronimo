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
package javax.mail.internet;

import javax.mail.internet.HeaderTokenizer.Token;

import junit.framework.TestCase;

/**
 * @version $Revision: 1.1 $ $Date: 2003/09/04 01:31:41 $
 */
public class HeaderTokenizerTest extends TestCase {
    public void testTokenizer() throws ParseException {
        Token t;
        HeaderTokenizer ht;
        ht =
            new HeaderTokenizer("To: \"Geronimo List\" <geronimo-dev@apache.org>, \n\r Geronimo User <geronimo-user@apache.org>");
        assertEquals("To", ht.peek().getValue());
        assertEquals("To", ht.next().getValue());
        assertEquals(":", ht.peek().getValue());
        assertEquals(":", ht.next().getValue());
        t = ht.next();
        assertEquals("Geronimo List", t.getValue());
        assertEquals(Token.QUOTEDSTRING, t.getType());
        assertEquals("<", ht.next().getValue());
        assertEquals("geronimo-dev", ht.next().getValue());
        assertEquals("@", ht.next().getValue());
        assertEquals("apache", ht.next().getValue());
        assertEquals(".", ht.next().getValue());
        assertEquals("org", ht.next().getValue());
        assertEquals(">", ht.next().getValue());
        assertEquals(",", ht.next().getValue());
        assertEquals("Geronimo", ht.next().getValue());
        assertEquals("User", ht.next().getValue());
        assertEquals("<", ht.next().getValue());
        assertEquals("geronimo-user", ht.next().getValue());
        assertEquals("@", ht.next().getValue());
        assertEquals("apache", ht.next().getValue());
        assertEquals(".", ht.next().getValue());
        assertEquals("org>", ht.getRemainder());
        assertEquals("org", ht.peek().getValue());
        assertEquals("org>", ht.getRemainder());
        assertEquals("org", ht.next().getValue());
        assertEquals(">", ht.next().getValue());
        assertEquals(Token.EOF, ht.next().getType());
        ht = new HeaderTokenizer("   ");
        assertEquals(Token.EOF, ht.next().getType());
        ht = new HeaderTokenizer("J2EE");
        assertEquals("J2EE", ht.next().getValue());
        assertEquals(Token.EOF, ht.next().getType());
        // test comments
        doComment(true);
        doComment(false);
    }
    public void doComment(boolean ignore) throws ParseException {
        HeaderTokenizer ht;
        Token t;
        ht =
            new HeaderTokenizer(
                "Apache(Geronimo)J2EE",
                HeaderTokenizer.RFC822,
                ignore);
        t = ht.next();
        assertEquals("Apache", t.getValue());
        assertEquals(Token.ATOM, t.getType());
        if (!ignore) {
            t = ht.next();
            assertEquals("Geronimo", t.getValue());
            assertEquals(Token.COMMENT, t.getType());
        }
        t = ht.next();
        assertEquals("J2EE", t.getValue());
        assertEquals(Token.ATOM, t.getType());
        assertEquals(Token.EOF, ht.next().getType());
    }
}
