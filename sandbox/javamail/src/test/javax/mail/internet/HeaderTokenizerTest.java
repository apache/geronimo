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

package javax.mail.internet;

import javax.mail.internet.HeaderTokenizer.Token;

import junit.framework.TestCase;

/**
 * @version $Revision: 1.3 $ $Date: 2004/03/10 10:00:30 $
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
