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

package org.apache.geronimo.common.mutable;

import junit.framework.TestCase;
import org.apache.geronimo.common.coerce.NotCoercibleException;

/**
 * Unit test for {@link MuCharacter} class.
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/30 08:00:10 $
 */
public class MuCharacterTest extends TestCase {

    public void testConstructors() {
        char c = 0;

        MuCharacter muCharacter = new MuCharacter();
        assertEquals(c,muCharacter.get());

        c = 'a';
        muCharacter = new MuCharacter(c);
        assertEquals(c,muCharacter.get());

        muCharacter = new MuCharacter(new Character(c));
        assertEquals(c,muCharacter.get());
    }

    public void testSetAndGet() {
        char oldChar = 'A';
        char newChar = 'Z';

        MuCharacter muCharacter = new MuCharacter(oldChar);
        assertEquals(oldChar,muCharacter.set(newChar));
        assertEquals(newChar,muCharacter.get());
    }

    public void testSetObject() {
        MuCharacter muCharacter = new MuCharacter();

        Character charObj = new Character('S');
        muCharacter.setValue(charObj);
        assertEquals(charObj,muCharacter.getValue());

        // $ = 36
        Integer integerObj = new Integer(36);
        muCharacter.setValue(integerObj);
        assertEquals(36,muCharacter.get());
        assertEquals(new Character('$'),muCharacter.getValue());

        muCharacter.setValue(new MuCharacter('C'));
        assertEquals(new Character('C'),muCharacter.getValue());

        try {
            muCharacter.setValue(new String("QWERTY"));
            fail("Expected NotCoercibleException");
        } catch (NotCoercibleException ignore) {
        }
    }

    public void testCompare() {
        char lesser = 'A';
        char equal = 'B';
        char greater = 'C';

        MuCharacter muCharacter = new MuCharacter(equal);
        assertTrue(muCharacter.compareTo(equal) == 0);
        assertTrue(muCharacter.compareTo(lesser) > 0);
        assertTrue(muCharacter.compareTo(greater) < 0 );

        assertTrue(muCharacter.compareTo(new MuCharacter(equal)) == 0);
        try {
            muCharacter.compareTo(new String());
            fail("Expected ClassCastException");
        } catch (ClassCastException ignore) {
        }
    }

    public void testEquals() {
        MuCharacter muCharacter = new MuCharacter('A');
        assertTrue(muCharacter.equals(muCharacter));
        assertTrue(muCharacter.equals(new MuCharacter('A')));
        assertFalse(muCharacter.equals(null));
        assertFalse(muCharacter.equals(new String()));
    }

    public void testMisc() {
        char c = 'A';
        MuCharacter muCharacter = new MuCharacter(c);
        assertEquals(c,muCharacter.charValue());

        assertEquals("A",muCharacter.toString());

        assertEquals(muCharacter.hashCode(),new MuCharacter(c).hashCode());
    }
}