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

package org.apache.geronimo.common.mutable;

import junit.framework.TestCase;
import org.apache.geronimo.common.NotCoercibleException;


/**
 * Unit test for {@link MuShort} class.
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:58:27 $
 */
public class MuShortTest extends TestCase {

    public void testConstructors() {

        MuShort muShort = new MuShort();
        assertEquals(0, muShort.get());

        short s = 1;

        muShort = new MuShort(s);
        assertEquals(1, muShort.get());

        muShort = new MuShort(new Short(s));
        assertEquals(1, muShort.get());
    }

    public void testCommit() {
        short assumed = 10;
        short newVal = 20;

        MuShort muShort = new MuShort((short) 10);
        assertTrue(muShort.commit(assumed, newVal));
        assertFalse(muShort.commit(assumed, newVal));
    }

    public void testSwap() {

        MuShort six = new MuShort((short) 6);
        MuShort nine = new MuShort((short) 9);

        assertEquals(9, nine.swap(nine));
        assertEquals(9, six.swap(nine));
        assertEquals(9, six.get());
    }

    public void testPlusMinusMultiDivide() {
        MuShort muShort = new MuShort((short) 9);
        short val = 2;

        assertEquals(11, muShort.add(val));
        assertEquals(11, muShort.get());

        assertEquals(9, muShort.subtract(val));
        assertEquals(9, muShort.get());

        assertEquals(18, muShort.multiply(val));
        assertEquals(18, muShort.get());

        assertEquals(9, muShort.divide(val));
        assertEquals(9, muShort.get());
    }

    public void testIncDec() {
        MuShort muShort = new MuShort((short) 5);
        assertEquals(6, muShort.increment());
        assertEquals(6, muShort.get());

        assertEquals(5, muShort.decrement());
        assertEquals(5, muShort.get());
    }

    public void testBooleanOperations() {
        MuShort muShort = new MuShort((short) 9);
        assertEquals(-9, muShort.negate());

        muShort.set((short) 9);
        assertEquals(~9, muShort.complement());

        muShort.set((short) 6);
        assertEquals(6 | 9, muShort.or((short) 9));

        muShort.set((short) 9);
        assertEquals(9 & 6, muShort.and((short) 6));

        muShort.set((short) 6);
        assertEquals(6 ^ 9, muShort.xor((short) 9));

        muShort.set((short) 9);
        assertEquals(9 << 6, muShort.shiftLeft((short) 6));

        muShort.set((short) 6);
        assertEquals(6 >> 9, muShort.shiftRight(9));

        muShort.set((short) 9);
        assertEquals(9 >>> 6, muShort.shiftRightZero(6));
    }

    public void testCompare() {
        short lesser = 1;
        short equal = 2;
        short greater = 3;

        MuShort muShort = new MuShort(equal);

        assertTrue(muShort.compareTo(equal) == 0);
        assertTrue(muShort.compareTo(lesser) > 0);
        assertTrue(muShort.compareTo(greater) < 0);

        assertTrue(muShort.compareTo(new MuShort((short) 2)) == 0);
        assertTrue(muShort.compareTo(new MuShort((short) 5)) < 0);
        assertTrue(muShort.compareTo(new MuShort((short) 0)) > 0);
        try {
            muShort.compareTo(new String());
            fail("Expected ClassCastException");
        } catch (ClassCastException ignore) {
        }
    }

    public void testEquals() {
        MuShort muShort = new MuShort((short) 5);

        assertTrue(muShort.equals(muShort));
        assertTrue(muShort.equals(new MuShort((short) 5)));
        assertFalse(muShort.equals(null));
        assertFalse(muShort.equals(new String()));
    }

    public void testMisc() {
        MuShort muShort1 = new MuShort((short) 5);
        MuShort muShort2 = new MuShort(new Short((short) 5));

        assertTrue(muShort1.equals(muShort2));
        assertEquals("5", muShort2.toString());

        assertEquals(muShort1.hashCode(), muShort2.hashCode());
    }

    public void testSetObject() {
        MuShort muShort = new MuShort();

        muShort.setValue(new Short((short) 5));
        assertEquals(new Short((short) 5), muShort.getValue());
        assertEquals(5, muShort.get());

        muShort.setValue(new Short((short) 5));
        assertEquals(new Short((short) 5), muShort.getValue());
        assertEquals(5, muShort.get());

        muShort = new MuShort();
        muShort.set((short) 2);
        assertEquals(2, muShort.get());

        try {
            muShort.setValue(new String("5"));
            fail("Expected NotCoercibleException");
        } catch (NotCoercibleException ignore) {
        }
    }
}