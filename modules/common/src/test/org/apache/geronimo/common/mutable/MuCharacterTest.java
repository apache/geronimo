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

package org.apache.geronimo.common.mutable;

import junit.framework.TestCase;
import org.apache.geronimo.common.NotCoercibleException;

/**
 * Unit test for {@link MuCharacter} class.
 *
 * @version $Revision: 1.4 $ $Date: 2004/02/25 09:57:05 $
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
